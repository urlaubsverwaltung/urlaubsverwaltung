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
    private MailServiceImpl mailService;

    @Autowired
    public PersonServiceImpl(PersonDAO personDAO, AntragService antragService, MailServiceImpl mailService) {

        this.personDAO = personDAO;
        this.antragService = antragService;
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

        List<Person> persons = getAllPersons();

        for (Person person : persons) {
            person.setRestUrlaub(0);
        }
    }


    @Override
    public List<Person> getPersonsWithResturlaub() {

        return personDAO.getPersonsWithResturlaub();
    }


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
