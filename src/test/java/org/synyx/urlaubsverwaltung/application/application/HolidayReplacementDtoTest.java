package org.synyx.urlaubsverwaltung.application.application;


import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;

import static org.assertj.core.api.Assertions.assertThat;

class HolidayReplacementDtoTest {

    @Test
    void ensureDoNotLogNote() {
        final Person holidayReplacement = new Person();
        holidayReplacement.setId(1L);
        final HolidayReplacementDto holidayReplacementDto = new HolidayReplacementDto();
        holidayReplacementDto.setPerson(holidayReplacement);
        holidayReplacementDto.setNote("This is some text for the replacement note");

        assertThat(holidayReplacementDto)
            .hasToString("HolidayReplacementDto{person=Person{id='1'}, departments=null}");
        assertThat(holidayReplacementDto.toString()).doesNotContain("note");
    }
}
