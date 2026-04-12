package com.moneysnap.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.moneysnap.R
import kotlinx.coroutines.launch

private val ScreenBg = Color(0xFFF8FAFC)
private val AccentRed = Color(0xFFE55061)
private val SectionLabelColor = Color(0xFF94A3B8)
private val TitleDark = Color(0xFF1A1C1E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    onBackClick: () -> Unit,
    onNavigateToBiometric: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onSignOut: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.profile_sign_out),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.profile_sign_out_message),
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onSignOut()
                    }
                ) {
                    Text(
                        stringResource(R.string.profile_sign_out),
                        color = AccentRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(
                        stringResource(R.string.common_cancel),
                        color = Color.Gray
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    var showUpdatePasswordSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val authRepository = remember { com.moneysnap.data.repository.AuthRepositoryImpl() }

    if (showUpdatePasswordSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showUpdatePasswordSheet = false },
            sheetState = sheetState,
            dragHandle = null,
            containerColor = Color.Transparent
        ) {
            UpdatePasswordSheet(
                onDismiss = { showUpdatePasswordSheet = false },
                onUpdatePassword = { oldPass, newPass ->
                    scope.launch {
                        val result = authRepository.updatePassword(oldPass, newPass)
                        if (result.isSuccess) {
                            android.widget.Toast.makeText(context, context.getString(R.string.settings_password_success), android.widget.Toast.LENGTH_SHORT).show()
                            showUpdatePasswordSheet = false
                        } else {
                            val exception = result.exceptionOrNull()
                            val errorMessage = if (exception is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                                context.getString(R.string.settings_password_wrong)
                            } else {
                                exception?.message ?: context.getString(R.string.settings_unknown_error)
                            }
                            android.widget.Toast.makeText(context, errorMessage, android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.profile_account_settings),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TitleDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.biometric_cancel),
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // --- SECURITY & ACCESS ---
            SectionLabel(stringResource(R.string.settings_security_access))
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                SettingsRow(
                    icon = Icons.Default.Lock,
                    iconBgColor = AccentRed.copy(alpha = 0.1f),
                    iconTint = AccentRed,
                    title = stringResource(R.string.settings_change_password),
                    titleColor = AccentRed,
                    subtitle = null,
                    showChevron = true,
                    onClick = { showUpdatePasswordSheet = true }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = Color(0xFFF0F0F0)
                )
                SettingsRow(
                    icon = Icons.Default.Fingerprint,
                    iconBgColor = Color(0xFFE8EDF2),
                    iconTint = Color(0xFF475569),
                    title = stringResource(R.string.settings_biometric),
                    titleColor = TitleDark,
                    subtitle = stringResource(R.string.settings_biometric_subtitle),
                    showChevron = true,
                    onClick = onNavigateToBiometric
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- PREFERENCES ---
            SectionLabel(stringResource(R.string.settings_preferences))
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                SettingsRow(
                    icon = Icons.Default.Language,
                    iconBgColor = Color(0xFFE8EDF2),
                    iconTint = Color(0xFF475569),
                    title = stringResource(R.string.settings_preferences),
                    titleColor = TitleDark,
                    subtitle = stringResource(R.string.settings_language_subtitle),
                    showChevron = true,
                    onClick = onNavigateToLanguage
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- ACCOUNT ACTIONS ---
            SectionLabel(stringResource(R.string.profile_account_actions))
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                SettingsRow(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    iconBgColor = AccentRed.copy(alpha = 0.1f),
                    iconTint = AccentRed,
                    title = stringResource(R.string.profile_sign_out),
                    titleColor = AccentRed,
                    subtitle = null,
                    showChevron = false,
                    onClick = { showLogoutDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = SectionLabelColor,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    titleColor: Color,
    subtitle: String?,
    showChevron: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon badge
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = titleColor
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }

        // Trailing chevron
        if (showChevron) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
