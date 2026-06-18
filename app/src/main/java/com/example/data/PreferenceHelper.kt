package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

class PreferenceHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("duty_tracker_prefs", Context.MODE_PRIVATE)

    // Reactive states
    private val _theme = MutableStateFlow(getString("dtp_theme", "dark"))
    val theme: StateFlow<String> = _theme

    private val _lang = MutableStateFlow(getString("dtp_lang", "en"))
    val lang: StateFlow<String> = _lang

    private val _isPremium = MutableStateFlow(getString("dtp_premium", "false") == "true")
    val isPremium: StateFlow<Boolean> = _isPremium

    private val _profileUpdated = MutableStateFlow(0)
    val profileUpdated: StateFlow<Int> = _profileUpdated

    fun setString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
        if (key == "dtp_theme") {
            _theme.value = value
        } else if (key == "dtp_lang") {
            _lang.value = value
        } else if (key == "dtp_premium") {
            _isPremium.value = (value == "true")
        } else if (key.startsWith("dtp_")) {
            _profileUpdated.value += 1
        }
    }

    fun getString(key: String, default: String): String {
        return prefs.getString(key, default) ?: default
    }

    fun setFloat(key: String, value: Float) {
        prefs.edit().putFloat(key, value).apply()
        if (key.startsWith("dtp_") || key.startsWith("settings_")) {
            _profileUpdated.value += 1
        }
    }

    fun getFloat(key: String, default: Float): Float {
        return prefs.getFloat(key, default)
    }

    fun setInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
        if (key.startsWith("dtp_") || key.startsWith("settings_")) {
            _profileUpdated.value += 1
        }
    }

    fun getInt(key: String, default: Int): Int {
        return prefs.getInt(key, default)
    }

    fun getProfileJson(): String {
        val root = JSONObject().apply {
            put("name", getString("dtp_name", "User"))
            put("phone", getString("dtp_phone", ""))
            put("dialCode", getString("dtp_dial_code", "+91"))
            put("country", getString("dtp_country", "India"))
            put("countryCode", getString("dtp_country_code", "IN"))
            put("flag", getString("dtp_flag", "🇮🇳"))
            put("pin", getString("dtp_pin", ""))
            put("avatar", getString("dtp_avatar", ""))
            put("createdAt", getString("dtp_created_at", ""))
            put("lang", getString("dtp_lang", "en"))
            put("theme", getString("dtp_theme", "dark"))
            put("company", getString("dtp_company", ""))
            put("job", getString("dtp_job", ""))
            put("empid", getString("dtp_empid", ""))
        }
        return root.toString()
    }

    fun getSettingsJson(): String {
        val root = JSONObject().apply {
            put("cur", getString("settings_cur", "₹"))
            put("sal", getFloat("settings_sal", 500f).toDouble())
            put("otr", getFloat("settings_otr", 100f).toDouble())
            put("food", getFloat("settings_food", 50f).toDouble())
            put("pf", getFloat("settings_pf", 0f).toDouble())
            put("target", getFloat("settings_target", 15000f).toDouble())
            put("defShift", getString("settings_def_shift", "Morning"))
            put("workDays", getInt("settings_work_days", 6))
        }
        return root.toString()
    }

    fun importSettingsFromJson(jsonObj: JSONObject) {
        setString("settings_cur", jsonObj.optString("cur", "₹"))
        setFloat("settings_sal", jsonObj.optDouble("sal", 500.0).toFloat())
        setFloat("settings_otr", jsonObj.optDouble("otr", 100.0).toFloat())
        setFloat("settings_food", jsonObj.optDouble("food", 50.0).toFloat())
        setFloat("settings_pf", jsonObj.optDouble("pf", 0.0).toFloat())
        setFloat("settings_target", jsonObj.optDouble("target", 15000.0).toFloat())
        setString("settings_def_shift", jsonObj.optString("defShift", "Morning"))
        setInt("settings_work_days", jsonObj.optInt("workDays", 6))
    }

    fun clearAllData() {
        prefs.edit().clear().apply()
        _theme.value = "dark"
        _lang.value = "en"
        _isPremium.value = false
        _profileUpdated.value += 1
    }
}
