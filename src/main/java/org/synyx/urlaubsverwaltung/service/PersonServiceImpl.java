package org.synyx.urlaubsverwaltung.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.dao.PersonDAO;
import org.synyx.urlaubsverwaltung.domain.Person;

/**
 * implementation of the persondata-access-service. for now just passing
 * functions, but this can change(maybe)
 * 
 * @author johannes
 * 
 */
@Transactional
public class PersonServiceImpl implements PersonService {

    private PersonDAO personDAO;
    //wird hier und im anderen service benötigt, weil wir ja
    //ständig irgendwelche mails schicken müssen... =)
    private MailServiceImpl mailService;

    @Autowired
    public PersonServiceImpl(PersonDAO personDAO,MailServiceImpl mailService) {
        this.personDAO = personDAO;
        this.mailService = mailService;
    }

    @Override
    public void save(Person person) {
        personDAO.save(person);

    }

    @Override
    public void delete(Person person) {
        personDAO.delete(person);
    }

    @Override
    public Person getPersonByID(Integer id) {
        return personDAO.findOne(id);
    }

    @Override
    public List<Person> getAllPersons() {
        return personDAO.findAll();
    }

    @Override
    public void deleteResturlaub() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void messageDecayDayPersons() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateVacationDays() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
