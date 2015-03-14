package org.synyx.urlaubsverwaltung.core.sicknote;

import com.google.common.base.Optional;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.List;


/**
 * Implentation for {@link org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class SickNoteServiceImpl implements SickNoteService {

    @Value("${sicknote.sickPay.limit}")
    private int sickPayLimit;

    @Value("${sicknote.sickPay.notification}")
    private int sickPayNotificationTime;

    private SickNoteDAO sickNoteDAO;

    @Autowired
    public SickNoteServiceImpl(SickNoteDAO sickNoteDAO) {

        this.sickNoteDAO = sickNoteDAO;
    }

    @Override
    public void save(SickNote sickNote) {

        sickNoteDAO.save(sickNote);
    }


    @Override
    public Optional<SickNote> getById(Integer id) {

        return Optional.fromNullable(sickNoteDAO.findOne(id));
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

        DateMidnight endDate = DateMidnight.now().plusDays(sickPayNotificationTime);

        return sickNoteDAO.findSickNotesByMinimumLengthAndEndDate(sickPayLimit, endDate.toDate());
    }
}
