package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

public record SickNoteDto(String id, LocalDate startDate, DayOfWeek weekDayOfStartDate, LocalDate endDate,
                          DayLength dayLength, BigDecimal workDays, SickNotePersonDto person, String type,
                          String status) {

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
}
