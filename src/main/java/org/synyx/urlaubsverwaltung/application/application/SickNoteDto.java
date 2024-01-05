package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

public class SickNoteDto {
    private String id;
    private LocalDate startDate;
    private DayOfWeek weekDayOfStartDate;
    private LocalDate endDate;
    private DayLength dayLength;
    private BigDecimal workDays;
    private SickNotePersonDto person;
    private String type;
    private String status;

    private SickNoteDto(String id, LocalDate startDate, DayOfWeek weekDayOfStartDate, LocalDate endDate,
                       DayLength dayLength, BigDecimal workDays, SickNotePersonDto person, String type,
                       String status) {
        this.id = id;
        this.startDate = startDate;
        this.weekDayOfStartDate = weekDayOfStartDate;
        this.endDate = endDate;
        this.dayLength = dayLength;
        this.workDays = workDays;
        this.person = person;
        this.type = type;
        this.status = status;
    }

    SickNoteDto(SickNote sickNote) {
        this(sickNote.getId().toString(),
                sickNote.getStartDate(),
                sickNote.getWeekDayOfStartDate(),
                sickNote.getEndDate(),
                sickNote.getDayLength(),
                sickNote.getWorkDays(),
                new SickNotePersonDto(sickNote.getPerson().getNiceName(), sickNote.getPerson().getGravatarURL(), sickNote.getPerson().isInactive(), sickNote.getPerson().getId()),
                sickNote.getSickNoteType().getCategory().getMessageKey(),
                sickNote.getStatus().name());
    }

    public String getId() {
        return id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public DayOfWeek getWeekDayOfStartDate() {
        return weekDayOfStartDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public DayLength getDayLength() {
        return dayLength;
    }

    public BigDecimal getWorkDays() {
        return workDays;
    }

    public SickNotePersonDto getPerson() {
        return person;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

}
