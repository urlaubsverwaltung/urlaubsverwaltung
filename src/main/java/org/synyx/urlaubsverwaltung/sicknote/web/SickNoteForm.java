package org.synyx.urlaubsverwaltung.sicknote.web;

import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteType;

import java.time.LocalDate;

import static java.time.format.DateTimeFormatter.ISO_DATE;

/**
 * View class representing an sick note.
 */
public class SickNoteForm {

    private Integer id;
    private Person person;
    private SickNoteType sickNoteType;
    private LocalDate startDate;
    private LocalDate endDate;
    private DayLength dayLength;
    private LocalDate aubStartDate;
    private LocalDate aubEndDate;
    private String comment;

    protected SickNoteForm() {
        // default constructor needed for empty object
    }

    SickNoteForm(SickNote sickNote) {
        this.id = sickNote.getId();
        this.person = sickNote.getPerson();
        this.sickNoteType = sickNote.getSickNoteType();
        this.startDate = sickNote.getStartDate();
        this.endDate = sickNote.getEndDate();
        this.dayLength = sickNote.getDayLength();
        this.aubStartDate = sickNote.getAubStartDate();
        this.aubEndDate = sickNote.getAubEndDate();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public SickNoteType getSickNoteType() {
        return sickNoteType;
    }

    public void setSickNoteType(SickNoteType sickNoteType) {
        this.sickNoteType = sickNoteType;
    }

    public String getStartDateIsoValue() {
        if (startDate == null) {
            return "";
        }

        return startDate.format(ISO_DATE);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getEndDateIsoValue() {
        if (endDate == null) {
            return "";
        }

        return endDate.format(ISO_DATE);
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public DayLength getDayLength() {
        return dayLength;
    }

    public void setDayLength(DayLength dayLength) {
        this.dayLength = dayLength;
    }

    public LocalDate getAubStartDate() {
        return aubStartDate;
    }

    public String getAubStartDateIsoValue() {
        if (aubStartDate == null) {
            return "";
        }

        return aubStartDate.format(ISO_DATE);
    }

    public void setAubStartDate(LocalDate aubStartDate) {
        this.aubStartDate = aubStartDate;
    }

    public LocalDate getAubEndDate() {
        return aubEndDate;
    }

    public String getAubEndDateIsoValue() {
        if (aubEndDate == null) {
            return "";
        }

        return aubEndDate.format(ISO_DATE);
    }

    public void setAubEndDate(LocalDate aubEndDate) {
        this.aubEndDate = aubEndDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    SickNote generateSickNote() {
        SickNote sickNote = new SickNote();
        sickNote.setId(id);
        sickNote.setPerson(person);
        sickNote.setSickNoteType(sickNoteType);
        sickNote.setStartDate(startDate);
        sickNote.setEndDate(endDate);
        sickNote.setDayLength(dayLength);
        sickNote.setAubStartDate(aubStartDate);
        sickNote.setAubEndDate(aubEndDate);

        return sickNote;
    }

    @Override
    public String toString() {
        return "SickNoteForm{" +
            "id=" + id +
            ", person=" + person +
            ", sickNoteType=" + sickNoteType +
            ", startDate=" + startDate +
            ", endDate=" + endDate +
            ", dayLength=" + dayLength +
            ", aubStartDate=" + aubStartDate +
            ", aubEndDate=" + aubEndDate + '\'' +
            '}';
    }
}
