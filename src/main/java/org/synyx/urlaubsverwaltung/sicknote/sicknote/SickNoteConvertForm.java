package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Represents a form to convert a sick note to vacation.
 */
public class SickNoteConvertForm {

    private Person person;
    private DayLength dayLength;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long vacationType;
    private String reason;

    public SickNoteConvertForm() {
        // needed for Spring magic
    }

    public SickNoteConvertForm(SickNote sickNote) {
        this.person = sickNote.getPerson();
        this.dayLength = sickNote.getDayLength();
        this.startDate = sickNote.getStartDate();
        this.endDate = sickNote.getEndDate();
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public DayLength getDayLength() {
        return dayLength;
    }

    public void setDayLength(DayLength dayLength) {
        this.dayLength = dayLength;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Long getVacationType() {
        return vacationType;
    }

    public void setVacationType(Long vacationType) {
        this.vacationType = vacationType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public DayOfWeek getWeekDayOfStartDate() {
        return getStartDate().getDayOfWeek();
    }

    public DayOfWeek getWeekDayOfEndDate() {
        return getEndDate().getDayOfWeek();
    }
}
