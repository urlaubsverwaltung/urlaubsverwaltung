package org.synyx.urlaubsverwaltung.core.person;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.account.AccountService;
import org.synyx.urlaubsverwaltung.core.calendar.Day;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.security.Role;
import org.synyx.urlaubsverwaltung.web.person.PersonForm;

import java.math.BigDecimal;

import java.util.Arrays;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class PersonInteractionServiceTest {

    private PersonInteractionService service;

    private PersonService personService;
    private WorkingTimeService workingTimeService;
    private AccountService accountService;
    private MailService mailService;

    private PersonForm examplePersonForm;

    @Before
    public void setUp() {

        personService = Mockito.mock(PersonService.class);
        workingTimeService = Mockito.mock(WorkingTimeService.class);
        accountService = Mockito.mock(AccountService.class);
        mailService = Mockito.mock(MailService.class);

        service = new PersonInteractionServiceImpl(personService, workingTimeService, accountService, mailService);

        examplePersonForm = new PersonForm();
        examplePersonForm.setLoginName("muster");
        examplePersonForm.setLastName("Muster");
        examplePersonForm.setFirstName("Marlene");
        examplePersonForm.setEmail("muster@synyx.de");
        examplePersonForm.setYear("2014");
        examplePersonForm.setAnnualVacationDays(new BigDecimal("28"));
        examplePersonForm.setRemainingVacationDays(new BigDecimal("4"));
        examplePersonForm.setRemainingVacationDaysExpire(true);
        examplePersonForm.setValidFrom(DateMidnight.now());
        examplePersonForm.setWorkingDays(Arrays.asList(Day.MONDAY.getDayOfWeek(), Day.TUESDAY.getDayOfWeek()));
        examplePersonForm.setPermissions(Arrays.asList(Role.USER));
    }


    @Test
    public void ensurePersonHasKeyPairAfterCreating() {

        Person person = new Person();

        Assert.assertNull(person.getPrivateKey());
        Assert.assertNull(person.getPublicKey());

        service.createOrUpdate(person, examplePersonForm);

        Assert.assertNotNull(person.getPrivateKey());
        Assert.assertNotNull(person.getPublicKey());
    }


    @Test
    public void ensurePersonIsPersistedOnCreating() {

        Person person = new Person();

        service.createOrUpdate(person, examplePersonForm);

        Mockito.verify(personService).save(person);
    }


    @Test
    public void ensurePersonHasValidWorkingTimeAndAccountAfterCreating() {

        Person person = new Person();

        service.createOrUpdate(person, examplePersonForm);

        Mockito.verify(workingTimeService).touch(Mockito.anyListOf(Integer.class), Mockito.any(DateMidnight.class),
            Mockito.eq(person));
        Mockito.verify(accountService).createHolidaysAccount(Mockito.eq(person),
            Mockito.eq(new DateMidnight(2014, 1, 1)), Mockito.eq(new DateMidnight(2014, 12, 31)),
            Mockito.eq(new BigDecimal("28")), Mockito.eq(new BigDecimal("4")), Mockito.eq(true));
    }
}
