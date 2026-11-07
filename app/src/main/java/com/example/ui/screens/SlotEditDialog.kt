package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SchoolClass
import com.example.data.model.Subject
import com.example.data.model.Teacher
import com.example.data.model.TimetableSlot
import com.example.domain.ConflictReport
import com.example.domain.TeacherStatus
import com.example.ui.components.StatusBadgeView
import com.example.ui.components.getDayNameMalayalam
import com.example.ui.viewmodel.TimetableViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotEditDialog(
    dayOfWeek: Int,
    periodIndex: Int,
    currentSlots: List<TimetableSlot>, // 0, 1 (whole class) or 2 (Batch A & Batch B)
    schoolClass: SchoolClass,
    viewModel: TimetableViewModel,
    onDismiss: () -> Unit
) {
    val uiState = viewModel.uiState.value
    val isBatchInitial = currentSlots.any { it.isBatchSplit }

    var isBatchSplit by remember { mutableStateOf(isBatchInitial) }

    // Whole Class State
    val firstSlot = currentSlots.firstOrNull()
    var selectedSubjectId by remember {
        mutableStateOf(firstSlot?.subjectId ?: uiState.subjects.firstOrNull()?.id ?: "")
    }
    var selectedTeacherId by remember {
        mutableStateOf(firstSlot?.primaryTeacherId ?: uiState.teachers.firstOrNull()?.id ?: "")
    }
    var hasSharedTeacher by remember {
        mutableStateOf(!firstSlot?.secondaryTeacherId.isNullOrBlank() && firstSlot?.secondaryTeacherId != "NONE")
    }
    var selectedSecondaryTeacherId by remember {
        mutableStateOf(firstSlot?.secondaryTeacherId ?: "")
    }
    var shareType by remember {
        mutableStateOf(firstSlot?.shareType ?: "ONE_OF_THREE")
    }
    var slotNote by remember {
        mutableStateOf(firstSlot?.note ?: "")
    }

    // Batch A & B State
    val slotA = currentSlots.find { it.batchName == "ബാച്ച് A" || it.batchName == "Batch A" } ?: firstSlot
    val slotB = currentSlots.find { it.batchName == "ബാച്ച് B" || it.batchName == "Batch B" }

    var batchASubjectId by remember {
        mutableStateOf(slotA?.subjectId ?: uiState.subjects.firstOrNull()?.id ?: "")
    }
    var batchATeacherId by remember {
        mutableStateOf(slotA?.primaryTeacherId ?: uiState.teachers.firstOrNull()?.id ?: "")
    }
    var batchBSubjectId by remember {
        mutableStateOf(slotB?.subjectId ?: uiState.subjects.getOrNull(1)?.id ?: uiState.subjects.firstOrNull()?.id ?: "")
    }
    var batchBTeacherId by remember {
        mutableStateOf(slotB?.primaryTeacherId ?: uiState.teachers.getOrNull(1)?.id ?: uiState.teachers.firstOrNull()?.id ?: "")
    }

    // Conflict Report
    val conflictReport by remember(
        isBatchSplit, selectedTeacherId, selectedSecondaryTeacherId, hasSharedTeacher,
        batchATeacherId, batchBTeacherId
    ) {
        derivedStateOf {
            if (!isBatchSplit) {
                viewModel.validateSlot(
                    dayOfWeek = dayOfWeek,
                    periodIndex = periodIndex,
                    primaryTeacherId = selectedTeacherId,
                    secondaryTeacherId = if (hasSharedTeacher) selectedSecondaryTeacherId else null,
                    excludeSlotId = firstSlot?.id
                )
            } else {
                val reportA = viewModel.validateSlot(
                    dayOfWeek = dayOfWeek,
                    periodIndex = periodIndex,
                    primaryTeacherId = batchATeacherId,
                    secondaryTeacherId = null,
                    excludeSlotId = slotA?.id
                )
                if (reportA.hasConflict && reportA.isSevere) reportA
                else {
                    viewModel.validateSlot(
                        dayOfWeek = dayOfWeek,
                        periodIndex = periodIndex,
                        primaryTeacherId = batchBTeacherId,
                        secondaryTeacherId = null,
                        excludeSlotId = slotB?.id
                    )
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .testTag("slot_edit_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "പിരീഡ് $periodIndex - ${getDayNameMalayalam(dayOfWeek)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = schoolClass.malayalamName.ifBlank { schoolClass.name },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Conflict Warning Banner
                item {
                    if (conflictReport.hasConflict) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (conflictReport.isSevere) Color(0xFFFEE2E2) else Color(0xFFFEF3C7)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (conflictReport.isSevere) Color(0xFFDC2626) else Color(0xFFD97706),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = conflictReport.messageMl,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (conflictReport.isSevere) Color(0xFF991B1B) else Color(0xFF92400E)
                                    )
                                    Text(
                                        text = conflictReport.messageEn,
                                        fontSize = 11.sp,
                                        color = if (conflictReport.isSevere) Color(0xFFB91C1C) else Color(0xFFB45309)
                                    )
                                }
                            }
                        }
                    }
                }

                // Batch Mode Toggle
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "രണ്ട് ബാച്ചുകളായി തിരിക്കുക",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "ഒരേ സമയം രണ്ട് വിഷയങ്ങൾ (Batch A & B)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = isBatchSplit,
                                onCheckedChange = { isBatchSplit = it },
                                modifier = Modifier.testTag("batch_split_switch")
                            )
                        }
                    }
                }

                if (!isBatchSplit) {
                    // --- WHOLE CLASS MODE ---
                    item {
                        Text(
                            text = "വിഷയം തിരഞ്ഞെടുക്കുക (Subject)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        SubjectDropdown(
                            subjects = uiState.subjects,
                            selectedId = selectedSubjectId,
                            onSelect = { selectedSubjectId = it }
                        )
                    }

                    item {
                        Text(
                            text = "പ്രധാന അധ്യാപകൻ (Primary Teacher)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TeacherDropdownWithStatus(
                            teachers = uiState.teachers,
                            selectedId = selectedTeacherId,
                            dayOfWeek = dayOfWeek,
                            periodIndex = periodIndex,
                            viewModel = viewModel,
                            excludeSlotId = firstSlot?.id,
                            onSelect = { selectedTeacherId = it }
                        )
                    }

                    // Shared Teacher Option
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "ഷെയർ ക്ലാസ് / കോ-ടീച്ചർ",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "3-ൽ ഒരു പിരീഡ് രണ്ടാമതൊരു അധ്യാപകൻ എടുക്കുന്നു",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = hasSharedTeacher,
                                        onCheckedChange = { hasSharedTeacher = it },
                                        modifier = Modifier.testTag("shared_teacher_switch")
                                    )
                                }

                                AnimatedVisibility(visible = hasSharedTeacher) {
                                    Column(modifier = Modifier.padding(top = 10.dp)) {
                                        Text(
                                            text = "ഷെയർ ചെയ്യുന്ന അധ്യാപകൻ (Co-Teacher)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        TeacherDropdownWithStatus(
                                            teachers = uiState.teachers.filter { it.id != selectedTeacherId },
                                            selectedId = selectedSecondaryTeacherId,
                                            dayOfWeek = dayOfWeek,
                                            periodIndex = periodIndex,
                                            viewModel = viewModel,
                                            excludeSlotId = firstSlot?.id,
                                            onSelect = { selectedSecondaryTeacherId = it }
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "ഷെയർ രീതി (Share Pattern)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf(
                                                Pair("ONE_OF_THREE", "3-ൽ 1 പിരീഡ്"),
                                                Pair("ALTERNATE_WEEK", "ആഴ്ചവിട്ട്"),
                                                Pair("CO_TEACHING", "രണ്ടുപേരും")
                                            ).forEach { (typeKey, label) ->
                                                val isSelected = shareType == typeKey
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                                        .clickable { shareType = typeKey }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = label,
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = slotNote,
                            onValueChange = { slotNote = it },
                            label = { Text("കുറിപ്പ് / മുറി നമ്പർ (Optional Note)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                } else {
                    // --- BATCH SPLIT MODE ---
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "ബാച്ച് A (Batch A)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF6D28D9)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                SubjectDropdown(
                                    subjects = uiState.subjects,
                                    selectedId = batchASubjectId,
                                    onSelect = { batchASubjectId = it }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                TeacherDropdownWithStatus(
                                    teachers = uiState.teachers,
                                    selectedId = batchATeacherId,
                                    dayOfWeek = dayOfWeek,
                                    periodIndex = periodIndex,
                                    viewModel = viewModel,
                                    excludeSlotId = slotA?.id,
                                    onSelect = { batchATeacherId = it }
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "ബാച്ച് B (Batch B)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF047857)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                SubjectDropdown(
                                    subjects = uiState.subjects,
                                    selectedId = batchBSubjectId,
                                    onSelect = { batchBSubjectId = it }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                TeacherDropdownWithStatus(
                                    teachers = uiState.teachers,
                                    selectedId = batchBTeacherId,
                                    dayOfWeek = dayOfWeek,
                                    periodIndex = periodIndex,
                                    viewModel = viewModel,
                                    excludeSlotId = slotB?.id,
                                    onSelect = { batchBTeacherId = it }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val institutionId = uiState.selectedInstitutionId
                    val classId = schoolClass.id

                    // First clear any previous slots for this period
                    viewModel.clearPeriodSlots(classId, dayOfWeek, periodIndex)

                    if (!isBatchSplit) {
                        // Insert whole class slot
                        val slot = TimetableSlot(
                            id = firstSlot?.id ?: UUID.randomUUID().toString(),
                            institutionId = institutionId,
                            classId = classId,
                            dayOfWeek = dayOfWeek,
                            periodIndex = periodIndex,
                            isBatchSplit = false,
                            batchName = null,
                            subjectId = selectedSubjectId,
                            primaryTeacherId = selectedTeacherId,
                            secondaryTeacherId = if (hasSharedTeacher) selectedSecondaryTeacherId else null,
                            shareType = if (hasSharedTeacher) shareType else "NONE",
                            note = slotNote
                        )
                        viewModel.saveSlot(slot)
                    } else {
                        // Insert Batch A
                        val slot1 = TimetableSlot(
                            id = slotA?.id ?: UUID.randomUUID().toString(),
                            institutionId = institutionId,
                            classId = classId,
                            dayOfWeek = dayOfWeek,
                            periodIndex = periodIndex,
                            isBatchSplit = true,
                            batchName = "ബാച്ച് A",
                            subjectId = batchASubjectId,
                            primaryTeacherId = batchATeacherId,
                            note = "ബാച്ച് A ക്ലാസ്"
                        )
                        // Insert Batch B
                        val slot2 = TimetableSlot(
                            id = slotB?.id ?: UUID.randomUUID().toString(),
                            institutionId = institutionId,
                            classId = classId,
                            dayOfWeek = dayOfWeek,
                            periodIndex = periodIndex,
                            isBatchSplit = true,
                            batchName = "ബാച്ച് B",
                            subjectId = batchBSubjectId,
                            primaryTeacherId = batchBTeacherId,
                            note = "ബാച്ച് B ക്ലാസ്"
                        )
                        viewModel.saveSlot(slot1)
                        viewModel.saveSlot(slot2)
                    }
                    onDismiss()
                },
                modifier = Modifier.testTag("save_slot_button")
            ) {
                Text("സേവ് ചെയ്യുക (Save)")
            }
        },
        dismissButton = {
            Row {
                if (currentSlots.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            viewModel.clearPeriodSlots(schoolClass.id, dayOfWeek, periodIndex)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("delete_slot_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                OutlinedButton(onClick = onDismiss) {
                    Text("ക്യാൻസൽ")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDropdown(
    subjects: List<Subject>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentSubject = subjects.find { it.id == selectedId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = currentSubject?.malayalamName?.ifBlank { currentSubject.name } ?: "വിഷയം തിരഞ്ഞെടുക്കുക",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            subjects.forEach { subject ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(subject.malayalamName.ifBlank { subject.name }, fontWeight = FontWeight.Medium)
                            if (subject.name.isNotBlank() && subject.name != subject.malayalamName) {
                                Text(subject.name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    onClick = {
                        onSelect(subject.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDropdownWithStatus(
    teachers: List<Teacher>,
    selectedId: String,
    dayOfWeek: Int,
    periodIndex: Int,
    viewModel: TimetableViewModel,
    excludeSlotId: String?,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentTeacher = teachers.find { it.id == selectedId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = currentTeacher?.malayalamName?.ifBlank { currentTeacher.name } ?: "അധ്യാപകനെ തിരഞ്ഞെടുക്കുക",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            teachers.forEach { teacher ->
                val status = viewModel.getTeacherAvailabilityStatus(
                    teacher = teacher,
                    dayOfWeek = dayOfWeek,
                    periodIndex = periodIndex,
                    excludeSlotId = excludeSlotId
                )

                DropdownMenuItem(
                    text = {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = teacher.malayalamName.ifBlank { teacher.name },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            StatusBadgeView(status = status)
                        }
                    },
                    onClick = {
                        onSelect(teacher.id)
                        expanded = false
                    }
                )
                Divider()
            }
        }
    }
}
