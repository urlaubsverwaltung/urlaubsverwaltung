package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Collections.max;
import static java.util.Collections.min;
import static java.util.Comparator.naturalOrder;

@Component
public class SickNoteMapper {

    private final WorkingTimeCalendarService workingTimeCalendarService;

    SickNoteMapper(WorkingTimeCalendarService workingTimeCalendarService) {
        this.workingTimeCalendarService = workingTimeCalendarService;
    }

    public SickNote toSickNote(SickNoteEntity entity) {

        final LocalDate startDate = entity.getStartDate();
        final LocalDate endDate = entity.getEndDate();
        final DayLength dayLength = entity.getDayLength();

        return SickNote.builder()
            .id(entity.getId())
            .person(entity.getPerson())
            .applier(entity.getApplier())
            .sickNoteType(entity.getSickNoteType())
            .startDate(startDate)
            .endDate(endDate)
            .dayLength(dayLength)
            .aubStartDate(entity.getAubStartDate())
            .aubEndDate(entity.getAubEndDate())
            .lastEdited(entity.getLastEdited())
            .endOfSickPayNotificationSend(entity.getEndOfSickPayNotificationSend())
            .status(entity.getStatus())
            .build();
    }

    public SickNote toSickNote(SickNoteEntity entity, WorkingTimeCalendar workingTimeCalendar) {
        return SickNote.builder(toSickNote(entity))
            .workingTimeCalendar(workingTimeCalendar)
            .build();
    }

    public List<SickNote> toSickNoteWithWorkDays(Collection<SickNoteEntity> entities, DateRange dateRange) {
        if (entities.isEmpty()) {
            return List.of();
        }

        final List<Person> personsWithSickNotes = entities.stream().map(SickNoteEntity::getPerson).distinct().toList();

        // the work days of a sick note are its whole ones, also of a sick note reaching out of the requested range
        final LocalDate firstStartDate = entities.stream().map(SickNoteEntity::getStartDate).min(naturalOrder()).orElseThrow();
        final LocalDate lastEndDate = entities.stream().map(SickNoteEntity::getEndDate).max(naturalOrder()).orElseThrow();
        final DateRange calendarRange = new DateRange(min(List.of(dateRange.startDate(), firstStartDate)), max(List.of(dateRange.endDate(), lastEndDate)));

        final Map<Person, WorkingTimeCalendar> workingTimesByPersons = workingTimeCalendarService.getWorkingTimesByPersons(personsWithSickNotes, calendarRange);

        return entities
            .stream()
            .map(this::toSickNote)
            .map(sickNote -> SickNote.builder(sickNote).workingTimeCalendar(workingTimesByPersons.get(sickNote.getPerson())).build())
            .toList();
    }
}
