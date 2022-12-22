package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class SickNoteFormDtoTest {

    private final LocalDate day2019_04_16 = LocalDate.of(2019, 4, 16);
    private final Long id = 1L;
    private final Person person = new Person();
    private final SickNoteType type = new SickNoteType();

    @Test
    void ensureEmptyStartDateValidFromIsoValue() {

        final SickNoteFormDto sut = new SickNoteFormDto();
        sut.setStartDate(null);

        assertThat(sut.getStartDateIsoValue()).isEmpty();
    }

    @Test
    void ensureStartDateValidFromIsoValue() {

        final SickNoteFormDto sut = new SickNoteFormDto();
        sut.setStartDate(LocalDate.parse("2020-10-30"));

        assertThat(sut.getStartDateIsoValue()).isEqualTo("2020-10-30");
    }

    @Test
    void ensureEmptyEndDateValidFromIsoValue() {

        final SickNoteFormDto sut = new SickNoteFormDto();
        sut.setEndDate(null);

        assertThat(sut.getEndDateIsoValue()).isEmpty();
    }

    @Test
    void ensureEndDateValidFromIsoValue() {

        final SickNoteFormDto sut = new SickNoteFormDto();
        sut.setEndDate(LocalDate.parse("2020-10-30"));

        assertThat(sut.getEndDateIsoValue()).isEqualTo("2020-10-30");
    }

    @Test
    void ensureEmptyAubStartDateValidFromIsoValue() {

        final SickNoteFormDto sut = new SickNoteFormDto();
        sut.setAubStartDate(null);

        assertThat(sut.getAubStartDateIsoValue()).isEmpty();
    }

    @Test
    void ensureAubStartDateValidFromIsoValue() {

        final SickNoteFormDto sut = new SickNoteFormDto();
        sut.setAubStartDate(LocalDate.parse("2020-10-30"));

        assertThat(sut.getAubStartDateIsoValue()).isEqualTo("2020-10-30");
    }

    @Test
    void ensureEmptyAubEndDateValidFromIsoValue() {

        final SickNoteFormDto sut = new SickNoteFormDto();
        sut.setAubEndDate(null);

        assertThat(sut.getAubEndDateIsoValue()).isEmpty();
    }

    @Test
    void ensureAubEndDateValidFromIsoValue() {

        final SickNoteFormDto sut = new SickNoteFormDto();
        sut.setAubEndDate(LocalDate.parse("2020-10-30"));

        assertThat(sut.getAubEndDateIsoValue()).isEqualTo("2020-10-30");
    }

    @Test
    void toStringTest() {

        final SickNoteFormDto sut = new SickNoteFormDto();
        sut.setId(id);
        sut.setPerson(person);
        sut.setSickNoteType(type);
        sut.setStartDate(day2019_04_16);
        sut.setEndDate(day2019_04_16);
        sut.setDayLength(DayLength.FULL);
        sut.setAubStartDate(day2019_04_16);
        sut.setAubEndDate(day2019_04_16);
        sut.setComment("my comment");

        assertThat(sut).hasToString("SickNoteForm{id=1, person=Person{id='null'}, " +
            "sickNoteType=SickNoteType{category=null, messageKey='null'}, " +
            "startDate=2019-04-16, endDate=2019-04-16, dayLength=FULL, aubStartDate=2019-04-16, aubEndDate=2019-04-16'}");
    }
}
