package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createSickNote;


/**
 * Unit test for {@link SickNoteConvertForm}.
 */
class SickNoteConvertFormTest {

    @Test
    void ensureCorrectProvidedValuesFromSickNote() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate startDate = LocalDate.of(2014, 1, 1);
        final LocalDate endDate = LocalDate.of(2014, 1, 10);
        final SickNote sickNote = createSickNote(person, startDate, endDate, DayLength.NOON);

        final SickNoteConvertForm sut = new SickNoteConvertForm(sickNote);
        assertThat(sut.getPerson()).isNotNull();
        assertThat(sut.getDayLength()).isNotNull();
        assertThat(sut.getStartDate()).isNotNull();
        assertThat(sut.getEndDate()).isNotNull();

        assertThat(sut.getPerson()).isEqualTo(person);
        assertThat(sut.getDayLength()).isEqualTo(DayLength.NOON);
        assertThat(sut.getStartDate()).isEqualTo(startDate);
        assertThat(sut.getEndDate()).isEqualTo(endDate);
    }
}
