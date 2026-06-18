package com.example.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.net.Uri
import android.provider.MediaStore
import android.widget.DatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.data.*
import com.example.data.remote.ImgBbClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

// Styling Color Palettes
object DtpTheme {
    val BlueGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF4361EE), Color(0xFF4CC9F0))
    )
    val PurpleGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF1E0A3E), Color(0xFF2D1B69))
    )
    val LightPurpleGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFE8E0F8), Color(0xFFD8C8F5))
    )
    val GreenSuccess = Color(0xFF06D6A0)
    val RedDanger = Color(0xFFEF233C)
    val OrangeWarning = Color(0xFFFF9F1C)
}

// ────────────────────────────────═══════════════════════════
// SCREEN 1: SPLASH
// ────────────────────────────────═══════════════════════════
@Composable
fun SplashView(viewModel: AppViewModel) {
    val langState by viewModel.prefs.lang.collectAsState()
    val isDark by viewModel.prefs.theme.collectAsState()

    val darkBg = Color(0xFF060608)
    val lightBg = Color(0xFFF0F2FB)

    LaunchedEffect(Unit) {
        delay(2600)
        if (viewModel.isLoggedIn) {
            viewModel.setScreen(AppViewModel.Screen.Home)
        } else {
            viewModel.setScreen(AppViewModel.Screen.LangSelect)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark == "dark") darkBg else lightBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(DtpTheme.BlueGradient)
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
                    Text("🛡️", fontSize = 46.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Duty Tracker Pro",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark == "dark") Color.White else Color(0xFF0D0E1A)
            )
            Text(
                text = Translations.get(langState, "splash_sub"),
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Made with ❤️ by Nitai Studio",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = Translations.get(langState, "badge") + " - India Language Support",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// ────────────────────────────────═══════════════════════════
// SCREEN 2: LANG SELECT
// ────────────────────────────────═══════════════════════════
@Composable
fun LangSelectView(viewModel: AppViewModel) {
    val langState by viewModel.prefs.lang.collectAsState()
    val isDark by viewModel.prefs.theme.collectAsState()
    var searchQ by remember { mutableStateOf("") }
    var selectedVal by remember { mutableStateOf(langState) }

    val darkBg = Color(0xFF060608)
    val lightBg = Color(0xFFF0F2FB)

    val filtered = Translations.languages.filter {
        it.name.contains(searchQ, ignoreCase = true) || it.native.contains(searchQ, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark == "dark") darkBg else lightBg)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = Translations.get(selectedVal, "lang_title"),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark == "dark") Color.White else Color(0xFF0D0E1A)
            )
            Text(
                text = Translations.get(selectedVal, "lang_sub"),
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQ,
                onValueChange = { searchQ = it },
                placeholder = { Text(Translations.get(selectedVal, "search_lang")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_lang_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4361EE),
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f)
                ),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Grid of Language cards
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered.size) { index ->
                    val lang = filtered[index]
                    val isSel = lang.code == selectedVal
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSel) Color(0xFF4361EE).copy(alpha = 0.15f)
                                else if (isDark == "dark") Color(0xFF141418) else Color.White
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isSel) Color(0xFF4361EE) else Color.Gray.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedVal = lang.code }
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(lang.flag, fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lang.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark == "dark") Color.White else Color(0xFF0D0E1A)
                            )
                            Text(lang.native, fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.prefs.setString("dtp_lang", selectedVal)
                    viewModel.setScreen(AppViewModel.Screen.Onboarding)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("continue_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4361EE))
            ) {
                Text(
                    text = Translations.get(selectedVal, "continue"),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ────────────────────────────────═══════════════════════════
// SCREEN 3: ONBOARDING
// ────────────────────────────────═══════════════════════════
@Composable
fun OnboardingView(viewModel: AppViewModel) {
    val langState by viewModel.prefs.lang.collectAsState()
    val isDark by viewModel.prefs.theme.collectAsState()
    var obIdx by remember { mutableStateOf(0) }

    val slides = listOf(
        Triple("📅", Translations.get(langState, "ob_title_1"), Translations.get(langState, "ob_desc_1")),
        Triple("💰", Translations.get(langState, "ob_title_2"), Translations.get(langState, "ob_desc_2")),
        Triple("📊", Translations.get(langState, "ob_title_3"), Translations.get(langState, "ob_desc_3"))
    )

    val currentSlide = slides[obIdx]

    val darkBg = Color(0xFF060608)
    val lightBg = Color(0xFFF0F2FB)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark == "dark") darkBg else lightBg)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicator dots
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    slides.forEachIndexed { i, _ ->
                        Box(
                            modifier = Modifier
                                .size(width = if (i == obIdx) 24.dp else 8.dp, height = 8.dp)
                                .clip(CircleShape)
                                .background(if (i == obIdx) Color(0xFF4361EE) else Color.Gray.copy(alpha = 0.4f))
                        )
                    }
                }

                // Skip Button
                Text(
                    text = Translations.get(langState, "ob_skip"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4361EE),
                    modifier = Modifier
                        .clickable { viewModel.setScreen(AppViewModel.Screen.Authenticate) }
                        .testTag("skip_button")
                )
            }

            Spacer(modifier = Modifier.weight(0.15f))

            // Emoji / Image Display
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(DtpTheme.BlueGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(currentSlide.first, fontSize = 56.sp)
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = currentSlide.second,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark == "dark") Color.White else Color(0xFF0D0E1A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = currentSlide.third,
                fontSize = 15.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.weight(0.2f))

            // Footer navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (obIdx > 0) {
                    IconButton(
                        onClick = { obIdx-- },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isDark == "dark") Color(0xFF141418) else Color.White)
                            .border(
                                1.dp,
                                Color.Gray.copy(alpha = 0.2f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = if (isDark == "dark") Color.White else Color.Black
                        )
                    }
                }

                Button(
                    onClick = {
                        if (obIdx < slides.size - 1) {
                            obIdx++
                        } else {
                            viewModel.setScreen(AppViewModel.Screen.Authenticate)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("next_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4361EE))
                ) {
                    Text(
                        text = if (obIdx == slides.size - 1) Translations.get(langState, "ob_get_started") else Translations.get(langState, "ob_next"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ────────────────────────────────═══════════════════════════
// SCREEN 4: LOGIN / REGISTRATION & SECURITY PIN LOCK
// ────────────────────────────────═══════════════════════════
@Composable
fun AuthenticateView(viewModel: AppViewModel) {
    val langState by viewModel.prefs.lang.collectAsState()
    val isDark by viewModel.prefs.theme.collectAsState()

    var activeTabSignup by remember { mutableStateOf(false) } // false = Login, true = Signup
    var showCountryModal by remember { mutableStateOf(false) }

    // Forms
    var companyName by remember { mutableStateOf("") }
    var empId by remember { mutableStateOf("") }
    var fullname by remember { mutableStateOf("") }
    var phoneNo by remember { mutableStateOf("") }
    var userCountry by remember { mutableStateOf(Countries.list[0]) } // Def India

    // Auth screen flow: "form" or "pin" or "cpin"
    var pinScreenState by remember { mutableStateOf("form") }
    var enteredPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Compress & Upload via ImgBB
            coroutineScope.launch {
                try {
                    val stream = context.contentResolver.openInputStream(uri)
                    val bytes = stream?.readBytes()
                    stream?.close()
                    if (bytes != null) {
                        val uploadedUrl = ImgBbClient.uploadBytes(bytes)
                        if (uploadedUrl != null) {
                            viewModel.prefs.setString("dtp_avatar", uploadedUrl)
                            Log.d("AuthenticateView", "ImgBB avatar url: $uploadedUrl")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AuthenticateView", "Photo upload failed: ${e.message}")
                }
            }
        }
    }

    val darkBg = Color(0xFF060608)
    val lightBg = Color(0xFFF0F2FB)

    if (showCountryModal) {
        Dialog(onDismissRequest = { showCountryModal = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isDark == "dark") Color(0xFF141418) else Color.White)
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = Translations.get(langState, "select_country"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark == "dark") Color.White else Color.Black
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.height(300.dp)) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(Countries.list) { country ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            userCountry = country
                                            showCountryModal = false
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(country.flag, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "${country.name} (${country.dial})",
                                        fontSize = 15.sp,
                                        color = if (isDark == "dark") Color.White else Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark == "dark") darkBg else lightBg)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            // Header
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DtpTheme.BlueGradient)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isDark == "dark") Color(0xFF141418) else Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛡️", fontSize = 28.sp)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Duty Tracker Pro",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark == "dark") Color.White else Color(0xFF0D0E1A)
            )
            Text(
                text = if (pinScreenState == "form") {
                    if (activeTabSignup) Translations.get(langState, "signupTab") else Translations.get(langState, "loginTab")
                } else if (pinScreenState == "pin") {
                    Translations.get(langState, "enter_pin")
                } else {
                    Translations.get(langState, "confirm_pin")
                },
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Authenticate state switcher
            if (pinScreenState == "form") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isDark == "dark") Color(0xFF141418) else Color.White)
                        .border(
                            1.dp,
                            Color.Gray.copy(alpha = 0.15f),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Tabs Login/Signup
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark == "dark") Color(0xFF1E1E26) else Color(0xFFE4E6F2))
                                .padding(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (!activeTabSignup) Color(0xFF4361EE) else Color.Transparent)
                                    .clickable { activeTabSignup = false }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    Translations.get(langState, "loginTab"),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (activeTabSignup) Color(0xFF4361EE) else Color.Transparent)
                                    .clickable { activeTabSignup = true }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    Translations.get(langState, "signupTab"),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (activeTabSignup) {
                            // Avatar upload icon
                            val avAvatarState by viewModel.prefs.getString("dtp_avatar", "").let { mutableStateOf(it) }
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(DtpTheme.BlueGradient)
                                    .align(Alignment.CenterHorizontally)
                                    .clickable { imageLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (avAvatarState.isNotEmpty()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(avAvatarState),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Text("📷", fontSize = 28.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Name field
                            Text(
                                Translations.get(langState, "fullname"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = fullname,
                                onValueChange = { fullname = it },
                                placeholder = { Text(Translations.get(langState, "name_hint")) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                modifier = Modifier.fillMaxWidth().testTag("fullname_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Company
                            Text(
                                "COMPANY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = companyName,
                                onValueChange = { companyName = it },
                                placeholder = { Text("e.g. Acme Corp") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                modifier = Modifier.fillMaxWidth().testTag("company_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Employee ID
                            Text(
                                "EMPLOYEE ID",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = empId,
                                onValueChange = { empId = it },
                                placeholder = { Text("e.g. EMP-101") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                modifier = Modifier.fillMaxWidth().testTag("empid_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Phone
                        Text(
                            Translations.get(langState, "phone"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark == "dark") Color(0xFF1E1E26) else Color(0xFFE4E6F2))
                                    .clickable { showCountryModal = true }
                                    .padding(horizontal = 12.dp, vertical = 14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(userCountry.flag, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        userCountry.dial,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark == "dark") Color.White else Color.Black
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("▼", fontSize = 8.sp, color = Color.Gray)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = phoneNo,
                                onValueChange = { phoneNo = it },
                                placeholder = { Text(Translations.get(langState, "phone_hint")) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.weight(1f).testTag("phone_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (activeTabSignup && fullname.trim().isEmpty()) {
                                    Log.e("AuthenticateView", "Name empty")
                                    return@Button
                                }
                                if (phoneNo.trim().length < 5) return@Button

                                val uid = "${userCountry.dial.replace("+", "")}_${phoneNo.replace("\\D".toRegex(), "")}"
                                if (activeTabSignup) {
                                    // Go setting PIN
                                    pinScreenState = "pin"
                                } else {
                                    // Check database REST or simple local cache logic
                                    coroutineScope.launch {
                                        viewModel.restoreBackup(uid) { exists ->
                                            if (exists) {
                                                // Take user directly to PIN unlock
                                                pinScreenState = "pin"
                                            } else {
                                                // Create a placeholder profile locally
                                                viewModel.prefs.apply {
                                                    setString("dtp_uid", uid)
                                                    setString("dtp_name", "User")
                                                    setString("dtp_phone", phoneNo)
                                                    setString("dtp_dial_code", userCountry.dial)
                                                    setString("dtp_country", userCountry.name)
                                                    setString("dtp_country_code", userCountry.code)
                                                    setString("dtp_flag", userCountry.flag)
                                                    setString("dtp_pin", "0000") // default placeholder pin
                                                }
                                                viewModel.setScreen(AppViewModel.Screen.Home)
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_auth_continue"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4361EE))
                        ) {
                            Text(
                                Translations.get(langState, "continue"),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            } else if (pinScreenState == "pin") {
                // PIN dots and keypad input
                Column(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = if (activeTabSignup) "Choose 4-Digit PIN" else Translations.get(langState, "pinTitle"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark == "dark") Color.White else Color.Black
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        for (i in 0 until 4) {
                            val filled = i < enteredPin.length
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (filled) Color(0xFF4361EE) else Color.Gray.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Simplified dynamic programmatic keyboard input
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            if (it.length <= 4) enteredPin = it
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .testTag("pin_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        placeholder = { Text("Enter 4-Digit PIN", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4361EE)
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (enteredPin.length == 4) {
                                if (activeTabSignup) {
                                    pinScreenState = "cpin"
                                } else {
                                    // Check local pin
                                    val localPin = viewModel.prefs.getString("dtp_pin", "")
                                    if (enteredPin == localPin || localPin.isEmpty()) {
                                        viewModel.setScreen(AppViewModel.Screen.Home)
                                    } else {
                                        enteredPin = "" // Reset
                                        Log.e("AuthenticateView", "Incorrect PIN")
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_pin_verify"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4361EE))
                    ) {
                        Text(
                            if (activeTabSignup) "Next" else Translations.get(langState, "verify_pin"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        Translations.get(langState, "back"),
                        color = Color.Gray,
                        modifier = Modifier
                            .clickable { pinScreenState = "form" }
                            .padding(8.dp)
                    )
                }
            } else if (pinScreenState == "cpin") {
                // Confirm PIN
                Column(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = Translations.get(langState, "confirm_pin"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark == "dark") Color.White else Color.Black
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        for (i in 0 until 4) {
                            val filled = i < confirmPin.length
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (filled) Color(0xFF4361EE) else Color.Gray.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Real password input
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = {
                            if (it.length <= 4) confirmPin = it
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .testTag("confirm_pin_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        placeholder = { Text("Confirm PIN", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4361EE)
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (confirmPin.length == 4) {
                                if (enteredPin == confirmPin) {
                                    val uid = "${userCountry.dial.replace("+", "")}_${phoneNo.replace("\\D".toRegex(), "")}"
                                    // Save preferences
                                    viewModel.prefs.apply {
                                        setString("dtp_uid", uid)
                                        setString("dtp_name", fullname)
                                        setString("dtp_phone", phoneNo)
                                        setString("dtp_dial_code", userCountry.dial)
                                        setString("dtp_country", userCountry.name)
                                        setString("dtp_country_code", userCountry.code)
                                        setString("dtp_flag", userCountry.flag)
                                        setString("dtp_company", companyName)
                                        setString("dtp_empid", empId)
                                        setString("dtp_pin", enteredPin)
                                        setString("dtp_created_at", SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
                                    }
                                    viewModel.setScreen(AppViewModel.Screen.Home)
                                } else {
                                    confirmPin = "" // Reset
                                    Log.e("AuthenticateView", "PIN mismatch")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_pin_confirm"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06D6A0))
                    ) {
                        Text(
                            Translations.get(langState, "create_account"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        Translations.get(langState, "back"),
                        color = Color.Gray,
                        modifier = Modifier
                            .clickable { pinScreenState = "pin" }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}
