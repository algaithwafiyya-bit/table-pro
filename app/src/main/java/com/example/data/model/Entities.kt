package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an educational institution (e.g., Campus A, Wafiyya College, etc.)
 */
@Entity(tableName = "institutions")
data class Institution(
    @PrimaryKey val id: String,
    val name: String,
    val malayalamName: String,
    val code: String,
    val description: String = ""
)

/**
 * Teacher who can teach in one or both institutions.
 */
@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey val id: String,
    val name: String,
    val malayalamName: String,
    val phone: String = "",
    val email: String = "",
    val subjectSpecialization: String = "",
    val colorHex: String = "#1E3A8A",
    val institutionIdsJson: String = "[]" // JSON array of institution IDs
)

/**
 * Availability / Unavailability of a teacher.
 * If isFullDay is true, teacher is unavailable the entire day.
 * If isFullDay is false, unavailable for specific periodIndex (1-based).
 */
@Entity(tableName = "teacher_unavailability")
data class TeacherUnavailability(
    @PrimaryKey val id: String,
    val teacherId: String,
    val dayOfWeek: Int, // 1 = Monday, 2 = Tuesday, ..., 7 = Sunday
    val isFullDay: Boolean = true,
    val periodIndex: Int? = null, // 1-based index if specific period
    val reason: String = ""
)

/**
 * A class or division in an institution (e.g., 1st Year Wafiyya, Grade 10 A).
 */
@Entity(tableName = "classes")
data class SchoolClass(
    @PrimaryKey val id: String,
    val institutionId: String,
    val name: String,
    val malayalamName: String,
    val section: String = "",
    val hasBatches: Boolean = false,
    val batchNames: String = "Batch A,Batch B" // Comma separated batch names
)

/**
 * Subject taught in the institution.
 */
@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey val id: String,
    val institutionId: String, // Can also be "ALL" if shared
    val name: String,
    val malayalamName: String,
    val code: String = "",
    val colorHex: String = "#0284C7"
)

/**
 * Day-wise configuration of period counts and times for an institution.
 * Allows setting different period counts depending on the day (e.g. Friday 4, Monday 6).
 */
@Entity(tableName = "day_configs")
data class DayConfig(
    @PrimaryKey val id: String, // format: "${institutionId}_${dayOfWeek}"
    val institutionId: String,
    val dayOfWeek: Int, // 1 to 7
    val isActive: Boolean = true, // is school active this day?
    val periodCount: Int = 6, // number of periods for this day
    val periodTimesJson: String = "[]" // optional specific times e.g. ["09:00 - 09:45", ...]
)

/**
 * A scheduled slot in the timetable.
 * Supports:
 * - Whole class single subject
 * - Batch split: two subjects concurrently in the same slot (e.g., Batch A and Batch B)
 * - Shared teaching: primary teacher + secondary teacher sharing workload
 */
@Entity(tableName = "timetable_slots")
data class TimetableSlot(
    @PrimaryKey val id: String,
    val institutionId: String,
    val classId: String,
    val dayOfWeek: Int, // 1..7
    val periodIndex: Int, // 1..N
    val isBatchSplit: Boolean = false,
    val batchName: String? = null, // e.g. "Batch A" or "Batch B" (null if full class)
    val subjectId: String,
    val primaryTeacherId: String,
    val secondaryTeacherId: String? = null, // For shared teaching
    val shareType: String = "NONE", // NONE, ONE_OF_THREE, ALTERNATE_WEEK, CO_TEACHING
    val note: String = ""
)
