package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.calendar.CalendarAbsenceType.DEFAULT;
import static org.synyx.urlaubsverwaltung.calendar.CalendarAbsenceType.HOLIDAY_REPLACEMENT;

class CalendarAbsenceTypeTest {

    @Test
    void ensuresTheDefaultValuesOfTheMessageKeys() {
        assertThat(DEFAULT.getMessageKey()).isEqualTo("calendar.absence.type.default");
        assertThat(HOLIDAY_REPLACEMENT.getMessageKey()).isEqualTo("calendar.absence.type.holidayReplacement");
    }
}
