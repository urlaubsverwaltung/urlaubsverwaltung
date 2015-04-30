package org.synyx.urlaubsverwaltung.core.sicknote;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;

import java.util.List;
import java.util.Optional;


/**
 * Implentation for {@link org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class SickNoteServiceImpl implements SickNoteService {

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

        return Optional.ofNullable(sickNoteDAO.findOne(id));
    }


    @Override
    public List<SickNote> getByPersonAndPeriod(Person person, DateMidnight from, DateMidnight to) {

        return sickNoteDAO.findByPersonAndPeriod(person, from.toDate(), to.toDate());
    }


    @Override
    public List<SickNote> getByPeriod(DateMidnight from, DateMidnight to) {

        return sickNoteDAO.findByPeriod(from.toDate(), to.toDate());
    }


    @Override
    public List<SickNote> getSickNotesReachingEndOfSickPay() {

        Settings settings = settingsService.getSettings();

        DateMidnight endDate = DateMidnight.now().plusDays(settings.getDaysBeforeEndOfSickPayNotification());

        return sickNoteDAO.findSickNotesByMinimumLengthAndEndDate(settings.getMaximumSickPayDays(), endDate.toDate());
    }
}
