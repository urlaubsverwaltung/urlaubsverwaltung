package org.synyx.urlaubsverwaltung.application.dao;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.application.web.HolidayReplacementDto;
import org.synyx.urlaubsverwaltung.person.Person;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.application.dao.HolidayReplacementEntity.from;

class HolidayReplacementEntityTest {

    @Test
    void ensuresConvertingDtoToEntity() {
        final Person holidayReplacement = new Person();

        final HolidayReplacementDto holidayReplacementDto = new HolidayReplacementDto();
        holidayReplacementDto.setPerson(holidayReplacement);
        holidayReplacementDto.setNote("some note to the holiday replacement");

        final HolidayReplacementEntity holidayReplacementEntity = from(holidayReplacementDto);
        assertThat(holidayReplacementEntity.getPerson()).isEqualTo(holidayReplacement);
        assertThat(holidayReplacementEntity.getNote()).isEqualTo("some note to the holiday replacement");
    }

    @Test
    void ensureEquals() {

        final Person holidayReplacement = new Person();
        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);
        holidayReplacementEntity.setNote("first Note");

        final HolidayReplacementEntity sameHolidayReplacementEntity = new HolidayReplacementEntity();
        sameHolidayReplacementEntity.setPerson(holidayReplacement);
        sameHolidayReplacementEntity.setNote("first Note");

        final Person otherHolidayReplacement = new Person();
        final HolidayReplacementEntity holidayReplacementEntityOtherPerson = new HolidayReplacementEntity();
        holidayReplacementEntityOtherPerson.setPerson(otherHolidayReplacement);
        holidayReplacementEntityOtherPerson.setNote("first Note");

        final HolidayReplacementEntity holidayReplacementEntityOtherNote = new HolidayReplacementEntity();
        holidayReplacementEntityOtherNote.setPerson(holidayReplacement);
        holidayReplacementEntityOtherNote.setNote("second Note");

        assertThat(holidayReplacementEntity)
            .isEqualTo(holidayReplacementEntity)
            .isEqualTo(sameHolidayReplacementEntity)
            .isNotEqualTo(null)
            .isNotEqualTo(new Object())
            .isNotEqualTo(otherHolidayReplacement)
            .isNotEqualTo(holidayReplacementEntityOtherNote);
    }

    @Test
    void ensureHashCode() {

        final Person holidayReplacement = new Person();
        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);
        holidayReplacementEntity.setNote("first Note");

        final HolidayReplacementEntity sameHolidayReplacementEntity = new HolidayReplacementEntity();
        sameHolidayReplacementEntity.setPerson(holidayReplacement);
        sameHolidayReplacementEntity.setNote("first Note");

        final Person otherHolidayReplacement = new Person();
        final HolidayReplacementEntity holidayReplacementEntityOtherPerson = new HolidayReplacementEntity();
        holidayReplacementEntityOtherPerson.setPerson(otherHolidayReplacement);
        holidayReplacementEntityOtherPerson.setNote("first Note");

        final HolidayReplacementEntity holidayReplacementEntityOtherNote = new HolidayReplacementEntity();
        holidayReplacementEntityOtherNote.setPerson(holidayReplacement);
        holidayReplacementEntityOtherNote.setNote("second Note");

        assertThat(holidayReplacementEntity.hashCode())
            .isEqualTo(holidayReplacementEntity.hashCode())
            .isEqualTo(sameHolidayReplacementEntity.hashCode())
            .isNotEqualTo(otherHolidayReplacement.hashCode())
            .isNotEqualTo(new Object().hashCode())
            .isNotEqualTo(holidayReplacementEntityOtherNote.hashCode());
    }
}
