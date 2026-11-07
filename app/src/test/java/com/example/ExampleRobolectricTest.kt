package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Institution
import com.example.data.model.SchoolClass
import com.example.data.model.Teacher
import com.example.data.model.TeacherUnavailability
import com.example.data.model.TimetableSlot
import com.example.domain.ConflictChecker
import com.example.domain.TeacherStatus
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Timetable Pro", appName)
  }

  @Test
  fun `test teacher unavailability detection`() {
    val teacher = Teacher(
      id = "t1",
      name = "Usthad Ibrahim",
      malayalamName = "ഉസ്താദ് ഇബ്രാഹിം",
      institutionIdsJson = JSONArray(listOf("inst1", "inst2")).toString()
    )

    val unav = listOf(
      TeacherUnavailability(
        id = "u1",
        teacherId = "t1",
        dayOfWeek = 5,
        isFullDay = true,
        reason = "Friday Leave"
      )
    )

    val status = ConflictChecker.checkTeacherStatus(
      teacher = teacher,
      institutionId = "inst1",
      dayOfWeek = 5,
      periodIndex = 1,
      allSlots = emptyList(),
      allUnavailabilities = unav,
      allClasses = emptyList(),
      allInstitutions = emptyList()
    )

    assertTrue(status is TeacherStatus.Unavailable)
    assertEquals("Friday Leave", (status as TeacherStatus.Unavailable).reason)
  }

  @Test
  fun `test cross institution clash detection`() {
    val teacher = Teacher(
      id = "t1",
      name = "Dr. Abdul Rahman",
      malayalamName = "ഡോ. അബ്ദുൽ റഹ്മാൻ",
      institutionIdsJson = JSONArray(listOf("inst1", "inst2")).toString()
    )

    val institutions = listOf(
      Institution(id = "inst1", name = "Campus 1", malayalamName = "സ്ഥാപനം 1", code = "C1"),
      Institution(id = "inst2", name = "Campus 2", malayalamName = "സ്ഥാപനം 2", code = "C2")
    )

    val classes = listOf(
      SchoolClass(id = "c1", institutionId = "inst1", name = "1st Year", malayalamName = "ഒന്നാം വർഷം"),
      SchoolClass(id = "c2", institutionId = "inst2", name = "B.Com", malayalamName = "ബി.കോം")
    )

    // Slot at inst2 at Monday Period 3
    val slots = listOf(
      TimetableSlot(
        id = "s1",
        institutionId = "inst2",
        classId = "c2",
        dayOfWeek = 1,
        periodIndex = 3,
        subjectId = "sub1",
        primaryTeacherId = "t1"
      )
    )

    // Check status in inst1 at Monday Period 3
    val status = ConflictChecker.checkTeacherStatus(
      teacher = teacher,
      institutionId = "inst1",
      dayOfWeek = 1,
      periodIndex = 3,
      allSlots = slots,
      allUnavailabilities = emptyList(),
      allClasses = classes,
      allInstitutions = institutions
    )

    assertTrue(status is TeacherStatus.BusyOtherInstitution)
    val busyStatus = status as TeacherStatus.BusyOtherInstitution
    assertEquals("സ്ഥാപനം 2", busyStatus.institutionName)
  }
}
