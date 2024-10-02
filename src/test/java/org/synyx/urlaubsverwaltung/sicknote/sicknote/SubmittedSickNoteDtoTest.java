package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.api.PersonMapper;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;

class SubmittedSickNoteDtoTest {

    @ParameterizedTest
    @EnumSource(value = SickNoteStatus.class, names = {"ACTIVE", "SUBMITTED"})
    void convert(SickNoteStatus status) {

        final Person person = new Person("person", "Doe", "John", "john.doe@example.org");
        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SickNoteCategory.SICK_NOTE);
        sickNoteType.setMessageKey("Krankmeldung");
        LocalDate startDate = LocalDate.parse("2023-12-08");
        LocalDate endDate = startDate.plusDays(3);

        final SickNote sickNote = SickNote.builder()
                .person(person)
                .startDate(startDate)
                .endDate(endDate)
                .dayLength(FULL)
                .sickNoteType(sickNoteType)
                .status(status)
                .build();

        final SickNoteDto sickNoteDto = new SickNoteDto(sickNote);

        assertThat(sickNoteDto.getFrom()).isEqualTo("2023-12-08");
        assertThat(sickNoteDto.getTo()).isEqualTo("2023-12-11");
        assertThat(sickNoteDto.getDayLength()).isOne();
        assertThat(sickNoteDto.getPerson()).isEqualTo(PersonMapper.mapToDto(person));
        assertThat(sickNoteDto.getStatus()).isEqualTo("ACTIVE");
    }
}
