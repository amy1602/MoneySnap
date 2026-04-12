package com.moneysnap.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneysnap.presentation.theme.PrimaryPink
import androidx.compose.ui.res.stringResource
import com.moneysnap.R
import com.moneysnap.LocalImportFilePicker

val ProfileCardBg = Color(0xFFFFF0F5)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.provideFactory(LocalContext.current)
    ),
    onNavigateToCategories: () -> Unit,
    onNavigateToSelectAvatar: () -> Unit,
    onNavigateToAccountSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showChangeNameSheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val importContext = LocalContext.current
    val launchImportPicker = LocalImportFilePicker.current

    // Import result dialog
    uiState.importResultMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearImportResult() },
            title = {
                Text(
                    text = stringResource(R.string.profile_import_result_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(text = message, fontSize = 16.sp)
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearImportResult() }) {
                    Text(stringResource(R.string.common_ok), color = PrimaryPink, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

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
                        viewModel.logout()
                        onLogout()
                    }
                ) {
                    Text(
                        stringResource(R.string.profile_sign_out),
                        color = PrimaryPink,
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

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.profile_clear_history),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.profile_clear_history_message),
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearHistoryDialog = false
                        viewModel.clearAllHistory()
                    }
                ) {
                    Text(
                        stringResource(R.string.common_delete),
                        color = PrimaryPink,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearHistoryDialog = false }
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

    if (showChangeNameSheet) {
        ModalBottomSheet(
            onDismissRequest = { showChangeNameSheet = false },
            sheetState = sheetState,
            dragHandle = null,
            containerColor = Color.Transparent,
            scrimColor = Color.Black.copy(alpha = 0.32f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            ChangeNameSheet(
                currentName = uiState.name,
                currentAvatarId = uiState.avatarId,
                onSaveName = { newName ->
                    viewModel.updateName(newName)
                    showChangeNameSheet = false
                },
                onDismiss = { showChangeNameSheet = false }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // App Bar
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.profile_title),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back if necessary, but this is a root tab */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.biometric_cancel))
                    }
                },
                actions = {
                    // Spacer for balancing title center
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.Transparent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
    
            Spacer(modifier = Modifier.height(16.dp))
    
            // Avatar & Info
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    val currentAvatar = com.moneysnap.domain.model.AvatarConstants.getAvatarById(uiState.avatarId)
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE5E5E5)) // Neutral grey bg matching typical cat avatars
                            .clickable { onNavigateToSelectAvatar() },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = currentAvatar.drawableRes),
                            contentDescription = stringResource(R.string.profile_title),
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PrimaryPink)
                            .clickable { showChangeNameSheet = true }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.profile_edit_profile),
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
    
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.clickable { showChangeNameSheet = true }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.email,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
    
            Spacer(modifier = Modifier.height(32.dp))
    
            // Account Actions
            Text(
                text = stringResource(R.string.profile_account_actions),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
    
            val context = LocalContext.current
    
            ActionItem(
                icon = Icons.Default.WorkOutline,
                title = stringResource(R.string.profile_export_excel),
                subtitle = stringResource(R.string.profile_export_subtitle),
                onClick = {
                    viewModel.exportToExcel(context) { file ->
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, context.getString(R.string.profile_share_excel)))
                    }
                }
            )
            ActionItem(
                icon = Icons.Default.FileUpload,
                title = stringResource(R.string.profile_import_excel),
                subtitle = stringResource(R.string.profile_import_subtitle),
                onClick = {
                    launchImportPicker { uri ->
                        uri?.let { viewModel.importFromExcel(importContext, it) }
                    }
                }
            )
            ActionItem(
                icon = Icons.Default.GridView,
                title = stringResource(R.string.categories_title),
                subtitle = stringResource(R.string.profile_manage_categories),
                onClick = onNavigateToCategories
            )
            ActionItem(
                icon = Icons.Default.Settings,
                title = stringResource(R.string.profile_account_settings),
                subtitle = stringResource(R.string.profile_settings_subtitle),
                onClick = onNavigateToAccountSettings
            )
            ActionItem(
                icon = Icons.Default.DeleteSweep,
                title = stringResource(R.string.profile_clear_history),
                subtitle = stringResource(R.string.profile_clear_history_subtitle),
                isDestructive = true,
                onClick = { showClearHistoryDialog = true }
            )
            ActionItem(
                icon = Icons.Default.Logout,
                title = stringResource(R.string.profile_sign_out),
                subtitle = stringResource(R.string.profile_logout_subtitle),
                isDestructive = true,
                onClick = { showLogoutDialog = true }
            )
    
            Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom nav
        }
 
        if (uiState.isExporting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = false) {}, // Intercept clicks while loading
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = PrimaryPink)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.profile_exporting),
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        if (uiState.isImporting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = PrimaryPink)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.profile_importing),
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val tintColor = if (isDestructive) PrimaryPink else MaterialTheme.colorScheme.onBackground

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isDestructive) PrimaryPink.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tintColor)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = tintColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, color = Color.Gray, fontSize = 13.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}
