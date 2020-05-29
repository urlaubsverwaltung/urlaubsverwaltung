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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
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
        sut = new ApplicationForLeaveViewController(applicationService, workDaysCountService, departmentService,
            personService);
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
        headPerson.setPermissions(List.of(DEPARTMENT_HEAD));
        final Application applicationOfHead = new Application();
        applicationOfHead.setId(2);
        applicationOfHead.setPerson(headPerson);
        when(personService.getSignedInUser()).thenReturn(headPerson);

        final Person secondStagePerson = new Person();
        secondStagePerson.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        final Application applicationOfSecondStage = new Application();
        applicationOfSecondStage.setId(3);
        applicationOfSecondStage.setPerson(secondStagePerson);

        final List<Person> members = List.of(headPerson, person, secondStagePerson);
        when(departmentService.getManagedMembersOfDepartmentHead(headPerson)).thenReturn(members);
        when(applicationService.getForStatesAndPerson(List.of(WAITING), members))
            .thenReturn(List.of(application, applicationOfHead, applicationOfSecondStage));

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10);
        applicationCancellationRequest.setPerson(headPerson);
        applicationCancellationRequest.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationCancellationRequest.setStartDate(LocalDate.MAX);
        applicationCancellationRequest.setEndDate(LocalDate.MAX);
        applicationCancellationRequest.setDayLength(FULL);

        when(applicationService.getForStatesAndPerson(List.of(ALLOWED_CANCELLATION_REQUESTED), List.of(headPerson)))
            .thenReturn(List.of(applicationCancellationRequest));

        perform(get("/web/application")).andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(headPerson)))
            .andExpect(model().attribute("applications", hasSize(1)))
            .andExpect(model().attribute("applications", hasItem(instanceOf(ApplicationForLeave.class))))
            .andExpect(model().attribute("applications", hasItem(hasProperty("person", hasProperty("firstName", equalTo("Atticus"))))))
            .andExpect(model().attribute("applications_cancellation_request", hasSize(1)))
            .andExpect(view().name("application/app_list"));
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
        bossPerson.setPermissions(List.of(BOSS));
        final Application applicationOfBoss = new Application();
        applicationOfBoss.setId(2);
        applicationOfBoss.setPerson(bossPerson);
        applicationOfBoss.setStatus(WAITING);
        applicationOfBoss.setStartDate(LocalDate.MAX);
        applicationOfBoss.setEndDate(LocalDate.MAX);
        when(personService.getSignedInUser()).thenReturn(bossPerson);

        final Person secondStagePerson = new Person();
        secondStagePerson.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        final Application applicationOfSecondStage = new Application();
        applicationOfSecondStage.setId(3);
        applicationOfSecondStage.setPerson(secondStagePerson);
        applicationOfSecondStage.setStatus(WAITING);
        applicationOfSecondStage.setStartDate(LocalDate.MAX);
        applicationOfSecondStage.setEndDate(LocalDate.MAX);

        when(applicationService.getForStates(List.of(WAITING, TEMPORARY_ALLOWED)))
            .thenReturn(List.of(application, applicationOfBoss, applicationOfSecondStage));

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10);
        applicationCancellationRequest.setPerson(bossPerson);
        applicationCancellationRequest.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationCancellationRequest.setStartDate(LocalDate.MAX);
        applicationCancellationRequest.setEndDate(LocalDate.MAX);
        applicationCancellationRequest.setDayLength(FULL);

        when(applicationService.getForStatesAndPerson(List.of(ALLOWED_CANCELLATION_REQUESTED), List.of(bossPerson)))
            .thenReturn(List.of(applicationCancellationRequest));

        perform(get("/web/application")).andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(bossPerson)))
            .andExpect(model().attribute("applications", hasSize(3)))
            .andExpect(model().attribute("applications", hasItem(instanceOf(ApplicationForLeave.class))))
            .andExpect(model().attribute("applications_cancellation_request", hasSize(1)))
            .andExpect(view().name("application/app_list"));
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
        officePerson.setPermissions(List.of(OFFICE));
        final Application applicationOfBoss = new Application();
        applicationOfBoss.setId(2);
        applicationOfBoss.setPerson(officePerson);
        applicationOfBoss.setStatus(WAITING);
        applicationOfBoss.setStartDate(LocalDate.MAX);
        applicationOfBoss.setEndDate(LocalDate.MAX);
        when(personService.getSignedInUser()).thenReturn(officePerson);

        final Person secondStagePerson = new Person();
        secondStagePerson.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        final Application applicationOfSecondStage = new Application();
        applicationOfSecondStage.setId(3);
        applicationOfSecondStage.setPerson(secondStagePerson);
        applicationOfSecondStage.setStatus(WAITING);
        applicationOfSecondStage.setStartDate(LocalDate.MAX);
        applicationOfSecondStage.setEndDate(LocalDate.MAX);

        when(applicationService.getForStates(List.of(WAITING, TEMPORARY_ALLOWED)))
            .thenReturn(List.of(application, applicationOfBoss, applicationOfSecondStage));

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10);
        applicationCancellationRequest.setPerson(person);
        applicationCancellationRequest.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationCancellationRequest.setStartDate(LocalDate.MAX);
        applicationCancellationRequest.setEndDate(LocalDate.MAX);
        applicationCancellationRequest.setDayLength(FULL);

        when(applicationService.getForStates(List.of(ALLOWED_CANCELLATION_REQUESTED)))
            .thenReturn(List.of(applicationCancellationRequest));

        perform(get("/web/application")).andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(officePerson)))
            .andExpect(model().attribute("applications", hasSize(3)))
            .andExpect(model().attribute("applications", hasItem(instanceOf(ApplicationForLeave.class))))
            .andExpect(model().attribute("applications_cancellation_request", hasSize(1)))
            .andExpect(model().attribute("applications_cancellation_request", hasItem(instanceOf(ApplicationForLeave.class))))
            .andExpect(view().name("application/app_list"));
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
        officePerson.setPermissions(List.of(OFFICE));
        final Application applicationOfBoss = new Application();
        applicationOfBoss.setId(2);
        applicationOfBoss.setPerson(officePerson);
        applicationOfBoss.setStatus(WAITING);
        applicationOfBoss.setStartDate(LocalDate.MAX);
        applicationOfBoss.setEndDate(LocalDate.MAX);

        final Person secondStagePerson = new Person();
        secondStagePerson.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        final Application applicationOfSecondStage = new Application();
        applicationOfSecondStage.setId(3);
        applicationOfSecondStage.setPerson(secondStagePerson);
        applicationOfSecondStage.setStatus(WAITING);
        applicationOfSecondStage.setStartDate(LocalDate.MAX);
        applicationOfSecondStage.setEndDate(LocalDate.MAX);
        when(personService.getSignedInUser()).thenReturn(secondStagePerson);

        final List<Person> members = List.of(secondStagePerson, person, officePerson);
        when(departmentService.getManagedMembersForSecondStageAuthority(secondStagePerson)).thenReturn(members);

        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED), members))
            .thenReturn(List.of(application, applicationOfBoss, applicationOfSecondStage));

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10);
        applicationCancellationRequest.setPerson(secondStagePerson);
        applicationCancellationRequest.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationCancellationRequest.setStartDate(LocalDate.MAX);
        applicationCancellationRequest.setEndDate(LocalDate.MAX);
        applicationCancellationRequest.setDayLength(FULL);

        when(applicationService.getForStatesAndPerson(List.of(ALLOWED_CANCELLATION_REQUESTED), List.of(secondStagePerson)))
            .thenReturn(List.of(applicationCancellationRequest));

        perform(get("/web/application")).andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(secondStagePerson)))
            .andExpect(model().attribute("applications", hasSize(2)))
            .andExpect(model().attribute("applications", hasItem(hasProperty("person", hasProperty("firstName", equalTo("office"))))))
            .andExpect(model().attribute("applications", hasItem(hasProperty("person", hasProperty("firstName", equalTo("person"))))))
            .andExpect(model().attribute("applications", hasItem(instanceOf(ApplicationForLeave.class))))
            .andExpect(model().attribute("applications_cancellation_request", hasSize(1)))
            .andExpect(view().name("application/app_list"));
    }

    @Test
    void departmentHeadAndSecondStageAuthorityOfDifferentDepartmentsGrantsApplications() throws Exception {

        final Person departmentHeadAndSecondStageAuth = new Person();
        departmentHeadAndSecondStageAuth.setFirstName("departmentHeadAndSecondStageAuth");
        departmentHeadAndSecondStageAuth.setPermissions(List.of(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));
        when(personService.getSignedInUser()).thenReturn(departmentHeadAndSecondStageAuth);

        final Person userOfDepartmentA = new Person();
        userOfDepartmentA.setFirstName("userOfDepartmentA");
        userOfDepartmentA.setPermissions(List.of(USER));
        final Application applicationOfUserA = new Application();
        applicationOfUserA.setId(1);
        applicationOfUserA.setPerson(userOfDepartmentA);
        applicationOfUserA.setStatus(TEMPORARY_ALLOWED);
        applicationOfUserA.setStartDate(LocalDate.MAX);
        applicationOfUserA.setEndDate(LocalDate.MAX);

        final Person userOfDepartmentB = new Person();
        userOfDepartmentB.setFirstName("userOfDepartmentB");
        userOfDepartmentB.setPermissions(List.of(USER));
        final Application applicationOfUserB = new Application();
        applicationOfUserB.setId(2);
        applicationOfUserB.setPerson(userOfDepartmentB);
        applicationOfUserB.setStatus(WAITING);
        applicationOfUserB.setStartDate(LocalDate.MAX);
        applicationOfUserB.setEndDate(LocalDate.MAX);

        final List<Person> membersSecondStage = List.of(departmentHeadAndSecondStageAuth);
        when(departmentService.getManagedMembersForSecondStageAuthority(departmentHeadAndSecondStageAuth)).thenReturn(membersSecondStage);
        final List<Person> membersDepartment = List.of(userOfDepartmentA);
        when(departmentService.getManagedMembersOfDepartmentHead(departmentHeadAndSecondStageAuth)).thenReturn(membersDepartment);

        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED), membersSecondStage)).thenReturn(List.of(applicationOfUserB));
        when(applicationService.getForStatesAndPerson(List.of(WAITING), membersDepartment)).thenReturn(List.of(applicationOfUserA));

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10);
        applicationCancellationRequest.setPerson(departmentHeadAndSecondStageAuth);
        applicationCancellationRequest.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationCancellationRequest.setStartDate(LocalDate.MAX);
        applicationCancellationRequest.setEndDate(LocalDate.MAX);
        applicationCancellationRequest.setDayLength(FULL);

        when(applicationService.getForStatesAndPerson(List.of(ALLOWED_CANCELLATION_REQUESTED), List.of(departmentHeadAndSecondStageAuth)))
            .thenReturn(List.of(applicationCancellationRequest));

        perform(get("/web/application"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(departmentHeadAndSecondStageAuth)))
            .andExpect(model().attribute("applications", hasSize(2)))
            .andExpect(model().attribute("applications", hasItem(hasProperty("person", hasProperty("firstName", equalTo("userOfDepartmentA"))))))
            .andExpect(model().attribute("applications", hasItem(hasProperty("person", hasProperty("firstName", equalTo("userOfDepartmentB"))))))
            .andExpect(model().attribute("applications", hasItem(instanceOf(ApplicationForLeave.class))))
            .andExpect(model().attribute("applications_cancellation_request", hasSize(1)))
            .andExpect(view().name("application/app_list"));
    }

    @Test
    void departmentHeadAndSecondStageAuthorityOfSameDepartmentsGrantsApplications() throws Exception {

        final Person departmentHeadAndSecondStageAuth = new Person();
        departmentHeadAndSecondStageAuth.setFirstName("departmentHeadAndSecondStageAuth");
        departmentHeadAndSecondStageAuth.setPermissions(List.of(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));
        when(personService.getSignedInUser()).thenReturn(departmentHeadAndSecondStageAuth);

        final Person userOfDepartment = new Person();
        userOfDepartment.setFirstName("userOfDepartment");
        userOfDepartment.setPermissions(List.of(USER));

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

        final List<Person> members = List.of(departmentHeadAndSecondStageAuth, userOfDepartment);
        when(departmentService.getManagedMembersForSecondStageAuthority(departmentHeadAndSecondStageAuth)).thenReturn(members);
        when(departmentService.getManagedMembersOfDepartmentHead(departmentHeadAndSecondStageAuth)).thenReturn(members);

        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED), members)).thenReturn(List.of(waitingApplication, temporaryAllowedApplication));

        perform(get("/web/application"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(departmentHeadAndSecondStageAuth)))
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
        userPerson.setPermissions(List.of(USER));
        final Application applicationOfUser = new Application();
        applicationOfUser.setId(3);
        applicationOfUser.setPerson(userPerson);
        applicationOfUser.setStatus(WAITING);
        applicationOfUser.setStartDate(LocalDate.MAX);
        applicationOfUser.setEndDate(LocalDate.MAX);

        when(personService.getSignedInUser()).thenReturn(userPerson);
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED), List.of(userPerson)))
            .thenReturn(List.of(applicationOfUser));

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10);
        applicationCancellationRequest.setPerson(userPerson);
        applicationCancellationRequest.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationCancellationRequest.setStartDate(LocalDate.MAX);
        applicationCancellationRequest.setEndDate(LocalDate.MAX);
        applicationCancellationRequest.setDayLength(FULL);

        when(applicationService.getForStatesAndPerson(List.of(ALLOWED_CANCELLATION_REQUESTED), List.of(userPerson)))
            .thenReturn(List.of(applicationCancellationRequest));

        perform(get("/web/application")).andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(userPerson)))
            .andExpect(model().attribute("applications", hasSize(1)))
            .andExpect(model().attribute("applications", hasItem(hasProperty("person", hasProperty("firstName", equalTo("person"))))))
            .andExpect(model().attribute("applications", hasItem(instanceOf(ApplicationForLeave.class))))
            .andExpect(model().attribute("applications_cancellation_request", hasSize(1)))
            .andExpect(view().name("application/app_list"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
