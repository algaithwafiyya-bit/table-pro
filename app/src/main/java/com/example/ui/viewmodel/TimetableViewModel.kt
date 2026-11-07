package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.DayConfig
import com.example.data.model.Institution
import com.example.data.model.SchoolClass
import com.example.data.model.Subject
import com.example.data.model.Teacher
import com.example.data.model.TeacherUnavailability
import com.example.data.model.TimetableSlot
import com.example.data.repository.TimetableRepository
import com.example.domain.ConflictChecker
import com.example.domain.ConflictReport
import com.example.domain.TeacherStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

data class UiState(
    val institutions: List<Institution> = emptyList(),
    val selectedInstitutionId: String = "",
    val teachers: List<Teacher> = emptyList(),
    val unavailabilities: List<TeacherUnavailability> = emptyList(),
    val classes: List<SchoolClass> = emptyList(),
    val selectedClassId: String = "",
    val subjects: List<Subject> = emptyList(),
    val dayConfigs: List<DayConfig> = emptyList(),
    val slots: List<TimetableSlot> = emptyList(),
    val selectedDayOfWeek: Int = 1, // 1 = Monday
    val viewMode: TimetableViewMode = TimetableViewMode.DAY_LIST,
    val selectedTeacherFilterId: String? = null,
    val isMalayalamPrimary: Boolean = true
)

enum class TimetableViewMode {
    DAY_LIST,
    WEEK_GRID,
    TEACHER_VIEW
}

class TimetableViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TimetableRepository

    private val _selectedInstitutionId = MutableStateFlow("")
    val selectedInstitutionId: StateFlow<String> = _selectedInstitutionId.asStateFlow()

    private val _selectedClassId = MutableStateFlow("")
    val selectedClassId: StateFlow<String> = _selectedClassId.asStateFlow()

    private val _selectedDayOfWeek = MutableStateFlow(
        // Default to today or Monday if Sunday
        when (val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> 1
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 1
        }
    )
    val selectedDayOfWeek: StateFlow<Int> = _selectedDayOfWeek.asStateFlow()

    private val _viewMode = MutableStateFlow(TimetableViewMode.DAY_LIST)
    val viewMode: StateFlow<TimetableViewMode> = _viewMode.asStateFlow()

    private val _selectedTeacherFilterId = MutableStateFlow<String?>(null)
    val selectedTeacherFilterId: StateFlow<String?> = _selectedTeacherFilterId.asStateFlow()

    private val _isMalayalamPrimary = MutableStateFlow(true)
    val isMalayalamPrimary: StateFlow<Boolean> = _isMalayalamPrimary.asStateFlow()

    val uiState: StateFlow<UiState>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TimetableRepository(db.timetableDao())

        // Check if database needs seeding
        viewModelScope.launch {
            repository.allInstitutions.collect { list ->
                if (list.isEmpty()) {
                    repository.seedInitialData()
                } else if (_selectedInstitutionId.value.isEmpty()) {
                    _selectedInstitutionId.value = list.first().id
                }
            }
        }

        // Combine flows for unified UI state
        uiState = combine(
            combine(
                repository.allInstitutions,
                _selectedInstitutionId,
                repository.allTeachers,
                repository.allTeacherUnavailabilities,
                repository.allClasses
            ) { insts, selInst, teachers, unavs, cls ->
                // Ensure selected institution and class are valid
                val validInstId = if (selInst.isNotEmpty() && insts.any { it.id == selInst }) {
                    selInst
                } else {
                    insts.firstOrNull()?.id ?: ""
                }
                val classesForInst = cls.filter { it.institutionId == validInstId }
                val validClassId = if (classesForInst.any { it.id == _selectedClassId.value }) {
                    _selectedClassId.value
                } else {
                    classesForInst.firstOrNull()?.id ?: ""
                }
                if (_selectedClassId.value != validClassId) {
                    _selectedClassId.value = validClassId
                }
                Pair(Pair(insts, validInstId), Triple(teachers, unavs, cls))
            },
            combine(
                _selectedClassId,
                repository.allSubjects,
                repository.allDayConfigs,
                repository.allTimetableSlots,
                _selectedDayOfWeek
            ) { selClass, subjects, dayConfigs, slots, day ->
                Triple(Pair(selClass, subjects), Pair(dayConfigs, slots), day)
            },
            combine(
                _viewMode,
                _selectedTeacherFilterId,
                _isMalayalamPrimary
            ) { vm, tFilter, ml ->
                Triple(vm, tFilter, ml)
            }
        ) { part1, part2, part3 ->
            val insts = part1.first.first
            val validInstId = part1.first.second
            val teachers = part1.second.first
            val unavs = part1.second.second
            val classes = part1.second.third

            val validClassId = part2.first.first
            val subjects = part2.first.second
            val dayConfigs = part2.second.first
            val slots = part2.second.second
            val day = part2.third

            val vm = part3.first
            val tFilter = part3.second
            val ml = part3.third

            UiState(
                institutions = insts,
                selectedInstitutionId = validInstId,
                teachers = teachers,
                unavailabilities = unavs,
                classes = classes,
                selectedClassId = validClassId,
                subjects = subjects,
                dayConfigs = dayConfigs,
                slots = slots,
                selectedDayOfWeek = day,
                viewMode = vm,
                selectedTeacherFilterId = tFilter,
                isMalayalamPrimary = ml
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState()
        )
    }

    fun selectInstitution(id: String) {
        _selectedInstitutionId.value = id
        // Reset selected class for this institution
        val classes = uiState.value.classes.filter { it.institutionId == id }
        _selectedClassId.value = classes.firstOrNull()?.id ?: ""
    }

    fun selectClass(id: String) {
        _selectedClassId.value = id
    }

    fun selectDay(day: Int) {
        _selectedDayOfWeek.value = day
    }

    fun setViewMode(mode: TimetableViewMode) {
        _viewMode.value = mode
    }

    fun setSelectedTeacherFilter(teacherId: String?) {
        _selectedTeacherFilterId.value = teacherId
    }

    fun toggleLanguage() {
        _isMalayalamPrimary.value = !_isMalayalamPrimary.value
    }

    // --- Slot Operations ---

    fun saveSlot(slot: TimetableSlot) {
        viewModelScope.launch {
            repository.insertSlot(slot)
        }
    }

    fun deleteSlot(id: String) {
        viewModelScope.launch {
            repository.deleteSlot(id)
        }
    }

    fun clearPeriodSlots(classId: String, dayOfWeek: Int, periodIndex: Int) {
        viewModelScope.launch {
            repository.deleteSlotsForPeriod(classId, dayOfWeek, periodIndex)
        }
    }

    // --- Conflict Checking Helper ---

    fun validateSlot(
        dayOfWeek: Int,
        periodIndex: Int,
        primaryTeacherId: String,
        secondaryTeacherId: String?,
        excludeSlotId: String?
    ): ConflictReport {
        val state = uiState.value
        return ConflictChecker.validateAssignment(
            institutionId = state.selectedInstitutionId,
            classId = state.selectedClassId,
            dayOfWeek = dayOfWeek,
            periodIndex = periodIndex,
            primaryTeacherId = primaryTeacherId,
            secondaryTeacherId = secondaryTeacherId,
            excludeSlotId = excludeSlotId,
            allTeachers = state.teachers,
            allSlots = state.slots,
            allUnavailabilities = state.unavailabilities,
            allClasses = state.classes,
            allInstitutions = state.institutions
        )
    }

    fun getTeacherAvailabilityStatus(
        teacher: Teacher,
        dayOfWeek: Int,
        periodIndex: Int,
        excludeSlotId: String? = null
    ): TeacherStatus {
        val state = uiState.value
        return ConflictChecker.checkTeacherStatus(
            teacher = teacher,
            institutionId = state.selectedInstitutionId,
            dayOfWeek = dayOfWeek,
            periodIndex = periodIndex,
            allSlots = state.slots,
            allUnavailabilities = state.unavailabilities,
            allClasses = state.classes,
            allInstitutions = state.institutions,
            excludeSlotId = excludeSlotId
        )
    }

    // --- Teacher Operations ---

    fun addOrUpdateTeacher(teacher: Teacher) {
        viewModelScope.launch {
            repository.insertTeacher(teacher)
        }
    }

    fun deleteTeacher(id: String) {
        viewModelScope.launch {
            repository.deleteTeacher(id)
        }
    }

    // --- Availability Operations ---

    fun toggleUnavailability(
        teacherId: String,
        dayOfWeek: Int,
        isFullDay: Boolean,
        periodIndex: Int?,
        reason: String
    ) {
        viewModelScope.launch {
            val existing = uiState.value.unavailabilities.find {
                it.teacherId == teacherId && it.dayOfWeek == dayOfWeek &&
                ((isFullDay && it.isFullDay) || (!isFullDay && !it.isFullDay && it.periodIndex == periodIndex))
            }

            if (existing != null) {
                repository.deleteUnavailability(existing.id)
            } else {
                repository.insertUnavailability(
                    TeacherUnavailability(
                        id = UUID.randomUUID().toString(),
                        teacherId = teacherId,
                        dayOfWeek = dayOfWeek,
                        isFullDay = isFullDay,
                        periodIndex = periodIndex,
                        reason = reason
                    )
                )
            }
        }
    }

    fun removeUnavailability(id: String) {
        viewModelScope.launch {
            repository.deleteUnavailability(id)
        }
    }

    // --- Class Operations ---

    fun addOrUpdateClass(schoolClass: SchoolClass) {
        viewModelScope.launch {
            repository.insertClass(schoolClass)
            if (_selectedClassId.value.isEmpty()) {
                _selectedClassId.value = schoolClass.id
            }
        }
    }

    fun deleteClass(id: String) {
        viewModelScope.launch {
            repository.deleteClass(id)
        }
    }

    // --- Subject Operations ---

    fun addOrUpdateSubject(subject: Subject) {
        viewModelScope.launch {
            repository.insertSubject(subject)
        }
    }

    fun deleteSubject(id: String) {
        viewModelScope.launch {
            repository.deleteSubject(id)
        }
    }

    // --- Day Configuration Operations ---

    fun updateDayConfig(institutionId: String, dayOfWeek: Int, periodCount: Int, isActive: Boolean) {
        viewModelScope.launch {
            val id = "${institutionId}_${dayOfWeek}"
            repository.insertDayConfig(
                DayConfig(
                    id = id,
                    institutionId = institutionId,
                    dayOfWeek = dayOfWeek,
                    isActive = isActive,
                    periodCount = periodCount
                )
            )
        }
    }

    // --- Institution Operations ---

    fun updateInstitution(institution: Institution) {
        viewModelScope.launch {
            repository.updateInstitution(institution)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            repository.resetDatabase()
        }
    }
}
