package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Institution
import com.example.data.model.SchoolClass
import com.example.data.model.Subject
import com.example.ui.components.BatchTag
import com.example.ui.components.BilingualText
import com.example.ui.components.InstitutionHeaderSwitcher
import com.example.ui.viewmodel.TimetableViewModel
import java.util.UUID

@Composable
fun ClassesSubjectsScreen(
    viewModel: TimetableViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) } // 0 = Classes, 1 = Subjects

    var showAddClassDialog by remember { mutableStateOf(false) }
    var editingClass by remember { mutableStateOf<SchoolClass?>(null) }

    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<Subject?>(null) }

    val classesForSelectedInst = uiState.classes.filter { it.institutionId == uiState.selectedInstitutionId }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (selectedTabIndex == 0) {
                        editingClass = null
                        showAddClassDialog = true
                    } else {
                        editingSubject = null
                        showAddSubjectDialog = true
                    }
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text(if (selectedTabIndex == 0) "ക്ലാസ് ചേർക്കുക" else "വിഷയം ചേർക്കുക") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_class_subject_fab")
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
                ml = "ക്ലാസുകളും വിഷയങ്ങളും",
                en = "Classes & Subjects Management",
                titleSize = 18,
                subSize = 12,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Switcher for Institution
            InstitutionHeaderSwitcher(
                institutions = uiState.institutions,
                selectedId = uiState.selectedInstitutionId,
                onSelect = { viewModel.selectInstitution(it) },
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Tabs: Classes / Subjects
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("ക്ലാസുകൾ (${classesForSelectedInst.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("വിഷയങ്ങൾ (${uiState.subjects.size})", fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedTabIndex == 0) {
                // --- CLASSES TAB ---
                if (classesForSelectedInst.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "ക്ലാസുകൾ ചേർത്തിട്ടില്ല",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "താഴെയുള്ള + ബട്ടൺ ഉപയോഗിച്ച് പുതിയ ക്ലാസ് ചേർക്കാം.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(classesForSelectedInst) { cls ->
                            val slotCount = uiState.slots.count { it.classId == cls.id }

                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("class_card_${cls.id}"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Class,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cls.malayalamName.ifBlank { cls.name },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        if (cls.name.isNotBlank() && cls.name != cls.malayalamName) {
                                            Text(
                                                text = cls.name,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (cls.hasBatches) {
                                                BatchTag("ബാച്ച് സൗകര്യം ഉണ്ട് (${cls.batchNames})")
                                            } else {
                                                Text(
                                                    text = "സിംഗിൾ ബാച്ച്",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Text(
                                                text = "• $slotCount പിരീഡുകൾ",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Row {
                                        IconButton(onClick = {
                                            editingClass = cls
                                            showAddClassDialog = true
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        IconButton(onClick = { viewModel.deleteClass(cls.id) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // --- SUBJECTS TAB ---
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.subjects) { sub ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("subject_card_${sub.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE0F2FE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Book,
                                        contentDescription = null,
                                        tint = Color(0xFF0369A1)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sub.malayalamName.ifBlank { sub.name },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Row {
                                        if (sub.name.isNotBlank() && sub.name != sub.malayalamName) {
                                            Text(
                                                text = "${sub.name} ",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (sub.code.isNotBlank()) {
                                            Text(
                                                text = "(${sub.code})",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                Row {
                                    IconButton(onClick = {
                                        editingSubject = sub
                                        showAddSubjectDialog = true
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(onClick = { viewModel.deleteSubject(sub.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Class Dialog
    if (showAddClassDialog) {
        AddEditClassDialog(
            schoolClass = editingClass,
            institutions = uiState.institutions,
            currentInstitutionId = uiState.selectedInstitutionId,
            onDismiss = { showAddClassDialog = false },
            onSave = { cls ->
                viewModel.addOrUpdateClass(cls)
                showAddClassDialog = false
            }
        )
    }

    // Add / Edit Subject Dialog
    if (showAddSubjectDialog) {
        AddEditSubjectDialog(
            subject = editingSubject,
            onDismiss = { showAddSubjectDialog = false },
            onSave = { sub ->
                viewModel.addOrUpdateSubject(sub)
                showAddSubjectDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClassDialog(
    schoolClass: SchoolClass?,
    institutions: List<Institution>,
    currentInstitutionId: String,
    onDismiss: () -> Unit,
    onSave: (SchoolClass) -> Unit
) {
    var nameEn by remember { mutableStateOf(schoolClass?.name ?: "") }
    var nameMl by remember { mutableStateOf(schoolClass?.malayalamName ?: "") }
    var section by remember { mutableStateOf(schoolClass?.section ?: "A") }
    var hasBatches by remember { mutableStateOf(schoolClass?.hasBatches ?: false) }
    var batchNames by remember { mutableStateOf(schoolClass?.batchNames ?: "ബാച്ച് A,ബാച്ച് B") }
    var targetInstitutionId by remember { mutableStateOf(schoolClass?.institutionId ?: currentInstitutionId) }

    var expandedInst by remember { mutableStateOf(false) }
    val selectedInst = institutions.find { it.id == targetInstitutionId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (schoolClass == null) "പുതിയ ക്ലാസ് ചേർക്കുക" else "ക്ലാസ് മാറ്റങ്ങൾ വരുത്തുക",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Target Institution Selector
                Text(
                    text = "സ്ഥാപനം (Institution):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                ExposedDropdownMenuBox(
                    expanded = expandedInst,
                    onExpandedChange = { expandedInst = it }
                ) {
                    OutlinedTextField(
                        value = selectedInst?.malayalamName ?: selectedInst?.name ?: "സ്ഥാപനം തിരഞ്ഞെടുക്കുക",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInst) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedInst,
                        onDismissRequest = { expandedInst = false }
                    ) {
                        institutions.forEach { inst ->
                            DropdownMenuItem(
                                text = { Text(inst.malayalamName.ifBlank { inst.name }) },
                                onClick = {
                                    targetInstitutionId = inst.id
                                    expandedInst = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = nameMl,
                    onValueChange = { nameMl = it },
                    label = { Text("ക്ലാസിന്റെ പേര് (മലയാളം e.g. ഒന്നാം വർഷം)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("class_name_ml_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("Class Name (English e.g. 1st Year)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Batch Switch
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ബാച്ച് സംവിധാനം (Enable Batches)",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "ഒരേ ക്ലാസിൽ 2 വിഷയങ്ങൾ ഒന്നിച്ചു നടക്കാൻ",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = hasBatches,
                                onCheckedChange = { hasBatches = it },
                                modifier = Modifier.testTag("enable_batches_switch")
                            )
                        }

                        AnimatedVisibility(visible = hasBatches) {
                            OutlinedTextField(
                                value = batchNames,
                                onValueChange = { batchNames = it },
                                label = { Text("ബാച്ചുകളുടെ പേരുകൾ (Comma separated)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameMl.isNotBlank() || nameEn.isNotBlank()) {
                        onSave(
                            SchoolClass(
                                id = schoolClass?.id ?: UUID.randomUUID().toString(),
                                institutionId = targetInstitutionId,
                                name = nameEn.ifBlank { nameMl },
                                malayalamName = nameMl.ifBlank { nameEn },
                                section = section,
                                hasBatches = hasBatches,
                                batchNames = batchNames
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("save_class_button")
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

@Composable
fun AddEditSubjectDialog(
    subject: Subject?,
    onDismiss: () -> Unit,
    onSave: (Subject) -> Unit
) {
    var nameEn by remember { mutableStateOf(subject?.name ?: "") }
    var nameMl by remember { mutableStateOf(subject?.malayalamName ?: "") }
    var code by remember { mutableStateOf(subject?.code ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (subject == null) "പുതിയ വിഷയം ചേർക്കുക" else "വിഷയ മാറ്റങ്ങൾ വരുത്തുക",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nameMl,
                    onValueChange = { nameMl = it },
                    label = { Text("വിഷയത്തിന്റെ പേര് (മലയാളം e.g. തഫ്സീർ)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_name_ml_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("Subject Name (English)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("കോഡ് (Code e.g. TAF101)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameMl.isNotBlank() || nameEn.isNotBlank()) {
                        onSave(
                            Subject(
                                id = subject?.id ?: UUID.randomUUID().toString(),
                                institutionId = "ALL",
                                name = nameEn.ifBlank { nameMl },
                                malayalamName = nameMl.ifBlank { nameEn },
                                code = code
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("save_subject_button")
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
