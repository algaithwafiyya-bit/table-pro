package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayConfig
import com.example.data.model.SchoolClass
import com.example.data.model.Subject
import com.example.data.model.Teacher
import com.example.data.model.TimetableSlot
import com.example.ui.components.BatchTag
import com.example.ui.components.InstitutionHeaderSwitcher
import com.example.ui.components.SharedTag
import com.example.ui.components.getDayNameMalayalam
import com.example.ui.components.getDayShortMalayalam
import com.example.ui.viewmodel.TimetableViewMode
import com.example.ui.viewmodel.TimetableViewModel

@Composable
fun TimetableScreen(
    viewModel: TimetableViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var activeDialogSlotPeriod by remember { mutableStateOf<Int?>(null) }
    var activeDialogSlotDay by remember { mutableStateOf<Int?>(null) }

    val currentClass = uiState.classes.find { it.id == uiState.selectedClassId }
    val classesForInstitution = uiState.classes.filter { it.institutionId == uiState.selectedInstitutionId }

    // Day Config for selected day
    val currentDayConfig = uiState.dayConfigs.find {
        it.institutionId == uiState.selectedInstitutionId && it.dayOfWeek == uiState.selectedDayOfWeek
    } ?: DayConfig(
        id = "default",
        institutionId = uiState.selectedInstitutionId,
        dayOfWeek = uiState.selectedDayOfWeek,
        isActive = uiState.selectedDayOfWeek != 7,
        periodCount = if (uiState.selectedDayOfWeek == 5) 4 else if (uiState.selectedDayOfWeek == 7) 0 else 6
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // 1. Institution Switcher Header
        InstitutionHeaderSwitcher(
            institutions = uiState.institutions,
            selectedId = uiState.selectedInstitutionId,
            onSelect = { viewModel.selectInstitution(it) },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 2. Class Selector Chips
        if (classesForInstitution.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ക്ലാസ്:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(classesForInstitution) { cls ->
                        val isSelected = cls.id == uiState.selectedClassId
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectClass(cls.id) },
                            label = {
                                Text(
                                    cls.malayalamName.ifBlank { cls.name },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("class_chip_${cls.id}")
                        )
                    }
                }
            }
        }

        // 3. View Mode Toggle (Day List / Weekly Grid / Teacher View)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ടൈംടേബിൾ ഷെഡ്യൂൾ",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { viewModel.setViewMode(TimetableViewMode.DAY_LIST) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (uiState.viewMode == TimetableViewMode.DAY_LIST) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewAgenda,
                        contentDescription = "Day List",
                        tint = if (uiState.viewMode == TimetableViewMode.DAY_LIST) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.setViewMode(TimetableViewMode.WEEK_GRID) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (uiState.viewMode == TimetableViewMode.WEEK_GRID) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarViewMonth,
                        contentDescription = "Weekly Grid",
                        tint = if (uiState.viewMode == TimetableViewMode.WEEK_GRID) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 4. Day Tabs (Mon to Sat)
        if (uiState.viewMode == TimetableViewMode.DAY_LIST) {
            val days = listOf(1, 2, 3, 4, 5, 6)
            ScrollableTabRow(
                selectedTabIndex = days.indexOf(uiState.selectedDayOfWeek).coerceAtLeast(0),
                edgePadding = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                indicator = { tabPositions ->
                    val index = days.indexOf(uiState.selectedDayOfWeek)
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
                    val isSelected = uiState.selectedDayOfWeek == day
                    val dayCfg = uiState.dayConfigs.find {
                        it.institutionId == uiState.selectedInstitutionId && it.dayOfWeek == day
                    }
                    val periodCnt = dayCfg?.periodCount ?: if (day == 5) 4 else 6

                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.selectDay(day) },
                        modifier = Modifier.testTag("day_tab_$day")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = getDayShortMalayalam(day),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$periodCnt പിരീഡ്",
                                fontSize = 10.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 5. Main Content Area
        if (currentClass == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ക്ലാസുകൾ ലഭ്യമല്ല",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "ദയവായി 'ക്ലാസുകൾ' ടാബിൽ നിന്നും ഒരു ക്ലാസ് ചേർക്കുക.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            when (uiState.viewMode) {
                TimetableViewMode.DAY_LIST -> {
                    DayScheduleView(
                        dayOfWeek = uiState.selectedDayOfWeek,
                        periodCount = currentDayConfig.periodCount,
                        isActive = currentDayConfig.isActive,
                        schoolClass = currentClass,
                        slots = uiState.slots.filter {
                            it.classId == currentClass.id && it.dayOfWeek == uiState.selectedDayOfWeek
                        },
                        teachers = uiState.teachers,
                        subjects = uiState.subjects,
                        onEditPeriod = { period ->
                            activeDialogSlotPeriod = period
                            activeDialogSlotDay = uiState.selectedDayOfWeek
                        }
                    )
                }
                TimetableViewMode.WEEK_GRID -> {
                    WeekGridView(
                        schoolClass = currentClass,
                        dayConfigs = uiState.dayConfigs.filter { it.institutionId == uiState.selectedInstitutionId },
                        slots = uiState.slots.filter { it.classId == currentClass.id },
                        teachers = uiState.teachers,
                        subjects = uiState.subjects,
                        onEditPeriod = { day, period ->
                            activeDialogSlotDay = day
                            activeDialogSlotPeriod = period
                        }
                    )
                }
                TimetableViewMode.TEACHER_VIEW -> {
                    // Handled inside DayScheduleView / Grid
                }
            }
        }
    }

    // Slot Edit Dialog
    if (activeDialogSlotPeriod != null && activeDialogSlotDay != null && currentClass != null) {
        val period = activeDialogSlotPeriod!!
        val day = activeDialogSlotDay!!
        val periodSlots = uiState.slots.filter {
            it.classId == currentClass.id && it.dayOfWeek == day && it.periodIndex == period
        }

        SlotEditDialog(
            dayOfWeek = day,
            periodIndex = period,
            currentSlots = periodSlots,
            schoolClass = currentClass,
            viewModel = viewModel,
            onDismiss = {
                activeDialogSlotPeriod = null
                activeDialogSlotDay = null
            }
        )
    }
}

@Composable
fun DayScheduleView(
    dayOfWeek: Int,
    periodCount: Int,
    isActive: Boolean,
    schoolClass: SchoolClass,
    slots: List<TimetableSlot>,
    teachers: List<Teacher>,
    subjects: List<Subject>,
    onEditPeriod: (Int) -> Unit
) {
    if (!isActive || periodCount == 0) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ഈ ദിവസം അവധിയാണ് (Holiday / Off)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "ക്രമീകരണങ്ങളിൽ നിന്ന് പിരീഡുകൾ ക്രമീകരിക്കാം.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(periodCount) { index ->
            val periodIndex = index + 1
            val periodSlots = slots.filter { it.periodIndex == periodIndex }

            PeriodCardView(
                periodIndex = periodIndex,
                slots = periodSlots,
                teachers = teachers,
                subjects = subjects,
                onClick = { onEditPeriod(periodIndex) }
            )
        }
    }
}

@Composable
fun PeriodCardView(
    periodIndex: Int,
    slots: List<TimetableSlot>,
    teachers: List<Teacher>,
    subjects: List<Subject>,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("period_card_$periodIndex"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Period Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "P$periodIndex",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "പിരീഡ്",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Center details
            if (slots.isEmpty()) {
                // Empty Slot
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ക്ലാസ് അലോക്കേറ്റ് ചെയ്തിട്ടില്ല",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "+ ടൈംടേബിൾ ചേർക്കാൻ ഇവിടെ ടാപ്പ് ചെയ്യുക",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (slots.any { it.isBatchSplit }) {
                // Dual Batches in same period
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ബാച്ച് ക്ലാസുകൾ (Dual Batch Period)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6D28D9)
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    slots.forEach { slot ->
                        val sub = subjects.find { it.id == slot.subjectId }
                        val teacher = teachers.find { it.id == slot.primaryTeacherId }
                        val isBatchA = slot.batchName?.contains("A") == true

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isBatchA) Color(0xFFF5F3FF) else Color(0xFFECFDF5))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BatchTag(slot.batchName ?: "ബാച്ച്")
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sub?.malayalamName ?: sub?.name ?: "വിഷയം",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = teacher?.malayalamName ?: teacher?.name ?: "അധ്യാപകൻ",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                // Standard Whole Class Slot
                val slot = slots.first()
                val sub = subjects.find { it.id == slot.subjectId }
                val primaryTeacher = teachers.find { it.id == slot.primaryTeacherId }
                val secTeacher = teachers.find { it.id == slot.secondaryTeacherId }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sub?.malayalamName?.ifBlank { sub.name } ?: "വിഷയം",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = primaryTeacher?.malayalamName?.ifBlank { primaryTeacher.name } ?: "അധ്യാപകൻ",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Shared Teacher badge if exists
                    if (secTeacher != null && slot.secondaryTeacherId != "NONE") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SharedTag(
                                text = when (slot.shareType) {
                                    "ONE_OF_THREE" -> "3-ൽ 1 പിരീഡ് ഷെയർ"
                                    "ALTERNATE_WEEK" -> "ആഴ്ചവിട്ട് ഷെയർ"
                                    else -> "കോ-ടീച്ചിംഗ്"
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ഷെയർ: ${secTeacher.malayalamName.ifBlank { secTeacher.name }}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (slot.note.isNotBlank()) {
                        Text(
                            text = "കുറിപ്പ്: ${slot.note}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeekGridView(
    schoolClass: SchoolClass,
    dayConfigs: List<DayConfig>,
    slots: List<TimetableSlot>,
    teachers: List<Teacher>,
    subjects: List<Subject>,
    onEditPeriod: (Int, Int) -> Unit
) {
    val days = listOf(1, 2, 3, 4, 5, 6)
    val maxPeriods = dayConfigs.maxOfOrNull { it.periodCount } ?: 6

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
            .horizontalScroll(scrollState)
    ) {
        // Table Header: Days
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("പിരീഡ്", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            days.forEach { day ->
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getDayShortMalayalam(day),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Table Rows: Periods
        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            items(maxPeriods) { pIdx ->
                val periodIndex = pIdx + 1
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    // Period Header
                    Box(
                        modifier = Modifier
                            .width(70.dp)
                            .height(80.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "P$periodIndex",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Days Cells
                    days.forEach { day ->
                        val dayCfg = dayConfigs.find { it.dayOfWeek == day }
                        val isDayActive = dayCfg?.isActive ?: (day != 7)
                        val periodLimit = dayCfg?.periodCount ?: if (day == 5) 4 else 6

                        val cellSlots = slots.filter { it.dayOfWeek == day && it.periodIndex == periodIndex }

                        Box(
                            modifier = Modifier
                                .width(130.dp)
                                .height(80.dp)
                                .padding(horizontal = 3.dp)
                        ) {
                            if (!isDayActive || periodIndex > periodLimit) {
                                // Off period
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("—", color = Color.Gray)
                                }
                            } else {
                                OutlinedCard(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { onEditPeriod(day, periodIndex) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.outlinedCardColors(
                                        containerColor = if (cellSlots.isNotEmpty()) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (cellSlots.isEmpty()) {
                                            Text(
                                                text = "+ ചേർക്കുക",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        } else if (cellSlots.any { it.isBatchSplit }) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                BatchTag("ബാച്ച് A & B")
                                                Text(
                                                    text = "2 വിഷയങ്ങൾ",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        } else {
                                            val slot = cellSlots.first()
                                            val sub = subjects.find { it.id == slot.subjectId }
                                            val teacher = teachers.find { it.id == slot.primaryTeacherId }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = sub?.malayalamName?.take(10) ?: sub?.name?.take(10) ?: "വിഷയം",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = teacher?.malayalamName?.take(10) ?: teacher?.name?.take(10) ?: "അധ്യാപകൻ",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
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
        }
    }
}
