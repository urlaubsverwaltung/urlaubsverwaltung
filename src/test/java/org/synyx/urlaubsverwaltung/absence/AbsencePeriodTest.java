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
        final Person person = anyPerson();
        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, AbsencePeriod.AbsenceStatus.WAITING, "Erholungsurlaub", 1L, false);
        assertThat(morning).hasToString("AbstractRecordInfo{id=1}");
    }

    @Test
    void ensureRecordNoonVacationToStringDoesNotPrintAnyInfo() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, AbsencePeriod.AbsenceStatus.WAITING, "Erholungsurlaub", 1L, false);
        assertThat(noon).hasToString("AbstractRecordInfo{id=1}");
    }

    @Test
    void ensureRecordMorningSickToStringDoesNotPrintAnyInfo() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordMorningSick morning = new AbsencePeriod.RecordMorningSick(person, 1L, ACTIVE, "SICK_NOTE", 1L);
        assertThat(morning).hasToString("AbstractRecordInfo{id=1}");
    }

    @Test
    void ensureRecordNoonSickToStringDoesNotPrintAnyInfo() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordNoonSick noon = new AbsencePeriod.RecordNoonSick(person, 1L, ACTIVE, "SICK_NOTE", 1L);
        assertThat(noon).hasToString("AbstractRecordInfo{id=1}");
    }

    @Test
    void ensureNoonSickIsActive() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordNoonSick noon = new AbsencePeriod.RecordNoonSick(person, 1L, ACTIVE, "SICK_NOTE", 1L);
        final boolean isActive = noon.hasStatusOneOf(ACTIVE);
        assertThat(isActive).isTrue();
    }

    @Test
    void ensureNoonSickIsActiveWithMultiple() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordNoonSick noon = new AbsencePeriod.RecordNoonSick(person, 1L, ACTIVE, "SICK_NOTE", 1L);
        final boolean isActive = noon.hasStatusOneOf(ACTIVE, ALLOWED);
        assertThat(isActive).isTrue();
    }

    @Test
    void ensureNoonSickIsNotActive() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordNoonSick noon = new AbsencePeriod.RecordNoonSick(person, 1L, ACTIVE, "SICK_NOTE", 1L);
        final boolean isActive = noon.hasStatusOneOf(ALLOWED);
        assertThat(isActive).isFalse();
    }

    @Test
    void ensureMorningSickIsActive() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordMorningSick morning = new AbsencePeriod.RecordMorningSick(person, 1L, ACTIVE, "SICK_NOTE", 1L);
        final boolean isActive = morning.hasStatusOneOf(ACTIVE);
        assertThat(isActive).isTrue();
    }

    @Test
    void ensureMorningSickIsActiveWithMultiple() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordMorningSick morning = new AbsencePeriod.RecordMorningSick(person, 1L, ACTIVE, "SICK_NOTE", 1L);
        final boolean isActive = morning.hasStatusOneOf(ACTIVE, ALLOWED);
        assertThat(isActive).isTrue();
    }

    @Test
    void ensureMorningSickIsNotActive() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordMorningSick morning = new AbsencePeriod.RecordMorningSick(person, 1L, ACTIVE, "SICK_NOTE", 1L);
        final boolean isActive = morning.hasStatusOneOf(ALLOWED);
        assertThat(isActive).isFalse();
    }

    @Test
    void ensureVacationMorningIsAllowed() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, ALLOWED, "Erholungsurlaub", 1L, false);
        final boolean isAllowed = morning.hasStatusOneOf(ALLOWED);
        assertThat(isAllowed).isTrue();
    }

    @Test
    void ensureVacationMorningIsAllowedMultiple() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, ALLOWED, "Erholungsurlaub", 1L, false);
        final boolean isAllowed = morning.hasStatusOneOf(ALLOWED_CANCELLATION_REQUESTED, ALLOWED);
        assertThat(isAllowed).isTrue();
    }

    @Test
    void ensureVacationMorningIsNotAllowed() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, ALLOWED, "Erholungsurlaub", 1L, false);
        final boolean isAllowed = morning.hasStatusOneOf(ALLOWED_CANCELLATION_REQUESTED);
        assertThat(isAllowed).isFalse();
    }

    @Test
    void ensureVacationNoonIsAllowed() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, ALLOWED, "Erholungsurlaub", 1L, false);
        final boolean isAllowed = noon.hasStatusOneOf(ALLOWED);
        assertThat(isAllowed).isTrue();
    }

    @Test
    void ensureVacationNoonIsAllowedMultiple() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, ALLOWED, "Erholungsurlaub", 1L, false);
        final boolean isAllowed = noon.hasStatusOneOf(ALLOWED_CANCELLATION_REQUESTED, ALLOWED);
        assertThat(isAllowed).isTrue();
    }

    @Test
    void ensureVacationNoonIsNotAllowed() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, ALLOWED, "Erholungsurlaub", 1L, false);
        final boolean isAllowed = noon.hasStatusOneOf(ALLOWED_CANCELLATION_REQUESTED);
        assertThat(isAllowed).isFalse();
    }

    @Test
    void ensureVacationMorningHasStatusWaiting() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, WAITING, "Erholungsurlaub", 1L, false);
        assertThat(morning.hasStatusWaiting()).isTrue();
    }

    @Test
    void ensureVacationMorningHasNotStatusWaiting() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, ALLOWED, "Erholungsurlaub", 1L, false);
        assertThat(morning.hasStatusWaiting()).isFalse();
    }

    @Test
    void ensureVacationNoonHasStatusWaiting() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, WAITING, "Erholungsurlaub", 1L, false);
        assertThat(noon.hasStatusWaiting()).isTrue();
    }

    @Test
    void ensureVacationNoonHasNotStatusWaiting() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, ALLOWED, "Erholungsurlaub", 1L, false);
        assertThat(noon.hasStatusWaiting()).isFalse();
    }

    @Test
    void ensureVacationMorningHasStatusAllowed() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, ALLOWED, "Erholungsurlaub", 1L, false);
        assertThat(morning.hasStatusAllowed()).isTrue();
    }

    @Test
    void ensureVacationMorningHasStatusAllowedCancellationRequested() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, ALLOWED_CANCELLATION_REQUESTED, "Erholungsurlaub", 1L, false);
        assertThat(morning.hasStatusAllowedCancellationRequested()).isTrue();
    }

    @Test
    void ensureVacationMorningHasNotStatusAllowed() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, TEMPORARY_ALLOWED, "Erholungsurlaub", 1L, false);
        assertThat(morning.hasStatusAllowed()).isFalse();
    }

    @Test
    void ensureVacationNoonHasStatusAllowed() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, ALLOWED, "Erholungsurlaub", 1L, false);
        assertThat(noon.hasStatusAllowed()).isTrue();
    }

    @Test
    void ensureVacationNoonHasNotStatusAllowed() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, TEMPORARY_ALLOWED, "Erholungsurlaub", 1L, false);
        assertThat(noon.hasStatusAllowed()).isFalse();
    }

    @Test
    void isHalfDayAbsenceIsFullDay() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordMorningSick morning = new AbsencePeriod.RecordMorningSick(person, 1L, ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.RecordNoonSick noon = new AbsencePeriod.RecordNoonSick(person, 1L, ACTIVE, "SICK_NOTE", 1L);
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
        final Person person = anyPerson();
        final AbsencePeriod.RecordMorningSick morning = new AbsencePeriod.RecordMorningSick(person, 1L, ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.of(2013, NOVEMBER, 19), new Person(), morning);
        assertThat(record.isHalfDayAbsence()).isTrue();
    }

    @Test
    void isHalfDayAbsenceIsNoon() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordNoonSick noon = new AbsencePeriod.RecordNoonSick(person, 1L, ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record record = new AbsencePeriod.Record(LocalDate.of(2013, NOVEMBER, 19), new Person(), noon);
        assertThat(record.isHalfDayAbsence()).isTrue();
    }

    @Test
    void isVisibleToEverybodyMorning() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, ALLOWED, "Erholungsurlaub", 1L, true);
        assertThat(morning.isVisibleToEveryone()).isTrue();
    }

    @Test
    void isNotVisibleToEverybodyMorning() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(person, 1L, ALLOWED, "Erholungsurlaub", 1L, false);
        assertThat(morning.isVisibleToEveryone()).isFalse();
    }

    @Test
    void isVisibleToEverybodyNoon() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, ALLOWED, "Erholungsurlaub", 1L, true);
        assertThat(noon.isVisibleToEveryone()).isTrue();
    }

    @Test
    void isNotVisibleToEverybodyNoon() {
        final Person person = anyPerson();
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(person, 1L, ALLOWED, "Erholungsurlaub", 1L, false);
        assertThat(noon.isVisibleToEveryone()).isFalse();
    }

    private Person anyPerson() {
        return new Person("muster", "Muster", "Marlene", "muster@example.org");
    }
}
