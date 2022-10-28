package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeEntity;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createSickNote;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;


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

    @Test
    void ensureGeneratesBasicApplicationForLeave() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate startDate = LocalDate.of(2014, 1, 1);
        final LocalDate endDate = LocalDate.of(2014, 1, 10);
        final SickNote sickNote = createSickNote(person, startDate, endDate, FULL);
        final VacationTypeEntity vacationType = TestDataCreator.createVacationTypeEntity(VacationCategory.UNPAIDLEAVE);

        final SickNoteConvertForm sut = new SickNoteConvertForm(sickNote);
        sut.setReason("Some Reason");
        sut.setVacationType(vacationType);

        final Application applicationForLeave = sut.generateApplicationForLeave(Clock.systemUTC());
        assertThat(applicationForLeave.getPerson()).isEqualTo(person);
        assertThat(applicationForLeave.getVacationType()).isEqualTo(vacationType);
        assertThat(applicationForLeave.getDayLength()).isEqualTo(FULL);
        assertThat(applicationForLeave.getStartDate()).isEqualTo(startDate);
        assertThat(applicationForLeave.getEndDate()).isEqualTo(endDate);
        assertThat(applicationForLeave.getStatus()).isEqualTo(ALLOWED);
        assertThat(applicationForLeave.getReason()).isEqualTo("Some Reason");
        assertThat(applicationForLeave.getApplicationDate()).isNotNull();
        assertThat(applicationForLeave.getEditedDate()).isNotNull();
    }
}
