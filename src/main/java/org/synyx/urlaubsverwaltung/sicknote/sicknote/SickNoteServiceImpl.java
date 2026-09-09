package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.SUBMITTED;

/**
 * Implementation for {@link SickNoteService}.
 */
@Service
class SickNoteServiceImpl implements SickNoteService {

    private final SickNoteRepository sickNoteRepository;
    private final SettingsService settingsService;
    private final WorkingTimeCalendarService workingTimeCalendarService;
    private final SickNoteMapper sickNoteMapper;
    private final Clock clock;

    SickNoteServiceImpl(SickNoteRepository sickNoteRepository, SettingsService settingsService,
                        WorkingTimeCalendarService workingTimeCalendarService, SickNoteMapper sickNoteMapper,
                        Clock clock) {

        this.sickNoteRepository = sickNoteRepository;
        this.settingsService = settingsService;
        this.workingTimeCalendarService = workingTimeCalendarService;
        this.sickNoteMapper = sickNoteMapper;
        this.clock = clock;
    }

    @Override
    public SickNote save(SickNote sickNote) {

        final SickNoteEntity entity = toSickNoteEntity(sickNote);
        entity.setLastEdited(LocalDate.now(clock));

        final SickNoteEntity saved = sickNoteRepository.save(entity);
        return sickNoteMapper.toSickNote(saved);
    }

    @Override
    public Optional<SickNote> getById(Long id) {

        final Optional<SickNoteEntity> maybe = sickNoteRepository.findById(id);
        if (maybe.isEmpty()) {
            return Optional.empty();
        }

        final SickNoteEntity sickNoteEntity = maybe.get();
        final WorkingTimeCalendar workingTimes = getWorkingTimeCalendar(sickNoteEntity);

        return Optional.of(sickNoteMapper.toSickNote(sickNoteEntity, workingTimes));
    }

    @Override
    public List<SickNote> getByPersonAndPeriod(Person person, LocalDate from, LocalDate to) {
        final List<SickNoteEntity> entities = sickNoteRepository.findByPersonAndPeriod(person, from, to);
        return sickNoteMapper.toSickNoteWithWorkDays(entities, new DateRange(from, to));
    }

    @Override
    public Optional<SickNote> getSickNoteOfYesterdayOrLastWorkDay(Person person) {

        final LocalDate now = LocalDate.now(clock);
        final Optional<SickNoteEntity> lastSickNote = sickNoteRepository.findFirstByPersonAndStatusInAndEndDateIsLessThanOrderByEndDateDesc(person, List.of(SUBMITTED, ACTIVE), now);

        if (lastSickNote.isPresent()) {

            final SickNoteEntity sickNoteEntity = lastSickNote.get();

            if (endsOnLastWorkDayBefore(sickNoteEntity, now)) {
                final WorkingTimeCalendar workingTimes = getWorkingTimeCalendar(sickNoteEntity);
                return Optional.of(sickNoteMapper.toSickNote(sickNoteEntity, workingTimes));
            }
        }

        return Optional.empty();
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
            .map(sickNoteMapper::toSickNote)
            .toList();
    }

    @Override
    public List<SickNote> getAllActiveByPeriod(LocalDate from, LocalDate to) {
        final List<SickNoteEntity> entities = sickNoteRepository.findByPersonPermissionsIsInAndStatusInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(List.of(USER), List.of(ACTIVE), from, to);
        return sickNoteMapper.toSickNoteWithWorkDays(entities, new DateRange(from, to));
    }

    @Override
    public List<SickNote> getForStatesSince(List<SickNoteStatus> sickNoteStatuses, LocalDate since) {
        final List<SickNoteEntity> entities = sickNoteRepository.findByStatusInAndEndDateGreaterThanEqual(sickNoteStatuses, since);
        return sickNoteMapper.toSickNoteWithWorkDays(entities, new DateRange(since, LocalDate.now(clock)));
    }

    @Override
    public List<SickNote> getForStatesAndPerson(List<SickNoteStatus> sickNoteStatuses, List<Person> persons) {
        final List<SickNoteEntity> entities = sickNoteRepository.findByStatusInAndPersonIn(sickNoteStatuses, persons);

        final Optional<LocalDate> min = entities.stream().min(comparing(SickNoteEntity::getStartDate)).map(SickNoteEntity::getStartDate);
        final Optional<LocalDate> max = entities.stream().max(comparing(SickNoteEntity::getEndDate)).map(SickNoteEntity::getEndDate);
        if (min.isEmpty() || max.isEmpty()) {
            return List.of();
        }

        return sickNoteMapper.toSickNoteWithWorkDays(entities, new DateRange(min.get(), max.get()));
    }

    @Override
    public List<SickNote> getForStatesAndPersonSince(List<SickNoteStatus> sickNoteStatuses, List<Person> persons, LocalDate since) {
        final List<SickNoteEntity> entities = sickNoteRepository.findByStatusInAndPersonInAndEndDateIsGreaterThanEqual(sickNoteStatuses, persons, since);
        return sickNoteMapper.toSickNoteWithWorkDays(entities, new DateRange(since, LocalDate.now(clock)));
    }

    @Override
    public List<SickNote> getForStatesAndPerson(List<SickNoteStatus> sickNoteStatus, List<Person> persons, LocalDate start, LocalDate end) {
        final List<SickNoteEntity> entities = sickNoteRepository.findByStatusInAndPersonInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(sickNoteStatus, persons, start, end);
        return sickNoteMapper.toSickNoteWithWorkDays(entities, new DateRange(start, end));
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
            .map(sickNoteMapper::toSickNote)
            .toList();
    }

    @Override
    public void deleteSickNoteApplier(Person applier) {
        final List<SickNoteEntity> entities = sickNoteRepository.findByApplier(applier);
        for (SickNoteEntity entity : entities) {
            entity.setApplier(null);
        }
        sickNoteRepository.saveAll(entities);
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

    /**
     * Whether the given sick note ends on the last day the person worked before the given date. That is the case if the
     * next work day following the end of the sick note is the given date itself, in other words: every day in between is
     * a day the person does not work on - a weekend, a public holiday or a day off. A sick note ending on the day before
     * the given date always does.
     *
     * @param sickNoteEntity sick note to check, has to end before the given date
     * @param date           date to look back from, usually today
     * @return {@code true} if no work day lies between the end of the sick note and the given date
     */
    private boolean endsOnLastWorkDayBefore(SickNoteEntity sickNoteEntity, LocalDate date) {

        final Person person = sickNoteEntity.getPerson();
        final LocalDate endDate = sickNoteEntity.getEndDate();

        final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarService
            .getWorkingTimesByPersons(List.of(person), new DateRange(endDate, date))
            .get(person);

        // the calendar ends with the given date, therefore no next work day at all means the person does not work
        // on any of the days in between - and not on the given date either.
        return workingTimeCalendar.nextWorkingFollowingTo(endDate)
            .map(nextWorkDay -> !nextWorkDay.isBefore(date))
            .orElse(true);
    }

    private WorkingTimeCalendar getWorkingTimeCalendar(SickNoteEntity sickNoteEntity) {

        final Person person = sickNoteEntity.getPerson();
        final LocalDate startDate = sickNoteEntity.getStartDate();
        final LocalDate endDate = sickNoteEntity.getEndDate();

        final DateRange dateRange = new DateRange(startDate, endDate);

        return workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), dateRange)
            .get(person);
    }
}
