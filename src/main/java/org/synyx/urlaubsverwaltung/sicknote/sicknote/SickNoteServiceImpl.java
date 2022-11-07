package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;

/**
 * Implementation for {@link SickNoteService}.
 */
@Service
class SickNoteServiceImpl implements SickNoteService {

    private final SickNoteRepository sickNoteRepository;
    private final SettingsService settingsService;
    private final Clock clock;

    @Autowired
    SickNoteServiceImpl(SickNoteRepository sickNoteRepository, SettingsService settingsService, Clock clock) {
        this.sickNoteRepository = sickNoteRepository;
        this.settingsService = settingsService;
        this.clock = clock;
    }

    @Override
    public SickNote save(SickNote sickNote) {
        return sickNoteRepository.save(sickNote);
    }

    @Override
    public Optional<SickNote> getById(Integer id) {
        return sickNoteRepository.findById(id);
    }

    @Override
    public List<SickNote> getByPersonAndPeriod(Person person, LocalDate from, LocalDate to) {
        return sickNoteRepository.findByPersonAndPeriod(person, from, to);
    }

    @Override
    public List<SickNote> getActiveByPeriodAndPersonHasRole(LocalDate from, LocalDate to, List<Role> roles) {
        return sickNoteRepository.findByPersonPermissionsIsInAndStatusInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(roles, List.of(ACTIVE), from, to);
    }

    @Override
    public List<SickNote> getSickNotesReachingEndOfSickPay() {

        final Settings settings = settingsService.getSettings();
        final SickNoteSettings sickNoteSettings = settings.getSickNoteSettings();

        final LocalDate today = LocalDate.now(clock);
        final Integer maximumSickPayDays = sickNoteSettings.getMaximumSickPayDays();
        final Integer daysBeforeEndOfSickPayNotification = sickNoteSettings.getDaysBeforeEndOfSickPayNotification();

        return sickNoteRepository.findSickNotesToNotifyForSickPayEnd(maximumSickPayDays, daysBeforeEndOfSickPayNotification, today);
    }

    @Override
    public List<SickNote> getAllActiveByPeriod(LocalDate from, LocalDate to) {
        return sickNoteRepository.findByPersonPermissionsIsInAndStatusInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(List.of(USER), List.of(ACTIVE), from, to);
    }

    @Override
    public Long getNumberOfPersonsWithMinimumOneSickNote(int year) {
        return sickNoteRepository.findNumberOfPersonsWithMinimumOneSickNote(year);
    }

    @Override
    public List<SickNote> getForStatesSince(List<SickNoteStatus> sickNoteStatuses, LocalDate since) {
        return sickNoteRepository.findByStatusInAndEndDateGreaterThanEqual(sickNoteStatuses, since);
    }

    @Override
    public List<SickNote> getForStatesAndPersonSince(List<SickNoteStatus> sickNoteStatuses, List<Person> persons, LocalDate since) {
        return sickNoteRepository.findByStatusInAndPersonInAndEndDateIsGreaterThanEqual(sickNoteStatuses, persons, since);
    }

    @Override
    public List<SickNote> getForStatesAndPerson(List<SickNoteStatus> sickNoteStatus, List<Person> persons, LocalDate start, LocalDate end) {
        return sickNoteRepository.findByStatusInAndPersonInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(sickNoteStatus, persons, start, end);
    }

    @Override
    public List<SickNote> getForStatesAndPersonAndPersonHasRoles(List<SickNoteStatus> sickNoteStatus, List<Person> persons, List<Role> roles, LocalDate start, LocalDate end) {
        return sickNoteRepository.findByStatusInAndPersonInAndPersonPermissionsInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(sickNoteStatus, persons, roles, start, end);
    }

    @Override
    public void setEndOfSickPayNotificationSend(SickNote sickNote) {

        sickNote.setEndOfSickPayNotificationSend(LocalDate.now(clock));
        sickNoteRepository.save(sickNote);
    }

    @Override
    public void deleteAllByPerson(Person person) {
        sickNoteRepository.deleteByPerson(person);
    }

    @Override
    public void deleteSickNoteApplier(Person applier) {
        final List<SickNote> sickNotes = sickNoteRepository.findByApplier(applier);
        sickNotes.forEach(sickNote -> sickNote.setApplier(null));
        sickNoteRepository.saveAll(sickNotes);
    }
}
