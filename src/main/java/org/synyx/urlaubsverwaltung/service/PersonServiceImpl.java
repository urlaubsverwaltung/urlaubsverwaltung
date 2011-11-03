package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.dao.PersonDAO;
import org.synyx.urlaubsverwaltung.domain.Antrag;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.util.ArrayList;
import java.util.List;


/**
 * implementation of the persondata-access-service. for now just passing functions, but this can change(maybe)
 *
 * @author  johannes
 */
@Transactional
public class PersonServiceImpl implements PersonService {

    private PersonDAO personDAO;
    private AntragService antragService;

    // wird hier und im anderen service benötigt, weil wir ja
    // ständig irgendwelche mails schicken müssen... =)
    private MailService mailService;

    @Autowired
    public PersonServiceImpl(PersonDAO personDAO, AntragService antragService, MailService mailService) {

        this.personDAO = personDAO;
        this.antragService = antragService;
        this.mailService = mailService;
    }

    /**
     * @see  PersonService#save(org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public void save(Person person) {

        personDAO.save(person);
    }


    /**
     * @see  PersonService#delete(org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public void delete(Person person) {

        personDAO.delete(person);
    }


    /**
     * @see  PersonService#getPersonByID(java.lang.Integer)
     */
    @Override
    public Person getPersonByID(Integer id) {

        return personDAO.findOne(id);
    }


    /**
     * @see  PersonService#getAllPersons()
     */
    @Override
    public List<Person> getAllPersons() {

        return personDAO.findAll();
    }


    /**
     * @see  PersonService#deleteResturlaub()
     */
    @Override
    public void deleteResturlaub() {

        List<Person> persons = getAllPersons();

        for (Person person : persons) {
            person.setRestUrlaub(0);
        }
    }


    /**
     * @see  PersonService#getPersonsWithResturlaub()
     */
    @Override
    public List<Person> getPersonsWithResturlaub() {

        return personDAO.getPersonsWithResturlaub();
    }


    /**
     * @see  PersonService#updateVacationDays()
     */
    @Override
    public void updateVacationDays() {

        List<Person> persons = getAllPersons();
        Integer rest;

        for (Person person : persons) {
            rest = person.getRemainingVacationDays();

            if (rest > 0) {
                person.setRestUrlaub(person.getRestUrlaub() + rest);
            }
        }
    }


    /**
     * @see  PersonService#getAllUrlauberForThisWeekAndPutItInAnEmail(org.joda.time.DateMidnight, org.joda.time.DateMidnight)
     */
    @Override
    public void getAllUrlauberForThisWeekAndPutItInAnEmail(DateMidnight startDate, DateMidnight endDate) {

        List<Antrag> requests = antragService.getAllRequestsForACertainTime(startDate, endDate);

        List<Person> urlauber = new ArrayList<Person>();

        for (Antrag antrag : requests) {
            urlauber.add(antrag.getPerson());
        }

        mailService.sendWeeklyVacationForecast(urlauber);
    }
}
