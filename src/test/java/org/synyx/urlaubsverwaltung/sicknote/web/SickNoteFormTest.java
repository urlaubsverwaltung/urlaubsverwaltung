package org.synyx.urlaubsverwaltung.sicknote.web;

import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteType;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class SickNoteFormTest {

    private final Instant day2019_04_16 = Instant.from(LocalDate.of(2019, 4, 16));
    private final Integer id = 1;
    private final Person person = new Person();
    private final SickNoteType type = new SickNoteType();

    private SickNoteForm sut;

    @Before
    public void setUp() {
        sut = new SickNoteForm();

        sut.setId(id);
        sut.setPerson(person);
        sut.setSickNoteType(type);
        sut.setStartDate(day2019_04_16);
        sut.setEndDate(day2019_04_16);
        sut.setDayLength(DayLength.FULL);
        sut.setAubStartDate(day2019_04_16);
        sut.setAubEndDate(day2019_04_16);
        sut.setComment("my comment");
    }

    @Test
    public void checkGeneratedSickNote() {

        SickNote sickNote = sut.generateSickNote();


        assertThat(sickNote.getId()).isEqualTo(id);
        assertThat(sickNote.getPerson()).isEqualTo(person);
        assertThat(sickNote.getSickNoteType()).isEqualTo(type);
        assertThat(sickNote.getStartDate()).isEqualTo(day2019_04_16);
        assertThat(sickNote.getEndDate()).isEqualTo(day2019_04_16);
        assertThat(sickNote.getDayLength()).isEqualTo(DayLength.FULL);
        assertThat(sickNote.getAubStartDate()).isEqualTo(day2019_04_16);
        assertThat(sickNote.getAubEndDate()).isEqualTo(day2019_04_16);
    }

    @Test
    public void checkCopyConstructur() {
        SickNote sickNote = sut.generateSickNote();

        SickNoteForm sickNoteForm = new SickNoteForm(sickNote);

        assertThat(sickNoteForm.getId()).isEqualTo(id);
        assertThat(sickNoteForm.getPerson()).isEqualTo(person);
        assertThat(sickNoteForm.getSickNoteType()).isEqualTo(type);
        assertThat(sickNoteForm.getStartDate()).isEqualTo(day2019_04_16);
        assertThat(sickNoteForm.getEndDate()).isEqualTo(day2019_04_16);
        assertThat(sickNoteForm.getDayLength()).isEqualTo(DayLength.FULL);
        assertThat(sickNoteForm.getAubStartDate()).isEqualTo(day2019_04_16);
        assertThat(sickNoteForm.getAubEndDate()).isEqualTo(day2019_04_16);
    }

    @Test
    public void toStringTest() {
        SickNote sickNote = sut.generateSickNote();
        SickNoteForm sickNoteForm = new SickNoteForm(sickNote);

        assertThat(sickNoteForm.toString()).isEqualTo("SickNoteForm{id=1, person=Person{id='null'}, " +
            "sickNoteType=SickNoteType{category=null, messageKey='null'}, " +
            "startDate=2019-04-16, endDate=2019-04-16, dayLength=FULL, aubStartDate=2019-04-16, aubEndDate=2019-04-16'}");
    }
}
