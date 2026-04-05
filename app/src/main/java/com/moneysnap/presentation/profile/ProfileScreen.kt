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

val ProfileCardBg = Color(0xFFFFF0F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.provideFactory(LocalContext.current)
    ),
    onNavigateToCategories: () -> Unit,
    onNavigateToSelectAvatar: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showChangeNameSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                    "Profile",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            navigationIcon = {
                IconButton(onClick = { /* Handle back if necessary, but this is a root tab */ }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                        contentDescription = "Avatar",
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
                        contentDescription = "Edit Profile",
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

        // Stats Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Total Savings",
                value = uiState.totalSavings,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Transactions",
                value = uiState.transactionCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Account Actions
        Text(
            text = "ACCOUNT ACTIONS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        ActionItem(
            icon = Icons.Default.WorkOutline,
            title = "Export to Excel",
            subtitle = "Download your financial reports",
            onClick = { /* TODO */ }
        )
        ActionItem(
            icon = Icons.Default.GridView,
            title = "Categories",
            subtitle = "Manage your spending categories",
            onClick = onNavigateToCategories
        )
        ActionItem(
            icon = Icons.Default.Settings,
            title = "Account Settings",
            subtitle = "Security, notifications, and privacy",
            onClick = { /* TODO */ }
        )
        ActionItem(
            icon = Icons.Default.Logout,
            title = "Logout",
            subtitle = "Sign out of your account",
            isDestructive = true,
            onClick = {
                viewModel.logout()
                onLogout()
            }
        )

        Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom nav
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ProfileCardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryPink)
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
