package org.synyx.urlaubsverwaltung.core.person;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.core.account.Account;
import org.synyx.urlaubsverwaltung.core.account.AccountService;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.security.CryptoUtil;
import org.synyx.urlaubsverwaltung.web.person.PersonForm;

import java.math.BigDecimal;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
@Transactional
public class PersonInteractionServiceImpl implements PersonInteractionService {

    private static final Logger LOG = Logger.getLogger(PersonInteractionServiceImpl.class);

    private final PersonService personService;
    private final WorkingTimeService workingTimeService;
    private final AccountService accountService;
    private final MailService mailService;

    @Autowired
    public PersonInteractionServiceImpl(PersonService personService, WorkingTimeService workingTimeService,
        AccountService accountService, MailService mailService) {

        this.personService = personService;
        this.workingTimeService = workingTimeService;
        this.accountService = accountService;
        this.mailService = mailService;
    }

    @Override
    public Person create(PersonForm personForm) {

        Person person = personForm.generatePerson();

        try {
            KeyPair keyPair = CryptoUtil.generateKeyPair();
            person.setPrivateKey(keyPair.getPrivate().getEncoded());
            person.setPublicKey(keyPair.getPublic().getEncoded());
        } catch (NoSuchAlgorithmException ex) {
            LOG.error("An error occurred while trying to generate a key pair for the person " + person.toString(), ex);
            mailService.sendKeyGeneratingErrorNotification(personForm.getLoginName(), ex.getMessage());
        }

        personService.save(person);

        touchWorkingTime(person, personForm);

        touchAccount(person, personForm);

        LOG.info("Created: " + person.toString());

        return person;
    }


    private void touchWorkingTime(Person person, PersonForm personForm) {

        workingTimeService.touch(personForm.getWorkingDays(), personForm.getValidFrom(), person);
    }


    private void touchAccount(Person person, PersonForm personForm) {

        DateMidnight validFrom = personForm.getHolidaysAccountValidFrom();
        DateMidnight validTo = personForm.getHolidaysAccountValidTo();

        BigDecimal annualVacationDays = personForm.getAnnualVacationDays();
        BigDecimal remainingVacationDays = personForm.getRemainingVacationDays();
        boolean expiring = personForm.isRemainingVacationDaysExpire();

        // check if there is an existing account
        Account account = accountService.getHolidaysAccount(validFrom.getYear(), person);

        if (account == null) {
            accountService.createHolidaysAccount(person, validFrom, validTo, annualVacationDays, remainingVacationDays,
                expiring);
        } else {
            accountService.editHolidaysAccount(account, validFrom, validTo, annualVacationDays, remainingVacationDays,
                expiring);
        }
    }


    @Override
    public Person update(PersonForm personForm) {

        Person personToUpdate = personService.getPersonByID(personForm.getId());

        personForm.fillPersonAttributes(personToUpdate);

        personService.save(personToUpdate);

        touchWorkingTime(personToUpdate, personForm);

        touchAccount(personToUpdate, personForm);

        LOG.info("Updated " + personToUpdate.toString());

        return personToUpdate;
    }
}
