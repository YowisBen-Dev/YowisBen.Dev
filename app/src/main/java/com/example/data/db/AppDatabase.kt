package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CashPayment
import com.example.data.model.CashSettings
import com.example.data.model.DailySnapshot
import com.example.data.model.Expense
import com.example.data.model.Student
import com.example.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Student::class,
        CashSettings::class,
        CashPayment::class,
        Expense::class,
        DailySnapshot::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kasDao(): KasDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kas_xi_f4_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.kasDao())
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        val dao = database.kasDao()
                        val currentSettings = dao.getSettingsSync()
                        if (currentSettings == null) {
                            populateDatabase(dao)
                        }
                    }
                }
            }

            private suspend fun populateDatabase(dao: KasDao) {
                dao.insertOrUpdateSettings(InitialData.defaultSettings)
                val existingStudents = dao.getAllActiveStudentsSync()
                if (existingStudents.isEmpty()) {
                    dao.insertStudents(InitialData.defaultStudents)
                    val ym = DateUtils.getCurrentYearMonth()
                    val payments = InitialData.createInitialPayments(ym)
                    dao.insertPayments(payments)
                    val expenses = InitialData.createInitialExpenses(DateUtils.getTodayIso())
                    expenses.forEach { dao.insertExpense(it) }
                }
            }
        }
    }
}
