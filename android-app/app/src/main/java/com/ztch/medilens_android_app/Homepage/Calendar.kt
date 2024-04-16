package com.ztch.medilens_android_app.Homepage

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar

data class CalendarUiModel(
    var selectedDate: Date,
    var visibleDates: List<Date>
) {

    data class Date(
        val date: LocalDate,
        val isSelected: Boolean,
        val isToday: Boolean
    ) {

        val day: String = date.format(DateTimeFormatter.ofPattern("E"))
        val dayHeader: String = date.format(DateTimeFormatter.ofPattern("EEEE"))
    }
    // shifts the calendar to the previous week
    fun onLeftArrowClick() {
        val lastSelectedDate = visibleDates.first().date
        val newStartDate = lastSelectedDate.minusDays(7)
        updateCalendar(newStartDate, lastSelectedDate)
    }

    // shifts the calendar to the next week
    fun onRightArrowClick() {
        val lastSelectedDate = visibleDates.first().date
        val newStartDate = lastSelectedDate.plusDays(7)
        updateCalendar(newStartDate, lastSelectedDate)
    }

    private fun updateCalendar(newStartDate: LocalDate, lastSelectedDate: LocalDate) {
        val newUiModel = CalendarDataSource().getData(newStartDate, lastSelectedDate)
        selectedDate = newUiModel.selectedDate
        visibleDates = newUiModel.visibleDates
        Log.d("CalendarUiModel", "updateCalendar: $newStartDate, $lastSelectedDate")
    }
}

class CalendarDataSource {

        val today: LocalDate
        get() = LocalDate.now()

    fun getData(startDate: LocalDate = today, lastSelectedDate: LocalDate): CalendarUiModel {
        val firstDayOfWeek = startDate.with(DayOfWeek.MONDAY)
        val endDayOfWeek = firstDayOfWeek.plusDays(6)
        val visibleDates = getDatesBetween(firstDayOfWeek, endDayOfWeek)
        return toUiModel(visibleDates, lastSelectedDate)
    }

    fun getDayWeekAhead(startDate: LocalDate = Calendar.getInstance().time.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()): LocalDate {
        val firstDayOfWeek = startDate
        val endDayOfWeek = firstDayOfWeek.plusDays(6)
        return endDayOfWeek
    }

    private fun getDatesBetween(startDate: LocalDate, endDate: LocalDate): List<LocalDate> =
        (0..ChronoUnit.DAYS.between(startDate, endDate)).map { startDate.plusDays(it) }

    private fun toUiModel(
        dateList: List<LocalDate>,
        lastSelectedDate: LocalDate
    ): CalendarUiModel =
        CalendarUiModel(
            selectedDate = toItemUiModel(lastSelectedDate, true),
            visibleDates = dateList.map { toItemUiModel(it, it.isEqual(lastSelectedDate)) }
        )

    private fun toItemUiModel(date: LocalDate, isSelectedDate: Boolean) =
        CalendarUiModel.Date(
            isSelected = isSelectedDate,
            isToday = date.isEqual(today),
            date = date
        )
}

