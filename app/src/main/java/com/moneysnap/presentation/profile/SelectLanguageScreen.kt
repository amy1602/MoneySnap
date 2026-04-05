package com.moneysnap.presentation.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneysnap.data.local.LanguagePreferences

private val ScreenBg = Color(0xFFF8FAFC)
private val AccentRed = Color(0xFFE55061)
private val TitleDark = Color(0xFF1A1C1E)

data class LanguageItem(
    val code: String,
    val name: String,
    val subtitle: String,
    val flag: String
)

private val languages = listOf(
    LanguageItem("en", "English (US)", "Default System Language", "🇺🇸"),
    LanguageItem("zh", "中文", "Chinese", "🇨🇳"),
    LanguageItem("vi", "Tiếng Việt", "Vietnamese", "🇻🇳")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectLanguageScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val languagePrefs = remember { LanguagePreferences(context) }
    var selectedCode by remember { mutableStateOf(languagePrefs.selectedLanguage) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Select Language",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TitleDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TitleDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ScreenBg)
            )
        },
        containerColor = ScreenBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Section label
            Text(
                text = "LOCALIZATION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AccentRed,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Heading
            Text(
                text = "Choose Your Language",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TitleDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = "Select your preferred language to experience the financial ledger in your native tongue.",
                fontSize = 15.sp,
                color = Color.Gray,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Language list
            languages.forEach { lang ->
                val isSelected = lang.code == selectedCode
                LanguageRow(
                    language = lang,
                    isSelected = isSelected,
                    onClick = {
                        selectedCode = lang.code
                        languagePrefs.selectedLanguage = lang.code
                        Toast.makeText(
                            context,
                            "Language changed to ${lang.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun LanguageRow(
    language: LanguageItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) AccentRed else Color.Transparent
    val bgColor = if (isSelected) AccentRed.copy(alpha = 0.04f) else Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flag emoji in a rounded container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = language.flag,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Name + subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = language.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TitleDark
                )
                if (language.subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = language.subtitle,
                        fontSize = 13.sp,
                        color = if (isSelected) AccentRed else Color.Gray
                    )
                }
            }

            // Trailing: checkmark if selected, chevron otherwise
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(AccentRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
