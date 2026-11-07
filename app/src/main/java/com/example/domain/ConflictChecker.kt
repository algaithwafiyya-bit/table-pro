package com.example.domain

import com.example.data.model.Institution
import com.example.data.model.SchoolClass
import com.example.data.model.Teacher
import com.example.data.model.TeacherUnavailability
import com.example.data.model.TimetableSlot
import org.json.JSONArray

sealed class TeacherStatus {
    object Available : TeacherStatus()
    data class Unavailable(val reason: String, val isFullDay: Boolean) : TeacherStatus()
    data class BusyOtherInstitution(val institutionName: String, val className: String) : TeacherStatus()
    data class BusyThisInstitution(val className: String) : TeacherStatus()
    object NotAssignedToInstitution : TeacherStatus()
}

data class ConflictReport(
    val hasConflict: Boolean,
    val isSevere: Boolean, // Severe = unavailable or already in another class at same time
    val messageEn: String,
    val messageMl: String
)

object ConflictChecker {

    fun parseInstitutionIds(json: String): List<String> {
        return try {
            val list = mutableListOf<String>()
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Determine the status of a specific teacher for a specific Day and Period.
     * excludeSlotId can be passed when editing an existing slot.
     */
    fun checkTeacherStatus(
        teacher: Teacher,
        institutionId: String,
        dayOfWeek: Int,
        periodIndex: Int,
        allSlots: List<TimetableSlot>,
        allUnavailabilities: List<TeacherUnavailability>,
        allClasses: List<SchoolClass>,
        allInstitutions: List<Institution>,
        excludeSlotId: String? = null
    ): TeacherStatus {
        // 1. Check if teacher belongs to this institution
        val teacherInstitutions = parseInstitutionIds(teacher.institutionIdsJson)
        if (teacherInstitutions.isNotEmpty() && !teacherInstitutions.contains(institutionId)) {
            return TeacherStatus.NotAssignedToInstitution
        }

        // 2. Check unavailability (leave / off / duty)
        val unavailabilities = allUnavailabilities.filter {
            it.teacherId == teacher.id && it.dayOfWeek == dayOfWeek
        }
        for (u in unavailabilities) {
            if (u.isFullDay) {
                return TeacherStatus.Unavailable(u.reason.ifBlank { "Full Day Leave" }, true)
            }
            if (u.periodIndex == periodIndex) {
                return TeacherStatus.Unavailable(u.reason.ifBlank { "Period $periodIndex Off" }, false)
            }
        }

        // 3. Check timetable clashes across all institutions
        val clashingSlots = allSlots.filter { slot ->
            slot.id != excludeSlotId &&
            slot.dayOfWeek == dayOfWeek &&
            slot.periodIndex == periodIndex &&
            (slot.primaryTeacherId == teacher.id || slot.secondaryTeacherId == teacher.id)
        }

        for (slot in clashingSlots) {
            if (slot.institutionId != institutionId) {
                val instName = allInstitutions.find { it.id == slot.institutionId }?.malayalamName
                    ?: allInstitutions.find { it.id == slot.institutionId }?.name
                    ?: "മറ്റൊരു സ്ഥാപനം (Other Institution)"
                val clsName = allClasses.find { it.id == slot.classId }?.malayalamName
                    ?: allClasses.find { it.id == slot.classId }?.name
                    ?: "ക്ലാസ് (Class)"
                return TeacherStatus.BusyOtherInstitution(instName, clsName)
            } else {
                val clsName = allClasses.find { it.id == slot.classId }?.malayalamName
                    ?: allClasses.find { it.id == slot.classId }?.name
                    ?: "ക്ലാസ് (Class)"
                return TeacherStatus.BusyThisInstitution(clsName)
            }
        }

        return TeacherStatus.Available
    }

    /**
     * Validates whether a candidate slot assignment will create conflicts.
     */
    fun validateAssignment(
        institutionId: String,
        classId: String,
        dayOfWeek: Int,
        periodIndex: Int,
        primaryTeacherId: String,
        secondaryTeacherId: String?,
        excludeSlotId: String?,
        allTeachers: List<Teacher>,
        allSlots: List<TimetableSlot>,
        allUnavailabilities: List<TeacherUnavailability>,
        allClasses: List<SchoolClass>,
        allInstitutions: List<Institution>
    ): ConflictReport {
        val primaryTeacher = allTeachers.find { it.id == primaryTeacherId }
            ?: return ConflictReport(true, true, "Please select a teacher", "ദയവായി ഒരു അധ്യാപകനെ തിരഞ്ഞെടുക്കുക")

        val primaryStatus = checkTeacherStatus(
            teacher = primaryTeacher,
            institutionId = institutionId,
            dayOfWeek = dayOfWeek,
            periodIndex = periodIndex,
            allSlots = allSlots,
            allUnavailabilities = allUnavailabilities,
            allClasses = allClasses,
            allInstitutions = allInstitutions,
            excludeSlotId = excludeSlotId
        )

        val report = when (primaryStatus) {
            is TeacherStatus.Unavailable -> {
                ConflictReport(
                    hasConflict = true,
                    isSevere = true,
                    messageEn = "${primaryTeacher.name} is marked UNAVAILABLE (${if (primaryStatus.isFullDay) "Full Day" else "Period $periodIndex"}: ${primaryStatus.reason})",
                    messageMl = "${primaryTeacher.malayalamName.ifBlank { primaryTeacher.name }} ഈ സമയത്ത് ലഭ്യമല്ല (${if (primaryStatus.isFullDay) "മുഴുവൻ ദിവസം" else "പിരീഡ് $periodIndex"}: ${primaryStatus.reason})"
                )
            }
            is TeacherStatus.BusyOtherInstitution -> {
                ConflictReport(
                    hasConflict = true,
                    isSevere = true,
                    messageEn = "CROSS-INSTITUTION CONFLICT: ${primaryTeacher.name} already has class at ${primaryStatus.institutionName} (${primaryStatus.className})",
                    messageMl = "മറ്റൊരു സ്ഥാപനത്തിലെ തടസ്സം: ${primaryTeacher.malayalamName.ifBlank { primaryTeacher.name }} ഈ സമയത്ത് ${primaryStatus.institutionName}-ൽ (${primaryStatus.className}) ക്ലാസിലാണ്!"
                )
            }
            is TeacherStatus.BusyThisInstitution -> {
                ConflictReport(
                    hasConflict = true,
                    isSevere = true,
                    messageEn = "CONFLICT: ${primaryTeacher.name} is already teaching ${primaryStatus.className} at this period!",
                    messageMl = "തടസ്സം: ${primaryTeacher.malayalamName.ifBlank { primaryTeacher.name }} ഇതേ സമയത്ത് ${primaryStatus.className}-ൽ ക്ലാസിലാണ്!"
                )
            }
            is TeacherStatus.NotAssignedToInstitution -> {
                ConflictReport(
                    hasConflict = true,
                    isSevere = false,
                    messageEn = "Note: ${primaryTeacher.name} is not marked as teaching in this institution.",
                    messageMl = "ശ്രദ്ധിക്കുക: ${primaryTeacher.malayalamName.ifBlank { primaryTeacher.name }} ഈ സ്ഥാപനത്തിൽ അധ്യാപകനായി രജിസ്റ്റർ ചെയ്തിട്ടില്ല."
                )
            }
            is TeacherStatus.Available -> null
        }

        if (report != null) return report

        // Check secondary teacher if shared
        if (!secondaryTeacherId.isNullOrBlank() && secondaryTeacherId != "NONE") {
            val secondaryTeacher = allTeachers.find { it.id == secondaryTeacherId }
            if (secondaryTeacher != null) {
                val secStatus = checkTeacherStatus(
                    teacher = secondaryTeacher,
                    institutionId = institutionId,
                    dayOfWeek = dayOfWeek,
                    periodIndex = periodIndex,
                    allSlots = allSlots,
                    allUnavailabilities = allUnavailabilities,
                    allClasses = allClasses,
                    allInstitutions = allInstitutions,
                    excludeSlotId = excludeSlotId
                )

                when (secStatus) {
                    is TeacherStatus.Unavailable -> {
                        return ConflictReport(
                            hasConflict = true,
                            isSevere = true,
                            messageEn = "Shared Teacher ${secondaryTeacher.name} is UNAVAILABLE (${secStatus.reason})",
                            messageMl = "ഷെയർ അധ്യാപകൻ ${secondaryTeacher.malayalamName.ifBlank { secondaryTeacher.name }} ഈ സമയത്ത് ലഭ്യമല്ല (${secStatus.reason})"
                        )
                    }
                    is TeacherStatus.BusyOtherInstitution -> {
                        return ConflictReport(
                            hasConflict = true,
                            isSevere = true,
                            messageEn = "Shared Teacher ${secondaryTeacher.name} has class at ${secStatus.institutionName} (${secStatus.className})",
                            messageMl = "ഷെയർ അധ്യാപകൻ ${secondaryTeacher.malayalamName.ifBlank { secondaryTeacher.name }} ${secStatus.institutionName}-ൽ (${secStatus.className}) ക്ലാസിലാണ്!"
                        )
                    }
                    is TeacherStatus.BusyThisInstitution -> {
                        return ConflictReport(
                            hasConflict = true,
                            isSevere = true,
                            messageEn = "Shared Teacher ${secondaryTeacher.name} has class at ${secStatus.className}!",
                            messageMl = "ഷെയർ അധ്യാപകൻ ${secondaryTeacher.malayalamName.ifBlank { secondaryTeacher.name }} ${secStatus.className}-ൽ ക്ലാസിലാണ്!"
                        )
                    }
                    else -> {}
                }
            }
        }

        return ConflictReport(
            hasConflict = false,
            isSevere = false,
            messageEn = "Teacher is available and ready for allocation.",
            messageMl = "അധ്യാപകൻ ലഭ്യമാണ്. തടസ്സങ്ങളൊന്നുമില്ല."
        )
    }
}
