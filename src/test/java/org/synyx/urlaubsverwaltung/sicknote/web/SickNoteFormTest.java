package org.synyx.urlaubsverwaltung.sicknote.web;

import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteAction;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteComment;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteType;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

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

        assertEquals(id, sickNote.getId());
        assertEquals(person, sickNote.getPerson());
        assertEquals(type, sickNote.getSickNoteType());
        assertEquals(day2019_04_16, sickNote.getStartDate());
        assertEquals(day2019_04_16, sickNote.getEndDate());
        assertEquals(dayLength, sickNote.getDayLength());
        assertEquals(day2019_04_16, sickNote.getAubStartDate());
        assertEquals(day2019_04_16, sickNote.getAubEndDate());
    }

    @Test
    public void checkGeneratedSickNoteComment() {
        String comment = "my comment";
        sut.setComment(comment);

        SickNote sickNote = new SickNote();

        SickNoteComment sickNoteComment = sut.generateSickNoteComment(sickNote);

        assertEquals(comment, sickNoteComment.getText());
        assertEquals(SickNoteAction.COMMENTED, sickNoteComment.getAction());
        assertEquals(sickNote, sickNoteComment.getSickNote());
    }

    @Test
    public void checkCopyConstructur() {
        SickNote sickNote = sut.generateSickNote();

        SickNoteForm sickNoteForm = new SickNoteForm(sickNote);

        assertEquals(id, sickNoteForm.getId());
        assertEquals(person, sickNoteForm.getPerson());
        assertEquals(type, sickNoteForm.getSickNoteType());
        assertEquals(day2019_04_16, sickNoteForm.getStartDate());
        assertEquals(day2019_04_16, sickNoteForm.getEndDate());
        assertEquals(dayLength, sickNoteForm.getDayLength());
        assertEquals(day2019_04_16, sickNoteForm.getAubStartDate());
        assertEquals(day2019_04_16, sickNoteForm.getAubEndDate());
    }

    @Test
    public void verifyToString() {
        String stringRepresentation = "SickNoteForm{" +
            "id=1, " +
            "person=Person[id=<null>," +
                "loginName=<null>," +
                "lastName=<null>," +
                "firstName=<null>," +
                "email=<null>," +
                "permissions=[]" +
            "], " +
            "sickNoteType=SickNoteType[category=<null>," +
                "messageKey=<null>,id=<null>" +
            "], " +
            "startDate=2019-04-16, " +
            "endDate=2019-04-16, " +
            "dayLength=FULL, " +
            "aubStartDate=2019-04-16, " +
            "aubEndDate=2019-04-16, " +
            "comment='my comment'" +
        "}";

        assertEquals(stringRepresentation, sut.toString());
    }
}
