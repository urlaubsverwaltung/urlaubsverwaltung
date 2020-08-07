package org.synyx.urlaubsverwaltung.sicknote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.AbsenceSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;


/**
 * Implementation for {@link org.synyx.urlaubsverwaltung.sicknote.SickNoteService}.
 */
@Service
class SickNoteServiceImpl implements SickNoteService {

    private final SickNoteDAO sickNoteDAO;
    private final SettingsService settingsService;
    private final Clock clock;

    @Autowired
    public SickNoteServiceImpl(SickNoteDAO sickNoteDAO, SettingsService settingsService, Clock clock) {

        this.sickNoteDAO = sickNoteDAO;
        this.settingsService = settingsService;
        this.clock = clock;
    }

    @Override
    public void save(SickNote sickNote) {

        sickNoteDAO.save(sickNote);
    }


    @Override
    public Optional<SickNote> getById(Integer id) {

        return sickNoteDAO.findById(id);
    }


    @Override
    public List<SickNote> getByPersonAndPeriod(Person person, Instant from, Instant to) {

        return sickNoteDAO.findByPersonAndPeriod(person, from, to);
    }


    @Override
    public List<SickNote> getByPeriod(Instant from, Instant to) {

        return sickNoteDAO.findByPeriod(from, to);
    }


    @Override
    public List<SickNote> getSickNotesReachingEndOfSickPay() {

        Settings settings = settingsService.getSettings();
        AbsenceSettings absenceSettings = settings.getAbsenceSettings();

        Instant endDate = Instant.now(clock)
            .plus(absenceSettings.getDaysBeforeEndOfSickPayNotification(), ChronoUnit.DAYS);

        return sickNoteDAO.findSickNotesByMinimumLengthAndEndDate(absenceSettings.getMaximumSickPayDays(), endDate);
    }

    @Override
    public List<SickNote> getAllActiveByYear(int year) {

        return sickNoteDAO.findAllActiveByYear(year);
    }

    @Override
    public Long getNumberOfPersonsWithMinimumOneSickNote(int year) {

        return sickNoteDAO.findNumberOfPersonsWithMinimumOneSickNote(year);
    }

    @Override
    public List<SickNote> getForStates(List<SickNoteStatus> sickNoteStatuses) {

        return sickNoteDAO.findByStatusIn(sickNoteStatuses);
    }

    @Override
    public List<SickNote> getForStatesAndPerson(List<SickNoteStatus> sickNoteStatuses, List<Person> persons) {

        return sickNoteDAO.findByStatusInAndPersonIn(sickNoteStatuses, persons);
    }
}
