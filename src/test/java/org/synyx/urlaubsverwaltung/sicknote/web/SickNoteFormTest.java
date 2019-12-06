package org.synyx.urlaubsverwaltung.sicknote.web;

import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteType;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class SickNoteFormTest {

    private final LocalDate day2019_04_16 = LocalDate.of(2019, 4, 16);
    private final Integer id = 1;
    private final Person person = new Person();
    private final SickNoteType type = new SickNoteType();
    private final DayLength dayLength = DayLength.FULL;
    private final String comment = "my comment";

    private SickNoteForm sut;

    @Before
    public void setUp() {
        sut = new SickNoteForm();

        sut.setId(id);
        sut.setPerson(person);
        sut.setSickNoteType(type);
        sut.setStartDate(day2019_04_16);
        sut.setEndDate(day2019_04_16);
        sut.setDayLength(dayLength);
        sut.setAubStartDate(day2019_04_16);
        sut.setAubEndDate(day2019_04_16);
        sut.setComment(comment);
    }

    @Test
    public void checkGeneratedSickNote() {

        SickNote sickNote = sut.generateSickNote();


        assertThat(sickNote.getId()).isEqualTo(id);
        assertThat(sickNote.getPerson()).isEqualTo(person);
        assertThat(sickNote.getSickNoteType()).isEqualTo(type);
        assertThat(sickNote.getStartDate()).isEqualTo(day2019_04_16);
        assertThat(sickNote.getEndDate()).isEqualTo(day2019_04_16);
        assertThat(sickNote.getDayLength()).isEqualTo(dayLength);
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
        assertThat(sickNoteForm.getDayLength()).isEqualTo(dayLength);
        assertThat(sickNoteForm.getAubStartDate()).isEqualTo(day2019_04_16);
        assertThat(sickNoteForm.getAubEndDate()).isEqualTo(day2019_04_16);
    }
}
