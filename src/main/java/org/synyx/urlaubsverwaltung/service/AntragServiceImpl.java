package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.dao.AntragDAO;
import org.synyx.urlaubsverwaltung.domain.Antrag;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.State;

import java.util.List;


/**
 * implementation of the requestdata-access-service.
 *
 * @author  johannes
 */
@Transactional
public class AntragServiceImpl implements AntragService {

    private AntragDAO antragDAO;

    // wird hier und im anderen service benötigt, weil wir ja
    // ständig irgendwelche mails schicken müssen... =)
    private MailServiceImpl mailService;

    @Autowired
    public AntragServiceImpl(AntragDAO antragDAO, MailServiceImpl mailService) {

        this.antragDAO = antragDAO;
        this.mailService = mailService;
    }

    @Override
    public void save(Antrag antrag) {

        antragDAO.save(antrag);
    }


    @Override
    public void approve(Antrag antrag) {

        antrag.setState(State.GENEHMIGT);
        antragDAO.save(antrag);

        mailService.sendApprovedNotification(antrag.getPerson(), antrag);
    }


    @Override
    public void decline(Antrag antrag, String reasonToDecline) {

        antrag.setState(State.ABGELEHNT);
        antrag.setReasonToDecline(reasonToDecline);
        antragDAO.save(antrag);

        mailService.sendDeclinedNotification(antrag);
    }


    @Override
    public void wait(Antrag antrag) {

        antrag.setState(State.WARTEND);
        antragDAO.save(antrag);
    }


    @Override
    public void storno(Antrag antrag) {

        String emailAddress;

        if (antrag.getState() == State.WARTEND) {
            antrag.setState(State.STORNIERT);
            antragDAO.save(antrag);

            // wenn Antrag wartend war, bekommen Chefs die Email
            mailService.sendCanceledNotification(antrag, EmailAdr.CHEFS.getEmail());
        } else if (antrag.getState() == State.GENEHMIGT) {
            antrag.setState(State.STORNIERT);
            antragDAO.save(antrag);

            // wenn Antrag genehmigt war, bekommt Office die Email
            mailService.sendCanceledNotification(antrag, EmailAdr.OFFICE.getEmail());
        }
    }


    @Override
    public Antrag getRequestById(Integer id) {

        return antragDAO.findOne(id);
    }


    @Override
    public List<Antrag> getAllRequestsForPerson(Person person) {

        antragDAO.getAllRequestsForPerson(person);

        return null;
    }


    @Override
    public List<Antrag> getAllRequests() {

        return antragDAO.findAll();
    }


    @Override
    public List<Antrag> getAllRequestsByState(State state) {

        antragDAO.getAllRequestsByState(state);

        return null;
    }


    @Override
    public List<Antrag> getAllRequestsForACertainTime(DateMidnight startDate, DateMidnight endDate) {

        return antragDAO.getAllRequestsForACertainTime(startDate, endDate);
    }
}
