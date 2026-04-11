package com.moneysnap.presentation.report

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneysnap.domain.model.CategoryConstants
import com.moneysnap.presentation.home.TransactionItem
import com.moneysnap.presentation.theme.PrimaryPink
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

val ScreenBg = Color(0xFFF8FAFC)
val CardBg = Color(0xFFFFFFFF)
val TextDark = Color(0xFF1A1C1E)
val TextGray = Color(0xFF94A3B8)
val IncomeGreen = Color(0xFF4CAF50)
val ExpenseTan = Color(0xFFF1E4D3) // Light tan/beige color for expense bars

@Composable
fun ReportScreen(
    viewModel: ReportViewModel = viewModel(
        factory = ReportViewModel.provideFactory(LocalContext.current)
    ),
    onNavigateToTransaction: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Report Type Toggle (Month / Year)
        ReportTypeToggle(
            selectedType = uiState.reportType,
            onTypeSelected = { viewModel.setReportType(it) }
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        // Header - Date Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.previousMonth() }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month", tint = TextDark)
            }
            Text(
                text = uiState.currentMonthYearString,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextDark,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            IconButton(onClick = { viewModel.nextMonth() }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = TextDark)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Net Balance Card
        NetBalanceCard(uiState)

        Spacer(modifier = Modifier.height(24.dp))

        // Flow Analysis Chart
        FlowAnalysisSection(uiState)

        Spacer(modifier = Modifier.height(24.dp))

        // Category Spending Donut Chart
        CategorySpendingSection(uiState)

        Spacer(modifier = Modifier.height(24.dp))

        // Significant Outflow List
        SignificantOutflowSection(uiState, onNavigateToTransaction)

        Spacer(modifier = Modifier.height(100.dp)) // Padding for bottom nav
    }
}

@Composable
fun NetBalanceCard(uiState: ReportUiState) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "NET BALANCE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currencyFormatter.format(uiState.netBalance),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryPink
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Income", fontSize = 12.sp, color = TextGray)
                    Text(
                        text = "+${currencyFormatter.format(uiState.totalIncome)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IncomeGreen
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Expense", fontSize = 12.sp, color = TextGray)
                    Text(
                        text = "-${currencyFormatter.format(uiState.totalExpense)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryPink
                    )
                }
            }
        }
    }
}

@Composable
fun FlowAnalysisSection(uiState: ReportUiState) {
    Text(
        text = "FLOW ANALYSIS",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = TextGray,
        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Chart Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val chartHeight = size.height
                val barWidth = 16.dp.toPx()
                val gapBetweenBars = 4.dp.toPx()
                val topPadding = 16.dp.toPx()
                val maxAmount = if (uiState.maxWeeklyAmount <= 0.0) 1.0 else uiState.maxWeeklyAmount

                val segmentWidth = size.width / uiState.weeklyFlows.size

                uiState.weeklyFlows.forEachIndexed { index, flow ->
                    val centerX = segmentWidth * index + segmentWidth / 2
                    val incomeHeight = ((flow.income / maxAmount) * (chartHeight - topPadding)).toFloat()
                    val expenseHeight = ((flow.expense / maxAmount) * (chartHeight - topPadding)).toFloat()

                    // Income Bar
                    val incomeX = centerX - (barWidth + gapBetweenBars) / 2
                    drawRoundRect(
                        color = PrimaryPink,
                        topLeft = Offset(incomeX, chartHeight - incomeHeight),
                        size = Size(barWidth, incomeHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )

                    // Expense Bar
                    val expenseX = centerX + gapBetweenBars / 2
                    drawRoundRect(
                        color = ExpenseTan,
                        topLeft = Offset(expenseX, chartHeight - expenseHeight),
                        size = Size(barWidth, expenseHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // X-Axis Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                uiState.weeklyFlows.forEach {
                    Text(
                        text = it.weekName,
                        fontSize = 12.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PrimaryPink))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Income", fontSize = 12.sp, color = TextDark)
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ExpenseTan))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Expense", fontSize = 12.sp, color = TextDark)
            }
        }
    }
}

@Composable
fun CategorySpendingSection(uiState: ReportUiState) {
    if (uiState.categorySpendings.isEmpty()) return

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "CATEGORY SPENDING",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextGray,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
    
    Spacer(modifier = Modifier.height(12.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Donut Chart
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .size(140.dp)
                    .aspectRatio(1f)
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var startAngle = -90f
                    val strokeWidth = 24.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val offset = Offset(
                        (size.width - diameter) / 2f,
                        (size.height - diameter) / 2f
                    )
                    
                    uiState.categorySpendings.forEach { spending ->
                        val sweepAngle = spending.percentage * 360f
                        val color = try {
                            Color(android.graphics.Color.parseColor(spending.category?.color ?: "#000000"))
                        } catch (e: Exception) {
                            Color.Gray
                        }
                        
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = offset,
                            size = Size(diameter, diameter),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += sweepAngle
                    }
                }
                
                // Center Text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOTAL", fontSize = 10.sp, color = TextGray)
                    Text(
                        text = currencyFormatter.format(uiState.totalExpense),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
            }

            // Legend
            Column(
                modifier = Modifier.weight(1f).padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.categorySpendings.forEach { spending ->
                    val color = try {
                        Color(android.graphics.Color.parseColor(spending.category?.color ?: "#000000"))
                    } catch (e: Exception) {
                        Color.Gray
                    }
                    val percentageStr = "${(spending.percentage * 100).toInt()}%"
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = spending.category?.name ?: "Unknown",
                                fontSize = 12.sp,
                                color = TextDark,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = percentageStr,
                                fontSize = 11.sp,
                                color = TextGray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SignificantOutflowSection(uiState: ReportUiState, onNavigateToTransaction: (String) -> Unit) {
    if (uiState.significantOutflows.isEmpty()) return

    Text(
        text = "SIGNIFICANT OUTFLOW",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = TextGray,
        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            val formatter = NumberFormat.getCurrencyInstance(Locale.US)
            
            uiState.significantOutflows.forEach { item ->
                val iconName = item.category?.icon ?: "Error"
                val vectorIcon = CategoryConstants.getIconByName(iconName)
                val catColor = item.category?.color ?: "#FF0000"
                val tint = try {
                    Color(android.graphics.Color.parseColor(catColor))
                } catch (e: Exception) {
                    PrimaryPink
                }

                val dateStr = android.text.format.DateFormat.format("MMM dd, yyyy", java.util.Date(item.transaction.date)).toString()
                val subtitle = "$dateStr • ${item.category?.name ?: "Unknown"}"

                TransactionItem(
                    title = item.transaction.note.ifBlank { item.category?.name ?: "Expense" },
                    subtitle = subtitle,
                    amount = "-${formatter.format(item.transaction.amount)}",
                    icon = vectorIcon,
                    iconTint = tint,
                    isNegative = true,
                    onClick = { onNavigateToTransaction(item.transaction.id) }
                )
            }
        }
    }
}

@Composable
fun ReportTypeToggle(
    selectedType: ReportType,
    onTypeSelected: (ReportType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Month button
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(if (selectedType == ReportType.MONTHLY) Color.White else Color.Transparent)
                .clickable { onTypeSelected(ReportType.MONTHLY) }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Month",
                color = if (selectedType == ReportType.MONTHLY) TextDark else TextGray,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
        
        // Year button
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(if (selectedType == ReportType.YEARLY) Color.White else Color.Transparent)
                .clickable { onTypeSelected(ReportType.YEARLY) }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Year",
                color = if (selectedType == ReportType.YEARLY) TextDark else TextGray,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}
