package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.remote.FirebaseRestClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dutyDao = db.dutyDao()
    private val advanceDao = db.advanceDao()
    val prefs = PreferenceHelper(application)
    private val restClient = FirebaseRestClient()

    // Screen State Route Enum
    enum class Screen {
        Splash, LangSelect, Onboarding, Authenticate, Home, Reports, Graph, Advance, Settings, Profile
    }

    private val _currentScreen = MutableStateFlow(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Auth flows
    val isLoggedIn: Boolean
        get() = prefs.getString("dtp_uid", "").isNotEmpty()

    // Live Room lists
    private val _dutyEntries = MutableStateFlow<List<DutyEntry>>(emptyList())
    val dutyEntries: StateFlow<List<DutyEntry>> = _dutyEntries.asStateFlow()

    private val _advanceEntries = MutableStateFlow<List<AdvanceEntry>>(emptyList())
    val advanceEntries: StateFlow<List<AdvanceEntry>> = _advanceEntries.asStateFlow()

    // Syncing state
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Active calendar navigation date
    private val _calendarYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val calendarYear: StateFlow<Int> = _calendarYear.asStateFlow()

    private val _calendarMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH)) // 0-indexed
    val calendarMonth: StateFlow<Int> = _calendarMonth.asStateFlow()

    init {
        // Collect DB changes
        viewModelScope.launch {
            dutyDao.getAllFlow().collectLatest {
                _dutyEntries.value = it
            }
        }
        viewModelScope.launch {
            advanceDao.getAllFlow().collectLatest {
                _advanceEntries.value = it
            }
        }
    }

    fun setScreen(screen: Screen) {
        _currentScreen.value = screen
    }

    fun navigateMonth(offset: Int) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, _calendarYear.value)
            set(Calendar.MONTH, _calendarMonth.value)
            add(Calendar.MONTH, offset)
        }
        _calendarYear.value = cal.get(Calendar.YEAR)
        _calendarMonth.value = cal.get(Calendar.MONTH)
    }

    // Save duty
    fun saveDuty(date: String, type: String, otHours: Float, lateMins: Int, shift: String) {
        viewModelScope.launch {
            dutyDao.insert(DutyEntry(date, type, otHours, lateMins, shift))
            triggerBackup()
        }
    }

    fun deleteDuty(date: String) {
        viewModelScope.launch {
            dutyDao.deleteByDate(date)
            triggerBackup()
        }
    }

    // Save advance
    fun addAdvance(date: String, reason: String, amount: Double, status: String) {
        viewModelScope.launch {
            advanceDao.insert(AdvanceEntry(date = date, reason = reason, amount = amount, status = status))
            triggerBackup()
        }
    }

    fun toggleAdvanceStatus(entry: AdvanceEntry) {
        viewModelScope.launch {
            val nextStatus = if (entry.status == "taken") "repaid" else "taken"
            advanceDao.insert(entry.copy(status = nextStatus))
            triggerBackup()
        }
    }

    fun deleteAdvance(id: Int) {
        viewModelScope.launch {
            advanceDao.deleteById(id)
            triggerBackup()
        }
    }

    // Dynamic wage metrics calculated in real-time
    fun getSalaryPreview(): Float {
        val sal = prefs.getFloat("settings_sal", 500f)
        val food = prefs.getFloat("settings_food", 50f)
        val pf = prefs.getFloat("settings_pf", 0f)
        // Simulated for 26 days
        return (sal + food) * 26f - pf
    }

    // Multi-device Backup Sync via Firebase REST API
    fun triggerBackup() {
        val uid = prefs.getString("dtp_uid", "")
        if (uid.isEmpty()) return

        viewModelScope.launch {
            _isSyncing.value = true
            val profile = prefs.getProfileJson()
            val settings = prefs.getSettingsJson()

            // Map duty entries to JSON object
            val dutyObj = JSONObject()
            _dutyEntries.value.forEach {
                dutyObj.put(it.date, JSONObject().apply {
                    put("type", it.type)
                    put("ot", it.otHours.toDouble())
                    put("late", it.lateMins)
                    put("shift", it.shift)
                })
            }

            // Map advance entries to JSON array
            val advArr = JSONArray()
            _advanceEntries.value.forEach {
                val item = JSONObject().apply {
                    put("id", it.id)
                    put("date", it.date)
                    put("reason", it.reason)
                    put("amount", it.amount)
                    put("status", it.status)
                }
                advArr.put(item)
            }

            restClient.syncUserData(
                uid = uid,
                profileJson = profile,
                settingsJson = settings,
                dutyJson = dutyObj.toString(),
                advanceJson = advArr.toString()
            )
            _isSyncing.value = false
        }
    }

    // Restore dataset from cloud
    fun restoreBackup(uid: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val restData = restClient.fetchUserData(uid)
                if (restData != null) {
                    // Restore preferences
                    prefs.setString("dtp_uid", uid)
                    prefs.setString("dtp_name", restData.optString("name", "User"))
                    prefs.setString("dtp_phone", restData.optString("phone", ""))
                    prefs.setString("dtp_dial_code", restData.optString("dialCode", "+91"))
                    prefs.setString("dtp_country", restData.optString("country", "India"))
                    prefs.setString("dtp_country_code", restData.optString("countryCode", "IN"))
                    prefs.setString("dtp_flag", restData.optString("flag", "🇮🇳"))
                    prefs.setString("dtp_pin", restData.optString("pin", ""))
                    prefs.setString("dtp_avatar", restData.optString("avatar", ""))
                    prefs.setString("dtp_created_at", restData.optString("createdAt", ""))
                    prefs.setString("dtp_lang", restData.optString("lang", "en"))
                    prefs.setString("dtp_theme", restData.optString("theme", "dark"))
                    prefs.setString("dtp_company", restData.optString("company", ""))
                    prefs.setString("dtp_job", restData.optString("job", ""))
                    prefs.setString("dtp_empid", restData.optString("empid", ""))

                    if (restData.has("settings")) {
                        prefs.importSettingsFromJson(restData.getJSONObject("settings"))
                    }

                    // Restore duty entries
                    dutyDao.clearAll()
                    if (restData.has("attendance")) {
                        val att = restData.getJSONObject("attendance")
                        att.keys().forEach { date ->
                            val item = att.getJSONObject(date)
                            val type = item.optString("type", "Present")
                            val ot = item.optDouble("ot", 0.0).toFloat()
                            val late = item.optInt("late", 0)
                            val shift = item.optString("shift", "Morning")
                            dutyDao.insert(DutyEntry(date, type, ot, late, shift))
                        }
                    }

                    // Restore advances
                    advanceDao.clearAll()
                    if (restData.has("advances")) {
                        val advs = restData.getJSONArray("advances")
                        for (i in 0 until advs.length()) {
                            val item = advs.getJSONObject(i)
                            advanceDao.insert(
                                AdvanceEntry(
                                    date = item.optString("date", ""),
                                    reason = item.optString("reason", ""),
                                    amount = item.optDouble("amount", 0.0),
                                    status = item.optString("status", "taken")
                                )
                            )
                        }
                    }
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                Log.e("AppViewModel", "Restore failed: ${e.message}", e)
                onComplete(false)
            }
        }
    }

    fun clearAllDataLocal() {
        viewModelScope.launch {
            dutyDao.clearAll()
            advanceDao.clearAll()
            prefs.clearAllData()
            _currentScreen.value = Screen.Splash
        }
    }

    // Share report as image - Dynamically formats payroll invoices on Bitmap
    fun generateReportBitmap(
        context: Context,
        startDate: String,
        endDate: String,
        onUriReady: (Uri?) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Calculate metrics
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val start = sdf.parse(startDate) ?: Date()
                val end = sdf.parse(endDate) ?: Date()

                var presentCount = 0
                var halfDayCount = 0
                var leaveCount = 0
                var sickCount = 0
                var holidayCount = 0
                var offCount = 0
                var totalOtHours = 0f
                var totalLateMins = 0

                val monthEntries = _dutyEntries.value.filter {
                    try {
                        val d = sdf.parse(it.date) ?: return@filter false
                        d in start..end
                    } catch (e: Exception) {
                        false
                    }
                }

                monthEntries.forEach {
                    when (it.type) {
                        "Present" -> presentCount++
                        "Half Day" -> halfDayCount++
                        "Leave" -> leaveCount++
                        "Sick" -> sickCount++
                        "Holiday" -> holidayCount++
                        "Off" -> offCount++
                    }
                    totalOtHours += it.otHours
                    totalLateMins += it.lateMins
                }

                val salRule = prefs.getFloat("settings_sal", 500f)
                val foodRule = prefs.getFloat("settings_food", 50f)
                val otRule = prefs.getFloat("settings_otr", 100f)
                val pfRule = prefs.getFloat("settings_pf", 0f)

                val effectivePresentDays = presentCount + (halfDayCount * 0.5f)
                val baseEarn = effectivePresentDays * salRule
                val foodEarn = effectivePresentDays * foodRule
                val otEarn = totalOtHours * otRule
                val grossEarn = baseEarn + foodEarn + otEarn
                val netPay = maxOf(0f, grossEarn - pfRule)

                val width = 800
                val height = 1000
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)

                // Background
                val isDarkTheme = prefs.getString("dtp_theme", "dark") == "dark"
                canvas.drawColor(if (isDarkTheme) 0xFF060608.toInt() else 0xFFF0F2FB.toInt())

                val paint = Paint().apply {
                    isAntiAlias = true
                    textSize = 24f
                    color = if (isDarkTheme) 0xFFFFFFFF.toInt() else 0xFF0D0E1A.toInt()
                }

                // Header gradient simulated or drawn
                val headerPaint = Paint().apply {
                    color = 0xFF1A1F6E.toInt()
                }
                canvas.drawRect(0f, 0f, width.toFloat(), 180f, headerPaint)

                paint.color = 0xFFFFFFFF.toInt()
                paint.textSize = 34f
                paint.isFakeBoldText = true
                canvas.drawText("Duty Tracker Pro", 40f, 60f, paint)

                paint.textSize = 24f
                paint.isFakeBoldText = false
                val name = prefs.getString("dtp_name", "User")
                canvas.drawText("👤 Employee: $name", 40f, 105f, paint)
                canvas.drawText("📅 Period: $startDate to $endDate", 40f, 140f, paint)

                // Summary
                paint.color = if (isDarkTheme) 0xFFFFFFFF.toInt() else 0xFF0D0E1A.toInt()
                paint.textSize = 28f
                paint.isFakeBoldText = true
                canvas.drawText("ATTENDANCE SUMMARY", 40f, 240f, paint)

                paint.textSize = 22f
                paint.isFakeBoldText = false
                var y = 290f
                val list = listOf(
                    "Present: $presentCount days",
                    "Half Day: $halfDayCount days",
                    "Leave: $leaveCount days",
                    "Sick: $sickCount days",
                    "Holiday: $holidayCount days",
                    "Off Day: $offCount days",
                    "Total Overtime: $totalOtHours hours",
                    "Total Late: $totalLateMins mins"
                )
                list.forEach {
                    canvas.drawText("● $it", 50f, y, paint)
                    y += 35f
                }

                y += 20f
                paint.textSize = 28f
                paint.isFakeBoldText = true
                canvas.drawText("EARNINGS & PAYROLL", 40f, y, paint)
                y += 50f

                paint.textSize = 22f
                paint.isFakeBoldText = false
                val cur = prefs.getString("settings_cur", "₹")
                val payrollList = listOf(
                    "Base Salary: $cur${baseEarn.toInt()}",
                    "Food Allowance: $cur${foodEarn.toInt()}",
                    "Overtime Pay: $cur${otEarn.toInt()}",
                    "PF Deduction: -$cur${pfRule.toInt()}",
                    "Gross Earnings: $cur${grossEarn.toInt()}"
                )
                payrollList.forEach {
                    canvas.drawText(it, 50f, y, paint)
                    y += 35f
                }

                y += 30f
                paint.color = 0xFF06D6A0.toInt() // success green
                paint.textSize = 34f
                paint.isFakeBoldText = true
                canvas.drawText("NET PAYABLE Check: $cur${netPay.toInt()}", 40f, y, paint)

                paint.color = 0xFF888888.toInt()
                paint.textSize = 18f
                paint.isFakeBoldText = false
                canvas.drawText("Formed using Duty Tracker Pro · Nitai Studio 🇮🇳", 40f, height - 40f, paint)

                val file = File(context.cacheDir, "Duty_Report.png")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                }
                bitmap.recycle()

                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                onUriReady(uri)
            } catch (e: Exception) {
                Log.e("AppViewModel", "Bitmap draw failed: ${e.message}", e)
                onUriReady(null)
            }
        }
    }
}
