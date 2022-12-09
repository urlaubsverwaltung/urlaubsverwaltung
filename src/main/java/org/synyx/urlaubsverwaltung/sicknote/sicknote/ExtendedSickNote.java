package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Represents an extended {@link SickNote} with information about the number
 * of work days. (depending on working time of the person)
 */
public class ExtendedSickNote {

    private final Integer id;
    private final Person person;
    private final Person applier;
    private final SickNoteType sickNoteType;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final DayLength dayLength;
    private final LocalDate aubStartDate;
    private final LocalDate aubEndDate;
    private final LocalDate lastEdited;
    private final LocalDate endOfSickPayNotificationSend;
    private final SickNoteStatus status;

    private final BigDecimal workDays;

    public ExtendedSickNote(SickNote sickNote, WorkDaysCountService workDaysCountService) {
        this.id = sickNote.getId();
        this.person = sickNote.getPerson();
        this.applier = sickNote.getApplier();
        this.sickNoteType = sickNote.getSickNoteType();
        this.startDate = sickNote.getStartDate();
        this.endDate = sickNote.getEndDate();
        this.dayLength = sickNote.getDayLength();
        this.aubStartDate = sickNote.getAubStartDate();
        this.aubEndDate = sickNote.getAubEndDate();
        this.lastEdited = sickNote.getLastEdited();
        this.endOfSickPayNotificationSend = sickNote.getEndOfSickPayNotificationSend();
        this.status = sickNote.getStatus();

        // calculate the work days
        this.workDays = workDaysCountService.getWorkDaysCount(dayLength, startDate, endDate, person);
    }

    public Integer getId() {
        return id;
    }

    public Person getPerson() {
        return person;
    }

    public Person getApplier() {
        return applier;
    }

    public SickNoteType getSickNoteType() {
        return sickNoteType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public DayLength getDayLength() {
        return dayLength;
    }

    public LocalDate getAubStartDate() {
        return aubStartDate;
    }

    public LocalDate getAubEndDate() {
        return aubEndDate;
    }

    public LocalDate getLastEdited() {
        return lastEdited;
    }

    public LocalDate getEndOfSickPayNotificationSend() {
        return endOfSickPayNotificationSend;
    }

    public SickNoteStatus getStatus() {
        return status;
    }

    public BigDecimal getWorkDays() {
        return workDays;
    }

    public DayOfWeek getWeekDayOfStartDate() {
        return getStartDate().getDayOfWeek();
    }

    public DayOfWeek getWeekDayOfEndDate() {
        return getEndDate().getDayOfWeek();
    }

    public boolean isActive() {
        return SickNoteStatus.ACTIVE.equals(getStatus());
    }

    public boolean isAubPresent() {
        return getAubStartDate() != null && getAubEndDate() != null;
    }

    public Period getPeriod() {
        return new Period(getStartDate(), getEndDate(), getDayLength());
    }
}
