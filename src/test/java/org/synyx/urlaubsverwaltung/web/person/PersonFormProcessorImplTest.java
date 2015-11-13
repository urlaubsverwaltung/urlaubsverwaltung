package org.synyx.urlaubsverwaltung.web.person;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.core.account.service.AccountService;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.core.department.Department;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.period.WeekDay;
import org.synyx.urlaubsverwaltung.core.person.MailNotification;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class PersonFormProcessorImplTest {

    private PersonFormProcessor service;

    private PersonService personService;
    private WorkingTimeService workingTimeService;
    private AccountService accountService;
    private AccountInteractionService accountInteractionService;
    private DepartmentService departmentService;

    private PersonForm examplePersonForm;

    @Before
    public void setUp() {

        personService = Mockito.mock(PersonService.class);
        workingTimeService = Mockito.mock(WorkingTimeService.class);
        accountService = Mockito.mock(AccountService.class);
        accountInteractionService = Mockito.mock(AccountInteractionService.class);
        departmentService = Mockito.mock(DepartmentService.class);

        service = new PersonFormProcessorImpl(personService, workingTimeService, accountService,
                accountInteractionService, departmentService);

        examplePersonForm = new PersonForm(2014);
        examplePersonForm.setLoginName("muster");
        examplePersonForm.setLastName("Muster");
        examplePersonForm.setFirstName("Marlene");
        examplePersonForm.setEmail("muster@synyx.de");
        examplePersonForm.setAnnualVacationDays(new BigDecimal("28"));
        examplePersonForm.setRemainingVacationDays(new BigDecimal("4"));
        examplePersonForm.setRemainingVacationDaysNotExpiring(new BigDecimal("3"));
        examplePersonForm.setValidFrom(DateMidnight.now());
        examplePersonForm.setWorkingDays(Arrays.asList(WeekDay.MONDAY.getDayOfWeek(), WeekDay.TUESDAY.getDayOfWeek()));
        examplePersonForm.setPermissions(Collections.singletonList(Role.USER));
        examplePersonForm.setNotifications(Collections.singletonList(MailNotification.NOTIFICATION_USER));

        Mockito.when(accountService.getHolidaysAccount(Mockito.anyInt(), Mockito.any(Person.class)))
            .thenReturn(Optional.<Account>empty());
    }


    @Test
    public void ensurePersonWillBeCreatedByPersonFormAttributes() {

        service.create(examplePersonForm);

        Mockito.verify(personService)
            .create("muster", "Muster", "Marlene", "muster@synyx.de",
                Collections.singletonList(MailNotification.NOTIFICATION_USER), Collections.singletonList(Role.USER));
    }


    @Test
    public void ensurePersonHasValidWorkingTimeAfterCreation() {

        Person person = service.create(examplePersonForm);

        Mockito.verify(workingTimeService)
            .touch(Mockito.anyListOf(Integer.class), Mockito.any(DateMidnight.class), Mockito.eq(person));
    }


    @Test
    public void ensurePersonHasValidAccountAfterCreation() {

        Person person = service.create(examplePersonForm);

        Mockito.verify(accountInteractionService)
            .createHolidaysAccount(Mockito.eq(person), Mockito.eq(new DateMidnight(2014, 1, 1)),
                Mockito.eq(new DateMidnight(2014, 12, 31)), Mockito.eq(new BigDecimal("28")),
                Mockito.eq(new BigDecimal("4")), Mockito.eq(new BigDecimal("3")));
    }


    @Test
    public void ensureDepartmentHeadsAreUpdatedIfPersonLosesDepartmentHeadRole() {

        Person person = TestDataCreator.createPerson();

        Mockito.when(personService.update(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(),
                    Mockito.anyString(), Mockito.anyString(), Mockito.anyListOf(MailNotification.class),
                    Mockito.anyListOf(Role.class)))
            .thenReturn(person);

        Department department = TestDataCreator.createDepartment();
        department.setDepartmentHeads(Collections.singletonList(person));

        Assert.assertEquals("Wrong number of department heads", 1, department.getDepartmentHeads().size());

        Mockito.when(departmentService.getManagedDepartmentsOfDepartmentHead(Mockito.any(Person.class)))
            .thenReturn(Collections.singletonList(department));

        service.update(examplePersonForm);

        Mockito.verify(departmentService).update(department);

        Assert.assertEquals("Wrong number of department heads", 0, department.getDepartmentHeads().size());
    }


    @Test
    public void ensureDepartmentHeadsAreNotUpdatedIfPersonHaveNotBeenDepartmentHead() {

        Person person = TestDataCreator.createPerson();

        Mockito.when(personService.update(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(),
                    Mockito.anyString(), Mockito.anyString(), Mockito.anyListOf(MailNotification.class),
                    Mockito.anyListOf(Role.class)))
            .thenReturn(person);

        Department department = TestDataCreator.createDepartment();

        Assert.assertEquals("Wrong number of department heads", 0, department.getDepartmentHeads().size());

        Mockito.when(departmentService.getManagedDepartmentsOfDepartmentHead(Mockito.any(Person.class)))
            .thenReturn(Collections.singletonList(department));

        service.update(examplePersonForm);

        Mockito.verify(departmentService, Mockito.never()).update(department);

        Assert.assertEquals("Wrong number of department heads", 0, department.getDepartmentHeads().size());
    }


    @Test
    public void ensureDepartmentHeadsAreNotUpdatedIfPersonIsStillDepartmentHead() {

        Person person = TestDataCreator.createPerson();
        person.setPermissions(Arrays.asList(Role.USER, Role.DEPARTMENT_HEAD));

        Mockito.when(personService.update(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(),
                    Mockito.anyString(), Mockito.anyString(), Mockito.anyListOf(MailNotification.class),
                    Mockito.anyListOf(Role.class)))
            .thenReturn(person);

        service.update(examplePersonForm);

        Mockito.verifyZeroInteractions(departmentService);
    }
}
