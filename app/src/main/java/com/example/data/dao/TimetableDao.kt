package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DayConfig
import com.example.data.model.Institution
import com.example.data.model.SchoolClass
import com.example.data.model.Subject
import com.example.data.model.Teacher
import com.example.data.model.TeacherUnavailability
import com.example.data.model.TimetableSlot
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {

    // --- Institutions ---
    @Query("SELECT * FROM institutions ORDER BY name ASC")
    fun getAllInstitutions(): Flow<List<Institution>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstitution(institution: Institution)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstitutions(institutions: List<Institution>)

    @Update
    suspend fun updateInstitution(institution: Institution)

    @Query("DELETE FROM institutions WHERE id = :id")
    suspend fun deleteInstitution(id: String)

    // --- Teachers ---
    @Query("SELECT * FROM teachers ORDER BY name ASC")
    fun getAllTeachers(): Flow<List<Teacher>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: Teacher)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachers(teachers: List<Teacher>)

    @Update
    suspend fun updateTeacher(teacher: Teacher)

    @Query("DELETE FROM teachers WHERE id = :id")
    suspend fun deleteTeacher(id: String)

    // --- Teacher Unavailability ---
    @Query("SELECT * FROM teacher_unavailability")
    fun getAllTeacherUnavailabilities(): Flow<List<TeacherUnavailability>>

    @Query("SELECT * FROM teacher_unavailability WHERE teacherId = :teacherId")
    fun getTeacherUnavailabilityForTeacher(teacherId: String): Flow<List<TeacherUnavailability>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnavailability(unavailability: TeacherUnavailability)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnavailabilities(list: List<TeacherUnavailability>)

    @Query("DELETE FROM teacher_unavailability WHERE id = :id")
    suspend fun deleteUnavailability(id: String)

    @Query("DELETE FROM teacher_unavailability WHERE teacherId = :teacherId AND dayOfWeek = :dayOfWeek AND ((:isFullDay = 1 AND isFullDay = 1) OR (periodIndex = :periodIndex))")
    suspend fun removeSpecificUnavailability(teacherId: String, dayOfWeek: Int, isFullDay: Boolean, periodIndex: Int?)

    // --- Classes ---
    @Query("SELECT * FROM classes ORDER BY name ASC")
    fun getAllClasses(): Flow<List<SchoolClass>>

    @Query("SELECT * FROM classes WHERE institutionId = :institutionId ORDER BY name ASC")
    fun getClassesForInstitution(institutionId: String): Flow<List<SchoolClass>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(schoolClass: SchoolClass)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<SchoolClass>)

    @Update
    suspend fun updateClass(schoolClass: SchoolClass)

    @Query("DELETE FROM classes WHERE id = :id")
    suspend fun deleteClass(id: String)

    // --- Subjects ---
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<Subject>)

    @Update
    suspend fun updateSubject(subject: Subject)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubject(id: String)

    // --- Day Configurations ---
    @Query("SELECT * FROM day_configs")
    fun getAllDayConfigs(): Flow<List<DayConfig>>

    @Query("SELECT * FROM day_configs WHERE institutionId = :institutionId")
    fun getDayConfigsForInstitution(institutionId: String): Flow<List<DayConfig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayConfig(dayConfig: DayConfig)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayConfigs(dayConfigs: List<DayConfig>)

    // --- Timetable Slots ---
    @Query("SELECT * FROM timetable_slots")
    fun getAllTimetableSlots(): Flow<List<TimetableSlot>>

    @Query("SELECT * FROM timetable_slots WHERE institutionId = :institutionId AND classId = :classId")
    fun getSlotsForClass(institutionId: String, classId: String): Flow<List<TimetableSlot>>

    @Query("SELECT * FROM timetable_slots WHERE institutionId = :institutionId AND classId = :classId AND dayOfWeek = :dayOfWeek")
    fun getSlotsForClassAndDay(institutionId: String, classId: String, dayOfWeek: Int): Flow<List<TimetableSlot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: TimetableSlot)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlots(slots: List<TimetableSlot>)

    @Update
    suspend fun updateSlot(slot: TimetableSlot)

    @Query("DELETE FROM timetable_slots WHERE id = :id")
    suspend fun deleteSlot(id: String)

    @Query("DELETE FROM timetable_slots WHERE classId = :classId AND dayOfWeek = :dayOfWeek AND periodIndex = :periodIndex")
    suspend fun deleteSlotsForPeriod(classId: String, dayOfWeek: Int, periodIndex: Int)

    // --- Clear All for Reset ---
    @Query("DELETE FROM timetable_slots")
    suspend fun clearTimetableSlots()

    @Query("DELETE FROM teacher_unavailability")
    suspend fun clearTeacherUnavailability()

    @Query("DELETE FROM classes")
    suspend fun clearClasses()

    @Query("DELETE FROM subjects")
    suspend fun clearSubjects()

    @Query("DELETE FROM teachers")
    suspend fun clearTeachers()

    @Query("DELETE FROM institutions")
    suspend fun clearInstitutions()

    @Query("DELETE FROM day_configs")
    suspend fun clearDayConfigs()
}
