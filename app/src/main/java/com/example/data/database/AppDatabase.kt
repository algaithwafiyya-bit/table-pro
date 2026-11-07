package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.TimetableDao
import com.example.data.model.DayConfig
import com.example.data.model.Institution
import com.example.data.model.SchoolClass
import com.example.data.model.Subject
import com.example.data.model.Teacher
import com.example.data.model.TeacherUnavailability
import com.example.data.model.TimetableSlot

@Database(
    entities = [
        Institution::class,
        Teacher::class,
        TeacherUnavailability::class,
        SchoolClass::class,
        Subject::class,
        DayConfig::class,
        TimetableSlot::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timetableDao(): TimetableDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "timetable_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
