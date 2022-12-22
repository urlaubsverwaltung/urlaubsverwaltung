package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;

/**
 * Implementation for {@link SickNoteService}.
 */
@Service
class SickNoteServiceImpl implements SickNoteService {

    private final SickNoteRepository sickNoteRepository;
    private final SettingsService settingsService;
    private final WorkingTimeCalendarService workingTimeCalendarService;
    private final Clock clock;

    @Autowired
    SickNoteServiceImpl(SickNoteRepository sickNoteRepository, SettingsService settingsService,
                        WorkingTimeCalendarService workingTimeCalendarService, Clock clock) {

        this.sickNoteRepository = sickNoteRepository;
        this.settingsService = settingsService;
        this.workingTimeCalendarService = workingTimeCalendarService;
        this.clock = clock;
    }

    @Override
    public SickNote save(SickNote sickNote) {
        final SickNoteEntity entity = toSickNoteEntity(sickNote);
        entity.setLastEdited(LocalDate.now(clock));

        final SickNoteEntity saved = sickNoteRepository.save(entity);

        return toSickNote(saved);
    }

    @Override
    public Optional<SickNote> getById(Long id) {
        return sickNoteRepository.findById(id)
            .map(SickNoteServiceImpl::toSickNote)
            .map(sickNote -> {
                final Person person = sickNote.getPerson();
                final DateRange dateRange = new DateRange(sickNote.getStartDate(), sickNote.getEndDate());
                final Map<Person, WorkingTimeCalendar> workingTimesByPersons = workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), dateRange);
                return SickNote.builder(sickNote).workingTimeCalendar(workingTimesByPersons.get(sickNote.getPerson())).build();
            });
    }

    @Override
    public List<SickNote> getByPersonAndPeriod(Person person, LocalDate from, LocalDate to) {
        final List<SickNoteEntity> entities = sickNoteRepository.findByPersonAndPeriod(person, from, to);
        return toSickNoteWithWorkDays(entities, new DateRange(from, to));
    }

    @Override
    public List<SickNote> getSickNotesReachingEndOfSickPay() {

        final Settings settings = settingsService.getSettings();
        final SickNoteSettings sickNoteSettings = settings.getSickNoteSettings();

        final LocalDate today = LocalDate.now(clock);
        final Integer maximumSickPayDays = sickNoteSettings.getMaximumSickPayDays();
        final Integer daysBeforeEndOfSickPayNotification = sickNoteSettings.getDaysBeforeEndOfSickPayNotification();

        return sickNoteRepository.findSickNotesToNotifyForSickPayEnd(maximumSickPayDays, daysBeforeEndOfSickPayNotification, today)
            .stream()
            .map(SickNoteServiceImpl::toSickNote)
            .collect(toList());
    }

    @Override
    public List<SickNote> getAllActiveByPeriod(LocalDate from, LocalDate to) {
        final List<SickNoteEntity> entities = sickNoteRepository.findByPersonPermissionsIsInAndStatusInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(List.of(USER), List.of(ACTIVE), from, to);
        return toSickNoteWithWorkDays(entities, new DateRange(from, to));
    }

    @Override
    public List<SickNote> getForStatesSince(List<SickNoteStatus> sickNoteStatuses, LocalDate since) {
        final List<SickNoteEntity> entities = sickNoteRepository.findByStatusInAndEndDateGreaterThanEqual(sickNoteStatuses, since);
        return toSickNoteWithWorkDays(entities, new DateRange(since, LocalDate.now(clock)));
    }

    @Override
    public List<SickNote> getForStatesAndPersonSince(List<SickNoteStatus> sickNoteStatuses, List<Person> persons, LocalDate since) {
        final List<SickNoteEntity> entities = sickNoteRepository.findByStatusInAndPersonInAndEndDateIsGreaterThanEqual(sickNoteStatuses, persons, since);
        return toSickNoteWithWorkDays(entities, new DateRange(since, LocalDate.now(clock)));
    }

    @Override
    public List<SickNote> getForStatesAndPerson(List<SickNoteStatus> sickNoteStatus, List<Person> persons, LocalDate start, LocalDate end) {
        final List<SickNoteEntity> entities = sickNoteRepository.findByStatusInAndPersonInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(sickNoteStatus, persons, start, end);
        return toSickNoteWithWorkDays(entities, new DateRange(start, end));
    }

    @Override
    public List<SickNote> getForStatesAndPersonAndPersonHasRoles(List<SickNoteStatus> sickNoteStatus, List<Person> persons, List<Role> roles, LocalDate start, LocalDate end) {
        final List<SickNoteEntity> entities = sickNoteRepository.findByStatusInAndPersonInAndPersonPermissionsInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(sickNoteStatus, persons, roles, start, end);
        return toSickNoteWithWorkDays(entities, new DateRange(start, end));
    }

    @Override
    public void setEndOfSickPayNotificationSend(SickNote sickNote) {

        final SickNote sickNoteWithNewNotificationSendDate = SickNote.builder(sickNote)
            .endOfSickPayNotificationSend(LocalDate.now(clock))
            .build();

        sickNoteRepository.save(toSickNoteEntity(sickNoteWithNewNotificationSendDate));
    }

    @Override
    public List<SickNote> deleteAllByPerson(Person person) {
        return sickNoteRepository.deleteByPerson(person)
            .stream()
            .map(SickNoteServiceImpl::toSickNote)
            .collect(toList());
    }

    @Override
    public void deleteSickNoteApplier(Person applier) {

        final List<SickNoteEntity> sickNoteEntities = sickNoteRepository.findByApplier(applier)
            .stream()
            .map(SickNoteServiceImpl::toSickNote)
            .map(SickNoteServiceImpl::sickNoteWithoutApplier)
            .map(SickNoteServiceImpl::toSickNoteEntity)
            .collect(toList());

        sickNoteRepository.saveAll(sickNoteEntities);
    }

    private static SickNote sickNoteWithoutApplier(SickNote sickNote) {
        return SickNote.builder(sickNote)
            .applier(null)
            .build();
    }

    private static SickNoteEntity toSickNoteEntity(SickNote sickNote) {
        final SickNoteEntity entity = new SickNoteEntity();
        entity.setId(sickNote.getId());
        entity.setPerson(sickNote.getPerson());
        entity.setApplier(sickNote.getApplier());
        entity.setSickNoteType(sickNote.getSickNoteType());
        entity.setStartDate(sickNote.getStartDate());
        entity.setEndDate(sickNote.getEndDate());
        entity.setDayLength(sickNote.getDayLength());
        entity.setAubStartDate(sickNote.getAubStartDate());
        entity.setAubEndDate(sickNote.getAubEndDate());
        entity.setLastEdited(sickNote.getLastEdited());
        entity.setEndOfSickPayNotificationSend(sickNote.getEndOfSickPayNotificationSend());
        entity.setStatus(sickNote.getStatus());
        return entity;
    }

    private static SickNote toSickNote(SickNoteEntity entity) {

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

    private List<SickNote> toSickNoteWithWorkDays(Collection<SickNoteEntity> entities, DateRange dateRange) {
        if (entities.isEmpty()) {
            return List.of();
        }

        final List<Person> personsWithSickNotes = entities.stream().map(SickNoteEntity::getPerson).distinct().collect(toList());
        final Map<Person, WorkingTimeCalendar> workingTimesByPersons = workingTimeCalendarService.getWorkingTimesByPersons(personsWithSickNotes, dateRange);

        return entities
            .stream()
            .map(SickNoteServiceImpl::toSickNote)
            .map(sickNote -> SickNote.builder(sickNote).workingTimeCalendar(workingTimesByPersons.get(sickNote.getPerson())).build())
            .collect(toList());
    }
}
