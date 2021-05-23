package org.synyx.urlaubsverwaltung.absence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AbsencePeriodTest {

    @Test
    void ensureRecordMorningVacationToStringDoesNotPrintAnyInfo() {
        final AbsencePeriod.RecordMorningVacation morning = new AbsencePeriod.RecordMorningVacation(1, AbsencePeriod.AbsenceStatus.WAITING);
        assertThat(morning).hasToString("AbstractRecordInfo{type=*****, id=1, status=****}");
    }

    @Test
    void ensureRecordNoonVacationToStringDoesNotPrintAnyInfo() {
        final AbsencePeriod.RecordNoonVacation noon = new AbsencePeriod.RecordNoonVacation(1, AbsencePeriod.AbsenceStatus.WAITING);
        assertThat(noon).hasToString("AbstractRecordInfo{type=*****, id=1, status=****}");
    }

    @Test
    void ensureRecordMorningSickToStringDoesNotPrintAnyInfo() {
        final AbsencePeriod.RecordMorningSick morning = new AbsencePeriod.RecordMorningSick(1);
        assertThat(morning).hasToString("AbstractRecordInfo{type=*****, id=1, status=****}");
    }

    @Test
    void ensureRecordNoonSickToStringDoesNotPrintAnyInfo() {
        final AbsencePeriod.RecordNoonSick noon = new AbsencePeriod.RecordNoonSick(1);
        assertThat(noon).hasToString("AbstractRecordInfo{type=*****, id=1, status=****}");
    }
}
