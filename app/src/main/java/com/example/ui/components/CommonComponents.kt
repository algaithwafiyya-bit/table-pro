package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Institution
import com.example.domain.TeacherStatus
import com.example.ui.theme.BatchBadgeBg
import com.example.ui.theme.BatchBadgeText
import com.example.ui.theme.SharedBadgeBg
import com.example.ui.theme.SharedBadgeText
import com.example.ui.theme.StatusAvailableBg
import com.example.ui.theme.StatusAvailableText
import com.example.ui.theme.StatusClashBg
import com.example.ui.theme.StatusClashText
import com.example.ui.theme.StatusUnavailableBg
import com.example.ui.theme.StatusUnavailableText

@Composable
fun BilingualText(
    ml: String,
    en: String,
    modifier: Modifier = Modifier,
    isMlPrimary: Boolean = true,
    titleSize: Int = 16,
    subSize: Int = 12,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier = modifier) {
        val primary = if (isMlPrimary) ml.ifBlank { en } else en.ifBlank { ml }
        val secondary = if (isMlPrimary) en else ml

        Text(
            text = primary,
            fontSize = titleSize.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (secondary.isNotBlank() && secondary != primary) {
            Text(
                text = secondary,
                fontSize = subSize.sp,
                fontWeight = FontWeight.Normal,
                color = color.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun InstitutionHeaderSwitcher(
    institutions: List<Institution>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (institutions.isEmpty()) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("institution_switcher_surface"),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            institutions.forEach { inst ->
                val isSelected = inst.id == selectedId
                val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .clickable { onSelect(inst.id) }
                        .padding(vertical = 10.dp, horizontal = 12.dp)
                        .testTag("institution_tab_${inst.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = inst.malayalamName.ifBlank { inst.name },
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = contentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (inst.code.isNotBlank()) {
                                Text(
                                    text = inst.code,
                                    fontSize = 10.sp,
                                    color = contentColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadgeView(
    status: TeacherStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon, label) = when (status) {
        is TeacherStatus.Available -> {
            Tuple4(
                StatusAvailableBg,
                StatusAvailableText,
                Icons.Default.CheckCircle,
                "ലഭ്യമാണ് (Available)"
            )
        }
        is TeacherStatus.Unavailable -> {
            Tuple4(
                StatusUnavailableBg,
                StatusUnavailableText,
                Icons.Default.Error,
                "ലഭ്യമല്ല: ${status.reason}"
            )
        }
        is TeacherStatus.BusyOtherInstitution -> {
            Tuple4(
                StatusUnavailableBg,
                StatusUnavailableText,
                Icons.Default.Error,
                "മറ്റൊരു കോളേജിൽ ക്ലാസുണ്ട്: ${status.institutionName}"
            )
        }
        is TeacherStatus.BusyThisInstitution -> {
            Tuple4(
                StatusClashBg,
                StatusClashText,
                Icons.Default.Warning,
                "ഇതേ കോളേജിൽ ക്ലാസുണ്ട്: ${status.className}"
            )
        }
        is TeacherStatus.NotAssignedToInstitution -> {
            Tuple4(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.onSurfaceVariant,
                Icons.Default.Warning,
                "ഈ കോളേജിൽ ഉൾപ്പെട്ടിട്ടില്ല"
            )
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BatchTag(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = BatchBadgeText,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(BatchBadgeBg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun SharedTag(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = SharedBadgeText,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SharedBadgeBg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

fun getDayNameMalayalam(dayOfWeek: Int): String {
    return when (dayOfWeek) {
        1 -> "തിങ്കൾ (Mon)"
        2 -> "ചൊവ്വ (Tue)"
        3 -> "ബുധൻ (Wed)"
        4 -> "വ്യാഴം (Thu)"
        5 -> "വെള്ളി (Fri)"
        6 -> "ശനി (Sat)"
        7 -> "ഞായർ (Sun)"
        else -> "ദിവസം $dayOfWeek"
    }
}

fun getDayShortMalayalam(dayOfWeek: Int): String {
    return when (dayOfWeek) {
        1 -> "തിങ്കൾ"
        2 -> "ചൊവ്വ"
        3 -> "ബുധൻ"
        4 -> "വ്യാഴം"
        5 -> "വെള്ളി"
        6 -> "ശനി"
        7 -> "ഞായർ"
        else -> "Day $dayOfWeek"
    }
}
