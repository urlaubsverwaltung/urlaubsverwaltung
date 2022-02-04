package org.synyx.urlaubsverwaltung.application.application;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeEntity;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
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
    @Mock
    private MessageSource messageSource;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("");

        sut = new ApplicationForLeaveViewController(applicationService, workDaysCountService, departmentService,
            personService, clock, messageSource);
    }

    @Test
    void getApplicationForDepartmentHead() throws Exception {

        final Person person = new Person();
        person.setId(1);
        person.setFirstName("Atticus");
        final Application application = new Application();
        application.setId(1);
        application.setVacationType(anyVacationType());
        application.setPerson(person);
        application.setStatus(WAITING);
        application.setStartDate(LocalDate.MAX);
        application.setEndDate(LocalDate.MAX);
        application.setDayLength(FULL);

        final Person headPerson = new Person();
        headPerson.setId(2);
        headPerson.setPermissions(List.of(DEPARTMENT_HEAD));
        final Application applicationOfHead = new Application();
        applicationOfHead.setId(2);
        applicationOfHead.setVacationType(anyVacationType());
        applicationOfHead.setPerson(headPerson);
        applicationOfHead.setStartDate(LocalDate.MAX);
        applicationOfHead.setEndDate(LocalDate.MAX);

        when(personService.getSignedInUser()).thenReturn(headPerson);

        final Person secondStagePerson = new Person();
        secondStagePerson.setId(3);
        secondStagePerson.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        final Application applicationOfSecondStage = new Application();
        applicationOfSecondStage.setId(3);
        applicationOfSecondStage.setVacationType(anyVacationType());
        applicationOfSecondStage.setPerson(secondStagePerson);
        applicationOfSecondStage.setStartDate(LocalDate.MAX);
        applicationOfSecondStage.setEndDate(LocalDate.MAX);

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10);
        applicationCancellationRequest.setVacationType(anyVacationType());
        applicationCancellationRequest.setPerson(headPerson);
        applicationCancellationRequest.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationCancellationRequest.setStartDate(LocalDate.MAX);
        applicationCancellationRequest.setEndDate(LocalDate.MAX);
        applicationCancellationRequest.setDayLength(FULL);

        when(departmentService.getMembersForDepartmentHead(headPerson))
            .thenReturn(List.of(headPerson, person, secondStagePerson));

        // applications for signed-in user
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(headPerson)))
            .thenReturn(List.of(applicationOfHead, applicationCancellationRequest));

        when(applicationService.getForStatesAndPerson(List.of(WAITING), List.of(headPerson, person, secondStagePerson)))
            .thenReturn(List.of(application, applicationOfHead, applicationOfSecondStage));

        perform(get("/web/application")).andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(headPerson)))
            .andExpect(model().attribute("userApplications", hasSize(2)))
            .andExpect(model().attribute("userApplications", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(2)),
                    hasProperty("cancellationRequested", is(false))
                ),
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(10)),
                    hasProperty("cancellationRequested", is(true))
                )
            )))
            .andExpect(model().attribute("otherApplications", hasSize(1)))
            .andExpect(model().attribute("otherApplications", hasItem(instanceOf(ApplicationForLeaveDto.class))))
            .andExpect(model().attribute("otherApplications", hasItem(hasProperty("person", hasProperty("name", equalTo("Atticus"))))))
            .andExpect(model().attributeDoesNotExist("applications_cancellation_request"))
            .andExpect(view().name("thymeleaf/application/application-overview"));
    }

    @Test
    void getApplicationForBoss() throws Exception {

        final Person person = new Person();
        final Application application = new Application();
        application.setId(1);
        application.setVacationType(anyVacationType());
        application.setPerson(person);
        application.setStatus(WAITING);
        application.setStartDate(LocalDate.MAX);
        application.setEndDate(LocalDate.MAX);
        application.setDayLength(FULL);

        final Person bossPerson = new Person();
        bossPerson.setPermissions(List.of(BOSS));
        final Application applicationOfBoss = new Application();
        applicationOfBoss.setId(2);
        applicationOfBoss.setVacationType(anyVacationType());
        applicationOfBoss.setPerson(bossPerson);
        applicationOfBoss.setStatus(WAITING);
        applicationOfBoss.setStartDate(LocalDate.MAX);
        applicationOfBoss.setEndDate(LocalDate.MAX);

        when(personService.getSignedInUser()).thenReturn(bossPerson);

        final Person secondStagePerson = new Person();
        secondStagePerson.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        final Application applicationOfSecondStage = new Application();
        applicationOfSecondStage.setId(3);
        applicationOfSecondStage.setVacationType(anyVacationType());
        applicationOfSecondStage.setPerson(secondStagePerson);
        applicationOfSecondStage.setStatus(WAITING);
        applicationOfSecondStage.setStartDate(LocalDate.MAX);
        applicationOfSecondStage.setEndDate(LocalDate.MAX);

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10);
        applicationCancellationRequest.setVacationType(anyVacationType());
        applicationCancellationRequest.setPerson(bossPerson);
        applicationCancellationRequest.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationCancellationRequest.setStartDate(LocalDate.MAX);
        applicationCancellationRequest.setEndDate(LocalDate.MAX);
        applicationCancellationRequest.setDayLength(FULL);

        // applications for signed-in user
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(bossPerson)))
            .thenReturn(List.of(applicationCancellationRequest));

        // other applications
        when(applicationService.getForStates(List.of(WAITING, TEMPORARY_ALLOWED)))
            .thenReturn(List.of(application, applicationOfBoss, applicationOfSecondStage));

        perform(get("/web/application")).andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(bossPerson)))
            .andExpect(model().attribute("userApplications", hasSize(1)))
            .andExpect(model().attribute("userApplications", hasItem(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(10)),
                    hasProperty("cancellationRequested", is(true))
                ))
            ))
            .andExpect(model().attribute("otherApplications", hasSize(2)))
            .andExpect(model().attribute("otherApplications", hasItem(instanceOf(ApplicationForLeaveDto.class))))
            .andExpect(model().attributeDoesNotExist("applications_cancellation_request"))
            .andExpect(view().name("thymeleaf/application/application-overview"));
    }

    @Test
    void getApplicationForOffice() throws Exception {

        final Person person = new Person();
        final Application application = new Application();
        application.setId(1);
        application.setVacationType(anyVacationType());
        application.setPerson(person);
        application.setStatus(TEMPORARY_ALLOWED);
        application.setStartDate(LocalDate.MAX);
        application.setEndDate(LocalDate.MAX);
        application.setDayLength(FULL);

        final Person officePerson = new Person();
        officePerson.setPermissions(List.of(OFFICE));
        final Application applicationOfOfficePerson = new Application();
        applicationOfOfficePerson.setId(2);
        applicationOfOfficePerson.setVacationType(anyVacationType());
        applicationOfOfficePerson.setPerson(officePerson);
        applicationOfOfficePerson.setStatus(WAITING);
        applicationOfOfficePerson.setStartDate(LocalDate.MAX);
        applicationOfOfficePerson.setEndDate(LocalDate.MAX);

        when(personService.getSignedInUser()).thenReturn(officePerson);

        final Person secondStagePerson = new Person();
        secondStagePerson.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        final Application applicationOfSecondStage = new Application();
        applicationOfSecondStage.setId(3);
        applicationOfSecondStage.setVacationType(anyVacationType());
        applicationOfSecondStage.setPerson(secondStagePerson);
        applicationOfSecondStage.setStatus(WAITING);
        applicationOfSecondStage.setStartDate(LocalDate.MAX);
        applicationOfSecondStage.setEndDate(LocalDate.MAX);

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10);
        applicationCancellationRequest.setVacationType(anyVacationType());
        applicationCancellationRequest.setPerson(person);
        applicationCancellationRequest.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationCancellationRequest.setStartDate(LocalDate.MAX);
        applicationCancellationRequest.setEndDate(LocalDate.MAX);
        applicationCancellationRequest.setDayLength(FULL);

        // applications for signed-in user
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(officePerson)))
            .thenReturn(List.of(applicationOfOfficePerson));

        // other applications
        when(applicationService.getForStates(List.of(WAITING, TEMPORARY_ALLOWED)))
            .thenReturn(List.of(application, applicationOfOfficePerson, applicationOfSecondStage));

        when(applicationService.getForStates(List.of(ALLOWED_CANCELLATION_REQUESTED)))
            .thenReturn(List.of(applicationCancellationRequest));

        perform(get("/web/application")).andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(officePerson)))
            .andExpect(model().attribute("userApplications", hasSize(1)))
            .andExpect(model().attribute("otherApplications", hasSize(2)))
            .andExpect(model().attribute("otherApplications", hasItem(instanceOf(ApplicationForLeaveDto.class))))
            .andExpect(model().attribute("applications_cancellation_request", hasSize(1)))
            .andExpect(model().attribute("applications_cancellation_request", hasItem(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(10)),
                    hasProperty("cancellationRequested", is(true))
                )
            )))
            .andExpect(view().name("thymeleaf/application/application-overview"));
    }

    @Test
    void getApplicationForSecondStage() throws Exception {

        final Person person = new Person();
        person.setFirstName("person");
        final Application application = new Application();
        application.setId(1);
        application.setVacationType(anyVacationType());
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
        applicationOfBoss.setVacationType(anyVacationType());
        applicationOfBoss.setPerson(officePerson);
        applicationOfBoss.setStatus(WAITING);
        applicationOfBoss.setStartDate(LocalDate.MAX);
        applicationOfBoss.setEndDate(LocalDate.MAX);

        final Person secondStagePerson = new Person();
        secondStagePerson.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        final Application applicationOfSecondStage = new Application();
        applicationOfSecondStage.setId(3);
        applicationOfSecondStage.setVacationType(anyVacationType());
        applicationOfSecondStage.setPerson(secondStagePerson);
        applicationOfSecondStage.setStatus(WAITING);
        applicationOfSecondStage.setStartDate(LocalDate.MAX);
        applicationOfSecondStage.setEndDate(LocalDate.MAX);

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10);
        applicationCancellationRequest.setVacationType(anyVacationType());
        applicationCancellationRequest.setPerson(secondStagePerson);
        applicationCancellationRequest.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationCancellationRequest.setStartDate(LocalDate.MAX);
        applicationCancellationRequest.setEndDate(LocalDate.MAX);
        applicationCancellationRequest.setDayLength(FULL);

        when(personService.getSignedInUser()).thenReturn(secondStagePerson);

        when(departmentService.getMembersForSecondStageAuthority(secondStagePerson))
            .thenReturn(List.of(secondStagePerson, person, officePerson));

        // applications for signed-in user
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(secondStagePerson)))
            .thenReturn(List.of(applicationCancellationRequest));

        // other applications
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED), List.of(secondStagePerson, person, officePerson)))
            .thenReturn(List.of(application, applicationOfBoss, applicationOfSecondStage));

        perform(get("/web/application")).andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(secondStagePerson)))
            .andExpect(model().attribute("userApplications", hasSize(1)))
            .andExpect(model().attribute("userApplications", hasItem(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(10)),
                    hasProperty("cancellationRequested", is(true))
                )
            )))
            .andExpect(model().attribute("otherApplications", hasSize(2)))
            .andExpect(model().attribute("otherApplications", hasItem(hasProperty("person", hasProperty("name", equalTo("office"))))))
            .andExpect(model().attribute("otherApplications", hasItem(hasProperty("person", hasProperty("name", equalTo("person"))))))
            .andExpect(model().attribute("otherApplications", hasItem(instanceOf(ApplicationForLeaveDto.class))))
            .andExpect(model().attributeDoesNotExist("applications_cancellation_request"))
            .andExpect(view().name("thymeleaf/application/application-overview"));
    }

    @Test
    void departmentHeadAndSecondStageAuthorityOfDifferentDepartmentsGrantsApplications() throws Exception {

        final Person departmentHeadAndSecondStageAuth = new Person();
        departmentHeadAndSecondStageAuth.setId(1);
        departmentHeadAndSecondStageAuth.setFirstName("departmentHeadAndSecondStageAuth");
        departmentHeadAndSecondStageAuth.setPermissions(List.of(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));

        when(personService.getSignedInUser()).thenReturn(departmentHeadAndSecondStageAuth);

        final Person userOfDepartmentA = new Person();
        userOfDepartmentA.setId(2);
        userOfDepartmentA.setFirstName("userOfDepartmentA");
        userOfDepartmentA.setPermissions(List.of(USER));
        final Application applicationOfUserA = new Application();
        applicationOfUserA.setId(1);
        applicationOfUserA.setVacationType(anyVacationType());
        applicationOfUserA.setPerson(userOfDepartmentA);
        applicationOfUserA.setStatus(TEMPORARY_ALLOWED);
        applicationOfUserA.setStartDate(LocalDate.MAX);
        applicationOfUserA.setEndDate(LocalDate.MAX);

        final Person userOfDepartmentB = new Person();
        userOfDepartmentB.setId(3);
        userOfDepartmentB.setFirstName("userOfDepartmentB");
        userOfDepartmentB.setPermissions(List.of(USER));
        final Application applicationOfUserB = new Application();
        applicationOfUserB.setId(2);
        applicationOfUserB.setVacationType(anyVacationType());
        applicationOfUserB.setPerson(userOfDepartmentB);
        applicationOfUserB.setStatus(WAITING);
        applicationOfUserB.setStartDate(LocalDate.MAX);
        applicationOfUserB.setEndDate(LocalDate.MAX);

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10);
        applicationCancellationRequest.setVacationType(anyVacationType());
        applicationCancellationRequest.setPerson(departmentHeadAndSecondStageAuth);
        applicationCancellationRequest.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationCancellationRequest.setStartDate(LocalDate.MAX);
        applicationCancellationRequest.setEndDate(LocalDate.MAX);
        applicationCancellationRequest.setDayLength(FULL);

        when(departmentService.getMembersForSecondStageAuthority(departmentHeadAndSecondStageAuth))
            .thenReturn(List.of(departmentHeadAndSecondStageAuth)); // todo check condition

        final List<Person> membersDepartment = List.of(userOfDepartmentA);
        when(departmentService.getMembersForDepartmentHead(departmentHeadAndSecondStageAuth)).thenReturn(membersDepartment);

        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(departmentHeadAndSecondStageAuth)))
            .thenReturn(List.of(applicationCancellationRequest));

        when(applicationService.getForStatesAndPerson(List.of(WAITING), membersDepartment))
            .thenReturn(List.of(applicationOfUserA, applicationOfUserB));

        // #getApplicationsForLeaveForSecondStageAuthority
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED), List.of(departmentHeadAndSecondStageAuth)))
            .thenReturn(List.of());

        perform(get("/web/application"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(departmentHeadAndSecondStageAuth)))
            .andExpect(model().attribute("userApplications", hasSize(1)))
            .andExpect(model().attribute("userApplications", hasItem(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(10)),
                    hasProperty("cancellationRequested", is(true))
                ))
            ))
            .andExpect(model().attribute("otherApplications", hasSize(2)))
            .andExpect(model().attribute("otherApplications", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("person",
                        hasProperty("name", equalTo("userOfDepartmentA"))
                    )
                ),
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("person",
                        hasProperty("name", equalTo("userOfDepartmentB"))
                    )
                )
            )))
            .andExpect(model().attributeDoesNotExist("applications_cancellation_request"))
            .andExpect(view().name("thymeleaf/application/application-overview"));
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
        temporaryAllowedApplication.setVacationType(anyVacationType());
        temporaryAllowedApplication.setPerson(userOfDepartment);
        temporaryAllowedApplication.setStatus(TEMPORARY_ALLOWED);
        temporaryAllowedApplication.setStartDate(LocalDate.MAX);
        temporaryAllowedApplication.setEndDate(LocalDate.MAX);

        final Application waitingApplication = new Application();
        waitingApplication.setId(2);
        waitingApplication.setVacationType(anyVacationType());
        waitingApplication.setPerson(userOfDepartment);
        waitingApplication.setStatus(WAITING);
        waitingApplication.setStartDate(LocalDate.MAX);
        waitingApplication.setEndDate(LocalDate.MAX);

        final List<Person> members = List.of(departmentHeadAndSecondStageAuth, userOfDepartment);
        when(departmentService.getMembersForSecondStageAuthority(departmentHeadAndSecondStageAuth)).thenReturn(members);
        when(departmentService.getMembersForDepartmentHead(departmentHeadAndSecondStageAuth)).thenReturn(members);

        // applications for signed-in user
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(departmentHeadAndSecondStageAuth)))
            .thenReturn(List.of());

        // other applications
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED), List.of(departmentHeadAndSecondStageAuth, userOfDepartment)))
            .thenReturn(List.of(waitingApplication, temporaryAllowedApplication));

        perform(get("/web/application"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(departmentHeadAndSecondStageAuth)))
            .andExpect(model().attribute("userApplications", hasSize(0)))
            .andExpect(model().attribute("otherApplications", hasSize(2)))
            .andExpect(model().attribute("otherApplications", hasItems(
                allOf(
                    hasProperty("person", hasProperty("name", equalTo("userOfDepartment"))),
                    hasProperty("statusWaiting", equalTo(true))
                ),
                allOf(
                    hasProperty("person", hasProperty("name", equalTo("userOfDepartment"))),
                    hasProperty("statusWaiting", equalTo(false))
                )
            )))
            .andExpect(model().attribute("otherApplications", hasItem(instanceOf(ApplicationForLeaveDto.class))))
            .andExpect(view().name("thymeleaf/application/application-overview"));
    }

    @Test
    void getApplicationForUser() throws Exception {

        final Person userPerson = new Person();
        userPerson.setFirstName("person");
        userPerson.setPermissions(List.of(USER));
        final Application applicationOfUser = new Application();
        applicationOfUser.setId(3);
        applicationOfUser.setVacationType(anyVacationType());
        applicationOfUser.setPerson(userPerson);
        applicationOfUser.setStatus(WAITING);
        applicationOfUser.setStartDate(LocalDate.MAX);
        applicationOfUser.setEndDate(LocalDate.MAX);

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10);
        applicationCancellationRequest.setVacationType(anyVacationType());
        applicationCancellationRequest.setPerson(userPerson);
        applicationCancellationRequest.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationCancellationRequest.setStartDate(LocalDate.MAX);
        applicationCancellationRequest.setEndDate(LocalDate.MAX);
        applicationCancellationRequest.setDayLength(FULL);

        when(personService.getSignedInUser()).thenReturn(userPerson);
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(userPerson)))
            .thenReturn(List.of(applicationOfUser, applicationCancellationRequest));

        perform(get("/web/application")).andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(userPerson)))
            .andExpect(model().attribute("userApplications", hasSize(2)))
            .andExpect(model().attribute("userApplications", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(3)),
                    hasProperty("person",
                        hasProperty("name", equalTo("person"))
                    )
                ),
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(10)),
                    hasProperty("cancellationRequested", is(true))
                ))
            ))
            .andExpect(model().attribute("otherApplications", hasSize(0)))
            .andExpect(model().attributeDoesNotExist("applications_cancellation_request"))
            .andExpect(view().name("thymeleaf/application/application-overview"));
    }

    @Test
    void ensureReplacementItem() throws Exception {
        final Person signedInUser = new Person();
        signedInUser.setId(1337);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");
        signedInUser.setLastName("Wayne");

        final Person applicationPerson = new Person();
        applicationPerson.setId(1);
        applicationPerson.setFirstName("Alfred");
        applicationPerson.setLastName("Pennyworth");
        applicationPerson.setPermissions(List.of(USER));

        final HolidayReplacementEntity holidayReplacement = new HolidayReplacementEntity();
        holidayReplacement.setPerson(signedInUser);
        holidayReplacement.setNote("awesome, thanks dude!");

        final Application application = new Application();
        application.setId(3);
        application.setPerson(applicationPerson);
        application.setStartDate(LocalDate.now(clock).plusDays(1));
        application.setEndDate(LocalDate.now(clock).plusDays(1));
        application.setHolidayReplacements(List.of(holidayReplacement));

        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(applicationService.getForHolidayReplacement(signedInUser, LocalDate.now(clock)))
            .thenReturn(List.of(application));

        perform(get("/web/application"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(signedInUser)))
            .andExpect(model().attribute("applications_holiday_replacements", contains(
                allOf(
                    hasProperty("person",
                        hasProperty("name", is("Alfred Pennyworth"))
                    ),
                    hasProperty("note", is("awesome, thanks dude!"))
                )
            )))
            .andExpect(view().name("thymeleaf/application/application-overview"));
    }

    @Test
    void ensureSecondStageAuthorityViewsAllowButton() throws Exception {

        final Person departmentHeadAndSecondStageAuth = new Person();
        departmentHeadAndSecondStageAuth.setId(1);
        departmentHeadAndSecondStageAuth.setFirstName("departmentHeadAndSecondStageAuth");
        departmentHeadAndSecondStageAuth.setPermissions(List.of(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));

        when(personService.getSignedInUser()).thenReturn(departmentHeadAndSecondStageAuth);

        final Person userOfDepartmentA = new Person();
        userOfDepartmentA.setId(2);
        userOfDepartmentA.setFirstName("userOfDepartmentA");
        userOfDepartmentA.setPermissions(List.of(USER));
        final Application applicationOfUserA = new Application();
        applicationOfUserA.setId(1);
        applicationOfUserA.setVacationType(anyVacationType());
        applicationOfUserA.setPerson(userOfDepartmentA);
        applicationOfUserA.setStatus(WAITING);
        applicationOfUserA.setStartDate(LocalDate.MAX);
        applicationOfUserA.setEndDate(LocalDate.MAX);
        applicationOfUserA.setTwoStageApproval(true);


        when(departmentService.getManagedMembersForSecondStageAuthority(departmentHeadAndSecondStageAuth))
            .thenReturn(List.of(userOfDepartmentA));

        final List<Person> membersDepartment = List.of(userOfDepartmentA);
        when(departmentService.getManagedMembersOfDepartmentHead(departmentHeadAndSecondStageAuth)).thenReturn(membersDepartment);

        // #getApplicationsForLeaveForSecondStageAuthority
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(departmentHeadAndSecondStageAuth)))
            .thenReturn(Lists.emptyList());

        // #getApplicationsForLeaveForSecondStageAuthority
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED), List.of(userOfDepartmentA)))
            .thenReturn(List.of(applicationOfUserA));

        perform(get("/web/application"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(departmentHeadAndSecondStageAuth)))
            .andExpect(model().attribute("userApplications", hasSize(0)))
            .andExpect(model().attribute("otherApplications", hasSize(1)))
            .andExpect(model().attribute("otherApplications", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("person",
                        hasProperty("name", equalTo("userOfDepartmentA"))
                    ),
                    hasProperty("temporaryApproveAllowed", equalTo(false)
                    )
                )
            )))
            .andExpect(model().attributeDoesNotExist("applications_cancellation_request"))
            .andExpect(view().name("thymeleaf/application/application-overview"));
    }

    @ParameterizedTest
    @EnumSource(value = ApplicationStatus.class, names = {"WAITING", "TEMPORARY_ALLOWED"})
    void ensureReplacementItemIsPendingForApplicationStatus(ApplicationStatus status) throws Exception {
        final Person signedInUser = new Person();
        signedInUser.setId(1337);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");

        final Person applicationPerson = new Person();
        applicationPerson.setId(1);
        applicationPerson.setFirstName("Alfred");
        applicationPerson.setPermissions(List.of(USER));

        final HolidayReplacementEntity holidayReplacement = new HolidayReplacementEntity();
        holidayReplacement.setPerson(signedInUser);

        final Application application = new Application();
        application.setId(3);
        application.setPerson(applicationPerson);
        application.setStatus(status);
        application.setStartDate(LocalDate.now(clock).plusDays(1));
        application.setEndDate(LocalDate.now(clock).plusDays(1));
        application.setHolidayReplacements(List.of(holidayReplacement));

        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(applicationService.getForHolidayReplacement(signedInUser, LocalDate.now(clock)))
            .thenReturn(List.of(application));

        perform(get("/web/application"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("applications_holiday_replacements", contains(
                hasProperty("pending", is(true))
            )))
            .andExpect(view().name("thymeleaf/application/application-overview"));
    }

    @ParameterizedTest
    @EnumSource(value = ApplicationStatus.class, names = {"ALLOWED", "ALLOWED_CANCELLATION_REQUESTED", "REJECTED", "CANCELLED", "REVOKED"})
    void ensureReplacementItemIsNotPendingForApplicationStatus(ApplicationStatus status) throws Exception {
        final Person signedInUser = new Person();
        signedInUser.setId(1337);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");
        signedInUser.setLastName("Wayne");

        final Person applicationPerson = new Person();
        applicationPerson.setId(1);
        applicationPerson.setFirstName("Alfred");
        applicationPerson.setLastName("Pennyworth");
        applicationPerson.setPermissions(List.of(USER));

        final HolidayReplacementEntity holidayReplacement = new HolidayReplacementEntity();
        holidayReplacement.setPerson(signedInUser);
        holidayReplacement.setNote("awesome, thanks dude!");

        final Application application = new Application();
        application.setId(3);
        application.setPerson(applicationPerson);
        application.setStatus(status);
        application.setStartDate(LocalDate.now(clock).plusDays(1));
        application.setEndDate(LocalDate.now(clock).plusDays(1));
        application.setHolidayReplacements(List.of(holidayReplacement));

        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(applicationService.getForHolidayReplacement(signedInUser, LocalDate.now(clock)))
            .thenReturn(List.of(application));

        perform(get("/web/application"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("applications_holiday_replacements", contains(
                hasProperty("pending", is(false))
            )))
            .andExpect(view().name("thymeleaf/application/application-overview"));
    }

    private static VacationTypeEntity anyVacationType() {
        final VacationTypeEntity vacationType = new VacationTypeEntity();
        vacationType.setCategory(VacationCategory.HOLIDAY);
        vacationType.setMessageKey("vacationTypeMessageKey");
        return vacationType;
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
