package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static java.time.DayOfWeek.FRIDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.SUBMITTED;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.WORKDAY;

class SickNoteDtoTest {

    @Test
    void sickNoteDtoBySickNote() {

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SickNoteCategory.SICK_NOTE);
        final Person person = new Person("username", "lastname", "firstname", "email");
        person.setId(2L);

        final WorkingTimeCalendar.WorkingDayInformation workingDayInformation = new WorkingTimeCalendar.WorkingDayInformation(FULL, WORKDAY, WORKDAY);
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(
                Map.of(
                        LocalDate.of(2024, 1, 5), workingDayInformation,
                        LocalDate.of(2024, 1, 8), workingDayInformation));

        final SickNote sickNote = SickNote.builder()
                .id(1L)
                .startDate(LocalDate.of(2024, 1, 5))
                .endDate(LocalDate.of(2024, 1, 8))
                .dayLength(FULL)
                .person(person)
                .sickNoteType(sickNoteType)
                .status(SUBMITTED)
                .workingTimeCalendar(workingTimeCalendar)
                .build();

        final SickNoteDto sickNoteDto = new SickNoteDto(sickNote);

        assertThat(sickNoteDto.getId()).isEqualTo("1");
        assertThat(sickNoteDto.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 5));
        assertThat(sickNoteDto.getWeekDayOfStartDate()).isEqualTo(FRIDAY);
        assertThat(sickNoteDto.getEndDate()).isEqualTo(LocalDate.of(2024, 1, 8));
        assertThat(sickNoteDto.getDayLength()).isEqualTo(FULL);
        assertThat(sickNoteDto.getWorkDays()).isEqualTo(BigDecimal.valueOf(2L));
        assertThat(sickNoteDto.getPerson().getId()).isEqualTo(2L);
        assertThat(sickNoteDto.getPerson().getName()).isEqualTo("firstname lastname");
        assertThat(sickNoteDto.getPerson().getAvatarUrl()).isEqualTo("https://gravatar.com/avatar/0c83f57c786a0b4a39efab23731c7ebc");
        assertThat(sickNoteDto.getPerson().getIsInactive()).isFalse();
        assertThat(sickNoteDto.getType()).isEqualTo("application.data.sicknotetype.sicknote");
        assertThat(sickNoteDto.getStatus()).isEqualTo("SUBMITTED");
    }
}
