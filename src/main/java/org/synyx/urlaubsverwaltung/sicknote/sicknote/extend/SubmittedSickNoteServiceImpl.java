package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteEntity;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteMapper;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SubmittedSickNote;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionStatus.SUBMITTED;

@Service
class SubmittedSickNoteServiceImpl implements SubmittedSickNoteService {

    private final SickNoteExtensionRepository extensionRepository;
    private final SickNoteService sickNoteService;
    private final WorkingTimeCalendarService workingTimeCalendarService;
    private final SickNoteMapper sickNoteMapper;

    SubmittedSickNoteServiceImpl(SickNoteExtensionRepository extensionRepository, SickNoteService sickNoteService, WorkingTimeCalendarService workingTimeCalendarService, SickNoteMapper sickNoteMapper) {
        this.extensionRepository = extensionRepository;
        this.sickNoteService = sickNoteService;
        this.workingTimeCalendarService = workingTimeCalendarService;
        this.sickNoteMapper = sickNoteMapper;
    }

    @Override
    public List<SubmittedSickNote> findSubmittedSickNotes(List<Person> persons) {
        final Stream<SubmittedSickNote> sickNotes = findSickNotesWithStatusSubmitted(persons);
        final Stream<SubmittedSickNote> extensions = findExtensionsWithStatusSubmitted(persons);
        return Stream.concat(sickNotes, extensions).distinct().toList();
    }

    private Stream<SubmittedSickNote> findSickNotesWithStatusSubmitted(List<Person> persons) {
        return sickNoteService.getForStatesAndPerson(List.of(SickNoteStatus.SUBMITTED), persons)
            .stream().map(SubmittedSickNote::new);
    }

    private Stream<SubmittedSickNote> findExtensionsWithStatusSubmitted(List<Person> persons) {

        final List<Long> personIds = persons.stream().map(Person::getId).toList();
        final List<SickNoteExtensionProjection> projections = extensionRepository.findAllByStatusAndPersonIsIn(SUBMITTED, personIds);

        if (projections.isEmpty()) {
            return Stream.of();
        }

        final Map<Person, WorkingTimeCalendar> workingTimesByPersons = getWorkingTimeCalendars(persons, projections);

        return projections
            .stream()
            .map(projection -> {
                final SickNoteEntity sickNoteEntity = projection.getSickNote();
                final SickNoteExtensionEntity extensionEntity = projection.getSickNoteExtension();

                final Person person = sickNoteEntity.getPerson();
                final WorkingTimeCalendar workingTimeCalendar = workingTimesByPersons.get(person);

                final SickNote sickNote = sickNoteMapper.toSickNote(sickNoteEntity, workingTimeCalendar);

                final BigDecimal additionalWorkdays = workingTimeCalendar.workingTime(sickNote.getEndDate(), extensionEntity.getNewEndDate())
                    // workingTime for the same date is 1. therefore we have to subtract 1 for "additional" days
                    .subtract(BigDecimal.ONE);

                final SickNoteExtension extension = toSickNoteExtension(extensionEntity, additionalWorkdays);

                return new SubmittedSickNote(sickNote, Optional.of(extension));
            });
    }

    private Map<Person, WorkingTimeCalendar> getWorkingTimeCalendars(List<Person> persons, List<SickNoteExtensionProjection> projections) {
        final LocalDate minStartDate = getMinStartDate(projections);
        final LocalDate maxEndDate = getMaxEndDate(projections);
        final DateRange dateRange = new DateRange(minStartDate, maxEndDate);
        return workingTimeCalendarService.getWorkingTimesByPersons(persons, dateRange);
    }

    private LocalDate getMinStartDate(List<SickNoteExtensionProjection> projections) {
        return projections.stream()
            .map(SickNoteExtensionProjection::getSickNote)
            .map(SickNoteEntity::getStartDate)
            .min(LocalDate::compareTo)
            .orElseThrow(() -> new IllegalStateException("expected at least one sickNote"));
    }

    private LocalDate getMaxEndDate(List<SickNoteExtensionProjection> projections) {
        return projections.stream()
            .map(projection -> List.of(projection.getSickNote().getEndDate(), projection.getSickNoteExtension().getNewEndDate()))
            .flatMap(List::stream)
            .max(LocalDate::compareTo)
            .orElseThrow(() -> new IllegalStateException("expected at least one element."));
    }

    private SickNoteExtension toSickNoteExtension(SickNoteExtensionEntity entity, BigDecimal additionalWorkdays) {
        return new SickNoteExtension(
            entity.getId(),
            entity.getSickNoteId(),
            entity.getNewEndDate(),
            entity.getStatus(),
            additionalWorkdays
        );
    }
}
