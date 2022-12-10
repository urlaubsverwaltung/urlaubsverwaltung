package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.springframework.format.annotation.DateTimeFormat;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;

import java.time.LocalDate;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.DD_MM_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YYYY;

/**
 * View class representing an sick note.
 */
public class SickNoteForm {

    private Integer id;
    private Person person;
    private SickNoteType sickNoteType;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY})
    private LocalDate startDate;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY})
    private LocalDate endDate;
    private DayLength dayLength = FULL;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY})
    private LocalDate aubStartDate;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY})
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
        return startDate == null ? "" : startDate.format(ISO_DATE);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getEndDateIsoValue() {
        return endDate == null ? "" : endDate.format(ISO_DATE);
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
        return aubStartDate == null ? "" : aubStartDate.format(ISO_DATE);
    }

    public void setAubStartDate(LocalDate aubStartDate) {
        this.aubStartDate = aubStartDate;
    }

    public LocalDate getAubEndDate() {
        return aubEndDate;
    }

    public String getAubEndDateIsoValue() {
        return aubEndDate == null ? "" : aubEndDate.format(ISO_DATE);
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
