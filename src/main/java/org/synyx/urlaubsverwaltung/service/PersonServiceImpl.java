package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.dao.HolidayEntitlementDAO;
import org.synyx.urlaubsverwaltung.dao.PersonDAO;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.HolidayEntitlement;
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;

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
    private HolidayEntitlementDAO holidayEntitlementDAO;
    private MailService mailService;

    @Autowired
    public PersonServiceImpl(PersonDAO personDAO, ApplicationService applicationService,
        HolidayEntitlementDAO holidayEntitlementDAO, MailService mailService, HolidaysAccountService accountService) {

        this.personDAO = personDAO;
        this.applicationService = applicationService;
        this.holidayEntitlementDAO = holidayEntitlementDAO;
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
     * @see  PersonService#updateVacationDays(int)
     */
    @Override
    public void updateVacationDays(int year) {

        HolidaysAccount account;

        List<Person> persons = getAllPersons();

        for (Person person : persons) {
            HolidayEntitlement entitlement = accountService.getHolidayEntitlement(year, person);

            if (entitlement == null) {
                // if entitlement for this year not yet existent
                // take that one from past year
                entitlement = accountService.newHolidayEntitlement(person, year,
                        accountService.getHolidayEntitlement(year - 1, person).getVacationDays());
            }

            account = accountService.getHolidaysAccount(year, person);

            if (account == null) {
                // if account not yet existent, take current entitlement and set vacation days of past year to remaining
                // vacation days of current year

                BigDecimal days = accountService.getHolidaysAccount(year - 1, person).getVacationDays();

                // create new account
                accountService.newHolidaysAccount(person, entitlement.getVacationDays(), days, year);
                account = accountService.getHolidaysAccount(year, person);
            } else {
                // if account existent
                BigDecimal days = accountService.getHolidaysAccount(year - 1, person).getVacationDays();

                if ((account.getVacationDays().compareTo(entitlement.getVacationDays())) == -1) {
                    // if account of this year already has been used
                    // take at first remaining vacation days for filling account (vacation days)
                    BigDecimal vacDays;

                    // vacDays contains all vacation days that person has this year
                    vacDays = account.getVacationDays().add(days);

                    // set remaining vacation days to 0
                    days = BigDecimal.ZERO;

                    if ((vacDays.compareTo(entitlement.getVacationDays())) == 1) {
                        // if vacDays > entitlement
                        // remaining vacation days = vacDays - entitlement
                        days = vacDays.subtract(entitlement.getVacationDays());

                        // set normal vacation days as filled full
                        vacDays = entitlement.getVacationDays();
                    }

                    account.setVacationDays(vacDays);
                }

                account.setRemainingVacationDays(days);
            }

            accountService.saveHolidaysAccount(account);
        }
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
     * @see  PersonService#getHolidayEntitlementByPersonAndYear(org.synyx.urlaubsverwaltung.domain.Person, int)
     */
    @Override
    public HolidayEntitlement getHolidayEntitlementByPersonAndYear(Person person, int year) {

        return holidayEntitlementDAO.getHolidayEntitlementByYearAndPerson(year, person);
    }


    /**
     * @see  PersonService#getHolidayEntitlementByPersonForAllYears(org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public List<HolidayEntitlement> getHolidayEntitlementByPersonForAllYears(Person person) {

        return holidayEntitlementDAO.getHolidayEntitlementByPerson(person);
    }


    /**
     * @see  PersonService#getAllPersonsExceptOne(java.lang.Integer)
     */
    @Override
    public List<Person> getAllPersonsExceptOne(Integer id) {

        return personDAO.getAllPersonsExceptOne(id);
    }
}
