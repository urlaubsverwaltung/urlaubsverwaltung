package org.synyx.urlaubsverwaltung.application.web;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class ApplicationForLeaveViewControllerTest {

    private ApplicationForLeaveViewController sut;

    @Mock
    private ApplicationService applicationService;
    @Mock
    private WorkDaysCountService workDaysCountService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private PersonService personService;

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeaveViewController(applicationService, workDaysCountService, departmentService, personService);
    }

    @Test
    void getApplicationForDepartmentHead() throws Exception {

        final Person person = new Person();
        person.setFirstName("Atticus");
        final Application application = new Application();
        application.setId(1);
        application.setPerson(person);
        application.setStatus(WAITING);
        application.setStartDate(LocalDate.MAX);
        application.setEndDate(LocalDate.MAX);
        application.setDayLength(FULL);

        final Person headPerson = new Person();
        headPerson.setPermissions(singletonList(DEPARTMENT_HEAD));
        final Application applicationOfHead = new Application();
        applicationOfHead.setId(2);
        applicationOfHead.setPerson(headPerson);

        final Person secondStagePerson = new Person();
        secondStagePerson.setPermissions(singletonList(SECOND_STAGE_AUTHORITY));
        final Application applicationOfSecondStage = new Application();
        applicationOfSecondStage.setId(3);
        applicationOfSecondStage.setPerson(secondStagePerson);

        when(personService.getSignedInUser()).thenReturn(headPerson);
        when(departmentService.getManagedMembersOfDepartmentHead(headPerson)).thenReturn(asList(headPerson, person, secondStagePerson));
        when(applicationService.getApplicationsForACertainState(WAITING)).thenReturn(asList(application, applicationOfHead, applicationOfSecondStage));

        final ResultActions resultActions = perform(get("/web/application"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("applications", hasSize(1)));
        resultActions.andExpect(model().attribute("applications", hasItem(instanceOf(ApplicationForLeave.class))));
        resultActions.andExpect(model().attribute("applications", hasItem(hasProperty("person",
            hasProperty("firstName", equalTo("Atticus"))))));
        resultActions.andExpect(view().name("application/app_list"));
    }

    @Test
    void getApplicationForBoss() throws Exception {

        final Person person = new Person();
        final Application application = new Application();
        application.setId(1);
        application.setPerson(person);
        application.setStatus(WAITING);
        application.setStartDate(LocalDate.MAX);
        application.setEndDate(LocalDate.MAX);
        application.setDayLength(FULL);

        final Person bossPerson = new Person();
        bossPerson.setPermissions(singletonList(BOSS));
        final Application applicationOfBoss = new Application();
        applicationOfBoss.setId(2);
        applicationOfBoss.setPerson(bossPerson);
        applicationOfBoss.setStatus(WAITING);
        applicationOfBoss.setStartDate(LocalDate.MAX);
        applicationOfBoss.setEndDate(LocalDate.MAX);

        final Person secondStagePerson = new Person();
        secondStagePerson.setPermissions(singletonList(SECOND_STAGE_AUTHORITY));
        final Application applicationOfSecondStage = new Application();
        applicationOfSecondStage.setId(3);
        applicationOfSecondStage.setPerson(secondStagePerson);
        applicationOfSecondStage.setStatus(WAITING);
        applicationOfSecondStage.setStartDate(LocalDate.MAX);
        applicationOfSecondStage.setEndDate(LocalDate.MAX);

        when(personService.getSignedInUser()).thenReturn(bossPerson);
        when(applicationService.getApplicationsForACertainState(WAITING)).thenReturn(asList(application, applicationOfBoss, applicationOfSecondStage));

        final ResultActions resultActions = perform(get("/web/application"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("applications", hasSize(3)));
        resultActions.andExpect(model().attribute("applications", hasItem(instanceOf(ApplicationForLeave.class))));
        resultActions.andExpect(view().name("application/app_list"));
    }

    @Test
    void getApplicationForOffice() throws Exception {

        final Person person = new Person();
        final Application application = new Application();
        application.setId(1);
        application.setPerson(person);
        application.setStatus(TEMPORARY_ALLOWED);
        application.setStartDate(LocalDate.MAX);
        application.setEndDate(LocalDate.MAX);
        application.setDayLength(FULL);

        final Person officePerson = new Person();
        officePerson.setPermissions(singletonList(OFFICE));
        final Application applicationOfBoss = new Application();
        applicationOfBoss.setId(2);
        applicationOfBoss.setPerson(officePerson);
        applicationOfBoss.setStatus(WAITING);
        applicationOfBoss.setStartDate(LocalDate.MAX);
        applicationOfBoss.setEndDate(LocalDate.MAX);

        final Person secondStagePerson = new Person();
        secondStagePerson.setPermissions(singletonList(SECOND_STAGE_AUTHORITY));
        final Application applicationOfSecondStage = new Application();
        applicationOfSecondStage.setId(3);
        applicationOfSecondStage.setPerson(secondStagePerson);
        applicationOfSecondStage.setStatus(WAITING);
        applicationOfSecondStage.setStartDate(LocalDate.MAX);
        applicationOfSecondStage.setEndDate(LocalDate.MAX);

        when(personService.getSignedInUser()).thenReturn(officePerson);
        when(applicationService.getApplicationsForACertainState(WAITING)).thenReturn(asList(applicationOfBoss, applicationOfSecondStage));
        when(applicationService.getApplicationsForACertainState(TEMPORARY_ALLOWED)).thenReturn(singletonList(application));

        final ResultActions resultActions = perform(get("/web/application"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("applications", hasSize(3)));
        resultActions.andExpect(model().attribute("applications", hasItem(instanceOf(ApplicationForLeave.class))));
        resultActions.andExpect(view().name("application/app_list"));
    }

    @Test
    void getApplicationForSecondStage() throws Exception {

        final Person person = new Person();
        person.setFirstName("person");
        final Application application = new Application();
        application.setId(1);
        application.setPerson(person);
        application.setStatus(TEMPORARY_ALLOWED);
        application.setStartDate(LocalDate.MAX);
        application.setEndDate(LocalDate.MAX);
        application.setDayLength(FULL);

        final Person officePerson = new Person();
        officePerson.setFirstName("office");
        officePerson.setPermissions(singletonList(OFFICE));
        final Application applicationOfBoss = new Application();
        applicationOfBoss.setId(2);
        applicationOfBoss.setPerson(officePerson);
        applicationOfBoss.setStatus(WAITING);
        applicationOfBoss.setStartDate(LocalDate.MAX);
        applicationOfBoss.setEndDate(LocalDate.MAX);

        final Person secondStagePerson = new Person();
        secondStagePerson.setPermissions(singletonList(SECOND_STAGE_AUTHORITY));
        final Application applicationOfSecondStage = new Application();
        applicationOfSecondStage.setId(3);
        applicationOfSecondStage.setPerson(secondStagePerson);
        applicationOfSecondStage.setStatus(WAITING);
        applicationOfSecondStage.setStartDate(LocalDate.MAX);
        applicationOfSecondStage.setEndDate(LocalDate.MAX);

        when(personService.getSignedInUser()).thenReturn(secondStagePerson);
        when(departmentService.getManagedMembersForSecondStageAuthority(secondStagePerson)).thenReturn(asList(secondStagePerson, person, officePerson));
        when(applicationService.getApplicationsForACertainState(WAITING)).thenReturn(asList(applicationOfBoss, applicationOfSecondStage));
        when(applicationService.getApplicationsForACertainState(TEMPORARY_ALLOWED)).thenReturn(singletonList(application));

        final ResultActions resultActions = perform(get("/web/application"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("applications", hasSize(2)));
        resultActions.andExpect(model().attribute("applications", hasItem(hasProperty("person",
            hasProperty("firstName", equalTo("office"))))));
        resultActions.andExpect(model().attribute("applications", hasItem(hasProperty("person",
            hasProperty("firstName", equalTo("person"))))));
        resultActions.andExpect(model().attribute("applications", hasItem(instanceOf(ApplicationForLeave.class))));
        resultActions.andExpect(view().name("application/app_list"));
    }

    @Test
    void departmentHeadAndSecondStageAuthorityOfDifferentDepartmentsGrantsApplications() throws Exception {

        final Person departmentHeadAndSecondStageAuth = new Person();
        departmentHeadAndSecondStageAuth.setFirstName("departmentHeadAndSecondStageAuth");
        departmentHeadAndSecondStageAuth.setPermissions(asList(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));

        final Person userOfDepartmentA = new Person();
        userOfDepartmentA.setFirstName("userOfDepartmentA");
        userOfDepartmentA.setPermissions(singletonList(USER));
        final Application applicationOfUserA = new Application();
        applicationOfUserA.setId(1);
        applicationOfUserA.setPerson(userOfDepartmentA);
        applicationOfUserA.setStatus(TEMPORARY_ALLOWED);
        applicationOfUserA.setStartDate(LocalDate.MAX);
        applicationOfUserA.setEndDate(LocalDate.MAX);

        final Person userOfDepartmentB = new Person();
        userOfDepartmentB.setFirstName("userOfDepartmentB");
        userOfDepartmentB.setPermissions(singletonList(USER));
        final Application applicationOfUserB = new Application();
        applicationOfUserB.setId(2);
        applicationOfUserB.setPerson(userOfDepartmentB);
        applicationOfUserB.setStatus(WAITING);
        applicationOfUserB.setStartDate(LocalDate.MAX);
        applicationOfUserB.setEndDate(LocalDate.MAX);

        when(personService.getSignedInUser()).thenReturn(departmentHeadAndSecondStageAuth);
        when(departmentService.getManagedMembersForSecondStageAuthority(departmentHeadAndSecondStageAuth)).thenReturn(asList(departmentHeadAndSecondStageAuth, userOfDepartmentA));
        when(departmentService.getManagedMembersOfDepartmentHead(departmentHeadAndSecondStageAuth)).thenReturn(asList(departmentHeadAndSecondStageAuth, userOfDepartmentB));
        when(applicationService.getApplicationsForACertainState(TEMPORARY_ALLOWED))
            .thenReturn(singletonList(applicationOfUserA));
        when(applicationService.getApplicationsForACertainState(WAITING))
            .thenReturn(singletonList(applicationOfUserB));

        perform(get("/web/application"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("applications", hasSize(2)))
            .andExpect(model().attribute("applications", hasItem(hasProperty("person",
                hasProperty("firstName", equalTo("userOfDepartmentA"))))))
            .andExpect(model().attribute("applications", hasItem(hasProperty("person",
                hasProperty("firstName", equalTo("userOfDepartmentB"))))))
            .andExpect(model().attribute("applications", hasItem(instanceOf(ApplicationForLeave.class))))
            .andExpect(view().name("application/app_list"));
    }

    @Test
    void departmentHeadAndSecondStageAuthorityOfSameDepartmentsGrantsApplications() throws Exception {

        final Person departmentHeadAndSecondStageAuth = new Person();
        departmentHeadAndSecondStageAuth.setFirstName("departmentHeadAndSecondStageAuth");
        departmentHeadAndSecondStageAuth.setPermissions(asList(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));

        final Person userOfDepartment = new Person();
        userOfDepartment.setFirstName("userOfDepartment");
        userOfDepartment.setPermissions(singletonList(USER));
        final Application temporaryAllowedApplication = new Application();
        temporaryAllowedApplication.setId(1);
        temporaryAllowedApplication.setPerson(userOfDepartment);
        temporaryAllowedApplication.setStatus(TEMPORARY_ALLOWED);
        temporaryAllowedApplication.setStartDate(LocalDate.MAX);
        temporaryAllowedApplication.setEndDate(LocalDate.MAX);

        final Application waitingApplication = new Application();
        waitingApplication.setId(2);
        waitingApplication.setPerson(userOfDepartment);
        waitingApplication.setStatus(WAITING);
        waitingApplication.setStartDate(LocalDate.MAX);
        waitingApplication.setEndDate(LocalDate.MAX);


        when(personService.getSignedInUser()).thenReturn(departmentHeadAndSecondStageAuth);
        when(departmentService.getManagedMembersForSecondStageAuthority(departmentHeadAndSecondStageAuth)).thenReturn(asList(departmentHeadAndSecondStageAuth, userOfDepartment));
        when(departmentService.getManagedMembersOfDepartmentHead(departmentHeadAndSecondStageAuth)).thenReturn(asList(departmentHeadAndSecondStageAuth, userOfDepartment));
        when(applicationService.getApplicationsForACertainState(TEMPORARY_ALLOWED))
            .thenReturn(singletonList(temporaryAllowedApplication));
        when(applicationService.getApplicationsForACertainState(WAITING))
            .thenReturn(singletonList(waitingApplication));

        perform(get("/web/application"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("applications", hasSize(2)))
            .andExpect(model().attribute("applications", hasItems(
                hasProperty("person", hasProperty("firstName", equalTo("userOfDepartment"))),
                hasProperty("status", equalTo(WAITING)),
                hasProperty("person", hasProperty("firstName", equalTo("userOfDepartment"))),
                hasProperty("status", equalTo(TEMPORARY_ALLOWED)))))

            .andExpect(model().attribute("applications", hasItem(instanceOf(ApplicationForLeave.class))))
            .andExpect(view().name("application/app_list"));
    }

    @Test
    void getApplicationForUser() throws Exception {

        final Person userPerson = new Person();
        userPerson.setFirstName("person");
        userPerson.setPermissions(singletonList(USER));
        final Application applicationOfUser = new Application();
        applicationOfUser.setId(3);
        applicationOfUser.setPerson(userPerson);
        applicationOfUser.setStatus(WAITING);
        applicationOfUser.setStartDate(LocalDate.MAX);
        applicationOfUser.setEndDate(LocalDate.MAX);

        when(personService.getSignedInUser()).thenReturn(userPerson);
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED), List.of(userPerson))).thenReturn(asList(applicationOfUser));

        final ResultActions resultActions = perform(get("/web/application"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("applications", hasSize(1)));
        resultActions.andExpect(model().attribute("applications", hasItem(hasProperty("person",
            hasProperty("firstName", equalTo("person"))))));
        resultActions.andExpect(model().attribute("applications", hasItem(instanceOf(ApplicationForLeave.class))));
        resultActions.andExpect(view().name("application/app_list"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
