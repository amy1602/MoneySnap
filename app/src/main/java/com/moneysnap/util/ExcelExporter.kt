package com.moneysnap.util

import android.content.Context
import com.moneysnap.domain.model.Transaction
import com.moneysnap.domain.model.Category
import com.moneysnap.domain.model.TransactionType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelExporter {
    
    fun exportTransactionsToExcel(
        context: Context, 
        transactions: List<Transaction>, 
        categories: List<Category>
    ): File {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Transactions")
        
        // Header
        val headerRow = sheet.createRow(0)
        val headers = arrayOf("Date", "Category", "Type", "Amount", "Note")
        for (i in headers.indices) {
            val cell = headerRow.createCell(i)
            cell.setCellValue(headers[i])
            // Minimal styling: can be expanded if needed
        }
        
        val categoryMap = categories.associateBy { it.id }
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        
        var rowNum = 1
        for (transaction in transactions.sortedByDescending { it.date }) {
            val row = sheet.createRow(rowNum++)
            val categoryName = categoryMap[transaction.categoryId]?.name ?: "Unknown"
            
            row.createCell(0).setCellValue(dateFormatter.format(Date(transaction.date)))
            row.createCell(1).setCellValue(categoryName)
            row.createCell(2).setCellValue(if(transaction.type == TransactionType.EXPENSE) "Expense" else "Income")
            row.createCell(3).setCellValue(transaction.amount)
            row.createCell(4).setCellValue(transaction.note)
        }
        
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "MoneySnap_Report_$timeStamp.xlsx"
        
        // Save to cache directory so FileProvider can access it
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        
        val file = File(exportDir, fileName)
        FileOutputStream(file).use { outputStream ->
            workbook.write(outputStream)
        }
        workbook.close()
        
        return file
    }
}
