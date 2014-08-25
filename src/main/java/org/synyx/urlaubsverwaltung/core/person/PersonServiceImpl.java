package org.synyx.urlaubsverwaltung.core.person;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.chrono.GregorianChronology;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.DateFormat;
import org.synyx.urlaubsverwaltung.core.account.Account;
import org.synyx.urlaubsverwaltung.core.account.AccountService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.util.NumberUtil;
import org.synyx.urlaubsverwaltung.security.CryptoUtil;
import org.synyx.urlaubsverwaltung.security.Role;
import org.synyx.urlaubsverwaltung.web.person.PersonForm;

import java.math.BigDecimal;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Implementation for interface {@link PersonService}.
 *
 * @author  Aljona Murygina
 * @author  Johannes Reuter
 */
@Service
@Transactional
class PersonServiceImpl implements PersonService {

    private static final Logger LOG = Logger.getLogger(PersonServiceImpl.class);

    private PersonDAO personDAO;
    private ApplicationService applicationService;
    private AccountService accountService;
    private MailService mailService;
    private WorkingTimeService workingTimeService;

    @Autowired
    public PersonServiceImpl(PersonDAO personDAO, ApplicationService applicationService, MailService mailService,
        AccountService accountService, WorkingTimeService workingTimeService) {

        this.personDAO = personDAO;
        this.applicationService = applicationService;
        this.mailService = mailService;
        this.accountService = accountService;
        this.workingTimeService = workingTimeService;
    }

    /**
     * @see  PersonService#save(Person)
     */
    @Override
    public void save(Person person) {

        personDAO.save(person);
    }


    /**
     * @see  PersonService#createOrUpdate(Person, org.synyx.urlaubsverwaltung.web.person.PersonForm, java.util.Locale)
     */
    @Override
    public void createOrUpdate(Person person, PersonForm personForm, Locale locale) {

        boolean newPerson = false;

        if (person.getId() == null) {
            newPerson = true;

            try {
                KeyPair keyPair = CryptoUtil.generateKeyPair();
                person.setPrivateKey(keyPair.getPrivate().getEncoded());
                person.setPublicKey(keyPair.getPublic().getEncoded());
            } catch (NoSuchAlgorithmException ex) {
                LOG.error("Beim Erstellen der Keys für den neuen Benutzer mit dem Login " + personForm.getLoginName()
                    + " ist ein Fehler aufgetreten.", ex);
                mailService.sendKeyGeneratingErrorNotification(personForm.getLoginName());
            }

        }

        // set person information from PersonForm object on person that is updated
        person = personForm.fillPersonObject(person);

        save(person);

        workingTimeService.touch(personForm.getWorkingDays(), personForm.getValidFrom(), person);

        int year = Integer.parseInt(personForm.getYear());
        int dayFrom = Integer.parseInt(personForm.getDayFrom());
        int monthFrom = Integer.parseInt(personForm.getMonthFrom());
        int dayTo = Integer.parseInt(personForm.getDayTo());
        int monthTo = Integer.parseInt(personForm.getMonthTo());

        DateMidnight validFrom = new DateMidnight(year, monthFrom, dayFrom);
        DateMidnight validTo = new DateMidnight(year, monthTo, dayTo);

        BigDecimal annualVacationDays = NumberUtil.parseNumber(personForm.getAnnualVacationDays(), locale);
        BigDecimal remainingVacationDays = NumberUtil.parseNumber(personForm.getRemainingVacationDays(), locale);
        boolean expiring = personForm.isRemainingVacationDaysExpire();

        // check if there is an existing account
        Account account = accountService.getHolidaysAccount(year, person);

        if (account == null) {
            accountService.createHolidaysAccount(person, validFrom, validTo, annualVacationDays, remainingVacationDays,
                expiring);
        } else {
            accountService.editHolidaysAccount(account, validFrom, validTo, annualVacationDays, remainingVacationDays,
                expiring);
        }

        if (newPerson) {
            LOG.info(DateMidnight.now(GregorianChronology.getInstance()).toString(DateFormat.PATTERN)
                + " Ein neuer Mitarbeiter wurde angelegt. ID: " + person.getId()
                + ", Vorname: " + person.getFirstName() + ", Nachname: " + person.getLastName());
        } else {
            LOG.info(DateMidnight.now(GregorianChronology.getInstance()).toString(DateFormat.PATTERN)
                + " ID: " + person.getId()
                + " Der Mitarbeiter " + person.getFirstName() + " " + person.getLastName()
                + " wurde editiert.");
        }
    }

    /**
     * @see  PersonService#deactivate(Person)
     */
    @Override
    public void deactivate(Person person) {

        person.setActive(false);
    }


    /**
     * @see  PersonService#activate(Person)
     */
    @Override
    public void activate(Person person) {

        person.setActive(true);
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
     * @see  PersonService#getPersonsWithExpiringRemainingVacationDays()
     */
    @Override
    public List<Person> getPersonsWithExpiringRemainingVacationDays() {

        List<Person> personsWithRemainingVacationDays = new ArrayList<Person>();

        int year = DateMidnight.now().getYear();

        List<Person> persons = getAllPersons();

        for (Person person : persons) {
            if (accountService.getHolidaysAccount(year, person).isRemainingVacationDaysExpire()) {
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

        List<Application> applications = applicationService.getAllowedApplicationsForACertainPeriod(startDate, endDate);

        Map<String, Person> persons = new HashMap<String, Person>();

        for (Application application : applications) {
            persons.put(application.getPerson().getLoginName(), application.getPerson());
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

        // TODO: this is a very dirty hack, so should be replaced some day

        List<Person> vips = new ArrayList<Person>();
        List<Person> persons = getAllPersons();

        for (Person p : persons) {
            if (p.hasRole(role)) {
                vips.add(p);
            }
        }

        return vips;
    }
}
