package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.Institution
import com.example.data.model.Teacher
import com.example.domain.ConflictChecker
import com.example.ui.components.BilingualText
import com.example.ui.viewmodel.TimetableViewModel
import org.json.JSONArray
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeachersScreen(
    viewModel: TimetableViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var showTeacherDialog by remember { mutableStateOf(false) }
    var editingTeacher by remember { mutableStateOf<Teacher?>(null) }
    var selectedFilterInstitutionId by remember { mutableStateOf("ALL") }

    val filteredTeachers = uiState.teachers.filter { teacher ->
        if (selectedFilterInstitutionId == "ALL") true
        else {
            val instIds = ConflictChecker.parseInstitutionIds(teacher.institutionIdsJson)
            instIds.contains(selectedFilterInstitutionId)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingTeacher = null
                    showTeacherDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Teacher") },
                text = { Text("അധ്യാപകനെ ചേർക്കുക") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_teacher_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Header
            BilingualText(
                ml = "അധ്യാപകരുടെ വിവരങ്ങൾ",
                en = "Teachers Directory & Workload",
                titleSize = 18,
                subSize = 12,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "രണ്ട് സ്ഥാപനങ്ങളിലുമുള്ള അധ്യാപകരെ ചേർക്കാനും നിയന്ത്രിക്കാനും സാധിക്കും.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Filter Chips (All, Inst 1, Inst 2)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ഫിൽട്ടർ:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 8.dp)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = selectedFilterInstitutionId == "ALL",
                            onClick = { selectedFilterInstitutionId = "ALL" },
                            label = { Text("എല്ലാവരും (${uiState.teachers.size})") }
                        )
                    }
                    items(uiState.institutions) { inst ->
                        val count = uiState.teachers.count {
                            ConflictChecker.parseInstitutionIds(it.institutionIdsJson).contains(inst.id)
                        }
                        FilterChip(
                            selected = selectedFilterInstitutionId == inst.id,
                            onClick = { selectedFilterInstitutionId = inst.id },
                            label = { Text("${inst.malayalamName.ifBlank { inst.name }} ($count)") }
                        )
                    }
                }
            }

            // Teachers List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTeachers) { teacher ->
                    val teacherInstIds = ConflictChecker.parseInstitutionIds(teacher.institutionIdsJson)
                    val assignedInstitutions = uiState.institutions.filter { teacherInstIds.contains(it.id) }

                    // Count total assigned periods across all institutions
                    val totalSlots = uiState.slots.filter {
                        it.primaryTeacherId == teacher.id || it.secondaryTeacherId == teacher.id
                    }

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("teacher_card_${teacher.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = teacher.malayalamName.ifBlank { teacher.name },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    if (teacher.name.isNotBlank() && teacher.name != teacher.malayalamName) {
                                        Text(
                                            text = teacher.name,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (teacher.subjectSpecialization.isNotBlank()) {
                                        Text(
                                            text = teacher.subjectSpecialization,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Row {
                                    IconButton(onClick = {
                                        editingTeacher = teacher
                                        showTeacherDialog = true
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    IconButton(onClick = { viewModel.deleteTeacher(teacher.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Institution Badges
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                assignedInstitutions.forEach { inst ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.School,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = inst.malayalamName.ifBlank { inst.name },
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }

                                if (assignedInstitutions.size > 1) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFEF3C7))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "രണ്ട് സ്ഥാപനങ്ങളിലും (Both)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB45309)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Total Teaching Periods Summary
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ആകെ ഷെഡ്യൂൾ ചെയ്ത പിരീഡുകൾ:",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${totalSlots.size} പിരീഡ് / ആഴ്ച",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Teacher Dialog
    if (showTeacherDialog) {
        AddEditTeacherDialog(
            teacher = editingTeacher,
            institutions = uiState.institutions,
            onDismiss = { showTeacherDialog = false },
            onSave = { teacher ->
                viewModel.addOrUpdateTeacher(teacher)
                showTeacherDialog = false
            }
        )
    }
}

@Composable
fun AddEditTeacherDialog(
    teacher: Teacher?,
    institutions: List<Institution>,
    onDismiss: () -> Unit,
    onSave: (Teacher) -> Unit
) {
    var nameEn by remember { mutableStateOf(teacher?.name ?: "") }
    var nameMl by remember { mutableStateOf(teacher?.malayalamName ?: "") }
    var phone by remember { mutableStateOf(teacher?.phone ?: "") }
    var subjectSpecialization by remember { mutableStateOf(teacher?.subjectSpecialization ?: "") }

    val initialSelectedInstitutions = remember {
        if (teacher != null) {
            ConflictChecker.parseInstitutionIds(teacher.institutionIdsJson).toMutableList()
        } else {
            // Default to all institutions for convenience
            institutions.map { it.id }.toMutableList()
        }
    }
    val selectedInstitutionIds = remember { mutableStateListOf(*initialSelectedInstitutions.toTypedArray()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (teacher == null) "പുതിയ അധ്യാപകനെ ചേർക്കുക" else "അധ്യാപക വിവരങ്ങൾ പുതുക്കുക",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = nameMl,
                        onValueChange = { nameMl = it },
                        label = { Text("അധ്യാപകന്റെ പേര് (മലയാളം)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("teacher_name_ml_input"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        label = { Text("Teacher Name (English)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("teacher_name_en_input"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = subjectSpecialization,
                        onValueChange = { subjectSpecialization = it },
                        label = { Text("പ്രധാന വിഷയം (Specialization)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("ഫോൺ നമ്പർ (Phone)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Multi-Institution Selection
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "പഠിപ്പിക്കുന്ന കോളേജുകൾ തിരഞ്ഞെടുക്കുക:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "ചില അധ്യാപകർ രണ്ടു സ്ഥാപനത്തിലും പഠിപ്പിക്കുന്നുണ്ടെങ്കിൽ രണ്ടും ടിക്ക് ചെയ്യുക.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        institutions.forEach { inst ->
                            val isChecked = selectedInstitutionIds.contains(inst.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) {
                                            selectedInstitutionIds.remove(inst.id)
                                        } else {
                                            selectedInstitutionIds.add(inst.id)
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedInstitutionIds.add(inst.id)
                                        else selectedInstitutionIds.remove(inst.id)
                                    },
                                    modifier = Modifier.testTag("inst_check_${inst.id}")
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = inst.malayalamName.ifBlank { inst.name },
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                    if (inst.code.isNotBlank()) {
                                        Text(inst.code, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameMl.isNotBlank() || nameEn.isNotBlank()) {
                        val instJson = JSONArray(selectedInstitutionIds.toList()).toString()
                        val newTeacher = Teacher(
                            id = teacher?.id ?: UUID.randomUUID().toString(),
                            name = nameEn.ifBlank { nameMl },
                            malayalamName = nameMl.ifBlank { nameEn },
                            phone = phone,
                            subjectSpecialization = subjectSpecialization,
                            colorHex = teacher?.colorHex ?: "#1E40AF",
                            institutionIdsJson = instJson
                        )
                        onSave(newTeacher)
                    }
                },
                modifier = Modifier.testTag("save_teacher_button")
            ) {
                Text("സേവ് ചെയ്യുക")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("ക്യാൻസൽ")
            }
        }
    )
}
