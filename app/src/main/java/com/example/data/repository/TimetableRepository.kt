package com.example.data.repository

import com.example.data.dao.TimetableDao
import com.example.data.model.DayConfig
import com.example.data.model.Institution
import com.example.data.model.SchoolClass
import com.example.data.model.Subject
import com.example.data.model.Teacher
import com.example.data.model.TeacherUnavailability
import com.example.data.model.TimetableSlot
import kotlinx.coroutines.flow.Flow

class TimetableRepository(private val dao: TimetableDao) {

    val allInstitutions: Flow<List<Institution>> = dao.getAllInstitutions()
    val allTeachers: Flow<List<Teacher>> = dao.getAllTeachers()
    val allTeacherUnavailabilities: Flow<List<TeacherUnavailability>> = dao.getAllTeacherUnavailabilities()
    val allClasses: Flow<List<SchoolClass>> = dao.getAllClasses()
    val allSubjects: Flow<List<Subject>> = dao.getAllSubjects()
    val allDayConfigs: Flow<List<DayConfig>> = dao.getAllDayConfigs()
    val allTimetableSlots: Flow<List<TimetableSlot>> = dao.getAllTimetableSlots()

    suspend fun insertInstitution(institution: Institution) = dao.insertInstitution(institution)
    suspend fun updateInstitution(institution: Institution) = dao.updateInstitution(institution)
    suspend fun deleteInstitution(id: String) = dao.deleteInstitution(id)

    suspend fun insertTeacher(teacher: Teacher) = dao.insertTeacher(teacher)
    suspend fun updateTeacher(teacher: Teacher) = dao.updateTeacher(teacher)
    suspend fun deleteTeacher(id: String) = dao.deleteTeacher(id)

    suspend fun insertUnavailability(unavailability: TeacherUnavailability) = dao.insertUnavailability(unavailability)
    suspend fun deleteUnavailability(id: String) = dao.deleteUnavailability(id)
    suspend fun removeSpecificUnavailability(teacherId: String, dayOfWeek: Int, isFullDay: Boolean, periodIndex: Int?) =
        dao.removeSpecificUnavailability(teacherId, dayOfWeek, isFullDay, periodIndex)

    suspend fun insertClass(schoolClass: SchoolClass) = dao.insertClass(schoolClass)
    suspend fun updateClass(schoolClass: SchoolClass) = dao.updateClass(schoolClass)
    suspend fun deleteClass(id: String) = dao.deleteClass(id)

    suspend fun insertSubject(subject: Subject) = dao.insertSubject(subject)
    suspend fun updateSubject(subject: Subject) = dao.updateSubject(subject)
    suspend fun deleteSubject(id: String) = dao.deleteSubject(id)

    suspend fun insertDayConfig(dayConfig: DayConfig) = dao.insertDayConfig(dayConfig)

    suspend fun insertSlot(slot: TimetableSlot) = dao.insertSlot(slot)
    suspend fun updateSlot(slot: TimetableSlot) = dao.updateSlot(slot)
    suspend fun deleteSlot(id: String) = dao.deleteSlot(id)
    suspend fun deleteSlotsForPeriod(classId: String, dayOfWeek: Int, periodIndex: Int) =
        dao.deleteSlotsForPeriod(classId, dayOfWeek, periodIndex)

    suspend fun resetDatabase() {
        dao.clearTimetableSlots()
        dao.clearTeacherUnavailability()
        dao.clearClasses()
        dao.clearSubjects()
        dao.clearTeachers()
        dao.clearInstitutions()
        dao.clearDayConfigs()
        DefaultDataSeeder.seedInitialDataIfEmpty(dao)
    }

    suspend fun seedInitialData() {
        DefaultDataSeeder.seedInitialDataIfEmpty(dao)
    }
}
