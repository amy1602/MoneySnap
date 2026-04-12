package com.moneysnap.presentation.transaction

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneysnap.R
import com.moneysnap.domain.model.Category
import com.moneysnap.domain.model.CategoryConstants
import com.moneysnap.domain.model.TransactionType
import com.moneysnap.presentation.theme.PrimaryPink
import java.text.SimpleDateFormat
import java.util.*

fun formatAmount(amount: String): String {
    if (amount.isEmpty() || amount == "0") return amount
    return try {
        val number = amount.toLong()
        val formatted = StringBuilder()
        val str = number.toString()
        val startIndex = str.length % 3
        str.forEachIndexed { index, c ->
            if (index != 0 && index % 3 == startIndex) formatted.append('.')
            formatted.append(c)
        }
        formatted.toString()
    } catch (e: Exception) {
        amount
    }
}

@Composable
fun BlinkingCursor() {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 600 // Faster for "twinkling" effect
                1f at 0
                1f at 299
                0f at 300
                0f at 599
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "cursor_alpha"
    )

    Box(
        modifier = Modifier
            .width(2.dp)
            .height(40.dp)
            .padding(start = 4.dp)
            .background(PrimaryPink.copy(alpha = alpha))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionId: String? = null,
    onCloseClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: AddTransactionViewModel = viewModel(
        factory = AddTransactionViewModel.provideFactory(LocalContext.current)
    )
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    var showKeypad by remember { mutableStateOf(true) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(transactionId) {
        transactionId?.let { viewModel.loadTransaction(it) }
    }

    LaunchedEffect(viewModel.toastMessage) {
        viewModel.toastMessage.collect { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            onSaveSuccess()
            viewModel.resetForm()
        }
    }

    // Main screen back handler for keypad
    BackHandler(enabled = showKeypad && !showCategoryPicker && !showDatePicker) {
        showKeypad = false
    }

    Box(modifier = Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            // Handlebar
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(40.dp, 4.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f), CircleShape)
                    .align(Alignment.CenterHorizontally)
            )

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (transactionId == null) stringResource(R.string.add_transaction_title) else stringResource(R.string.edit_transaction_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                IconButton(onClick = onCloseClick) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.biometric_cancel), tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Transaction Type Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TransactionTypeButton(
                    text = stringResource(R.string.common_expense),
                    isSelected = state.type == TransactionType.EXPENSE,
                    onClick = { viewModel.onTypeChange(TransactionType.EXPENSE) },
                    modifier = Modifier.weight(1f)
                )
                TransactionTypeButton(
                    text = stringResource(R.string.common_income),
                    isSelected = state.type == TransactionType.INCOME,
                    onClick = { viewModel.onTypeChange(TransactionType.INCOME) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            val keyboardController = LocalSoftwareKeyboardController.current
            val focusManager = LocalFocusManager.current
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        showKeypad = true
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.add_transaction_enter_amount),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (showKeypad) PrimaryPink else Color.Gray,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val formattedAmount = "$ ${formatAmount(state.amount)}"
                var amountFontSize by remember(formattedAmount) { mutableStateOf(48.sp) }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = formattedAmount,
                        fontSize = amountFontSize,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = PrimaryPink,
                        maxLines = 1,
                        softWrap = false,
                        onTextLayout = { result ->
                            if (result.hasVisualOverflow && amountFontSize > 20.sp) {
                                amountFontSize = (amountFontSize.value * 0.9f).sp
                            }
                        }
                    )
                    if (showKeypad) {
                        BlinkingCursor()
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Input Fields
            ModalInputField(
                icon = Icons.Default.Category,
                placeholder = if (state.categoryName == "Select category") stringResource(R.string.add_transaction_select_category) else state.categoryName,
                onClick = { 
                    showKeypad = false
                    showCategoryPicker = true 
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            ModalInputField(
                icon = Icons.Default.CalendarToday,
                placeholder = dateFormatter.format(Date(state.date)),
                onClick = { 
                    showKeypad = false
                    showDatePicker = true 
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.note,
                onValueChange = { viewModel.onNoteChange(it) },
                placeholder = { Text(stringResource(R.string.add_transaction_note_hint), color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (it.isFocused) showKeypad = false },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF8F8F8),
                    unfocusedContainerColor = Color(0xFFF8F8F8)
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Save Button
            Button(
                onClick = { viewModel.saveTransaction() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                enabled = !state.isSaving,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPink,
                    contentColor = Color.White,
                    disabledContainerColor = PrimaryPink.copy(alpha = 0.5f)
                )
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(if (transactionId == null) stringResource(R.string.add_transaction_save) else stringResource(R.string.add_transaction_update), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cancel Button
            TextButton(
                onClick = onCloseClick,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(stringResource(R.string.common_cancel), color = Color.Gray, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(if (showKeypad) 340.dp else 40.dp))
        }

        // Custom Keyboard Overlay
        AnimatedVisibility(
            visible = showKeypad,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .navigationBarsPadding()
                ) {
                    NumericKeypad(
                        onNumberClick = { viewModel.onNumberClick(it) },
                        onBackspace = { viewModel.onBackspace() },
                        onClear = { viewModel.onAmountChange("0") },
                        onDone = { showKeypad = false }
                    )
                }
            }
        }

        if (showCategoryPicker) {
            CategoryPicker(
                categories = categories,
                onCategorySelected = { category ->
                    viewModel.onCategorySelect(category.id, category.name)
                    showCategoryPicker = false
                },
                onDismiss = { showCategoryPicker = false }
            )
        }

        if (showDatePicker) {
            BackHandler { showDatePicker = false }
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.date)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { viewModel.onDateChange(it) }
                        showDatePicker = false
                    }) {
                        Text(stringResource(R.string.common_ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(stringResource(R.string.common_cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

val CalcGreen = Color(0xFF2E7D32)

@Composable
fun NumericKeypad(
    onNumberClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onDone: () -> Unit
) {
    val spacing = 8.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Row 1: C ÷ × ⌫
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            CalcButton(label = "C", isGreenText = true, onClick = onClear, modifier = Modifier.weight(1f))
            CalcButton(label = "÷", isGreenText = true, onClick = { /* future */ }, modifier = Modifier.weight(1f))
            CalcButton(label = "×", isGreenText = true, onClick = { /* future */ }, modifier = Modifier.weight(1f))
            CalcButton(label = "BACKSPACE", isGreenText = true, onClick = onBackspace, modifier = Modifier.weight(1f))
        }

        // Rows 2-5 with the Done button spanning rows 4-5
        // Row 2: 7 8 9 −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            CalcButton(label = "7", onClick = { onNumberClick("7") }, modifier = Modifier.weight(1f))
            CalcButton(label = "8", onClick = { onNumberClick("8") }, modifier = Modifier.weight(1f))
            CalcButton(label = "9", onClick = { onNumberClick("9") }, modifier = Modifier.weight(1f))
            CalcButton(label = "−", isGreenText = true, onClick = { /* future */ }, modifier = Modifier.weight(1f))
        }

        // Row 3: 4 5 6 +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            CalcButton(label = "4", onClick = { onNumberClick("4") }, modifier = Modifier.weight(1f))
            CalcButton(label = "5", onClick = { onNumberClick("5") }, modifier = Modifier.weight(1f))
            CalcButton(label = "6", onClick = { onNumberClick("6") }, modifier = Modifier.weight(1f))
            CalcButton(label = "+", isGreenText = true, onClick = { /* future */ }, modifier = Modifier.weight(1f))
        }

        // Rows 4-5: [1 2 3] [Done]  /  [0 000 .] [Done continued]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            // Left side: two rows of 3 keys
            Column(
                modifier = Modifier.weight(3f),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                // Row 4: 1 2 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    CalcButton(label = "1", onClick = { onNumberClick("1") }, modifier = Modifier.weight(1f))
                    CalcButton(label = "2", onClick = { onNumberClick("2") }, modifier = Modifier.weight(1f))
                    CalcButton(label = "3", onClick = { onNumberClick("3") }, modifier = Modifier.weight(1f))
                }
                // Row 5: 0 000 .
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    CalcButton(label = "0", onClick = { onNumberClick("0") }, modifier = Modifier.weight(1f))
                    CalcButton(label = "000", onClick = { onNumberClick("000") }, modifier = Modifier.weight(1f))
                    CalcButton(label = ".", onClick = { /* integer only */ }, modifier = Modifier.weight(1f))
                }
            }

            // Right side: Done button spanning 2 rows
            Surface(
                onClick = onDone,
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp * 2 + spacing),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF00BCD4),
                contentColor = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Done",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CalcButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isGreenText: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF0F0F0),
        contentColor = if (isGreenText) CalcGreen else Color.Black
    ) {
        Box(contentAlignment = Alignment.Center) {
            when (label) {
                "BACKSPACE" -> Icon(
                    Icons.Default.Backspace,
                    contentDescription = "Backspace",
                    tint = CalcGreen,
                    modifier = Modifier.size(24.dp)
                )
                else -> Text(
                    text = label,
                    fontSize = if (label == "000") 18.sp else 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPicker(
    categories: List<Category>,
    onCategorySelected: (Category) -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxSize().statusBarsPadding()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.add_transaction_select_category),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.biometric_cancel), tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (categories.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.add_transaction_no_categories), color = Color.Gray)
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories.size) { index ->
                        val category = categories[index]
                        val parsedColor = try {
                            Color(android.graphics.Color.parseColor(if (category.color.startsWith("#")) category.color else "#${category.color}"))
                        } catch (e: Exception) {
                            PrimaryPink
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF8F8F8))
                                .clickable { onCategorySelected(category) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(parsedColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = CategoryConstants.getIconByName(category.icon),
                                    contentDescription = null,
                                    tint = parsedColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = category.name,
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionTypeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .background(
                color = if (isSelected) Color.White else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 16.sp
        )
    }
}

@Composable
fun ModalInputField(
    icon: ImageVector,
    placeholder: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8F8F8),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = placeholder,
                color = if (placeholder == stringResource(R.string.add_transaction_select_category)) Color.Gray else Color.Black,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            if (icon == Icons.Default.Category) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
            }
        }
    }
}
