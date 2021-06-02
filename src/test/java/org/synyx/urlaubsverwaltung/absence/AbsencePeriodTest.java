package org.synyx.urlaubsverwaltung.absence;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;

import static java.time.Month.NOVEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.absence.AbsencePeriod.AbsenceStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.absence.AbsencePeriod.AbsenceStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.absence.AbsencePeriod.AbsenceStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.absence.AbsencePeriod.AbsenceStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.absence.AbsencePeriod.AbsenceStatus.WAITING;

class AbsencePeriodTest {

    @Test
    void ensureRecordMorningVacationToStringDoesNotPrintAnyInfo() {
        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(1, AbsencePeriod.AbsenceStatus.WAITING);
        assertThat(morning).hasToString("AbstractRecordInfo{id=1}");
    }

    @Test
    void ensureRecordNoonVacationToStringDoesNotPrintAnyInfo() {
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(1, AbsencePeriod.AbsenceStatus.WAITING);
        assertThat(noon).hasToString("AbstractRecordInfo{id=1}");
    }

    @Test
    void ensureRecordMorningSickToStringDoesNotPrintAnyInfo() {
        final AbsencePeriod.RecordMorningSick morning = new AbsencePeriod.RecordMorningSick(1);
        assertThat(morning).hasToString("AbstractRecordInfo{id=1}");
    }

    @Test
    void ensureRecordNoonSickToStringDoesNotPrintAnyInfo() {
        final AbsencePeriod.RecordNoonSick noon = new AbsencePeriod.RecordNoonSick(1);
        assertThat(noon).hasToString("AbstractRecordInfo{id=1}");
    }

    @Test
    void ensureNoonSickIsActive() {
        final AbsencePeriod.RecordNoonSick noon = new AbsencePeriod.RecordNoonSick(1);
        final boolean isActive = noon.hasStatusOneOf(ACTIVE);
        assertThat(isActive).isTrue();
    }

    @Test
    void ensureNoonSickIsActiveWithMultiple() {
        final AbsencePeriod.RecordNoonSick noon = new AbsencePeriod.RecordNoonSick(1);
        final boolean isActive = noon.hasStatusOneOf(ACTIVE, ALLOWED);
        assertThat(isActive).isTrue();
    }

    @Test
    void ensureNoonSickIsNotActive() {
        final AbsencePeriod.RecordNoonSick noon = new AbsencePeriod.RecordNoonSick(1);
        final boolean isActive = noon.hasStatusOneOf(ALLOWED);
        assertThat(isActive).isFalse();
    }

    @Test
    void ensureMorningSickIsActive() {
        final AbsencePeriod.RecordMorningSick morning = new AbsencePeriod.RecordMorningSick(1);
        final boolean isActive = morning.hasStatusOneOf(ACTIVE);
        assertThat(isActive).isTrue();
    }

    @Test
    void ensureMorningSickIsActiveWithMultiple() {
        final AbsencePeriod.RecordMorningSick morning = new AbsencePeriod.RecordMorningSick(1);
        final boolean isActive = morning.hasStatusOneOf(ACTIVE, ALLOWED);
        assertThat(isActive).isTrue();
    }

    @Test
    void ensureMorningSickIsNotActive() {
        final AbsencePeriod.RecordMorningSick morning = new AbsencePeriod.RecordMorningSick(1);
        final boolean isActive = morning.hasStatusOneOf(ALLOWED);
        assertThat(isActive).isFalse();
    }

    @Test
    void ensureVacationMorningIsAllowed() {
        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(1, ALLOWED);
        final boolean isAllowed = morning.hasStatusOneOf(ALLOWED);
        assertThat(isAllowed).isTrue();
    }

    @Test
    void ensureVacationMorningIsAllowedMultiple() {
        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(1, ALLOWED);
        final boolean isAllowed = morning.hasStatusOneOf(ALLOWED_CANCELLATION_REQUESTED, ALLOWED);
        assertThat(isAllowed).isTrue();
    }

    @Test
    void ensureVacationMorningIsNotAllowed() {
        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(1, ALLOWED);
        final boolean isAllowed = morning.hasStatusOneOf(ALLOWED_CANCELLATION_REQUESTED);
        assertThat(isAllowed).isFalse();
    }

    @Test
    void ensureVacationNoonIsAllowed() {
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(1, ALLOWED);
        final boolean isAllowed = noon.hasStatusOneOf(ALLOWED);
        assertThat(isAllowed).isTrue();
    }

    @Test
    void ensureVacationNoonIsAllowedMultiple() {
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(1, ALLOWED);
        final boolean isAllowed = noon.hasStatusOneOf(ALLOWED_CANCELLATION_REQUESTED, ALLOWED);
        assertThat(isAllowed).isTrue();
    }

    @Test
    void ensureVacationNoonIsNotAllowed() {
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(1, ALLOWED);
        final boolean isAllowed = noon.hasStatusOneOf(ALLOWED_CANCELLATION_REQUESTED);
        assertThat(isAllowed).isFalse();
    }

    @Test
    void ensureVacationMorningHasStatusWaiting() {
        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(1, WAITING);
        assertThat(morning.hasStatusWaiting()).isTrue();
    }

    @Test
    void ensureVacationMorningHasNotStatusWaiting() {
        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(1, ALLOWED);
        assertThat(morning.hasStatusWaiting()).isFalse();
    }

    @Test
    void ensureVacationNoonHasStatusWaiting() {
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(1, WAITING);
        assertThat(noon.hasStatusWaiting()).isTrue();
    }

    @Test
    void ensureVacationNoonHasNotStatusWaiting() {
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(1, ALLOWED);
        assertThat(noon.hasStatusWaiting()).isFalse();
    }

    @Test
    void ensureVacationMorningHasStatusAllowed() {
        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(1, ALLOWED_CANCELLATION_REQUESTED);
        assertThat(morning.hasStatusAllowed()).isTrue();
    }

    @Test
    void ensureVacationMorningHasNotStatusAllowed() {
        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(1, TEMPORARY_ALLOWED);
        assertThat(morning.hasStatusAllowed()).isFalse();
    }

    @Test
    void ensureVacationNoonHasStatusAllowed() {
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(1, ALLOWED);
        assertThat(noon.hasStatusAllowed()).isTrue();
    }

    @Test
    void ensureVacationNoonHasNotStatusAllowed() {
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(1, TEMPORARY_ALLOWED);
        assertThat(noon.hasStatusAllowed()).isFalse();
    }

    @Test
    void isHalfDayAbsenceIsFullDay() {
        final AbsencePeriod.RecordMorningSick morning = new AbsencePeriod.RecordMorningSick(1);
        final AbsencePeriod.RecordNoonSick noon = new AbsencePeriod.RecordNoonSick(1);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.of(2013, NOVEMBER, 19), new Person(), morning, noon);
        assertThat(record.isHalfDayAbsence()).isFalse();
    }

    @Test
    void isHalfDayAbsenceIsNoDay() {
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.of(2013, NOVEMBER, 19), new Person(), null, null);
        assertThat(record.isHalfDayAbsence()).isFalse();
    }

    @Test
    void isHalfDayAbsenceIsMorning() {
        final AbsencePeriod.RecordMorningSick morning = new AbsencePeriod.RecordMorningSick(1);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.of(2013, NOVEMBER, 19), new Person(), morning);
        assertThat(record.isHalfDayAbsence()).isTrue();
    }

    @Test
    void isHalfDayAbsenceIsNoon() {
        final AbsencePeriod.RecordNoonSick noon = new AbsencePeriod.RecordNoonSick(1);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.of(2013, NOVEMBER, 19), new Person(), noon);
        assertThat(record.isHalfDayAbsence()).isTrue();
    }
}
