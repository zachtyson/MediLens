package com.ztch.medilens_android_app.Notifications

import android.content.Context
import androidx.room.*

// Create local database for storing alarms


@Database(entities = [AlarmItem::class], version = 1)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    // get instance
    private var instance: AlarmDatabase? = null
    fun getInstance(context: Context): AlarmDatabase {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context,
                AlarmDatabase::class.java,
                "alarm_database"
            ).build()
        }
        return instance!!
    }
}

@Entity (tableName = "alarms")
data class AlarmItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "start_time_millis") val startTimeMillis: Long,
    @ColumnInfo(name = "interval_millis") val intervalMillis: Long,
    @ColumnInfo(name = "image_uri") val imageUri: String
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
