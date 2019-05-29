package org.synyx.urlaubsverwaltung.sicknote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.AbsenceSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;


/**
 * Implementation for {@link org.synyx.urlaubsverwaltung.sicknote.SickNoteService}.
 */
@Service
class SickNoteServiceImpl implements SickNoteService {

    private final SickNoteDAO sickNoteDAO;
    private final SettingsService settingsService;

    @Autowired
    public SickNoteServiceImpl(SickNoteDAO sickNoteDAO, SettingsService settingsService) {

        this.sickNoteDAO = sickNoteDAO;
        this.settingsService = settingsService;
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
    public List<SickNote> getByPersonAndPeriod(Person person, LocalDate from, LocalDate to) {

        return sickNoteDAO.findByPersonAndPeriod(person, from, to);
    }


    @Override
    public List<SickNote> getByPeriod(LocalDate from, LocalDate to) {

        return sickNoteDAO.findByPeriod(from, to);
    }


    @Override
    public List<SickNote> getSickNotesReachingEndOfSickPay() {

        Settings settings = settingsService.getSettings();
        AbsenceSettings absenceSettings = settings.getAbsenceSettings();

        LocalDate endDate = ZonedDateTime.now(UTC)
            .plusDays(absenceSettings.getDaysBeforeEndOfSickPayNotification())
            .toLocalDate();

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
}
