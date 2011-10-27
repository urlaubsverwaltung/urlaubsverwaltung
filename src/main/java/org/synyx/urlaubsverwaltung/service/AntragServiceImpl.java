package org.synyx.urlaubsverwaltung.service;

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
    private MailServiceImpl mailService;

    @Autowired
    public AntragServiceImpl(AntragDAO antragDAO,MailServiceImpl mailService) {

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
    }


    @Override
    public void decline(Antrag antrag,String reason) {

        antrag.setState(State.ABGELEHNT);
        antrag.setReason(reason);
        antragDAO.save(antrag);
    }


    @Override
    public void wait(Antrag antrag) {

        antrag.setState(State.WARTEND);
        antragDAO.save(antrag);
    }


    @Override
    public void storno(Antrag antrag) {

        antrag.setState(State.STORNIERT);
        antragDAO.save(antrag);
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
}
