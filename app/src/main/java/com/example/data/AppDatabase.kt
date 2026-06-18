package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "duty_entries")
data class DutyEntry(
    @PrimaryKey val date: String, // "yyyy-MM-dd"
    val type: String, // "Present", "Half Day", "Leave", "Sick", "Holiday", "Off"
    val otHours: Float,
    val lateMins: Int,
    val shift: String // "Morning", "Evening", "Night"
)

@Entity(tableName = "advance_entries")
data class AdvanceEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // "yyyy-MM-dd"
    val reason: String,
    val amount: Double,
    val status: String // "taken", "repaid"
)

@Dao
interface DutyDao {
    @Query("SELECT * FROM duty_entries")
    fun getAllFlow(): Flow<List<DutyEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DutyEntry)

    @Query("DELETE FROM duty_entries WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("DELETE FROM duty_entries")
    suspend fun clearAll()
}

@Dao
interface AdvanceDao {
    @Query("SELECT * FROM advance_entries ORDER BY date DESC, id DESC")
    fun getAllFlow(): Flow<List<AdvanceEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: AdvanceEntry)

    @Query("DELETE FROM advance_entries WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM advance_entries")
    suspend fun clearAll()
}

@Database(entities = [DutyEntry::class, AdvanceEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dutyDao(): DutyDao
    abstract fun advanceDao(): AdvanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "duty_tracker_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
