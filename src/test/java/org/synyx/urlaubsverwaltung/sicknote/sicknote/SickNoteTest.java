package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;

class SickNoteTest {

    @Test
    void ensureAUBIsPresentIfAUBStartDateAndAUBEndDateAreSet() {

        final SickNote sickNote = SickNote.builder()
                .aubStartDate(LocalDate.now(UTC))
                .aubEndDate(LocalDate.now(UTC))
                .build();

        assertThat(sickNote.isAubPresent()).isTrue();
    }

    @Test
    void ensureAUBIsNotPresentIfOnlyAUBStartDateIsSet() {

        final SickNote sickNote = SickNote.builder()
                .aubStartDate(LocalDate.now(UTC))
                .build();

        assertThat(sickNote.isAubPresent()).isFalse();
    }

    @Test
    void ensureAUBIsNotPresentIfOnlyAUBEndDateIsSet() {

        final SickNote sickNote = SickNote.builder()
                .aubEndDate(LocalDate.now(UTC))
                .build();

        assertThat(sickNote.isAubPresent()).isFalse();
    }

    @Test
    void ensureAUBIsNotPresentIfNoAUBPeriodIsSet() {
        assertThat(SickNote.builder().build().isAubPresent()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = SickNoteStatus.class, names = {"CANCELLED", "CONVERTED_TO_VACATION"})
    void ensureIsNotActiveForInactiveStatus(SickNoteStatus status) {
        final SickNote sickNote = SickNote.builder().status(status).build();
        assertThat(sickNote.isActive()).isFalse();
    }

    @Test
    void ensureIsActiveForActiveStatus() {
        final SickNote sickNote = SickNote.builder().status(SickNoteStatus.ACTIVE).build();
        assertThat(sickNote.isActive()).isTrue();
    }

    @Test
    void ensureGetPeriodReturnsCorrectPeriod() {

        final LocalDate startDate = LocalDate.now(UTC);
        final LocalDate endDate = startDate.plusDays(2);

        final SickNote sickNote = SickNote.builder()
                .startDate(startDate)
                .endDate(endDate)
                .dayLength(DayLength.FULL)
                .build();

        final Period period = sickNote.getPeriod();
        assertThat(period).isNotNull();
        assertThat(period.getStartDate()).isEqualTo(startDate);
        assertThat(period.getEndDate()).isEqualTo(endDate);
        assertThat(period.getDayLength()).isEqualTo(DayLength.FULL);
    }

    @Test
    void nullsafeToString() {
        final SickNote sickNote = SickNote.builder().lastEdited(null).build();
        assertThat(sickNote).hasToString("SickNote{id=null, person=null, applier=null, " +
            "sickNoteType=null, startDate=null," +
            " endDate=null, dayLength=null, aubStartDate=null, aubEndDate=null, lastEdited=null," +
            " endOfSickPayNotificationSend=null, status=null, workDays=null}");
    }

    @Test
    void toStringTest() {
        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SickNoteCategory.SICK_NOTE);
        sickNoteType.setMessageKey("messageKey");

        final Person person = new Person();
        person.setId(1);

        final Person applier = new Person();
        applier.setId(2);

        final SickNote sickNote = SickNote.builder()
                .id(1)
                .sickNoteType(sickNoteType)
                .startDate(LocalDate.MIN)
                .endDate(LocalDate.MAX)
                .status(SickNoteStatus.ACTIVE)
                .dayLength(DayLength.FULL)
                .aubStartDate(LocalDate.MIN)
                .aubEndDate(LocalDate.MAX)
                .lastEdited(LocalDate.EPOCH)
                .endOfSickPayNotificationSend(LocalDate.EPOCH)
                .person(person)
                .applier(applier)
                .workDays(BigDecimal.valueOf(42))
                .build();

        assertThat(sickNote).hasToString("SickNote{id=1, person=Person{id='1'}, " +
            "applier=Person{id='2'}, sickNoteType=SickNoteType{category=SICK_NOTE, messageKey='messageKey'}, startDate=-999999999-01-01, " +
            "endDate=+999999999-12-31, dayLength=FULL, aubStartDate=-999999999-01-01, aubEndDate=+999999999-12-31," +
            " lastEdited=1970-01-01, endOfSickPayNotificationSend=1970-01-01, status=ACTIVE, workDays=42}");
    }
}
