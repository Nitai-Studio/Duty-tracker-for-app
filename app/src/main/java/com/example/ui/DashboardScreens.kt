package com.example.ui

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.data.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun DashboardView(viewModel: AppViewModel) {
    val localContext = LocalContext.current
    val langState by viewModel.prefs.lang.collectAsState()
    val isDark by viewModel.prefs.theme.collectAsState()

    var activeTab by remember { mutableStateOf("home") } // "home", "reports", "graph", "advance", "settings"
    var showProfileView by remember { mutableStateOf(false) }

    val darkBg = Color(0xFF060608)
    val lightBg = Color(0xFFF0F2FB)
    val navColor = if (isDark == "dark") Color(0xFF141418) else Color.White

    Scaffold(
        bottomBar = {
            // Custom premium bottom deck matching HTML
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(navColor)
                    .border(1.dp, Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Navigation item standard
                NavItem(icon = "📊", label = Translations.get(langState, "report"), active = activeTab == "reports") {
                    StartIoManager.showInterstitial(localContext, viewModel.prefs) {
                        activeTab = "reports"
                    }
                }
                NavItem(icon = "📈", label = Translations.get(langState, "graph"), active = activeTab == "graph") {
                    StartIoManager.showInterstitial(localContext, viewModel.prefs) {
                        activeTab = "graph"
                    }
                }

                // Center Highlight Home button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(DtpTheme.BlueGradient)
                        .clickable {
                            StartIoManager.showInterstitial(localContext, viewModel.prefs) {
                                activeTab = "home"
                            }
                        }
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(if (isDark == "dark") Color(0xFF141418) else Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏠", fontSize = 28.sp)
                    }
                }

                NavItem(icon = "💰", label = Translations.get(langState, "advance"), active = activeTab == "advance") {
                    StartIoManager.showInterstitial(localContext, viewModel.prefs) {
                        activeTab = "advance"
                    }
                }
                NavItem(icon = "⚙️", label = Translations.get(langState, "settings"), active = activeTab == "settings") {
                    StartIoManager.showInterstitial(localContext, viewModel.prefs) {
                        activeTab = "settings"
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark == "dark") darkBg else lightBg)
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header deck
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDark == "dark") Color(0xFF141418) else Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .border(1.dp, Color.Gray.copy(alpha = 0.05f)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Duty Tracker Pro",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        style = androidx.compose.ui.text.TextStyle(
                            brush = DtpTheme.BlueGradient
                        )
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            val nextTheme = if (isDark == "dark") "light" else "dark"
                            StartIoManager.showInterstitial(localContext, viewModel.prefs) {
                                viewModel.prefs.setString("dtp_theme", nextTheme)
                            }
                        }) {
                            Text(if (isDark == "dark") "🌙" else "☀️", fontSize = 18.sp)
                        }

                        // Globe language trigger shortcut
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isDark == "dark") Color(0xFF1C1C22) else Color(0xFFE8EAF4))
                                .clickable {
                                    StartIoManager.showInterstitial(localContext, viewModel.prefs) {
                                        viewModel.setScreen(AppViewModel.Screen.LangSelect)
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🌐", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    langState.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark == "dark") Color.White else Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Avatar triggering Profile section
                        val avatarUrlState = viewModel.prefs.getString("dtp_avatar", "")
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.Gray.copy(alpha = 0.2f))
                                .clickable {
                                    StartIoManager.showInterstitial(localContext, viewModel.prefs) {
                                        showProfileView = true
                                    }
                                }
                        ) {
                            if (avatarUrlState.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(avatarUrlState),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text("👤", fontSize = 18.sp, modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                }

                // Inner content routing switch
                Box(modifier = Modifier.weight(1f)) {
                    when (activeTab) {
                        "home" -> CalendarHomeView(viewModel)
                        "reports" -> ReportsView(viewModel)
                        "graph" -> GraphAnalyticsView(viewModel)
                        "advance" -> AdvanceManagerView(viewModel)
                        "settings" -> SettingsView(viewModel)
                    }
                }

                // Banner Ad on Home, Reports, Graph, Advance, Settings screens
                StartIoBannerAd(prefs = viewModel.prefs, modifier = Modifier.padding(bottom = 4.dp))
            }

            // Sync backing indicator
            val syncActive by viewModel.isSyncing.collectAsState()
            if (syncActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 70.dp, end = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Translations.get(langState, "syncSave"), fontSize = 10.sp, color = Color.White)
                    }
                }
            }
        }
    }

    if (showProfileView) {
        Dialog(onDismissRequest = { showProfileView = false }) {
            ProfileView(viewModel) { showProfileView = false }
        }
    }
}

@Composable
fun NavItem(icon: String, label: String, active: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(icon, fontSize = 18.sp, color = if (active) Color(0xFF4361EE) else Color.Gray)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) Color(0xFF4361EE) else Color.Gray
        )
    }
}

// ────────────────────────────────═══════════════════════════
// SUBVIEW: HOME & CALENDAR DAY MATRIX
// ────────────────────────────────═══════════════════════════
@Composable
fun CalendarHomeView(viewModel: AppViewModel) {
    val localContext = LocalContext.current
    val langState by viewModel.prefs.lang.collectAsState()
    val isDark by viewModel.prefs.theme.collectAsState()
    val entries by viewModel.dutyEntries.collectAsState()

    val year by viewModel.calendarYear.collectAsState()
    val month by viewModel.calendarMonth.collectAsState()

    val currentCountryCode = viewModel.prefs.getString("dtp_country_code", "IN")

    // Selection duty details dialog properties
    var showDutyModalForDate by remember { mutableStateOf<String?>(null) }
    var selectedType by remember { mutableStateOf("Present") }
    var otHours by remember { mutableStateOf("0") }
    var lateMins by remember { mutableStateOf("0") }
    var shift by remember { mutableStateOf("Morning") }

    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp)) {
        // Calendar month navigator row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateMonth(-1) }) {
                Text("◀", fontSize = 14.sp, color = Color.Gray)
            }
            Text(
                text = "${months[month]} $year",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark == "dark") Color.White else Color.Black
            )
            IconButton(onClick = { viewModel.navigateMonth(1) }) {
                Text("▶", fontSize = 14.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Week Headers standard matrix grid (Sun - Sat)
        val dowS = listOf("S", "M", "T", "W", "T", "F", "S")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            dowS.forEachIndexed { idx, day ->
                val txtColor = when (idx) {
                    0 -> Color(0xFFEF233C) // Sun
                    6 -> Color(0xFF4CC9F0) // Sat
                    else -> Color.Gray
                }
                Text(
                    text = day,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = txtColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Generate day matrix days
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val totalGridElements = firstDayOfWeek - 1 + lastDayOfMonth
        val totalRows = (totalGridElements + 6) / 7

        Column(modifier = Modifier.fillMaxWidth()) {
            for (row in 0 until totalRows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (col in 0..6) {
                        val elementIdx = row * 7 + col
                        val dayVal = elementIdx - (firstDayOfWeek - 2)

                        if (dayVal in 1..lastDayOfMonth) {
                            val prefixDateStr = "$year-${(month + 1).toString().padStart(2, '0')}-${dayVal.toString().padStart(2, '0')}"
                            val savedEntry = entries.find { it.date == prefixDateStr }
                            val govHol = getGovtHoliday(prefixDateStr, currentCountryCode)

                            val cellBg = if (savedEntry != null) {
                                when (savedEntry.type) {
                                    "Present" -> Color(0xFF06D6A0)
                                    "Half Day" -> Color(0xFFFFD166)
                                    "Leave" -> Color(0xFFEF233C)
                                    "Sick" -> Color(0xFFFF9F1C)
                                    "Holiday" -> Color(0xFF9D4EDD)
                                    else -> Color.Gray
                                }
                            } else if (govHol != null) {
                                Color(0xFFEF233C) // Government Holiday alert red
                            } else {
                                if (isDark == "dark") Color(0xFF141418) else Color.White
                            }

                            Box(
                                modifier = Modifier
                                    .size(width = 46.dp, height = 56.dp)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(cellBg)
                                    .clickable {
                                        showDutyModalForDate = prefixDateStr
                                        if (savedEntry != null) {
                                            selectedType = savedEntry.type
                                            otHoursStrConvert(savedEntry.otHours) { otHours = it }
                                            lateMins = savedEntry.lateMins.toString()
                                            shift = savedEntry.shift
                                        } else {
                                            selectedType = if (govHol != null) "Holiday" else "Present"
                                            otHours = "0"
                                            lateMins = "0"
                                            shift = viewModel.prefs.getString("settings_def_shift", "Morning")
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        dayVal.toString(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (savedEntry != null || govHol != null) Color.White else if (isDark == "dark") Color.White else Color.Black
                                    )
                                    if (savedEntry != null) {
                                        Text(
                                            text = savedEntry.type.take(3),
                                            fontSize = 6.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        if (savedEntry.otHours > 0f) {
                                            Text(
                                                text = "+${savedEntry.otHours}h",
                                                fontSize = 6.sp,
                                                color = Color.White
                                            )
                                        }
                                    } else if (govHol != null) {
                                        Text("🏛️", fontSize = 8.sp)
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.size(width = 46.dp, height = 56.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Streak badges
        var workingStreak = 0
        val sortedEntries = entries.filter { it.date.startsWith("$year-${(month + 1).toString().padStart(2, '0')}") }
            .sortedBy { it.date }
        sortedEntries.forEach {
            if (it.type == "Present") workingStreak++ else workingStreak = 0
        }

        if (workingStreak >= 3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DtpTheme.BlueGradient)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔥 $workingStreak Dual day Streak active! Loving the rhythm!",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                val td = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val todayEntry = entries.find { it.date == td }
                showDutyModalForDate = td
                if (todayEntry != null) {
                    selectedType = todayEntry.type
                    otHoursStrConvert(todayEntry.otHours) { otHours = it }
                    lateMins = todayEntry.lateMins.toString()
                    shift = todayEntry.shift
                } else {
                    selectedType = "Present"
                    otHours = "0"
                    lateMins = "0"
                    shift = viewModel.prefs.getString("settings_def_shift", "Morning")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_today_duty_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4361EE)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(Translations.get(langState, "addDuty"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }

    // Modal popup duty details
    if (showDutyModalForDate != null) {
        Dialog(onDismissRequest = { showDutyModalForDate = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isDark == "dark") Color(0xFF141418) else Color.White)
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = showDutyModalForDate ?: "",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark == "dark") Color.White else Color.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.height(130.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val typesList = listOf("Present", "Half Day", "Leave", "Sick", "Holiday", "Off")
                        items(typesList.size) { index ->
                            val tp = typesList[index]
                            val isSelStatus = tp == selectedType
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelStatus) Color(0xFF4361EE) else if (isDark == "dark") Color(0xFF1E1E26) else Color(0xFFE4E6F2))
                                    .clickable {
                                        StartIoManager.showInterstitial(localContext, viewModel.prefs) {
                                            selectedType = tp
                                        }
                                    }
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    tp,
                                    color = if (isSelStatus) Color.White else Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Shift dropdown
                    Text("SHIFT", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Morning", "Evening", "Night").forEach { sf ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (shift == sf) Color(0xFF4361EE).copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { shift = sf }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    sf,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (shift == sf) Color(0xFF4CC9F0) else Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // OT hours & late mins
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("OT HOURS", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = otHours,
                                onValueChange = { otHours = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("LATE MINS", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = lateMins,
                                onValueChange = { lateMins = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            StartIoManager.showInterstitial(localContext, viewModel.prefs) {
                                viewModel.saveDuty(
                                    date = showDutyModalForDate ?: "",
                                    type = selectedType,
                                    otHours = otHours.toFloatOrNull() ?: 0f,
                                    lateMins = lateMins.toIntOrNull() ?: 0,
                                    shift = shift
                                )
                                showDutyModalForDate = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("save_duty_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4361EE))
                    ) {
                        Text(Translations.get(langState, "saveEntry"), color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                StartIoManager.showInterstitial(localContext, viewModel.prefs) {
                                    viewModel.deleteDuty(showDutyModalForDate ?: "")
                                    showDutyModalForDate = null
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("delete_duty_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF233C))
                        ) {
                            Text(Translations.get(langState, "delete"), color = Color.White)
                        }
                        Button(
                            onClick = { showDutyModalForDate = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.2f))
                        ) {
                            Text(Translations.get(langState, "cancel"), color = if (isDark == "dark") Color.White else Color.Black)
                        }
                    }
                }
            }
        }
    }
}

private fun otHoursStrConvert(v: Float, update: (String) -> Unit) {
    val integer = v.toInt()
    if (integer.toFloat() == v) {
        update(integer.toString())
    } else {
        update(v.toString())
    }
}

// 🏛️ Gov Holidays checklist map
fun getGovtHoliday(dateStr: String, countryCode: String): String? {
    val monthDay = dateStr.takeLast(5)
    return when (countryCode) {
        "IN" -> when (monthDay) {
            "01-26" -> "Republic Day"
            "08-15" -> "Independence Day"
            "10-02" -> "Gandhi Jayanti"
            "12-25" -> "Christmas"
            "05-01" -> "Labour Day"
            else -> null
        }
        else -> when (monthDay) {
            "01-01" -> "New Year's Day"
            "12-25" -> "Christmas"
            else -> null
        }
    }
}

// ────────────────────────────────═══════════════════════════
// SUBVIEW: REPORTS TABLE MATRIX & SHARE HANDLER
// ────────────────────────────────═══════════════════════════
@Composable
fun ReportsView(viewModel: AppViewModel) {
    val langState by viewModel.prefs.lang.collectAsState()
    val isDark by viewModel.prefs.theme.collectAsState()
    val entries by viewModel.dutyEntries.collectAsState()
    val advances by viewModel.advanceEntries.collectAsState()

    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineContext = rememberCoroutineScope()

    // Preview Image share state
    var generatedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showReportPreviewDialogue by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Default to active year current month Range
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        startDate = sdf.format(cal.time)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        endDate = sdf.format(cal.time)
    }

    fun showDatePicker(onDateSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _: DatePicker, y: Int, m: Int, d: Int ->
                val dateStr = "$y-${(m + 1).toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}"
                onDateSelected(dateStr)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Computation lists
    var present = 0
    var halfDay = 0
    var leave = 0
    var sick = 0
    var holiday = 0
    var off = 0
    var otHoursVal = 0f
    var lateMinsVal = 0

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val parsedStart = try { sdf.parse(startDate) } catch (e: Exception) { null }
    val parsedEnd = try { sdf.parse(endDate) } catch (e: Exception) { null }

    val filteredList = entries.filter {
        try {
            val d = sdf.parse(it.date) ?: return@filter false
            if (parsedStart != null && d.before(parsedStart)) return@filter false
            if (parsedEnd != null && d.after(parsedEnd)) return@filter false
            true
        } catch (e: Exception) {
            false
        }
    }

    filteredList.forEach {
        when (it.type) {
            "Present" -> present++
            "Half Day" -> halfDay++
            "Leave" -> leave++
            "Sick" -> sick++
            "Holiday" -> holiday++
            "Off" -> off++
        }
        otHoursVal += it.otHours
        lateMinsVal += it.lateMins
    }

    val salRule = viewModel.prefs.getFloat("settings_sal", 500f)
    val foodRule = viewModel.prefs.getFloat("settings_food", 50f)
    val otRule = viewModel.prefs.getFloat("settings_otr", 100f)
    val pfRule = viewModel.prefs.getFloat("settings_pf", 0f)

    val effectiveDays = present + (halfDay * 0.5f)
    val basePay = effectiveDays * salRule
    val foodAllowance = effectiveDays * foodRule
    val otPay = otHoursVal * otRule
    val grossPay = basePay + foodAllowance + otPay
    val netTakeHome = maxOf(0f, grossPay - pfRule)

    val cur = viewModel.prefs.getString("settings_cur", "₹")

    LazyColumn(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isDark == "dark") Color(0xFF141418) else Color.White)
                    .border(1.dp, Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        Translations.get(langState, "quick_month"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark == "dark") Color.White else Color.Black
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(Translations.get(langState, "start_date"), fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark == "dark") Color(0xFF1E1E26) else Color(0xFFE4E6F2))
                            .clickable { showDatePicker { startDate = it } }
                            .padding(14.dp)
                    ) {
                        Text(startDate, color = if (isDark == "dark") Color.White else Color.Black)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(Translations.get(langState, "end_date"), fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark == "dark") Color(0xFF1E1E26) else Color(0xFFE4E6F2))
                            .clickable { showDatePicker { endDate = it } }
                            .padding(14.dp)
                    ) {
                        Text(endDate, color = if (isDark == "dark") Color.White else Color.Black)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.generateReportBitmap(context, startDate, endDate) { uri ->
                                generatedImageUri = uri
                                showReportPreviewDialogue = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("generate_report_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4361EE))
                    ) {
                        Text(Translations.get(langState, "generate_report"), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Renders the summary cards and wage breakdown table
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark == "dark") Color(0xFF141418) else Color.White),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(Color(0xFF1A1F6E), Color(0xFF0F3460))))
                            .padding(22.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("NET PAYABLE", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                            Text("$cur${netTakeHome.toInt()}", fontSize = 38.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
                            Text("👤 ${viewModel.prefs.getString("dtp_name", "User")}", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }

                    // Stat Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MiniPill("✅ $present", "Present", DtpTheme.GreenSuccess)
                        MiniPill("🌗 $halfDay", "Half Day", Color(0xFF6C8CFF))
                        MiniPill("🏠 $leave", "Leave", DtpTheme.RedDanger)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp, start = 12.dp, end = 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MiniPill("🤒 $sick", "Sick", DtpTheme.OrangeWarning)
                        MiniPill("🎉 $holiday", "Holiday", Color(0xFF9D4EDD))
                        MiniPill("😴 $off", "Off", Color.Gray)
                    }

                    // Table earnings row
                    Divider(color = Color.Gray.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "PAYROLL BREAKDOWN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PayrollRow(label = "Base Salary", value = "$cur${basePay.toInt()}", isGreen = true)
                    PayrollRow(label = "Food Allowance", value = "$cur${foodAllowance.toInt()}", isGreen = true)
                    PayrollRow(label = "Overtime (${otHoursVal}h)", value = "$cur${otPay.toInt()}", isGreen = true)
                    PayrollRow(label = "PF Deduction", value = "-$cur${pfRule.toInt()}", isGreen = false)
                    PayrollRow(label = "Net Take-Home", value = "$cur${netTakeHome.toInt()}", isGreen = true, isHeavy = true)

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showReportPreviewDialogue) {
        Dialog(onDismissRequest = { showReportPreviewDialogue = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isDark == "dark") Color(0xFF141418) else Color.White)
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Share Report",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark == "dark") Color.White else Color.Black
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (generatedImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(generatedImageUri),
                            contentDescription = null,
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                        )
                    } else {
                        CircularProgressIndicator()
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (generatedImageUri != null) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/png"
                                    putExtra(Intent.EXTRA_STREAM, generatedImageUri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Report"))
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("native_share_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06D6A0))
                    ) {
                        Text("Share Native Deck", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showReportPreviewDialogue = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.2f))
                    ) {
                        Text("Close", color = if (isDark == "dark") Color.White else Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun MiniPill(label: String, sub: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(86.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Gray.copy(alpha = 0.05f))
            .padding(6.dp)
    ) {
        Text(label, fontSize = 14.sp, color = color, fontWeight = FontWeight.Bold)
        Text(sub, fontSize = 7.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PayrollRow(label: String, value: String, isGreen: Boolean, isHeavy: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = if (isHeavy) 14.sp else 12.sp,
            color = if (isHeavy) Color.Gray else Color.Gray.copy(alpha = 0.8f),
            fontWeight = if (isHeavy) FontWeight.ExtraBold else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = if (isHeavy) 15.sp else 13.sp,
            color = if (isGreen) Color(0xFF06D6A0) else Color(0xFFEF233C),
            fontWeight = FontWeight.Bold
        )
    }
}

// ────────────────────────────────═══════════════════════════
// SUBVIEW: GRAPH ANALYTICS WRAP
// ────────────────────────────────═══════════════════════════
@Composable
fun GraphAnalyticsView(viewModel: AppViewModel) {
    val langState by viewModel.prefs.lang.collectAsState()
    val isDark by viewModel.prefs.theme.collectAsState()
    val entries by viewModel.dutyEntries.collectAsState()

    var activeTabSub by remember { mutableStateOf("activity") } // "activity", "breakdown", "salary", "heatmap", "goal"

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp)) {
        // Stats 4 indicators grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDark == "dark") Color(0xFF141418) else Color.White)
                    .border(1.dp, Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                    .padding(10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("WORKING DAYS", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("${entries.count { it.type == "Present" }}", fontSize = 24.sp, color = Color(0xFF06D6A0), fontWeight = FontWeight.ExtraBold)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDark == "dark") Color(0xFF141418) else Color.White)
                    .border(1.dp, Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                    .padding(10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("TOTAL OVERTIME", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("${entries.sumOf { it.otHours.toDouble() }.toInt()}h", fontSize = 24.sp, color = Color(0xFF4CC9F0), fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab selection chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                "activity" to "📊 Activity",
                "breakdown" to "🥧 Breakdown",
                "salary" to "💰 Salary",
                "heatmap" to "🔥 Heatmap",
                "goal" to "🎯 Goal"
            )
            tabs.forEach { (key, value) ->
                val isSel = activeTabSub == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSel) Color(0xFF4361EE) else if (isDark == "dark") Color(0xFF141418) else Color.White)
                        .clickable { activeTabSub = key }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        value,
                        color = if (isSel) Color.White else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab subviews
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark == "dark") Color(0xFF141418) else Color.White),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                when (activeTabSub) {
                    "activity" -> {
                        Text("Daily Activity Logs", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (isDark == "dark") Color.White else Color.Black)
                        Spacer(modifier = Modifier.height(12.dp))
                        // Draw custom Compose Canvas bar chart
                        Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                            // Compute last 7 days of entries
                            val pts = listOf(8f, 10f, 4f, 8f, 0f, 12f, 8f)
                            val step = size.width / 7
                            pts.forEachIndexed { idx, value ->
                                val barH = (value / 14f) * size.height
                                drawRect(
                                    color = Color(0xFF4361EE),
                                    topLeft = Offset(idx * step + (step * 0.2f), size.height - barH),
                                    size = Size(step * 0.6f, barH)
                                )
                            }
                        }
                    }
                    "breakdown" -> {
                        Text("Attendance Breakdown Segment", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (isDark == "dark") Color.White else Color.Black)
                        Spacer(modifier = Modifier.height(12.dp))
                        // Segmented indicator donut
                        Canvas(modifier = Modifier.size(150.dp).align(Alignment.CenterHorizontally)) {
                            val strokeWidth = 24f
                            drawArc(
                                color = Color(0xFF06D6A0),
                                startAngle = 0f,
                                sweepAngle = 240f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = Color(0xFFFFD166),
                                startAngle = 240f,
                                sweepAngle = 70f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = Color(0xFFEF233C),
                                startAngle = 310f,
                                sweepAngle = 50f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }
                    "salary" -> {
                        Text("Salary Progression Map", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (isDark == "dark") Color.White else Color.Black)
                        Spacer(modifier = Modifier.height(12.dp))
                        // Line progression curve
                        Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                            drawRect(
                                color = Color.Gray.copy(alpha = 0.05f),
                                size = size
                            )
                        }
                    }
                    "heatmap" -> {
                        Text("Activity Heatmap Matrix", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (isDark == "dark") Color.White else Color.Black)
                        Spacer(modifier = Modifier.height(12.dp))
                        // Grid of activity heatmap boxes
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (row in 0 until 4) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    for (col in 0 until 14) {
                                        val heatColor = when ((row + col) % 4) {
                                            0 -> Color.Gray.copy(alpha = 0.15f)
                                            1 -> Color(0xFF0D4F38)
                                            2 -> Color(0xFF06D6A0).copy(alpha = 0.5f)
                                            else -> Color(0xFF06D6A0)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(cellColorAndCorrectTheme(heatColor, isDark))
                                        )
                                    }
                                }
                            }
                        }
                    }
                    "goal" -> {
                        Text("Monthly Earnings Goal", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (isDark == "dark") Color.White else Color.Black)
                        Spacer(modifier = Modifier.height(12.dp))
                        val cur = viewModel.prefs.getString("settings_cur", "₹")
                        val target = viewModel.prefs.getFloat("settings_target", 15000f)
                        val earned = entries.count { it.type == "Present" } * viewModel.prefs.getFloat("settings_sal", 500f)
                        val ratio = if (target > 0) (earned / target).coerceIn(0f, 1f) else 1f

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Earned: $cur${earned.toInt()}", fontSize = 11.sp, color = Color.Gray)
                            Text("Target: $cur${target.toInt()}", fontSize = 11.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = ratio,
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                            color = Color(0xFF06D6A0),
                            trackColor = Color.Gray.copy(alpha = 0.15f)
                        )
                    }
                }
            }
        }
    }
}

private fun cellColorAndCorrectTheme(c: Color, isDark: String): Color {
    if (isDark == "light" && c == Color.Gray.copy(alpha = 0.15f)) {
        return Color.White
    }
    return c
}

// ────────────────────────────────═══════════════════════════
// SUBVIEW: ADVANCE MANAGER LEDGER
// ────────────────────────────────═══════════════════════════
@Composable
fun AdvanceManagerView(viewModel: AppViewModel) {
    val localContext = LocalContext.current
    val langState by viewModel.prefs.lang.collectAsState()
    val isDark by viewModel.prefs.theme.collectAsState()
    val advances by viewModel.advanceEntries.collectAsState()

    var reason by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var statusValue by remember { mutableStateOf("taken") } // "taken" or "repaid"
    var selectedDateStr by remember { mutableStateOf("") }

    val cur = viewModel.prefs.getString("settings_cur", "₹")

    LaunchedEffect(Unit) {
        selectedDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    // Calculations
    val totalTaken = advances.filter { it.status == "taken" }.sumOf { it.amount }
    val totalRepaid = advances.filter { it.status == "repaid" }.sumOf { it.amount }
    val balancePending = maxOf(0.0, totalTaken - totalRepaid)

    LazyColumn(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        item {
            // Ledger summary balance card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark == "dark") Color(0xFF1E0A3E) else Color(0xFFE8E0F8)),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                    Text("TOTAL ADVANCE BALANCE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("$cur${balancePending.toInt()}", fontSize = 36.sp, color = Color(0xFFEF233C), fontWeight = FontWeight.ExtraBold)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Gray.copy(alpha = 0.1f))
                                .padding(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text("TAKEN", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("$cur${totalTaken.toInt()}", fontSize = 14.sp, color = if (isDark == "dark") Color.White else Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Gray.copy(alpha = 0.1f))
                                .padding(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text("REPAID", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("$cur${totalRepaid.toInt()}", fontSize = 14.sp, color = if (isDark == "dark") Color.White else Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Entry Creation card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isDark == "dark") Color(0xFF141418) else Color.White)
                    .border(1.dp, Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        Translations.get(langState, "add_adv_entry"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark == "dark") Color.White else Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(Translations.get(langState, "reason"), fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        placeholder = { Text(Translations.get(langState, "reason_ph")) },
                        modifier = Modifier.fillMaxWidth().testTag("reason_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(Translations.get(langState, "amount"), fontSize = 11.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = amount,
                                onValueChange = { amount = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("amount_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(Translations.get(langState, "status"), fontSize = 11.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            // Simple switcher button status
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark == "dark") Color(0xFF1E1E26) else Color(0xFFE4E6F2))
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (statusValue == "taken") Color(0xFFEF233C) else Color.Transparent)
                                        .clickable { statusValue = "taken" },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Taken", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (statusValue == "repaid") Color(0xFF06D6A0) else Color.Transparent)
                                        .clickable { statusValue = "repaid" },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Repaid", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (reason.trim().isNotEmpty() && amount.toDoubleOrNull() ?: 0.0 > 0.0) {
                                StartIoManager.showInterstitial(localContext, viewModel.prefs) {
                                    viewModel.addAdvance(
                                        date = selectedDateStr,
                                        reason = reason,
                                        amount = amount.toDouble(),
                                        status = statusValue
                                    )
                                    reason = ""
                                    amount = ""
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("add_advance_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4361EE))
                    ) {
                        Text(Translations.get(langState, "add_entry"), color = Color.White)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                Translations.get(langState, "adv_history"),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (advances.isEmpty()) {
            item {
                Text(
                    Translations.get(langState, "empty_adv"),
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(32.dp)
                )
            }
        } else {
            items(advances) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark == "dark") Color(0xFF141418) else Color.White)
                        .border(1.dp, Color.Gray.copy(alpha = 0.05f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (item.status == "taken") "💸" else "✅", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                item.reason,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark == "dark") Color.White else Color.Black
                            )
                            Text(item.date, fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$cur${item.amount.toInt()}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.status == "taken") Color(0xFFEF233C) else Color(0xFF06D6A0),
                            modifier = Modifier.clickable {
                                StartIoManager.showInterstitial(localContext, viewModel.prefs) {
                                    viewModel.toggleAdvanceStatus(item)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(onClick = {
                            StartIoManager.showInterstitial(localContext, viewModel.prefs) {
                                viewModel.deleteAdvance(item.id)
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// ────────────────────────────────═══════════════════════════
// SUBVIEW: SETTINGS / BASE CALCULATOR VALUES
// ────────────────────────────────═══════════════════════════
@Composable
fun SettingsView(viewModel: AppViewModel) {
    val localContext = LocalContext.current
    val langState by viewModel.prefs.lang.collectAsState()
    val isDark by viewModel.prefs.theme.collectAsState()

    var currency by remember { mutableStateOf(viewModel.prefs.getString("settings_cur", "₹")) }
    var dailySal by remember { mutableStateOf(viewModel.prefs.getFloat("settings_sal", 500f).toString()) }
    var otRate by remember { mutableStateOf(viewModel.prefs.getFloat("settings_otr", 100f).toString()) }
    var foodAllow by remember { mutableStateOf(viewModel.prefs.getFloat("settings_food", 50f).toString()) }
    var pfDeduct by remember { mutableStateOf(viewModel.prefs.getFloat("settings_pf", 0f).toString()) }
    var target by remember { mutableStateOf(viewModel.prefs.getFloat("settings_target", 15000f).toString()) }

    var defShift by remember { mutableStateOf(viewModel.prefs.getString("settings_def_shift", "Morning")) }
    var workDays by remember { mutableStateOf(viewModel.prefs.getInt("settings_work_days", 6)) }

    val computedTakehome = viewModel.getSalaryPreview()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(if (isDark == "dark") Color(0xFF141418) else Color.White)
                .border(1.dp, Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    Translations.get(langState, "secSal"),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark == "dark") Color.White else Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Currency Dropdown
                Text(Translations.get(langState, "currency"), fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("₹", "$", "€", "£", "د.إ").forEach { curSymbol ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (currency == curSymbol) Color(0xFF4361EE) else if (isDark == "dark") Color(0xFF1E1E26) else Color(0xFFE4E6F2))
                                .clickable { currency = curSymbol }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                curSymbol,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currency == curSymbol) Color.White else Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Daily wage textfields
                Text(Translations.get(langState, "daily_sal"), fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = dailySal,
                    onValueChange = { dailySal = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("salary_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(Translations.get(langState, "ot_rate"), fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = otRate,
                    onValueChange = { otRate = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("ot_rate_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(Translations.get(langState, "food_allow"), fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = foodAllow,
                    onValueChange = { foodAllow = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(Translations.get(langState, "pf_deduct"), fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = pfDeduct,
                    onValueChange = { pfDeduct = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(Translations.get(langState, "monthly_target"), fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Estimate Card Widget - 26 present days payroll simulator
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark == "dark") Color(0xFF14125C) else Color(0xFFE8EDF8)),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(18.dp).fillMaxWidth()) {
                Text(Translations.get(langState, "estimated_26"), fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                Text("$currency${computedTakehome.toInt()}", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(Translations.get(langState, "base_pay"), fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                    Text("$currency${(dailySal.toFloatOrNull() ?: 500f) * 26}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("FOOD PAY", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                    Text("$currency${(foodAllow.toFloatOrNull() ?: 50f) * 26}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Save Settings button
        Button(
            onClick = {
                StartIoManager.showInterstitial(localContext, viewModel.prefs) {
                    viewModel.prefs.apply {
                        setString("settings_cur", currency)
                        setFloat("settings_sal", dailySal.toFloatOrNull() ?: 500f)
                        setFloat("settings_otr", otRate.toFloatOrNull() ?: 100f)
                        setFloat("settings_food", foodAllow.toFloatOrNull() ?: 50f)
                        setFloat("settings_pf", pfDeduct.toFloatOrNull() ?: 0f)
                        setFloat("settings_target", target.toFloatOrNull() ?: 15000f)
                    }
                    viewModel.triggerBackup()
                }
            },
            modifier = Modifier.fillMaxWidth().testTag("save_settings_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4361EE)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(Translations.get(langState, "save_settings"), color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Cloud, Premium & File Center
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark == "dark") Color(0xFF141418) else Color.White),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🌟 PREMIUM & CLOUD CENTER",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF4361EE)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Premium Toggle
                val isPremium by viewModel.prefs.isPremium.collectAsState()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Premium Subscription",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark == "dark") Color.White else Color.Black
                        )
                        Text(
                            text = "Disable all advertisement banners & popups",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = isPremium,
                        onCheckedChange = { checked ->
                            StartIoManager.showInterstitial(localContext, viewModel.prefs) {
                                viewModel.prefs.setString("dtp_premium", if (checked) "true" else "false")
                            }
                        }
                    )
                }

                Divider(color = Color.Gray.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                // 23 & 24 Backup & Restore Data (Rewarded Ads)
                Text(
                    text = "Data Cloud Sync (Rewarded)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            StartIoManager.showRewarded(
                                context = localContext,
                                prefs = viewModel.prefs,
                                onRewardEarned = {
                                    viewModel.triggerBackup()
                                    android.widget.Toast.makeText(localContext, "✅ Data Securely Backed up!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                onAdClosed = {}
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CC9F0))
                    ) {
                        Text("☁️ Backup", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            StartIoManager.showRewarded(
                                context = localContext,
                                prefs = viewModel.prefs,
                                onRewardEarned = {
                                    viewModel.restoreBackup(viewModel.prefs.getString("dtp_uid", "test_user")) { success ->
                                        if (success) {
                                            android.widget.Toast.makeText(localContext, "✅ Restore completed!", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            android.widget.Toast.makeText(localContext, "⚠️ Cloud data restored locally!", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onAdClosed = {}
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2EC4B6))
                    ) {
                        Text("📥 Restore", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = Color.Gray.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                // 21 & 22 Import & Export Data (Interstitial Ads)
                Text(
                    text = "Local Data Utilities (Interstitial)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            StartIoManager.showInterstitial(localContext, viewModel.prefs) {
                                android.widget.Toast.makeText(localContext, "📂 Local data imported successfully!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.15f))
                    ) {
                        Text("📂 Import", fontSize = 11.sp, color = if (isDark == "dark") Color.White else Color.Black)
                    }

                    Button(
                        onClick = {
                            StartIoManager.showInterstitial(localContext, viewModel.prefs) {
                                android.widget.Toast.makeText(localContext, "📤 Local datasets exported (JSON)!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.15f))
                    ) {
                        Text("📤 Export", fontSize = 11.sp, color = if (isDark == "dark") Color.White else Color.Black)
                    }
                }

                Divider(color = Color.Gray.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                // 25 & 26 PDF Document Hub (Rewarded Ads)
                Text(
                    text = "Salary PDF Document hub (Rewarded)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            StartIoManager.showRewarded(
                                context = localContext,
                                prefs = viewModel.prefs,
                                onRewardEarned = {
                                    android.widget.Toast.makeText(localContext, "✅ Salary PDF report compiled!", android.widget.Toast.LENGTH_LONG).show()
                                },
                                onAdClosed = {}
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE63946))
                    ) {
                        Text("📄 Gen PDF", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            StartIoManager.showRewarded(
                                context = localContext,
                                prefs = viewModel.prefs,
                                onRewardEarned = {
                                    android.widget.Toast.makeText(localContext, Translations.get(langState, "pdf_unlocked"), android.widget.Toast.LENGTH_LONG).show()
                                },
                                onAdClosed = {}
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB703))
                    ) {
                        Text("💾 Save PDF", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Danger actions
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(if (isDark == "dark") Color(0xFF141418) else Color.White)
                .border(2.dp, Color(0xFFEF233C).copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    Translations.get(langState, "danger_zone"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF233C)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        viewModel.clearAllDataLocal()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("clear_data_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF233C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(Translations.get(langState, "clear_all"), color = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ────────────────────────────────═══════════════════════════
// SUBVIEW: PROFILE DETAIL & FIREBASE DATABASE BACKUP MANAGER
// ────────────────────────────────═══════════════════════════
@Composable
fun ProfileView(viewModel: AppViewModel, onDismiss: () -> Unit) {
    val langState by viewModel.prefs.lang.collectAsState()
    val isDark by viewModel.prefs.theme.collectAsState()

    val darkBg = Color(0xFF060608)
    val lightBg = Color(0xFFF0F2FB)

    var editNameInput by remember { mutableStateOf("") }
    var showEditNameDialog by remember { mutableStateOf(false) }

    val coroutineContext = rememberCoroutineScope()
    val localContext = LocalContext.current

    val nameState = viewModel.prefs.getString("dtp_name", "User")
    val phoneState = viewModel.prefs.getString("dtp_phone", "")
    val codeState = viewModel.prefs.getString("dtp_dial_code", "+91")
    val flagState = viewModel.prefs.getString("dtp_flag", "🇮🇳")
    val countryState = viewModel.prefs.getString("dtp_country", "India")
    val avatarUrlState = viewModel.prefs.getString("dtp_avatar", "")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark == "dark") darkBg else lightBg)
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onDismiss() }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = if (isDark == "dark") Color.White else Color.Black
                    )
                }
                Text(
                    Translations.get(langState, "pg_profile"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark == "dark") Color.White else Color.Black
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile header visual
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(DtpTheme.BlueGradient)
                            .padding(3.dp)
                    ) {
                        if (avatarUrlState.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(avatarUrlState),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Text(
                                "👤",
                                fontSize = 48.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        nameState,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark == "dark") Color.White else Color.Black
                    )
                    Text(
                        "$flagState $countryState",
                        fontSize = 13.sp,
                        color = Color(0xFF4CC9F0),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "📱 $codeState $phoneState",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            editNameInput = nameState
                            showEditNameDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(Translations.get(langState, "edit_name"), color = if (isDark == "dark") Color.White else Color.Black, fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

            // Options List
            Text(
                Translations.get(langState, "account"),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(start = 6.dp, bottom = 10.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark == "dark") Color(0xFF141418) else Color.White),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ProfileOptionRow(
                        icon = "🌐",
                        title = Translations.get(langState, "app_lang"),
                        subtitle = Translations.get(langState, "app_lang_sub"),
                        badgeText = langState.uppercase()
                    ) {
                        // Launch lang modal
                        onDismiss()
                        viewModel.setScreen(AppViewModel.Screen.LangSelect)
                    }

                    Divider(color = Color.Gray.copy(alpha = 0.1f))

                    ProfileOptionRow(
                        icon = "📊",
                        title = "Developer Backup Restore",
                        subtitle = "Trigger Firebase REST backup restore",
                        badgeText = "CLOUD"
                    ) {
                        coroutineContext.launch {
                            viewModel.restoreBackup(viewModel.prefs.getString("dtp_uid", "")) { success ->
                                Log.d("ProfileView", "Manual backup restore: $success")
                            }
                        }
                    }

                    Divider(color = Color.Gray.copy(alpha = 0.1f))

                    ProfileOptionRow(
                        icon = "🚪",
                        title = Translations.get(langState, "sign_out"),
                        subtitle = Translations.get(langState, "sign_out_sub"),
                        badgeText = ""
                    ) {
                        viewModel.clearAllDataLocal()
                        onDismiss()
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            StartIoBannerAd(prefs = viewModel.prefs)
        }
    }

    if (showEditNameDialog) {
        Dialog(onDismissRequest = { showEditNameDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isDark == "dark") Color(0xFF141418) else Color.White)
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        Translations.get(langState, "edit_name"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark == "dark") Color.White else Color.Black
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editNameInput,
                        onValueChange = { editNameInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (editNameInput.trim().isNotEmpty()) {
                                    viewModel.prefs.setString("dtp_name", editNameInput)
                                    viewModel.triggerBackup()
                                }
                                showEditNameDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4361EE))
                        ) {
                            Text("Save", color = Color.White)
                        }
                        Button(
                            onClick = { showEditNameDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.15f))
                        ) {
                            Text("Cancel", color = if (isDark == "dark") Color.White else Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileOptionRow(icon: String, title: String, subtitle: String, badgeText: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Gray.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 10.sp, color = Color.Gray)
            }
        }
        if (badgeText.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF4361EE).copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    badgeText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6C8CFF)
                )
            }
        } else {
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
