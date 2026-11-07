package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.data.model.Teacher
import com.example.data.model.TeacherUnavailability
import com.example.domain.ConflictChecker
import com.example.ui.components.BilingualText
import com.example.ui.components.getDayNameMalayalam
import com.example.ui.components.getDayShortMalayalam
import com.example.ui.theme.StatusAvailableBg
import com.example.ui.theme.StatusAvailableText
import com.example.ui.theme.StatusClashBg
import com.example.ui.theme.StatusClashText
import com.example.ui.theme.StatusUnavailableBg
import com.example.ui.theme.StatusUnavailableText
import com.example.ui.viewmodel.TimetableViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilityScreen(
    viewModel: TimetableViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedTeacherId by remember {
        mutableStateOf(uiState.teachers.firstOrNull()?.id ?: "")
    }

    // Default to selected teacher or first teacher
    val currentTeacher = uiState.teachers.find { it.id == selectedTeacherId }
        ?: uiState.teachers.firstOrNull()

    var selectedDayOfWeek by remember { mutableStateOf(1) } // Mon = 1

    var showPeriodReasonDialogForPeriod by remember { mutableStateOf<Int?>(null) }
    var periodReasonText by remember { mutableStateOf("") }

    var showFullDayReasonDialog by remember { mutableStateOf(false) }
    var fullDayReasonText by remember { mutableStateOf("") }

    val days = listOf(1, 2, 3, 4, 5, 6)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Top Header
        BilingualText(
            ml = "അധ്യാപകരുടെ ലഭ്യത ക്രമീകരിക്കുക",
            en = "Teacher Availability Management",
            titleSize = 18,
            subSize = 12,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "ദിവസം മുഴുവനായോ ഓരോ പിരീഡുകൾ തിരിച്ചോ ലഭ്യത രേഖപ്പെടുത്താം.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Teacher Selection Chips
        if (uiState.teachers.isNotEmpty()) {
            Text(
                text = "അധ്യാപകൻ (Select Teacher):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                items(uiState.teachers) { teacher ->
                    val isSelected = teacher.id == (currentTeacher?.id ?: "")
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTeacherId = teacher.id },
                        label = {
                            Text(
                                teacher.malayalamName.ifBlank { teacher.name },
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("teacher_chip_${teacher.id}")
                    )
                }
            }
        }

        if (currentTeacher == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("അധ്യാപകരെ ചേർത്തിട്ടില്ല.")
            }
            return
        }

        // Day Selection Tabs
        ScrollableTabRow(
            selectedTabIndex = days.indexOf(selectedDayOfWeek).coerceAtLeast(0),
            edgePadding = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            indicator = { tabPositions ->
                val index = days.indexOf(selectedDayOfWeek)
                if (index in tabPositions.indices) {
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[index]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                }
            }
        ) {
            days.forEach { day ->
                val isSelected = selectedDayOfWeek == day
                // Check if teacher has full day off or any period off this day
                val hasAnyUnavailability = uiState.unavailabilities.any {
                    it.teacherId == currentTeacher.id && it.dayOfWeek == day
                }

                Tab(
                    selected = isSelected,
                    onClick = { selectedDayOfWeek = day },
                    modifier = Modifier.testTag("avail_day_tab_$day")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                    ) {
                        Text(
                            text = getDayShortMalayalam(day),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        if (hasAnyUnavailability) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error)
                            )
                        }
                    }
                }
            }
        }

        // Teacher Info Card with Institutions
        val teacherInstitutions = ConflictChecker.parseInstitutionIds(currentTeacher.institutionIdsJson)
        val instNames = uiState.institutions.filter { teacherInstitutions.contains(it.id) }
            .joinToString(", ") { it.malayalamName.ifBlank { it.name } }

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentTeacher.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentTeacher.malayalamName.ifBlank { currentTeacher.name },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "പഠിപ്പിക്കുന്ന കോളേജുകൾ: ${if (instNames.isNotBlank()) instNames else "എല്ലാ സ്ഥാപനങ്ങളും"}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Check full day unavailability
        val fullDayUnavailability = uiState.unavailabilities.find {
            it.teacherId == currentTeacher.id && it.dayOfWeek == selectedDayOfWeek && it.isFullDay
        }
        val isFullDayOff = fullDayUnavailability != null

        // 1. Full Day Toggle Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFullDayOff) StatusUnavailableBg else MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ദിവസം മുഴുവനും അവധി (Full Day Leave)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isFullDayOff) StatusUnavailableText else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isFullDayOff) "കാരണം: ${fullDayUnavailability?.reason}" else "ഈ ദിവസം മുഴുവൻ ലഭ്യമല്ലെങ്കിൽ ഓൺ ചെയ്യുക",
                        fontSize = 11.sp,
                        color = if (isFullDayOff) StatusUnavailableText.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isFullDayOff,
                    onCheckedChange = { checked ->
                        if (checked) {
                            showFullDayReasonDialog = true
                        } else {
                            if (fullDayUnavailability != null) {
                                viewModel.removeUnavailability(fullDayUnavailability.id)
                            }
                        }
                    },
                    modifier = Modifier.testTag("full_day_leave_switch")
                )
            }
        }

        // 2. Period-wise Availability
        Text(
            text = "പിരീഡ് തിരിച്ചുള്ള ലഭ്യത (${getDayNameMalayalam(selectedDayOfWeek)}):",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )

        val periodsCount = 6 // Standard periods

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(periodsCount) { idx ->
                val pIdx = idx + 1
                val periodUnavailability = uiState.unavailabilities.find {
                    it.teacherId == currentTeacher.id && it.dayOfWeek == selectedDayOfWeek &&
                    !it.isFullDay && it.periodIndex == pIdx
                }
                val isPeriodOff = periodUnavailability != null

                // Check if teacher has class scheduled anywhere
                val scheduledSlots = uiState.slots.filter {
                    it.dayOfWeek == selectedDayOfWeek && it.periodIndex == pIdx &&
                    (it.primaryTeacherId == currentTeacher.id || it.secondaryTeacherId == currentTeacher.id)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isPeriodOff) {
                                viewModel.removeUnavailability(periodUnavailability.id)
                            } else if (!isFullDayOff) {
                                showPeriodReasonDialogForPeriod = pIdx
                            }
                        }
                        .testTag("period_avail_row_$pIdx"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isFullDayOff -> StatusUnavailableBg.copy(alpha = 0.5f)
                            isPeriodOff -> StatusUnavailableBg
                            scheduledSlots.isNotEmpty() -> StatusClashBg
                            else -> StatusAvailableBg
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "P$pIdx",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            when {
                                isFullDayOff -> {
                                    Text(
                                        text = "മുഴുവൻ ദിവസം അവധി",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = StatusUnavailableText
                                    )
                                }
                                isPeriodOff -> {
                                    Text(
                                        text = "ലഭ്യമല്ല (Unavailable)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = StatusUnavailableText
                                    )
                                    Text(
                                        text = "കാരണം: ${periodUnavailability?.reason}",
                                        fontSize = 11.sp,
                                        color = StatusUnavailableText.copy(alpha = 0.8f)
                                    )
                                }
                                scheduledSlots.isNotEmpty() -> {
                                    val firstSlot = scheduledSlots.first()
                                    val inst = uiState.institutions.find { it.id == firstSlot.institutionId }
                                    val cls = uiState.classes.find { it.id == firstSlot.classId }
                                    Text(
                                        text = "ക്ലാസ് ഷെഡ്യൂൾ ചെയ്തിട്ടുണ്ട്",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = StatusClashText
                                    )
                                    Text(
                                        text = "${inst?.malayalamName ?: inst?.name} - ${cls?.malayalamName ?: cls?.name}",
                                        fontSize = 11.sp,
                                        color = StatusClashText.copy(alpha = 0.9f)
                                    )
                                }
                                else -> {
                                    Text(
                                        text = "ലഭ്യമാണ് (Available)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = StatusAvailableText
                                    )
                                    Text(
                                        text = "ടാപ്പ് ചെയ്ത് അവധിയായി രേഖപ്പെടുത്താം",
                                        fontSize = 11.sp,
                                        color = StatusAvailableText.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                        if (!isFullDayOff) {
                            if (isPeriodOff) {
                                IconButton(
                                    onClick = { viewModel.removeUnavailability(periodUnavailability.id) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = StatusUnavailableText
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (scheduledSlots.isNotEmpty()) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (scheduledSlots.isNotEmpty()) StatusClashText else StatusAvailableText
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for Full Day Reason
    if (showFullDayReasonDialog && currentTeacher != null) {
        AlertDialog(
            onDismissRequest = { showFullDayReasonDialog = false },
            title = {
                Text(
                    text = "മുഴുവൻ ദിവസം അവധി കാരണം",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "${currentTeacher.malayalamName.ifBlank { currentTeacher.name }} - ${getDayNameMalayalam(selectedDayOfWeek)}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = fullDayReasonText,
                        onValueChange = { fullDayReasonText = it },
                        label = { Text("അവധി കാരണം (Reason / Duty)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleUnavailability(
                            teacherId = currentTeacher.id,
                            dayOfWeek = selectedDayOfWeek,
                            isFullDay = true,
                            periodIndex = null,
                            reason = fullDayReasonText.ifBlank { "അവധി (Leave)" }
                        )
                        fullDayReasonText = ""
                        showFullDayReasonDialog = false
                    }
                ) {
                    Text("രേഖപ്പെടുത്തുക (Save)")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showFullDayReasonDialog = false }) {
                    Text("ക്യാൻസൽ")
                }
            }
        )
    }

    // Dialog for Period-wise Reason
    if (showPeriodReasonDialogForPeriod != null && currentTeacher != null) {
        val period = showPeriodReasonDialogForPeriod!!
        AlertDialog(
            onDismissRequest = { showPeriodReasonDialogForPeriod = null },
            title = {
                Text(
                    text = "പിരീഡ് $period ലഭ്യമല്ലെന്ന് രേഖപ്പെടുത്തുക",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "${currentTeacher.malayalamName.ifBlank { currentTeacher.name }} - ${getDayNameMalayalam(selectedDayOfWeek)}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = periodReasonText,
                        onValueChange = { periodReasonText = it },
                        label = { Text("ലഭ്യമല്ലാത്ത കാരണം (Reason / Other duty)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleUnavailability(
                            teacherId = currentTeacher.id,
                            dayOfWeek = selectedDayOfWeek,
                            isFullDay = false,
                            periodIndex = period,
                            reason = periodReasonText.ifBlank { "പിരീഡ് ലീവ് / ഡ്യൂട്ടി" }
                        )
                        periodReasonText = ""
                        showPeriodReasonDialogForPeriod = null
                    }
                ) {
                    Text("ലഭ്യമല്ലെന്ന് അടയാളപ്പെടുത്തുക")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPeriodReasonDialogForPeriod = null }) {
                    Text("ക്യാൻസൽ")
                }
            }
        )
    }
}
