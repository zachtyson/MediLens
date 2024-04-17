package com.ztch.medilens_android_app.Notifications

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Create local database for storing alarms


@Database(entities = [AlarmItem::class, PastAlarmItem::class, FutureAlarmItem::class, PendingAlarmItem::class], version = 1)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun pastAlarmDao(): PastAlarmDao
    abstract fun futureAlarmDao(): FutureAlarmDao
    abstract fun pendingAlarmDao(): PendingAlarmDao
    // get instance
    companion object {
        private const val DATABASE_NAME = "alarm_database"

        @Volatile
        private var INSTANCE: AlarmDatabase? = null

        fun getInstance(context: Context): AlarmDatabase {
            // Return the existing instance if it already exists
            return INSTANCE ?: synchronized(this) {
                // Check again within synchronized block to avoid multiple instances
                INSTANCE ?: buildDatabase(context).also {
                    INSTANCE = it
                }
            }
        }

        private fun buildDatabase(context: Context): AlarmDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AlarmDatabase::class.java, DATABASE_NAME
            ).fallbackToDestructiveMigration().build()
        }
    }
}

@Entity (tableName = "alarms")
data class AlarmItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "start_time_millis") val startTimeMillis: Long,
    @ColumnInfo(name = "interval_millis") val intervalMillis: Long,
    @ColumnInfo(name = "image_uri") val imageUri: String,
    @ColumnInfo(name = "db_id") val dbId: Int,
    @ColumnInfo(name = "db_user_id") val dbUserId: Int
)

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms")
    fun getAll(): List<AlarmItem>

    @Insert
    fun insertAll(vararg alarms: AlarmItem)

    @Delete
    fun delete(alarm: AlarmItem)

    @Delete
    fun deleteAll(vararg alarms: AlarmItem)
}


// tables for alarms
// past alarms that the user either said that they did or did not take
// alarms that have gone off but the user has not responded to
// future alarms that have not gone off yet

@Entity (tableName = "past_alarms")
data class PastAlarmItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "time_millis") val timeMillis: Long,
    @ColumnInfo(name = "image_uri") val imageUri: String,
    @ColumnInfo(name = "response") val response: Boolean,
    @ColumnInfo(name = "request_code") val requestCode: Int
)

@Entity (tableName = "future_alarms")
data class FutureAlarmItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "time_millis") val timeMillis: Long,
    @ColumnInfo(name = "image_uri") val imageUri: String,
    @ColumnInfo(name = "response") val response: Boolean,
    @ColumnInfo(name = "request_code") val requestCode: Int
)

@Entity (tableName = "pending_alarms")
data class PendingAlarmItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "time_millis") val timeMillis: Long,
    @ColumnInfo(name = "image_uri") val imageUri: String,
    @ColumnInfo(name = "response") val response: Boolean,
    @ColumnInfo(name = "request_code") val requestCode: Int
)

@Dao
interface PastAlarmDao {
    @Query("SELECT * FROM past_alarms")
    fun getAll(): List<PastAlarmItem>

    @Query("SELECT * FROM future_alarms WHERE request_code = :requestCode")
    fun getByRequestCode(requestCode: Int): PastAlarmItem?
    @Insert
    fun insertAll(vararg alarms: PastAlarmItem)

    @Insert
    fun insert(alarm: PastAlarmItem)

    @Delete
    fun delete(alarm: PastAlarmItem)

    @Delete
    fun deleteAll(vararg alarms: PastAlarmItem)

}

@Dao
interface PendingAlarmDao {
    @Query("SELECT * FROM pending_alarms")
    fun getAll(): List<PendingAlarmItem>

    @Query("SELECT * FROM future_alarms WHERE request_code = :requestCode")
    fun getByRequestCode(requestCode: Int): PendingAlarmItem?

    @Insert
    fun insertAll(vararg alarms: PendingAlarmItem)

    @Insert
    fun insert(alarm: PendingAlarmItem)

    @Delete
    fun delete(alarm: PendingAlarmItem)

    @Delete
    fun deleteAll(vararg alarms: PendingAlarmItem)

    @Query("DELETE FROM future_alarms WHERE request_code = :requestCode")
    fun deleteByRequestCode(requestCode: Int)  // Method to delete by requestCode
}

@Dao
interface FutureAlarmDao {
    @Query("SELECT * FROM future_alarms")
    fun getAll(): List<FutureAlarmItem>

    @Query("SELECT * FROM future_alarms WHERE request_code = :requestCode")
    fun getByRequestCode(requestCode: Int): FutureAlarmItem?

    @Insert
    fun insertAll(vararg alarms: FutureAlarmItem)

    @Insert
    fun insert(alarm: FutureAlarmItem)

    @Delete
    fun delete(alarm: FutureAlarmItem)

    @Delete
    fun deleteAll(vararg alarms: FutureAlarmItem)

    @Query("DELETE FROM future_alarms WHERE request_code = :requestCode")
    fun deleteByRequestCode(requestCode: Int)  // Method to delete by requestCode
}

