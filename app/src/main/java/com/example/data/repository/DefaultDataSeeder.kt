package com.example.data.repository

import com.example.data.dao.TimetableDao
import com.example.data.model.DayConfig
import com.example.data.model.Institution
import com.example.data.model.SchoolClass
import com.example.data.model.Subject
import com.example.data.model.Teacher
import com.example.data.model.TeacherUnavailability
import com.example.data.model.TimetableSlot
import org.json.JSONArray

object DefaultDataSeeder {

    suspend fun seedInitialDataIfEmpty(dao: TimetableDao) {
        // Institutions
        val inst1Id = "inst_wafiyya"
        val inst2Id = "inst_ihsan"

        val institutions = listOf(
            Institution(
                id = inst1Id,
                name = "Al Gaith Wafiyya College",
                malayalamName = "അൽ ഗൈഥ് വഫിയ്യ കോളേജ്",
                code = "AGWC",
                description = "സ്ഥാപനം 1 (മെയിൻ കാമ്പസ്)"
            ),
            Institution(
                id = inst2Id,
                name = "Al Ihsan Arts & Science College",
                malayalamName = "അൽ ഇഹ്സാൻ കോളേജ്",
                code = "AIAS",
                description = "സ്ഥാപനം 2 (സിറ്റി കാമ്പസ്)"
            )
        )
        dao.insertInstitutions(institutions)

        // Day Configs for both institutions
        val dayConfigs = mutableListOf<DayConfig>()
        for (instId in listOf(inst1Id, inst2Id)) {
            dayConfigs.add(DayConfig(id = "${instId}_1", institutionId = instId, dayOfWeek = 1, isActive = true, periodCount = 6)) // Monday
            dayConfigs.add(DayConfig(id = "${instId}_2", institutionId = instId, dayOfWeek = 2, isActive = true, periodCount = 6)) // Tuesday
            dayConfigs.add(DayConfig(id = "${instId}_3", institutionId = instId, dayOfWeek = 3, isActive = true, periodCount = 6)) // Wednesday
            dayConfigs.add(DayConfig(id = "${instId}_4", institutionId = instId, dayOfWeek = 4, isActive = true, periodCount = 6)) // Thursday
            dayConfigs.add(DayConfig(id = "${instId}_5", institutionId = instId, dayOfWeek = 5, isActive = true, periodCount = 4)) // Friday (Jumu'ah)
            dayConfigs.add(DayConfig(id = "${instId}_6", institutionId = instId, dayOfWeek = 6, isActive = true, periodCount = 5)) // Saturday
            dayConfigs.add(DayConfig(id = "${instId}_7", institutionId = instId, dayOfWeek = 7, isActive = false, periodCount = 0)) // Sunday
        }
        dao.insertDayConfigs(dayConfigs)

        // Teachers (Some in Inst 1, some in Inst 2, some in BOTH!)
        val bothInstitutionsJson = JSONArray(listOf(inst1Id, inst2Id)).toString()
        val inst1OnlyJson = JSONArray(listOf(inst1Id)).toString()
        val inst2OnlyJson = JSONArray(listOf(inst2Id)).toString()

        val teachers = listOf(
            Teacher(
                id = "t_abdul_rahman",
                name = "Dr. Abdul Rahman",
                malayalamName = "ഡോ. അബ്ദുൽ റഹ്മാൻ",
                phone = "9847012345",
                subjectSpecialization = "തഫ്സീർ & ഖുർആനിക് സയൻസ്",
                colorHex = "#1E40AF", // Blue
                institutionIdsJson = bothInstitutionsJson // Teaches at BOTH!
            ),
            Teacher(
                id = "t_ibrahim",
                name = "Usthad Ibrahim Wafy",
                malayalamName = "ഉസ്താദ് ഇബ്രാഹിം വാഫി",
                phone = "9847123456",
                subjectSpecialization = "ഫിഖ്ഹ് & ഉസൂൽ",
                colorHex = "#047857", // Emerald
                institutionIdsJson = bothInstitutionsJson // Teaches at BOTH!
            ),
            Teacher(
                id = "t_ali_hassan",
                name = "Usthad Ali Hassan",
                malayalamName = "ഉസ്താദ് അലി ഹസ്സൻ",
                phone = "9847234567",
                subjectSpecialization = "ഹദീസ് പഠനം",
                colorHex = "#B45309", // Amber
                institutionIdsJson = inst1OnlyJson // Inst 1 only
            ),
            Teacher(
                id = "t_fathima",
                name = "Prof. Fathima Zahra",
                malayalamName = "പ്രൊഫ. ഫാത്തിമ സഹ്റ",
                phone = "9847345678",
                subjectSpecialization = "ഇംഗ്ലീഷ് സാഹിത്യം",
                colorHex = "#7E22CE", // Purple
                institutionIdsJson = bothInstitutionsJson // Both!
            ),
            Teacher(
                id = "t_muhammad_rafiq",
                name = "Usthad Muhammad Rafiq",
                malayalamName = "ഉസ്താദ് മുഹമ്മദ് റഫീഖ്",
                phone = "9847456789",
                subjectSpecialization = "അറബിക് വ്യാകരണം (നഹ്‌വ്)",
                colorHex = "#0E7490", // Cyan
                institutionIdsJson = inst1OnlyJson
            ),
            Teacher(
                id = "t_shamsuddin",
                name = "Mr. Shamsuddin K",
                malayalamName = "മിസ്റ്റർ ഷംസുദ്ദീൻ കെ",
                phone = "9847567890",
                subjectSpecialization = "കമ്പ്യൂട്ടർ ആപ്ലിക്കേഷൻസ് & ഐ.ടി",
                colorHex = "#BE123C", // Rose
                institutionIdsJson = inst2OnlyJson // Inst 2 only
            ),
            Teacher(
                id = "t_siddique",
                name = "Usthad Siddique Faizy",
                malayalamName = "ഉസ്താദ് സിദ്ദീഖ് ഫൈസി",
                phone = "9847678901",
                subjectSpecialization = "താരീഖ് (ഇസ്ലാമിക ചരിത്രം)",
                colorHex = "#4338CA", // Indigo
                institutionIdsJson = bothInstitutionsJson
            )
        )
        dao.insertTeachers(teachers)

        // Classes for both institutions
        val classes = listOf(
            SchoolClass(
                id = "cls_w1",
                institutionId = inst1Id,
                name = "Wafiyya 1st Year (Sanawiyya)",
                malayalamName = "വഫിയ്യ ഒന്നാം വർഷം",
                section = "A",
                hasBatches = true,
                batchNames = "ബാച്ച് A,ബാച്ച് B"
            ),
            SchoolClass(
                id = "cls_w2",
                institutionId = inst1Id,
                name = "Wafiyya 2nd Year (Aliya)",
                malayalamName = "വഫിയ്യ രണ്ടാം വർഷം",
                section = "A",
                hasBatches = false
            ),
            SchoolClass(
                id = "cls_c1",
                institutionId = inst2Id,
                name = "B.A. Arabic 1st Year",
                malayalamName = "ബി.എ അറബിക് ഒന്നാം വർഷം",
                section = "A",
                hasBatches = true,
                batchNames = "ബാച്ച് A,ബാച്ച് B"
            ),
            SchoolClass(
                id = "cls_c2",
                institutionId = inst2Id,
                name = "B.Com 2nd Year",
                malayalamName = "ബി.കോം രണ്ടാം വർഷം",
                section = "B",
                hasBatches = false
            )
        )
        dao.insertClasses(classes)

        // Subjects
        val subjects = listOf(
            Subject(id = "sub_tafseer", institutionId = "ALL", name = "Tafseer al-Quran", malayalamName = "തഫ്സീർ (ഖുർആൻ വിവരണം)", code = "TAF101", colorHex = "#1E40AF"),
            Subject(id = "sub_fiqh", institutionId = "ALL", name = "Fiqh & Usool", malayalamName = "ഫിഖ്ഹ് & ഉസൂൽ", code = "FIQ102", colorHex = "#047857"),
            Subject(id = "sub_hadeeth", institutionId = "ALL", name = "Uloomul Hadeeth", malayalamName = "ഉലൂമുൽ ഹദീസ്", code = "HAD103", colorHex = "#B45309"),
            Subject(id = "sub_nahw", institutionId = "ALL", name = "Arabic Grammar (Nahw)", malayalamName = "അറബിക് വ്യാകരണം", code = "ARB104", colorHex = "#0E7490"),
            Subject(id = "sub_english", institutionId = "ALL", name = "Communicative English", malayalamName = "ഇംഗ്ലീഷ് സാഹിത്യം", code = "ENG105", colorHex = "#7E22CE"),
            Subject(id = "sub_it", institutionId = "ALL", name = "Computer Lab & IT", malayalamName = "കമ്പ്യൂട്ടർ ലാബ്", code = "COM106", colorHex = "#BE123C"),
            Subject(id = "sub_history", institutionId = "ALL", name = "Islamic History", malayalamName = "ഇസ്ലാമിക ചരിത്രം", code = "HIS107", colorHex = "#4338CA")
        )
        dao.insertSubjects(subjects)

        // Teacher Unavailability (Sample: Dr. Abdul Rahman is unavailable Friday periods 3 & 4; Usthad Ibrahim has Wednesday full day official duty)
        val unavailabilities = listOf(
            TeacherUnavailability(
                id = "unav_1",
                teacherId = "t_abdul_rahman",
                dayOfWeek = 5, // Friday
                isFullDay = false,
                periodIndex = 4,
                reason = "ജുമുഅ ഖുതുബ നിർവ്വഹണം (Jumu'ah sermon)"
            ),
            TeacherUnavailability(
                id = "unav_2",
                teacherId = "t_ibrahim",
                dayOfWeek = 3, // Wednesday
                isFullDay = true,
                reason = "യൂണിവേഴ്സിറ്റി മീറ്റിംഗ് (University Meeting)"
            )
        )
        dao.insertUnavailabilities(unavailabilities)

        // Timetable Slots: Showing full class, batch split, and shared teaching!
        val slots = listOf(
            // Wafiyya 1st Year - Monday
            // Period 1: Tafseer by Dr. Abdul Rahman (Inst 1)
            TimetableSlot(
                id = "slot_w1_m1",
                institutionId = inst1Id,
                classId = "cls_w1",
                dayOfWeek = 1,
                periodIndex = 1,
                isBatchSplit = false,
                subjectId = "sub_tafseer",
                primaryTeacherId = "t_abdul_rahman",
                note = "റെഗുലർ ക്ലാസ്"
            ),
            // Period 2: Fiqh - Shared teaching: Usthad Ibrahim (Primary) + Usthad Ali Hassan (Shared 1 of 3 periods!)
            TimetableSlot(
                id = "slot_w1_m2",
                institutionId = inst1Id,
                classId = "cls_w1",
                dayOfWeek = 1,
                periodIndex = 2,
                isBatchSplit = false,
                subjectId = "sub_fiqh",
                primaryTeacherId = "t_ibrahim",
                secondaryTeacherId = "t_ali_hassan",
                shareType = "ONE_OF_THREE",
                note = "3-ൽ 1 പിരീഡ് ഉസ്താദ് അലി ഹസ്സൻ ക്ലാസ് എടുക്കുന്നു"
            ),
            // Period 3: BATCH SPLIT!
            // Batch A -> Computer Lab with Mr. Shamsuddin
            // Batch B -> Communicative English with Prof. Fathima Zahra
            TimetableSlot(
                id = "slot_w1_m3_a",
                institutionId = inst1Id,
                classId = "cls_w1",
                dayOfWeek = 1,
                periodIndex = 3,
                isBatchSplit = true,
                batchName = "ബാച്ച് A",
                subjectId = "sub_it",
                primaryTeacherId = "t_shamsuddin",
                note = "കമ്പ്യൂട്ടർ പ്രാക്ടിക്കൽ ലാബ്"
            ),
            TimetableSlot(
                id = "slot_w1_m3_b",
                institutionId = inst1Id,
                classId = "cls_w1",
                dayOfWeek = 1,
                periodIndex = 3,
                isBatchSplit = true,
                batchName = "ബാച്ച് B",
                subjectId = "sub_english",
                primaryTeacherId = "t_fathima",
                note = "ഇംഗ്ലീഷ് ലാംഗ്വേജ് ലാബ്"
            ),
            // Period 4: Nahw by Usthad Muhammad Rafiq
            TimetableSlot(
                id = "slot_w1_m4",
                institutionId = inst1Id,
                classId = "cls_w1",
                dayOfWeek = 1,
                periodIndex = 4,
                isBatchSplit = false,
                subjectId = "sub_nahw",
                primaryTeacherId = "t_muhammad_rafiq"
            ),

            // College 2 (Al Ihsan) - Monday
            // Notice: Dr. Abdul Rahman teaches here on Period 2! (Which is after his Period 1 at Inst 1, so no conflict!)
            TimetableSlot(
                id = "slot_c1_m2",
                institutionId = inst2Id,
                classId = "cls_c1",
                dayOfWeek = 1,
                periodIndex = 2,
                isBatchSplit = false,
                subjectId = "sub_tafseer",
                primaryTeacherId = "t_abdul_rahman",
                note = "അൽ ഇഹ്സാൻ കോളേജ് ക്ലാസ്"
            )
        )
        dao.insertSlots(slots)
    }
}
