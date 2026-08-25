package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CashPayment
import com.example.data.model.CashSettings
import com.example.data.model.DailySnapshot
import com.example.data.model.Expense
import com.example.data.model.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface KasDao {
    // ---- Students ----
    @Query("SELECT * FROM students WHERE isActive = 1 ORDER BY absenNumber ASC")
    fun getAllActiveStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE isActive = 1 ORDER BY absenNumber ASC")
    suspend fun getAllActiveStudentsSync(): List<Student>

    @Query("SELECT * FROM students ORDER BY absenNumber ASC")
    suspend fun getAllStudentsSync(): List<Student>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Int): Student?

    // ---- Payments ----
    @Query("SELECT * FROM cash_payments WHERE date = :date")
    fun getPaymentsForDate(date: String): Flow<List<CashPayment>>

    @Query("SELECT * FROM cash_payments WHERE date = :date")
    suspend fun getPaymentsForDateSync(date: String): List<CashPayment>

    @Query("SELECT * FROM cash_payments WHERE date LIKE :monthPrefix || '%'")
    fun getPaymentsForMonth(monthPrefix: String): Flow<List<CashPayment>>

    @Query("SELECT * FROM cash_payments WHERE date LIKE :monthPrefix || '%'")
    suspend fun getPaymentsForMonthSync(monthPrefix: String): List<CashPayment>

    @Query("SELECT * FROM cash_payments ORDER BY paidAtTimestamp DESC")
    fun getAllPayments(): Flow<List<CashPayment>>

    @Query("SELECT * FROM cash_payments ORDER BY paidAtTimestamp DESC")
    suspend fun getAllPaymentsSync(): List<CashPayment>

    @Query("SELECT * FROM cash_payments WHERE studentId = :studentId ORDER BY date DESC")
    fun getPaymentsForStudent(studentId: Int): Flow<List<CashPayment>>

    @Query("SELECT * FROM cash_payments WHERE studentId = :studentId AND date = :date LIMIT 1")
    suspend fun getPaymentForStudentAndDate(studentId: Int, date: String): CashPayment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: CashPayment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<CashPayment>)

    @Query("DELETE FROM cash_payments WHERE studentId = :studentId AND date = :date")
    suspend fun deletePaymentByStudentAndDate(studentId: Int, date: String)

    @Query("DELETE FROM cash_payments WHERE id = :id")
    suspend fun deletePaymentById(id: Long)

    @Query("DELETE FROM cash_payments")
    suspend fun clearAllPayments()

    // ---- Settings ----
    @Query("SELECT * FROM cash_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<CashSettings?>

    @Query("SELECT * FROM cash_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsSync(): CashSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: CashSettings)

    // ---- Expenses ----
    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    suspend fun getAllExpensesSync(): List<Expense>

    @Query("SELECT * FROM expenses WHERE date LIKE :monthPrefix || '%' ORDER BY date DESC")
    fun getExpensesForMonth(monthPrefix: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE date LIKE :monthPrefix || '%' ORDER BY date DESC")
    suspend fun getExpensesForMonthSync(monthPrefix: String): List<Expense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Delete
    suspend fun deleteExpense(expense: Expense)

    // ---- Snapshots ----
    @Query("SELECT * FROM daily_snapshots ORDER BY date DESC")
    fun getAllSnapshots(): Flow<List<DailySnapshot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: DailySnapshot)
}
