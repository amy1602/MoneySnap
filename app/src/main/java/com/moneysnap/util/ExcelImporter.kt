package com.moneysnap.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.moneysnap.domain.model.Transaction
import com.moneysnap.domain.model.Category
import com.moneysnap.domain.model.TransactionType
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

data class ImportResult(
    val importedCount: Int,
    val skippedCount: Int,
    val errors: List<String>
)

object ExcelImporter {

    fun importTransactionsFromExcel(
        context: Context,
        uri: Uri,
        existingCategories: List<Category>,
        userId: String
    ): Pair<List<Transaction>, ImportResult> {
        val importedTransactions = mutableListOf<Transaction>()
        val errors = mutableListOf<String>()
        var skippedCount = 0

        val categoryMap = existingCategories.associateBy { it.name.lowercase() }
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)

            // Skip header row (row 0)
            for (rowIndex in 1..sheet.lastRowNum) {
                val row = sheet.getRow(rowIndex) ?: continue

                try {
                    val cell = row.getCell(0)

                    if (cell == null) {
                        skippedCount++
                        continue
                    }

                    val date: Long = try {
                        when (cell.cellType) {
                            CellType.NUMERIC -> {
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    cell.dateCellValue.time
                                } else {
                                    throw IllegalArgumentException("Not a date")
                                }
                            }

                            CellType.STRING -> {
                                val dateStr = cell.stringCellValue.trim()
                                if (dateStr.isEmpty()) {
                                    skippedCount++
                                    continue
                                }
                                dateFormatter.parse(dateStr)?.time
                                    ?: throw IllegalArgumentException("Parse failed")
                            }

                            else -> throw IllegalArgumentException("Unsupported cell type")
                        }
                    } catch (e: Exception) {
                        errors.add("Row ${rowIndex + 1}: Invalid date format")
                        skippedCount++
                        continue
                    }

                    // Column 1: Category name
                    val categoryName = row.getCell(1)?.stringCellValue?.trim() ?: ""

                    // Column 2: Type ("Expense" or "Income")
                    val typeStr = row.getCell(2)?.stringCellValue?.trim() ?: "Expense"
                    val type = if (typeStr.equals("Income", ignoreCase = true))
                        TransactionType.INCOME else TransactionType.EXPENSE

                    // Column 3: Amount (numeric)
                    val amount = try {
                        row.getCell(3)?.numericCellValue ?: 0.0
                    } catch (e: Exception) {
                        try {
                            row.getCell(3)?.stringCellValue?.toDoubleOrNull() ?: 0.0
                        } catch (e2: Exception) {
                            0.0
                        }
                    }
                    if (amount <= 0) {
                        errors.add("Row ${rowIndex + 1}: Invalid amount")
                        skippedCount++
                        continue
                    }

                    // Column 4: Note
                    val note = try {
                        row.getCell(4)?.stringCellValue?.trim() ?: ""
                    } catch (e: Exception) {
                        ""
                    }

                    // Resolve category ID from name
                    val category = categoryMap[categoryName.lowercase()]
                    val categoryId = category?.id ?: ""

                    if (categoryId.isEmpty() && categoryName.isNotEmpty()) {
                        errors.add("Row ${rowIndex + 1}: Category '$categoryName' not found, will be set to empty")
                    }

                    val transaction = Transaction(
                        id = UUID.randomUUID().toString(),
                        amount = amount,
                        categoryId = categoryId,
                        note = note,
                        date = date,
                        type = type,
                        userId = userId,
                        isDeleted = false,
                        updatedAt = System.currentTimeMillis()
                    )

                    importedTransactions.add(transaction)

                } catch (e: Exception) {
                    errors.add("Row ${rowIndex + 1}: ${e.message ?: "Unknown error"}")
                    skippedCount++
                }
            }

            workbook.close()
        } ?: run {
            errors.add("Could not open file")
        }

        val result = ImportResult(
            importedCount = importedTransactions.size,
            skippedCount = skippedCount,
            errors = errors
        )

        return Pair(importedTransactions, result)
    }
}
