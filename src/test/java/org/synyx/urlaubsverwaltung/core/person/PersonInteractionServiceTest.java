package org.synyx.urlaubsverwaltung.core.person;

import com.google.common.base.Optional;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.core.account.service.AccountService;
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
    private AccountInteractionService accountInteractionService;

    private PersonForm examplePersonForm;

    @Before
    public void setUp() {

        personService = Mockito.mock(PersonService.class);
        workingTimeService = Mockito.mock(WorkingTimeService.class);
        accountService = Mockito.mock(AccountService.class);
        accountInteractionService = Mockito.mock(AccountInteractionService.class);

        MailService mailService = Mockito.mock(MailService.class);

        service = new PersonInteractionServiceImpl(personService, workingTimeService, accountService,
                accountInteractionService, mailService);

        examplePersonForm = new PersonForm(2014);
        examplePersonForm.setLoginName("muster");
        examplePersonForm.setLastName("Muster");
        examplePersonForm.setFirstName("Marlene");
        examplePersonForm.setEmail("muster@synyx.de");
        examplePersonForm.setAnnualVacationDays(new BigDecimal("28"));
        examplePersonForm.setRemainingVacationDays(new BigDecimal("4"));
        examplePersonForm.setRemainingVacationDaysNotExpiring(new BigDecimal("3"));
        examplePersonForm.setValidFrom(DateMidnight.now());
        examplePersonForm.setWorkingDays(Arrays.asList(Day.MONDAY.getDayOfWeek(), Day.TUESDAY.getDayOfWeek()));
        examplePersonForm.setPermissions(Arrays.asList(Role.USER));

        Mockito.when(accountService.getHolidaysAccount(Mockito.anyInt(), Mockito.any(Person.class))).thenReturn(Optional
            .<Account>absent());
    }


    @Test
    public void ensurePersonHasKeyPairAfterCreating() {

        Person person = service.create(examplePersonForm);

        Assert.assertNotNull(person.getPrivateKey());
        Assert.assertNotNull(person.getPublicKey());
    }


    @Test
    public void ensurePersonIsPersistedOnCreating() {

        service.create(examplePersonForm);

        Mockito.verify(personService).save(Mockito.any(Person.class));
    }


    @Test
    public void ensurePersonHasValidWorkingTimeAndAccountAfterCreating() {

        Person person = service.create(examplePersonForm);

        Mockito.verify(workingTimeService).touch(Mockito.anyListOf(Integer.class), Mockito.any(DateMidnight.class),
            Mockito.eq(person));
        Mockito.verify(accountInteractionService).createHolidaysAccount(Mockito.eq(person),
            Mockito.eq(new DateMidnight(2014, 1, 1)), Mockito.eq(new DateMidnight(2014, 12, 31)),
            Mockito.eq(new BigDecimal("28")), Mockito.eq(new BigDecimal("4")), Mockito.eq(new BigDecimal("3")));
    }
}
