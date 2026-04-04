package com.moneysnap.presentation.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneysnap.domain.model.CategoryConstants
import com.moneysnap.domain.model.TransactionType
import com.moneysnap.presentation.theme.PrimaryPink
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModel.provideFactory(LocalContext.current)
    ),
    onEditTransactionClick: (String) -> Unit,
    onTransactionClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<String?>(null) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    val dateRangePickerState = rememberDateRangePickerState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Custom Header
        Text(
            text = "Transaction history",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Time & Category Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Time Filter
            Box(modifier = Modifier.weight(1f)) {
                val dateFormatter = SimpleDateFormat("MMM dd", Locale.US)
                val timeLabel = if (uiState.startDate != null && uiState.endDate != null) {
                    "${dateFormatter.format(Date(uiState.startDate!!))} - ${dateFormatter.format(Date(uiState.endDate!!))}"
                } else {
                    "All Time"
                }

                FilterChip(
                    text = timeLabel,
                    isSelected = uiState.startDate != null,
                    onClick = { showDateRangePicker = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Category Filter
            Box(modifier = Modifier.weight(1f)) {
                val catName = uiState.categories.find { it.id == uiState.categoryFilter }?.name ?: "All Categories"
                FilterChip(
                    text = catName,
                    isSelected = uiState.categoryFilter != null,
                    onClick = { categoryMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    DropdownMenuItem(text = { Text("All Categories") }, onClick = { viewModel.setCategoryFilter(null); categoryMenuExpanded = false })
                    uiState.categories.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat.name) }, onClick = { viewModel.setCategoryFilter(cat.id); categoryMenuExpanded = false })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showDateRangePicker) {
            DatePickerDialog(
                onDismissRequest = { showDateRangePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.setDateRange(
                                dateRangePickerState.selectedStartDateMillis,
                                dateRangePickerState.selectedEndDateMillis
                            )
                            showDateRangePicker = false
                        }
                    ) {
                        Text("Apply")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            viewModel.setDateRange(null, null)
                            showDateRangePicker = false
                        }
                    ) {
                        Text("Clear")
                    }
                }
            ) {
                DateRangePicker(
                    state = dateRangePickerState,
                    modifier = Modifier.weight(1f).padding(top = 16.dp)
                )
            }
        }

        if (uiState.groupedTransactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No transactions found", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp) // Space for bottom nav
            ) {
                uiState.groupedTransactions.forEach { (dateHeader, items) ->
                    item {
                        Text(
                            text = dateHeader,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    items(
                        items = items,
                        key = { it.transaction.id }
                    ) { item ->
                        SwipeableHistoryItem(
                            item = item,
                            onEdit = { onEditTransactionClick(item.transaction.id) },
                            onDelete = { 
                                transactionToDelete = item.transaction.id
                                showDeleteDialog = true
                            },
                            onClick = { onTransactionClick(item.transaction.id) }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog && transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                transactionToDelete = null
            },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction? This will impact your total balance. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        transactionToDelete?.let { viewModel.deleteTransaction(it) }
                        showDeleteDialog = false
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteDialog = false
                    transactionToDelete = null
                }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) PrimaryPink else Color(0xFFF5F5F5))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableHistoryItem(
    item: HistoryItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit = {}
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    false
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
                SwipeToDismissBoxValue.EndToStart -> Color(0xFFF44336)
                else -> Color.Transparent
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                else -> null
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = alignment
            ) {
                icon?.let { Icon(it, contentDescription = null, tint = Color.White) }
            }
        },
        content = {
            HistoryItemRow(item, onClick)
        }
    )
}

@Composable
fun HistoryItemRow(item: HistoryItem, onClick: () -> Unit = {}) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    val isExpense = item.transaction.type == TransactionType.EXPENSE
    
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(if (item.category?.color?.startsWith("#") == true) item.category.color else "#${item.category?.color ?: "FF2A65"}"))
    } catch (e: Exception) {
        PrimaryPink
    }

    val iconName = item.category?.icon ?: "Help"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(parsedColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = CategoryConstants.getIconByName(iconName),
                contentDescription = null,
                tint = parsedColor
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            val note = item.transaction.note.trim()
            val categoryName = item.category?.name ?: "Unknown"
            
            Text(
                text = categoryName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            if (note.isNotEmpty()) {
                Text(
                    text = note,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
        }

        val displayAmount = if (isExpense) "-${formatter.format(item.transaction.amount)}" else "+${formatter.format(item.transaction.amount)}"
        
        Text(
            text = displayAmount,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = if (isExpense) MaterialTheme.colorScheme.onBackground else Color(0xFF4CAF50)
        )
    }
}
