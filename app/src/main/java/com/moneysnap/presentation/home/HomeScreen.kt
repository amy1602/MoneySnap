package com.moneysnap.presentation.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneysnap.presentation.theme.PrimaryPink

val SuccessGreen = Color(0xFF4CAF50)
val ChartGrey = Color(0xFFEEEEEE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Money Manager",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Add transaction */ },
                containerColor = PrimaryPink,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            HomeBottomNavigation()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    text = "$4,250.00",
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Income and Expenses Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BalanceCard(
                    modifier = Modifier.weight(1f),
                    title = "INCOME",
                    amount = "$5,200.00",
                    trend = "+12.5%",
                    icon = Icons.Default.ArrowDownward,
                    color = SuccessGreen
                )
                BalanceCard(
                    modifier = Modifier.weight(1f),
                    title = "EXPENSES",
                    amount = "$950.00",
                    trend = "-5.2%",
                    icon = Icons.Default.ArrowUpward,
                    color = PrimaryPink
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Weekly Spending Chart
            WeeklySpendingSection()

            Spacer(modifier = Modifier.height(32.dp))

            // Recent Transactions
            RecentTransactionsSection()
            
            Spacer(modifier = Modifier.height(80.dp)) // Extra space for FAB and Bottom Nav
        }
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
fun WeeklySpendingSection() {
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
            val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
            val heights = listOf(0.4f, 0.6f, 0.3f, 0.9f, 0.5f, 0.2f, 0.3f)
            
            days.forEachIndexed { index, day ->
                val isHighlighted = day == "THU"
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    Canvas(modifier = Modifier
                        .width(32.dp)
                        .height(80.dp)) {
                        val barHeight = size.height * heights[index]
                        drawRoundRect(
                            color = if (isHighlighted) PrimaryPink else ChartGrey,
                            topLeft = Offset(0f, size.height - barHeight),
                            size = Size(size.width, barHeight),
                            cornerRadius = CornerRadius(16f, 16f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        day,
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
fun RecentTransactionsSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Transactions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("View All", fontSize = 14.sp, color = PrimaryPink, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        TransactionItem(
            title = "Starbucks Coffee",
            subtitle = "Today, 10:45 AM",
            amount = "-$4.50",
            icon = Icons.Default.LocalCafe,
            iconTint = PrimaryPink,
            isNegative = true
        )
        TransactionItem(
            title = "Uber Ride",
            subtitle = "Yesterday, 8:20 PM",
            amount = "-$15.00",
            icon = Icons.Default.DirectionsCar,
            iconTint = Color(0xFF2196F3), // Blue
            isNegative = true
        )
        TransactionItem(
            title = "Salary Deposit",
            subtitle = "2 days ago",
            amount = "+$2,800.00",
            icon = Icons.Default.AttachMoney,
            iconTint = SuccessGreen,
            isNegative = false
        )
        TransactionItem(
            title = "Apple Store",
            subtitle = "2 days ago",
            amount = "-$129.00",
            icon = Icons.Default.ShoppingBag,
            iconTint = Color(0xFF9C27B0), // Purple
            isNegative = true
        )
    }
}

@Composable
fun TransactionItem(
    title: String,
    subtitle: String,
    amount: String,
    icon: ImageVector,
    iconTint: Color,
    isNegative: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = Color.Gray, fontSize = 13.sp)
        }
        Text(
            text = amount,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = if (isNegative) MaterialTheme.colorScheme.onBackground else SuccessGreen
        )
    }
}

@Composable
fun HomeBottomNavigation() {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryPink,
                selectedTextColor = PrimaryPink,
                indicatorColor = PrimaryPink.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.History, contentDescription = "History") },
            label = { Text("History", fontSize = 10.sp) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Reports") },
            label = { Text("Reports", fontSize = 10.sp) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile", fontSize = 10.sp) }
        )
    }
}
