package com.tiswu.sagex.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tiswu.sagex.domain.TimeRange
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/** Preset chips + a custom date-range picker, emitting a [TimeRange]. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TimeRangeSelector(
    selected: TimeRange,
    onSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }

    FlowRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TimeRange.presets.forEach { preset ->
            FilterChip(
                selected = selected == preset,
                onClick = { onSelected(preset) },
                label = { Text(preset.label) },
            )
        }
        FilterChip(
            selected = selected is TimeRange.Custom,
            onClick = { showPicker = true },
            label = { Text(if (selected is TimeRange.Custom) "自定义 ✓" else "自定义") },
        )
    }

    if (showPicker) {
        val state = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val start = state.selectedStartDateMillis
                        val end = state.selectedEndDateMillis
                        if (start != null && end != null) {
                            onSelected(toLocalDayRange(start, end))
                        }
                        showPicker = false
                    },
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("取消") }
            },
        ) {
            DateRangePicker(state = state)
        }
    }
}

/**
 * `DateRangePicker` returns UTC-midnight millis for the selected calendar dates. Reinterpret
 * those dates in the system zone: start = local start-of-day of the start date, end = local
 * start-of-day of the day after the end date (exclusive), so the whole local end day is included.
 */
private fun toLocalDayRange(
    startUtcMillis: Long,
    endUtcMillis: Long,
    zone: ZoneId = ZoneId.systemDefault(),
): TimeRange.Custom {
    val startDate = Instant.ofEpochMilli(startUtcMillis).atZone(ZoneOffset.UTC).toLocalDate()
    val endDate = Instant.ofEpochMilli(endUtcMillis).atZone(ZoneOffset.UTC).toLocalDate()
    val startMillis = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
    val endMillis = endDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    return TimeRange.Custom(startMillis, endMillis)
}
