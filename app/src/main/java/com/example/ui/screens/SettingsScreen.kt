package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayConfig
import com.example.data.model.Institution
import com.example.ui.components.BilingualText
import com.example.ui.components.InstitutionHeaderSwitcher
import com.example.ui.components.getDayNameMalayalam
import com.example.ui.viewmodel.TimetableViewModel

@Composable
fun SettingsScreen(
    viewModel: TimetableViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var editingInstitution by remember { mutableStateOf<Institution?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val days = listOf(1, 2, 3, 4, 5, 6, 7) // Mon to Sun

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Header
        BilingualText(
            ml = "ആപ്പ് ക്രമീകരണങ്ങൾ",
            en = "Settings & Day Period Setup",
            titleSize = 18,
            subSize = 12,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "ദിവസങ്ങൾക്കനുസരിച്ച് പിരീഡുകളുടെ എണ്ണവും സ്ഥാപനങ്ങളുടെ പേരും സജ്ജമാക്കാം.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Institution Switcher
        InstitutionHeaderSwitcher(
            institutions = uiState.institutions,
            selectedId = uiState.selectedInstitutionId,
            onSelect = { viewModel.selectInstitution(it) },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val selectedInst = uiState.institutions.find { it.id == uiState.selectedInstitutionId }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Day-wise Period Count Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "ദിവസങ്ങൾ അനുസരിച്ചുള്ള പിരീഡ് എണ്ണം",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${selectedInst?.malayalamName ?: selectedInst?.name} - പിരീഡ് എണ്ണം ക്രമീകരിക്കുക",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        days.forEach { dayOfWeek ->
                            val config = uiState.dayConfigs.find {
                                it.institutionId == uiState.selectedInstitutionId && it.dayOfWeek == dayOfWeek
                            } ?: DayConfig(
                                id = "${uiState.selectedInstitutionId}_$dayOfWeek",
                                institutionId = uiState.selectedInstitutionId,
                                dayOfWeek = dayOfWeek,
                                isActive = dayOfWeek != 7,
                                periodCount = if (dayOfWeek == 5) 4 else if (dayOfWeek == 7) 0 else 6
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = getDayNameMalayalam(dayOfWeek),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (config.isActive) "${config.periodCount} പിരീഡുകൾ" else "അവധി ദിനം (Off / Holiday)",
                                        fontSize = 11.sp,
                                        color = if (config.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (config.isActive) {
                                        // Minus Button
                                        IconButton(
                                            onClick = {
                                                val newCount = (config.periodCount - 1).coerceAtLeast(1)
                                                viewModel.updateDayConfig(
                                                    institutionId = uiState.selectedInstitutionId,
                                                    dayOfWeek = dayOfWeek,
                                                    periodCount = newCount,
                                                    isActive = true
                                                )
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Minus",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Text(
                                            text = "${config.periodCount}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )

                                        // Plus Button
                                        IconButton(
                                            onClick = {
                                                val newCount = (config.periodCount + 1).coerceAtMost(10)
                                                viewModel.updateDayConfig(
                                                    institutionId = uiState.selectedInstitutionId,
                                                    dayOfWeek = dayOfWeek,
                                                    periodCount = newCount,
                                                    isActive = true
                                                )
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Plus",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Active Switch
                                    Switch(
                                        checked = config.isActive,
                                        onCheckedChange = { active ->
                                            viewModel.updateDayConfig(
                                                institutionId = uiState.selectedInstitutionId,
                                                dayOfWeek = dayOfWeek,
                                                periodCount = if (active) (if (dayOfWeek == 5) 4 else 6) else 0,
                                                isActive = active
                                            )
                                        },
                                        modifier = Modifier.testTag("day_switch_$dayOfWeek")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Institutions Names Configuration
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "സ്ഥാപനങ്ങളുടെ പേരുകൾ (Colleges)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        uiState.institutions.forEach { inst ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = inst.malayalamName.ifBlank { inst.name },
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${inst.name} (${inst.code})",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(onClick = { editingInstitution = inst }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Institution",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Share & Export Timetable
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ടൈംടേബിൾ പങ്കുവെക്കുക (Share / Export)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = "വാട്സ്ആപ്പിലേക്കോ പ്രിന്റ് ചെയ്യാനോ അനുയോജ്യമായ രീതിയിൽ ടൈംടേബിൾ ഷെയർ ചെയ്യാം.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                        Button(
                            onClick = {
                                val currentClass = uiState.classes.find { it.id == uiState.selectedClassId }
                                val shareText = buildTimetableShareText(
                                    institution = selectedInst,
                                    schoolClass = currentClass,
                                    slots = uiState.slots.filter { it.classId == uiState.selectedClassId },
                                    teachers = uiState.teachers,
                                    subjects = uiState.subjects
                                )
                                shareTimetable(context, shareText)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("share_timetable_button")
                        ) {
                            Text("ടൈംടേബിൾ ഷെയർ ചെയ്യുക")
                        }
                    }
                }
            }

            // 4. Reset to Demo / Initial Sample Data
            item {
                OutlinedButton(
                    onClick = { showResetConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_data_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("സാമ്പിൾ ഡാറ്റ വീണ്ടും സജ്ജമാക്കുക (Reset Sample Data)")
                }
            }
        }
    }

    // Edit Institution Dialog
    if (editingInstitution != null) {
        val inst = editingInstitution!!
        var nameEn by remember { mutableStateOf(inst.name) }
        var nameMl by remember { mutableStateOf(inst.malayalamName) }
        var code by remember { mutableStateOf(inst.code) }

        AlertDialog(
            onDismissRequest = { editingInstitution = null },
            title = {
                Text(text = "സ്ഥാപനത്തിന്റെ പേര് മാറ്റുക", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameMl,
                        onValueChange = { nameMl = it },
                        label = { Text("സ്ഥാപനത്തിന്റെ പേര് (മലയാളം)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        label = { Text("Institution Name (English)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("കോഡ് (Code)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateInstitution(
                            inst.copy(name = nameEn, malayalamName = nameMl, code = code)
                        )
                        editingInstitution = null
                    }
                ) {
                    Text("സേവ് ചെയ്യുക")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { editingInstitution = null }) {
                    Text("ക്യാൻസൽ")
                }
            }
        )
    }

    // Reset Confirm Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("ഡാറ്റ റീസെറ്റ് ചെയ്യണോ?", fontWeight = FontWeight.Bold) },
            text = { Text("മുഴുവൻ ഡാറ്റയും ഡെമോ സാമ്പിൾ ഡാറ്റയിലേക്ക് പുനഃസ്ഥാപിക്കപ്പെടും.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetToDefaults()
                        showResetConfirmDialog = false
                        Toast.makeText(context, "സാമ്പിൾ ഡാറ്റ പുനഃസ്ഥാപിച്ചു!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("റീസെറ്റ് ചെയ്യുക")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirmDialog = false }) {
                    Text("വേണ്ട")
                }
            }
        )
    }
}

private fun buildTimetableShareText(
    institution: Institution?,
    schoolClass: com.example.data.model.SchoolClass?,
    slots: List<com.example.data.model.TimetableSlot>,
    teachers: List<com.example.data.model.Teacher>,
    subjects: List<com.example.data.model.Subject>
): String {
    val builder = StringBuilder()
    builder.append("📅 ${institution?.malayalamName ?: "കോളേജ്"} - ടൈംടേബിൾ\n")
    builder.append("🏫 ക്ലാസ്: ${schoolClass?.malayalamName ?: schoolClass?.name ?: ""}\n")
    builder.append("===============================\n\n")

    val days = listOf(1, 2, 3, 4, 5, 6)
    days.forEach { day ->
        val daySlots = slots.filter { it.dayOfWeek == day }.sortedBy { it.periodIndex }
        if (daySlots.isNotEmpty()) {
            builder.append("📌 ${getDayNameMalayalam(day)}:\n")
            daySlots.forEach { slot ->
                val sub = subjects.find { it.id == slot.subjectId }
                val teacher = teachers.find { it.id == slot.primaryTeacherId }
                if (slot.isBatchSplit) {
                    builder.append("  • P${slot.periodIndex} [${slot.batchName}]: ${sub?.malayalamName ?: sub?.name} (${teacher?.malayalamName ?: teacher?.name})\n")
                } else {
                    builder.append("  • P${slot.periodIndex}: ${sub?.malayalamName ?: sub?.name} - ${teacher?.malayalamName ?: teacher?.name}")
                    if (!slot.secondaryTeacherId.isNullOrBlank() && slot.secondaryTeacherId != "NONE") {
                        val secT = teachers.find { it.id == slot.secondaryTeacherId }
                        builder.append(" (ഷെയർ: ${secT?.malayalamName ?: secT?.name})")
                    }
                    builder.append("\n")
                }
            }
            builder.append("\n")
        }
    }
    return builder.toString()
}

private fun shareTimetable(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "ടൈംടേബിൾ ഷെയർ ചെയ്യുക")
    context.startActivity(shareIntent)
}
