package org.synyx.urlaubsverwaltung.sicknote;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.function.Consumer;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.sicknote.SickNote}.
 */
class SickNoteTest {

    @Test
    void ensureLastModificationDateIsSetOnInitialization() {

        SickNote sickNote = new SickNote();

        assertThat(sickNote.getLastEdited()).isEqualTo(LocalDate.now(UTC));
    }


    @Test
    void ensureAUBIsPresentIfAUBStartDateAndAUBEndDateAreSet() {

        SickNote sickNote = new SickNote();
        sickNote.setAubStartDate(LocalDate.now(UTC));
        sickNote.setAubEndDate(LocalDate.now(UTC));

        assertThat(sickNote.isAubPresent()).isTrue();
    }


    @Test
    void ensureAUBIsNotPresentIfOnlyAUBStartDateIsSet() {

        SickNote sickNote = new SickNote();
        sickNote.setAubStartDate(LocalDate.now(UTC));

        assertThat(sickNote.isAubPresent()).isFalse();
    }


    @Test
    void ensureAUBIsNotPresentIfOnlyAUBEndDateIsSet() {

        SickNote sickNote = new SickNote();
        sickNote.setAubEndDate(LocalDate.now(UTC));

        assertThat(sickNote.isAubPresent()).isFalse();
    }


    @Test
    void ensureAUBIsNotPresentIfNoAUBPeriodIsSet() {

        assertThat(new SickNote().isAubPresent()).isFalse();
    }


    @Test
    void ensureIsNotActiveForInactiveStatus() {

        Consumer<SickNoteStatus> assertNotActive = (status) -> {
            SickNote sickNote = new SickNote();
            sickNote.setStatus(status);

            assertThat(sickNote.isActive()).isFalse();
        };

        assertNotActive.accept(SickNoteStatus.CANCELLED);
        assertNotActive.accept(SickNoteStatus.CONVERTED_TO_VACATION);
    }


    @Test
    void ensureIsActiveForActiveStatus() {

        Consumer<SickNoteStatus> assertActive = (status) -> {
            SickNote sickNote = new SickNote();
            sickNote.setStatus(status);

            assertThat(sickNote.isActive()).isTrue();
        };

        assertActive.accept(SickNoteStatus.ACTIVE);
    }

    @Test
    void ensureGetPeriodReturnsCorrectPeriod() {

        LocalDate startDate = LocalDate.now(UTC);
        LocalDate endDate = startDate.plusDays(2);

        SickNote sickNote = new SickNote();
        sickNote.setStartDate(startDate);
        sickNote.setEndDate(endDate);
        sickNote.setDayLength(DayLength.FULL);

        Period period = sickNote.getPeriod();

        assertThat(period).isNotNull();
        assertThat(period.getStartDate()).isEqualTo(startDate);
        assertThat(period.getEndDate()).isEqualTo(endDate);
        assertThat(period.getDayLength()).isEqualTo(DayLength.FULL);
    }

    @Test
    void nullsafeToString() {
        final SickNote sickNote = new SickNote();
        sickNote.setLastEdited(null);
        assertThat(sickNote).hasToString("SickNote{id=null, person=null, sickNoteType=null, startDate=null, endDate=null, dayLength=null, aubStartDate=null, aubEndDate=null, lastEdited=null, status=null}");
    }

    @Test
    void toStringTest() {
        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SickNoteCategory.SICK_NOTE);
        sickNoteType.setMessageKey("messageKey");

        final Person person = new Person();
        person.setId(1);

        final SickNote sickNote = new SickNote();
        sickNote.setId(1);
        sickNote.setSickNoteType(sickNoteType);
        sickNote.setStartDate(LocalDate.MIN);
        sickNote.setEndDate(LocalDate.MAX);
        sickNote.setStatus(SickNoteStatus.ACTIVE);
        sickNote.setDayLength(DayLength.FULL);
        sickNote.setAubStartDate(LocalDate.MIN);
        sickNote.setAubEndDate(LocalDate.MAX);
        sickNote.setLastEdited(LocalDate.EPOCH);
        sickNote.setPerson(person);

        assertThat(sickNote).hasToString("SickNote{id=1, person=Person{id='1'}, " +
            "sickNoteType=SickNoteType{category=SICK_NOTE, messageKey='messageKey'}, startDate=-999999999-01-01, " +
            "endDate=+999999999-12-31, dayLength=FULL, aubStartDate=-999999999-01-01, aubEndDate=+999999999-12-31," +
            " lastEdited=1970-01-01, status=ACTIVE}");
    }
}
