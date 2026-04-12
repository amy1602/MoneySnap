package com.moneysnap.presentation.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneysnap.presentation.theme.PrimaryPink
import com.moneysnap.presentation.transaction.AddTransactionScreen
import com.moneysnap.presentation.transaction.HistoryScreen
import com.moneysnap.presentation.profile.ProfileScreen

enum class HomeTab { Home, History, Reports, Profile }

val SuccessGreen = Color(0xFF4CAF50)
val ChartGrey = Color(0xFFEEEEEE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.provideFactory(LocalContext.current)
    ),
    onNavigateToCategories: () -> Unit = {},
    onNavigateToTransaction: (String) -> Unit = {},
    onNavigateToSelectAvatar: () -> Unit = {},
    onNavigateToAccountSettings: () -> Unit = {},
    onLogout: () -> Unit = {},
    profileViewModel: com.moneysnap.presentation.profile.ProfileViewModel = viewModel(
        factory = com.moneysnap.presentation.profile.ProfileViewModel.provideFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    var currentTab by rememberSaveable { mutableStateOf(HomeTab.Home) }
    var transactionIdToEdit by remember { mutableStateOf<String?>(null) }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { 
                showBottomSheet = false
                transactionIdToEdit = null
            },
            sheetState = sheetState,
            dragHandle = null,
            containerColor = Color.Transparent,
            scrimColor = Color.Black.copy(alpha = 0.32f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            AddTransactionScreen(
                transactionId = transactionIdToEdit,
                onCloseClick = { 
                    showBottomSheet = false
                    transactionIdToEdit = null
                },
                onSaveSuccess = { 
                    showBottomSheet = false
                    transactionIdToEdit = null
                    viewModel.refresh() // Refresh home data after save
                }
            )
        }
    }

    Scaffold(
        bottomBar = {
            HomeBottomNavigation(
                selectedTab = currentTab,
                onTabSelected = { currentTab = it },
                onAddClick = { showBottomSheet = true }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (currentTab) {
                HomeTab.Home -> HomeContent(
                    uiState = uiState,
                    onViewAllClick = { currentTab = HomeTab.History },
                    onTransactionClick = onNavigateToTransaction
                )
                HomeTab.History -> HistoryScreen(
                    onEditTransactionClick = { txId ->
                        transactionIdToEdit = txId
                        showBottomSheet = true
                    },
                    onTransactionClick = onNavigateToTransaction
                )
                HomeTab.Reports -> com.moneysnap.presentation.report.ReportScreen(
                    onNavigateToTransaction = onNavigateToTransaction
                )
                HomeTab.Profile -> com.moneysnap.presentation.profile.ProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateToCategories = onNavigateToCategories,
                    onNavigateToSelectAvatar = onNavigateToSelectAvatar,
                    onNavigateToAccountSettings = onNavigateToAccountSettings,
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
fun HomeContent(uiState: HomeUiState, onViewAllClick: () -> Unit, onTransactionClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Total Balance
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Balance",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.totalBalance,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Expenses and Income Column (Vertical Order)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BalanceCard(
                modifier = Modifier.fillMaxWidth(),
                title = "EXPENSES",
                amount = uiState.expenses,
                trend = uiState.expensesPercentage,
                icon = Icons.Default.ArrowUpward,
                color = PrimaryPink
            )
            BalanceCard(
                modifier = Modifier.fillMaxWidth(),
                title = "INCOME",
                amount = uiState.income,
                trend = uiState.incomePercentage,
                icon = Icons.Default.ArrowDownward,
                color = SuccessGreen
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Weekly Spending Chart
        WeeklySpendingSection(uiState.weeklySpending)

        Spacer(modifier = Modifier.height(32.dp))

        // Recent Transactions
        RecentTransactionsSection(uiState.recentTransactions, onViewAllClick, onTransactionClick)
        
        Spacer(modifier = Modifier.height(80.dp)) // Extra space for FAB and Bottom Nav
    }
}

@Composable
fun BalanceCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: String,
    trend: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(amount, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = trend,
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun WeeklySpendingSection(days: List<DailySpending>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Weekly Spending", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Last 7 Days", fontSize = 14.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        // Custom simple bar chart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            val fallbackDays = listOf(
                DailySpending("MON", 0.4f, false),
                DailySpending("TUE", 0.6f, false),
                DailySpending("WED", 0.3f, false),
                DailySpending("THU", 0.9f, true),
                DailySpending("FRI", 0.5f, false),
                DailySpending("SAT", 0.2f, false),
                DailySpending("SUN", 0.3f, false)
            )
            val chartData = days.ifEmpty { fallbackDays }
            
            chartData.forEach { day ->
                val isHighlighted = day.isToday
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    Canvas(modifier = Modifier
                        .width(32.dp)
                        .height(80.dp)) {
                        val barHeight = size.height * day.ratio
                        drawRoundRect(
                            color = if (isHighlighted) PrimaryPink else ChartGrey,
                            topLeft = Offset(0f, size.height - barHeight),
                            size = Size(size.width, barHeight),
                            cornerRadius = CornerRadius(16f, 16f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        day.dayOfWeek,
                        fontSize = 12.sp,
                        fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                        color = if (isHighlighted) PrimaryPink else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun RecentTransactionsSection(transactions: List<com.moneysnap.presentation.report.TransactionWithCategory>, onViewAllClick: () -> Unit, onTransactionClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Transactions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "View All", 
                fontSize = 14.sp, 
                color = PrimaryPink, 
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { onViewAllClick() }
                    .padding(4.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        if (transactions.isEmpty()) {
            Text("No recent transactions", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
        } else {
            val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US).apply {
                maximumFractionDigits = 0
            }
            transactions.forEach { txWithCat ->
                val tx = txWithCat.transaction
                val isExpense = tx.type == com.moneysnap.domain.model.TransactionType.EXPENSE
                val formattedAmount = formatter.format(tx.amount)
                val displayAmount = if (isExpense) "-$formattedAmount" else "+$formattedAmount"
                
                // Formatter for date (simplified)
                val dateStr = android.text.format.DateFormat.format("MMM dd, yyyy", java.util.Date(tx.date)).toString()
                
                val iconName = txWithCat.category?.icon ?: "Error"
                val vectorIcon = com.moneysnap.domain.model.CategoryConstants.getIconByName(iconName)
                val catColor = txWithCat.category?.color ?: if (isExpense) "#2196F3" else "#4CAF50"
                val tint = try {
                    Color(android.graphics.Color.parseColor(catColor))
                } catch (e: Exception) {
                    if (isExpense) Color(0xFF2196F3) else SuccessGreen
                }
                
                TransactionItem(
                    title = tx.note.ifBlank { txWithCat.category?.name ?: "Transaction" },
                    subtitle = dateStr,
                    amount = displayAmount,
                    icon = vectorIcon,
                    iconTint = tint,
                    isNegative = isExpense,
                    onClick = { onTransactionClick(tx.id) }
                )
            }
        }
    }
}

@Composable
fun TransactionItem(
    title: String,
    subtitle: String,
    amount: String,
    icon: ImageVector,
    iconTint: Color,
    isNegative: Boolean,
    onClick: () -> Unit = {},
    titleFontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    subtitleFontSize: androidx.compose.ui.unit.TextUnit = 13.sp,
    amountFontSize: androidx.compose.ui.unit.TextUnit = 16.sp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = titleFontSize)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = Color.Gray, fontSize = subtitleFontSize)
        }
        Text(
            text = amount,
            fontWeight = FontWeight.Bold,
            fontSize = amountFontSize,
            color = if (isNegative) MaterialTheme.colorScheme.onBackground else SuccessGreen
        )
    }
}

@Composable
fun HomeBottomNavigation(
    selectedTab: HomeTab = HomeTab.Home,
    onTabSelected: (HomeTab) -> Unit = {},
    onAddClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // The actual navigation bar surface
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter),
            color = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color.White)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                BottomNavItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    selected = selectedTab == HomeTab.Home,
                    onClick = { onTabSelected(HomeTab.Home) },
                    modifier = Modifier.weight(1f)
                )
                // History
                BottomNavItem(
                    icon = Icons.Default.History,
                    label = "History",
                    selected = selectedTab == HomeTab.History,
                    onClick = { onTabSelected(HomeTab.History) },
                    modifier = Modifier.weight(1f)
                )
                // Center spacer for the raised FAB
                Spacer(modifier = Modifier.weight(1f))
                // Reports
                BottomNavItem(
                    icon = Icons.Default.BarChart,
                    label = "Reports",
                    selected = selectedTab == HomeTab.Reports,
                    onClick = { onTabSelected(HomeTab.Reports) },
                    modifier = Modifier.weight(1f)
                )
                // Profile
                BottomNavItem(
                    icon = Icons.Default.Person,
                    label = "Profile",
                    selected = selectedTab == HomeTab.Profile,
                    onClick = { onTabSelected(HomeTab.Profile) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Raised center "+" button protruding above the bar
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
                // Layer 1: Pink shadow using native colored shadow
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    ambientColor = PrimaryPink,
                    spotColor = PrimaryPink
                )
                // Layer 2: Outer white border ring
                .background(Color.White, CircleShape)
                .size(62.dp),
            contentAlignment = Alignment.Center
        ) {
            // Layer 3: Pink button inside the border
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = PrimaryPink,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(52.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction", modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (selected) PrimaryPink else Color.Gray
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = color, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}
