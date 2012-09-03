package org.synyx.urlaubsverwaltung.service;

import org.synyx.urlaubsverwaltung.service.legacy.HolidaysAccountService;
import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.dao.PersonDAO;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Role;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;


/**
 * implementation of the persondata-access-service. for now just passing functions, but this can change(maybe)
 *
 * @author  Aljona Murygina
 * @author  Johannes Reuter
 */
@Transactional
public class PersonServiceImpl implements PersonService {

    private PersonDAO personDAO;
    private ApplicationService applicationService;
    private HolidaysAccountService accountService;
    private MailService mailService;

    @Autowired
    public PersonServiceImpl(PersonDAO personDAO, ApplicationService applicationService, MailService mailService,
        HolidaysAccountService accountService) {

        this.personDAO = personDAO;
        this.applicationService = applicationService;
        this.mailService = mailService;
        this.accountService = accountService;
    }

    /**
     * @see  PersonService#save(org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public void save(Person person) {

        personDAO.save(person);
    }


    /**
     * @see  PersonService#deactivate(org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public void deactivate(Person person) {

        // set person inactive
        person.setActive(false);
        person.setRole(Role.INACTIVE);
    }


    /**
     * @see  PersonService#activate(org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public void activate(Person person) {

        // set person inactive
        person.setActive(true);
        person.setRole(Role.USER);
    }


    /**
     * @see  PersonService#getPersonByID(java.lang.Integer)
     */
    @Override
    public Person getPersonByID(Integer id) {

        return personDAO.findOne(id);
    }


    /**
     * @see  PersonService#getPersonByLogin(java.lang.String)
     */
    @Override
    public Person getPersonByLogin(String loginName) {

        return personDAO.getPersonByLogin(loginName);
    }


    /**
     * @see  PersonService#getAllPersons()
     */
    @Override
    public List<Person> getAllPersons() {

        return personDAO.getPersonsOrderedByLastName();
    }


    /**
     * @see  PersonService#getPersonsWithRemainingVacationDays()
     */
    @Override
    public List<Person> getPersonsWithRemainingVacationDays() {

        List<Person> personsWithRemainingVacationDays = new ArrayList<Person>();

        int year = DateMidnight.now().getYear();

        List<Person> persons = getAllPersons();

        for (Person person : persons) {
            if (!accountService.getHolidaysAccount(year, person).getRemainingVacationDays().equals(BigDecimal.ZERO)) {
                personsWithRemainingVacationDays.add(person);
            }
        }

        return personsWithRemainingVacationDays;
    }


    /**
     * @see  PersonService#getAllPersonsOnHolidayForThisWeekAndPutItInAnEmail(org.joda.time.DateMidnight, org.joda.time.DateMidnight)
     */
    @Override
    public void getAllPersonsOnHolidayForThisWeekAndPutItInAnEmail(DateMidnight startDate, DateMidnight endDate) {

        List<Application> applications = applicationService.getApplicationsForACertainTime(startDate, endDate);

        List<Person> persons = new ArrayList<Person>();

        for (Application application : applications) {
            persons.add(application.getPerson());
        }

        mailService.sendWeeklyVacationForecast(persons);
    }


    /**
     * @see  PersonService#getAllPersonsExceptOne(java.lang.Integer)
     */
    @Override
    public List<Person> getAllPersonsExceptOne(Integer id) {

        return personDAO.getAllPersonsExceptOne(id);
    }


    @Override
    public List<Person> getInactivePersons() {

        return personDAO.getInactivePersons();
    }

    @Override
    public List<Person> getPersonsByRole(Role role) {
        return personDAO.getPersonsByRole(role);
    }
}
