package org.synyx.urlaubsverwaltung.core.person;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.account.Account;
import org.synyx.urlaubsverwaltung.core.account.AccountService;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.util.NumberUtil;
import org.synyx.urlaubsverwaltung.security.CryptoUtil;
import org.synyx.urlaubsverwaltung.web.person.PersonForm;

import java.math.BigDecimal;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import java.util.Locale;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
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
    public void createOrUpdate(Person person, PersonForm personForm, Locale locale) {

        String action;

        if (person.isNew()) {
            action = "Created";

            try {
                KeyPair keyPair = CryptoUtil.generateKeyPair();
                person.setPrivateKey(keyPair.getPrivate().getEncoded());
                person.setPublicKey(keyPair.getPublic().getEncoded());
            } catch (NoSuchAlgorithmException ex) {
                LOG.error("An error occurred while trying to generate a key pair for the person " + person.toString(),
                    ex);
                mailService.sendKeyGeneratingErrorNotification(personForm.getLoginName(), ex.getMessage());
            }
        } else {
            action = "Updated";
        }

        personForm.fillPersonAttributes(person);

        personService.save(person);

        touchWorkingTime(person, personForm);

        touchAccount(person, personForm, locale);

        LOG.info(action + " " + person.toString());
    }


    private void touchWorkingTime(Person person, PersonForm personForm) {

        workingTimeService.touch(personForm.getWorkingDays(), personForm.getValidFrom(), person);
    }


    private void touchAccount(Person person, PersonForm personForm, Locale locale) {

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
    }
}
