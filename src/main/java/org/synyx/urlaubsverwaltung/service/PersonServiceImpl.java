package org.synyx.urlaubsverwaltung.service;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.dao.PersonDAO;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.joda.time.chrono.GregorianChronology;
import org.synyx.urlaubsverwaltung.controller.ControllerConstants;
import org.synyx.urlaubsverwaltung.domain.Account;
import org.synyx.urlaubsverwaltung.view.PersonForm;


/**
 * implementation of the persondata-access-service. for now just passing functions, but this can change(maybe)
 *
 * @author  Aljona Murygina
 * @author  Johannes Reuter
 */
@Transactional
public class PersonServiceImpl implements PersonService {

    // audit logger: logs nontechnically occurences like 'user x applied for leave' or 'subtracted n days from
    // holidays account y'
    private static final Logger LOG = Logger.getLogger("audit");
    
    private PersonDAO personDAO;
    private ApplicationService applicationService;
    private HolidaysAccountService accountService;
    private MailService mailService;
    private CryptoService cryptoService;

    @Autowired
    public PersonServiceImpl(PersonDAO personDAO, ApplicationService applicationService, MailService mailService,
        HolidaysAccountService accountService, CryptoService cryptoService) {

        this.personDAO = personDAO;
        this.applicationService = applicationService;
        this.mailService = mailService;
        this.accountService = accountService;
        this.cryptoService = cryptoService;
    }

    /**
     * @see  PersonService#save(org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public void save(Person person) {

        personDAO.save(person);
    }
    
    /**
     * @see  PersonService#createOrUpdate(org.synyx.urlaubsverwaltung.domain.Person, org.synyx.urlaubsverwaltung.view.PersonForm) 
     */
    @Override
    public void createOrUpdate(Person person, PersonForm personForm) {
        
        boolean newPerson = false;

        if (person.getId() == null) {
            newPerson = true;

            try {
                KeyPair keyPair = cryptoService.generateKeyPair();
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
        
        int year = Integer.parseInt(personForm.getYear());
        int dayFrom = Integer.parseInt(personForm.getDayFrom());
        int monthFrom = Integer.parseInt(personForm.getMonthFrom());
        int dayTo = Integer.parseInt(personForm.getDayTo());
        int monthTo = Integer.parseInt(personForm.getMonthTo());

        DateMidnight validFrom = new DateMidnight(year, monthFrom, dayFrom);
        DateMidnight validTo = new DateMidnight(year, monthTo, dayTo);

        BigDecimal annualVacationDays = new BigDecimal(personForm.getAnnualVacationDays());
        BigDecimal remainingVacationDays = new BigDecimal(personForm.getRemainingVacationDays());
        boolean expiring = personForm.isRemainingVacationDaysExpire();

        // check if there is an existing account
        Account account = accountService.getHolidaysAccount(year, person);

        if (account == null) {
            accountService.createHolidaysAccount(person, validFrom, validTo, annualVacationDays,
                    remainingVacationDays, expiring);
        } else {
            accountService.editHolidaysAccount(account, validFrom, validTo, annualVacationDays, remainingVacationDays,
                    expiring);
        }

        if (newPerson) {
            LOG.info(DateMidnight.now(GregorianChronology.getInstance()).toString(ControllerConstants.DATE_FORMAT) + " Ein neuer Mitarbeiter wurde angelegt. ID: " + person.getId()
                    + ", Vorname: " + person.getFirstName() + ", Nachname: " + person.getLastName());
        } else {
            LOG.info(DateMidnight.now(GregorianChronology.getInstance()).toString(ControllerConstants.DATE_FORMAT) + " ID: " + person.getId()
                    + " Der Mitarbeiter " + person.getFirstName() + " " + person.getLastName()
                    + " wurde editiert.");
        }
        
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

        return personDAO.getPersonsByRole(role);
    }
}
