package com.moneysnap.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.moneysnap.data.local.dao.CategoryDao
import com.moneysnap.data.local.dao.TransactionDao
import com.moneysnap.data.local.dao.UserStatsDao
import com.moneysnap.data.local.entity.CategoryEntity
import com.moneysnap.data.local.entity.TransactionEntity
import com.moneysnap.data.local.entity.UserStatsEntity

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        UserStatsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MoneyDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun userStatsDao(): UserStatsDao

    companion object {
        @Volatile
        private var INSTANCE: MoneyDatabase? = null

        fun getDatabase(context: Context): MoneyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoneyDatabase::class.java,
                    "money_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
