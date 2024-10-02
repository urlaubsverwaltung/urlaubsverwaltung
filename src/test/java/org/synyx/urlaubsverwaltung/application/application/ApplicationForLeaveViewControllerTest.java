package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SubmittedSickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SubmittedSickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_EDIT;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.SUBMITTED;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.WORKDAY;

@ExtendWith(MockitoExtension.class)
class ApplicationForLeaveViewControllerTest {

    private ApplicationForLeaveViewController sut;

    @Mock
    private ApplicationService applicationService;
    @Mock
    private SubmittedSickNoteService submittedSickNoteService;
    @Mock
    private WorkDaysCountService workDaysCountService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private PersonService personService;
    @Mock
    private SettingsService settingsService;
    @Mock
    private MessageSource messageSource;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {

        userIsAllowedToSubmitSickNotes(false);

        sut = new ApplicationForLeaveViewController(applicationService, submittedSickNoteService, workDaysCountService, departmentService,
            personService, settingsService, clock, messageSource);
    }

    @Test
    void getApplicationForUser() throws Exception {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("");

        final Person userPerson = new Person();
        userPerson.setFirstName("person");
        userPerson.setPermissions(List.of(USER));
        final Application applicationOfUser = new Application();
        applicationOfUser.setId(3L);
        applicationOfUser.setVacationType(anyVacationType());
        applicationOfUser.setPerson(userPerson);
        applicationOfUser.setStatus(WAITING);
        applicationOfUser.setStartDate(LocalDate.MAX);
        applicationOfUser.setEndDate(LocalDate.MAX);

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10L);
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
            .andExpect(model().attribute("canAccessApplicationStatistics", is(false)))
            .andExpect(model().attribute("canAccessCancellationRequests", is(false)))
            .andExpect(model().attribute("canAccessOtherApplications", is(false)))
            .andExpect(model().attribute("canAccessSickNoteSubmissions", is(false)))
            .andExpect(model().attribute("userApplications", hasSize(2)))
            .andExpect(model().attribute("userApplications", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(3L)),
                    hasProperty("person",
                        hasProperty("name", equalTo("person"))
                    )
                ),
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(10L)),
                    hasProperty("cancellationRequested", is(true))
                ))
            ))
            .andExpect(model().attribute("otherApplications", hasSize(0)))
            .andExpect(model().attributeDoesNotExist("applications_cancellation_request"))
            .andExpect(model().attribute("applications_holiday_replacements", hasSize(0)))
            .andExpect(view().name("application/application-overview"));
    }

    @Test
    void getApplicationForBoss() throws Exception {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("");

        final Person person = new Person();
        final Application application = new Application();
        application.setId(1L);
        application.setVacationType(anyVacationType());
        application.setPerson(person);
        application.setStatus(WAITING);
        application.setStartDate(LocalDate.MAX);
        application.setEndDate(LocalDate.MAX);
        application.setDayLength(FULL);

        final Person bossPerson = new Person();
        bossPerson.setPermissions(List.of(BOSS));
        final Application applicationOfBoss = new Application();
        applicationOfBoss.setId(2L);
        applicationOfBoss.setVacationType(anyVacationType());
        applicationOfBoss.setPerson(bossPerson);
        applicationOfBoss.setStatus(WAITING);
        applicationOfBoss.setStartDate(LocalDate.MAX);
        applicationOfBoss.setEndDate(LocalDate.MAX);

        when(personService.getSignedInUser()).thenReturn(bossPerson);

        final Person secondStagePerson = new Person();
        secondStagePerson.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        final Application applicationOfSecondStage = new Application();
        applicationOfSecondStage.setId(3L);
        applicationOfSecondStage.setVacationType(anyVacationType());
        applicationOfSecondStage.setPerson(secondStagePerson);
        applicationOfSecondStage.setStatus(WAITING);
        applicationOfSecondStage.setStartDate(LocalDate.MAX);
        applicationOfSecondStage.setEndDate(LocalDate.MAX);

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10L);
        applicationCancellationRequest.setVacationType(anyVacationType());
        applicationCancellationRequest.setPerson(bossPerson);
        applicationCancellationRequest.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationCancellationRequest.setStartDate(LocalDate.MAX);
        applicationCancellationRequest.setEndDate(LocalDate.MAX);
        applicationCancellationRequest.setDayLength(FULL);

        // applications for signed-in user
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(bossPerson)))
            .thenReturn(List.of(applicationOfBoss, applicationCancellationRequest));

        // other applications
        when(applicationService.getForStates(List.of(WAITING, TEMPORARY_ALLOWED)))
            .thenReturn(List.of(application, applicationOfSecondStage));

        perform(get("/web/application")).andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(bossPerson)))
            .andExpect(model().attribute("canAccessApplicationStatistics", is(true)))
            .andExpect(model().attribute("canAccessCancellationRequests", is(false)))
            .andExpect(model().attribute("canAccessOtherApplications", is(true)))
            .andExpect(model().attribute("canAccessSickNoteSubmissions", is(false)))
            .andExpect(model().attribute("userApplications", hasSize(2)))
            .andExpect(model().attribute("userApplications", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(2L)),
                    hasProperty("cancellationRequested", is(false)),
                    hasProperty("cancelAllowed", is(true))
                ),
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(10L)),
                    hasProperty("cancellationRequested", is(true)),
                    hasProperty("cancelAllowed", is(false))
                ))
            ))
            .andExpect(model().attribute("otherApplications", hasSize(2)))
            .andExpect(model().attribute("otherApplications", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(1L)),
                    hasProperty("cancelAllowed", is(false))
                ),
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(3L)),
                    hasProperty("cancelAllowed", is(false))
                ))
            ))
            .andExpect(model().attributeDoesNotExist("applications_cancellation_request"))
            .andExpect(model().attribute("applications_holiday_replacements", hasSize(0)))
            .andExpect(view().name("application/application-overview"));
    }

    @Test
    void getApplicationForBossWithCancellationRequested() throws Exception {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("");

        final Person person = new Person();
        final Application application = new Application();
        application.setId(1L);
        application.setVacationType(anyVacationType());
        application.setPerson(person);
        application.setStatus(WAITING);
        application.setStartDate(LocalDate.MAX);
        application.setEndDate(LocalDate.MAX);
        application.setDayLength(FULL);

        final Person secondStagePerson = new Person();
        secondStagePerson.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        final Application applicationOfSecondStage = new Application();
        applicationOfSecondStage.setId(3L);
        applicationOfSecondStage.setVacationType(anyVacationType());
        applicationOfSecondStage.setPerson(secondStagePerson);
        applicationOfSecondStage.setStatus(WAITING);
        applicationOfSecondStage.setStartDate(LocalDate.MAX);
        applicationOfSecondStage.setEndDate(LocalDate.MAX);

        when(applicationService.getForStates(List.of(WAITING, TEMPORARY_ALLOWED)))
            .thenReturn(List.of(application, applicationOfSecondStage));

        final Application applicationCancellationRequestPerson = new Application();
        applicationCancellationRequestPerson.setId(11L);
        applicationCancellationRequestPerson.setVacationType(anyVacationType());
        applicationCancellationRequestPerson.setPerson(secondStagePerson);
        applicationCancellationRequestPerson.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationCancellationRequestPerson.setStartDate(LocalDate.MAX);
        applicationCancellationRequestPerson.setEndDate(LocalDate.MAX);
        applicationCancellationRequestPerson.setDayLength(FULL);

        when(applicationService.getForStates(List.of(ALLOWED_CANCELLATION_REQUESTED)))
            .thenReturn(List.of(applicationCancellationRequestPerson));

        // Bosses applications
        final Person bossPerson = new Person();
        bossPerson.setPermissions(List.of(BOSS, APPLICATION_CANCELLATION_REQUESTED));
        when(personService.getSignedInUser()).thenReturn(bossPerson);

        final Application applicationOfBoss = new Application();
        applicationOfBoss.setId(2L);
        applicationOfBoss.setVacationType(anyVacationType());
        applicationOfBoss.setPerson(bossPerson);
        applicationOfBoss.setStatus(WAITING);
        applicationOfBoss.setStartDate(LocalDate.MAX);
        applicationOfBoss.setEndDate(LocalDate.MAX);

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10L);
        applicationCancellationRequest.setVacationType(anyVacationType());
        applicationCancellationRequest.setPerson(bossPerson);
        applicationCancellationRequest.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationCancellationRequest.setStartDate(LocalDate.MAX);
        applicationCancellationRequest.setEndDate(LocalDate.MAX);
        applicationCancellationRequest.setDayLength(FULL);

        // applications for signed-in user
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(bossPerson)))
            .thenReturn(List.of(applicationOfBoss, applicationCancellationRequest));

        perform(get("/web/application")).andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(bossPerson)))
            .andExpect(model().attribute("canAccessApplicationStatistics", is(true)))
            .andExpect(model().attribute("canAccessCancellationRequests", is(true)))
            .andExpect(model().attribute("canAccessOtherApplications", is(true)))
            .andExpect(model().attribute("canAccessSickNoteSubmissions", is(false)))
            .andExpect(model().attribute("userApplications", hasSize(2)))
            .andExpect(model().attribute("userApplications", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(2L)),
                    hasProperty("cancellationRequested", is(false)),
                    hasProperty("cancelAllowed", is(true))
                ),
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(10L)),
                    hasProperty("cancellationRequested", is(true)),
                    hasProperty("cancelAllowed", is(false))
                ))
            ))
            .andExpect(model().attribute("otherApplications", hasSize(2)))
            .andExpect(model().attribute("otherApplications", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(1L)),
                    hasProperty("cancelAllowed", is(false))
                ),
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(3L)),
                    hasProperty("cancelAllowed", is(false))
                ))
            ))
            .andExpect(model().attribute("applications_cancellation_request", hasSize(1)))
            .andExpect(model().attribute("applications_cancellation_request", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(11L))
                )
            )))
            .andExpect(model().attribute("applications_holiday_replacements", hasSize(0)))
            .andExpect(view().name("application/application-overview"));
    }

    @Test
    void getApplicationForOffice() throws Exception {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("");

        final Person person = new Person();
        final Application application = new Application();
        application.setId(1L);
        application.setVacationType(anyVacationType());
        application.setPerson(person);
        application.setStatus(TEMPORARY_ALLOWED);
        application.setStartDate(LocalDate.MAX);
        application.setEndDate(LocalDate.MAX);
        application.setDayLength(FULL);

        final Person officePerson = new Person();
        officePerson.setPermissions(List.of(OFFICE));
        final Application applicationOfOfficePerson = new Application();
        applicationOfOfficePerson.setId(2L);
        applicationOfOfficePerson.setVacationType(anyVacationType());
        applicationOfOfficePerson.setPerson(officePerson);
        applicationOfOfficePerson.setStatus(WAITING);
        applicationOfOfficePerson.setStartDate(LocalDate.MAX);
        applicationOfOfficePerson.setEndDate(LocalDate.MAX);

        when(personService.getSignedInUser()).thenReturn(officePerson);

        final Person secondStagePerson = new Person();
        secondStagePerson.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        final Application applicationOfSecondStage = new Application();
        applicationOfSecondStage.setId(3L);
        applicationOfSecondStage.setVacationType(anyVacationType());
        applicationOfSecondStage.setPerson(secondStagePerson);
        applicationOfSecondStage.setStatus(WAITING);
        applicationOfSecondStage.setStartDate(LocalDate.MAX);
        applicationOfSecondStage.setEndDate(LocalDate.MAX);

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10L);
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
            .andExpect(model().attribute("canAccessApplicationStatistics", is(true)))
            .andExpect(model().attribute("canAccessCancellationRequests", is(true)))
            .andExpect(model().attribute("canAccessOtherApplications", is(true)))
            .andExpect(model().attribute("canAccessSickNoteSubmissions", is(false)))
            .andExpect(model().attribute("userApplications", hasSize(1)))
            .andExpect(model().attribute("userApplications", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(2L)),
                    hasProperty("cancellationRequested", is(false)),
                    hasProperty("cancelAllowed", is(true))
                )
            )))
            .andExpect(model().attribute("otherApplications", hasSize(2)))
            .andExpect(model().attribute("otherApplications", hasItem(instanceOf(ApplicationForLeaveDto.class))))
            .andExpect(model().attribute("otherApplications", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(1L)),
                    hasProperty("cancellationRequested", is(false)),
                    hasProperty("cancelAllowed", is(true))
                ),
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(3L)),
                    hasProperty("cancellationRequested", is(false)),
                    hasProperty("cancelAllowed", is(true))
                )
            )))
            .andExpect(model().attribute("applications_cancellation_request", hasSize(1)))
            .andExpect(model().attribute("applications_cancellation_request", hasItem(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(10L)),
                    hasProperty("cancellationRequested", is(true))
                )
            )))
            .andExpect(model().attribute("applications_holiday_replacements", hasSize(0)))
            .andExpect(view().name("application/application-overview"));
    }

    @Test
    void getApplicationForDepartmentHead() throws Exception {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("");

        final Person person = new Person();
        person.setId(1L);
        person.setFirstName("Atticus");
        final Application application = new Application();
        application.setId(1L);
        application.setVacationType(anyVacationType());
        application.setPerson(person);
        application.setStatus(WAITING);
        application.setStartDate(LocalDate.MAX);
        application.setEndDate(LocalDate.MAX);
        application.setDayLength(FULL);

        final Person headPerson = new Person();
        headPerson.setId(2L);
        headPerson.setPermissions(List.of(DEPARTMENT_HEAD));
        final Application applicationOfHead = new Application();
        applicationOfHead.setId(2L);
        applicationOfHead.setVacationType(anyVacationType());
        applicationOfHead.setPerson(headPerson);
        applicationOfHead.setStatus(WAITING);
        applicationOfHead.setStartDate(LocalDate.MAX);
        applicationOfHead.setEndDate(LocalDate.MAX);

        when(personService.getSignedInUser()).thenReturn(headPerson);

        final Person secondStagePerson = new Person();
        secondStagePerson.setId(3L);
        secondStagePerson.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        final Application applicationOfSecondStage = new Application();
        applicationOfSecondStage.setId(3L);
        applicationOfSecondStage.setVacationType(anyVacationType());
        applicationOfSecondStage.setPerson(secondStagePerson);
        applicationOfSecondStage.setStartDate(LocalDate.MAX);
        applicationOfSecondStage.setEndDate(LocalDate.MAX);

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10L);
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
            .andExpect(model().attribute("canAccessApplicationStatistics", is(true)))
            .andExpect(model().attribute("canAccessCancellationRequests", is(false)))
            .andExpect(model().attribute("canAccessOtherApplications", is(true)))
            .andExpect(model().attribute("canAccessSickNoteSubmissions", is(false)))
            .andExpect(model().attribute("userApplications", hasSize(2)))
            .andExpect(model().attribute("userApplications", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(2L)),
                    hasProperty("cancellationRequested", is(false)),
                    hasProperty("cancelAllowed", is(true))
                ),
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(10L)),
                    hasProperty("cancellationRequested", is(true)),
                    hasProperty("cancelAllowed", is(false))
                )
            )))
            .andExpect(model().attribute("otherApplications", hasSize(1)))
            .andExpect(model().attribute("otherApplications", hasItem(instanceOf(ApplicationForLeaveDto.class))))
            .andExpect(model().attribute("otherApplications", hasItem(hasProperty("person", hasProperty("name", equalTo("Atticus"))))))
            .andExpect(model().attributeDoesNotExist("applications_cancellation_request"))
            .andExpect(model().attribute("applications_holiday_replacements", hasSize(0)))
            .andExpect(view().name("application/application-overview"));
    }

    @Test
    void getApplicationForDepartmentHeadWithCancellationRequested() throws Exception {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("");

        final Person person = new Person();
        person.setId(1L);
        person.setFirstName("Atticus");
        final Application application = new Application();
        application.setId(1L);
        application.setVacationType(anyVacationType());
        application.setPerson(person);
        application.setStatus(WAITING);
        application.setStartDate(LocalDate.MAX);
        application.setEndDate(LocalDate.MAX);
        application.setDayLength(FULL);

        final Application applicationCancellationRequestPerson = new Application();
        applicationCancellationRequestPerson.setId(11L);
        applicationCancellationRequestPerson.setVacationType(anyVacationType());
        applicationCancellationRequestPerson.setPerson(person);
        applicationCancellationRequestPerson.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationCancellationRequestPerson.setStartDate(LocalDate.MAX);
        applicationCancellationRequestPerson.setEndDate(LocalDate.MAX);
        applicationCancellationRequestPerson.setDayLength(FULL);

        final Person headPerson = new Person();
        headPerson.setId(2L);
        headPerson.setPermissions(List.of(DEPARTMENT_HEAD, APPLICATION_CANCELLATION_REQUESTED));
        final Application applicationOfHead = new Application();
        applicationOfHead.setId(2L);
        applicationOfHead.setVacationType(anyVacationType());
        applicationOfHead.setPerson(headPerson);
        applicationOfHead.setStatus(WAITING);
        applicationOfHead.setStartDate(LocalDate.MAX);
        applicationOfHead.setEndDate(LocalDate.MAX);

        when(personService.getSignedInUser()).thenReturn(headPerson);

        final Person secondStagePerson = new Person();
        secondStagePerson.setId(3L);
        secondStagePerson.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        final Application applicationOfSecondStage = new Application();
        applicationOfSecondStage.setId(3L);
        applicationOfSecondStage.setVacationType(anyVacationType());
        applicationOfSecondStage.setPerson(secondStagePerson);
        applicationOfSecondStage.setStartDate(LocalDate.MAX);
        applicationOfSecondStage.setEndDate(LocalDate.MAX);

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10L);
        applicationCancellationRequest.setVacationType(anyVacationType());
        applicationCancellationRequest.setPerson(headPerson);
        applicationCancellationRequest.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationCancellationRequest.setStartDate(LocalDate.MAX);
        applicationCancellationRequest.setEndDate(LocalDate.MAX);
        applicationCancellationRequest.setDayLength(FULL);

        when(departmentService.getMembersForDepartmentHead(headPerson))
            .thenReturn(List.of(headPerson, person, secondStagePerson));

        when(applicationService.getForStatesAndPerson(List.of(ALLOWED_CANCELLATION_REQUESTED), List.of(headPerson, person, secondStagePerson)))
            .thenReturn(List.of(applicationCancellationRequestPerson));

        // applications for signed-in user
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(headPerson)))
            .thenReturn(List.of(applicationOfHead, applicationCancellationRequest));

        when(applicationService.getForStatesAndPerson(List.of(WAITING), List.of(headPerson, person, secondStagePerson)))
            .thenReturn(List.of(application, applicationOfHead, applicationOfSecondStage));

        perform(get("/web/application")).andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(headPerson)))
            .andExpect(model().attribute("canAccessApplicationStatistics", is(true)))
            .andExpect(model().attribute("canAccessCancellationRequests", is(true)))
            .andExpect(model().attribute("canAccessOtherApplications", is(true)))
            .andExpect(model().attribute("canAccessSickNoteSubmissions", is(false)))
            .andExpect(model().attribute("userApplications", hasSize(2)))
            .andExpect(model().attribute("userApplications", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(2L)),
                    hasProperty("cancellationRequested", is(false)),
                    hasProperty("cancelAllowed", is(true))
                ),
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(10L)),
                    hasProperty("cancellationRequested", is(true)),
                    hasProperty("cancelAllowed", is(false))
                )
            )))
            .andExpect(model().attribute("otherApplications", hasSize(1)))
            .andExpect(model().attribute("otherApplications", hasItem(instanceOf(ApplicationForLeaveDto.class))))
            .andExpect(model().attribute("otherApplications", hasItem(hasProperty("person", hasProperty("name", equalTo("Atticus"))))))
            .andExpect(model().attribute("applications_cancellation_request", hasSize(1)))
            .andExpect(model().attribute("applications_cancellation_request", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(11L))
                )
            )))
            .andExpect(model().attribute("applications_holiday_replacements", hasSize(0)))
            .andExpect(view().name("application/application-overview"));
    }

    @Test
    void getApplicationForSecondStage() throws Exception {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("");

        final Person person = new Person();
        person.setFirstName("person");
        final Application application = new Application();
        application.setId(1L);
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
        applicationOfBoss.setId(2L);
        applicationOfBoss.setVacationType(anyVacationType());
        applicationOfBoss.setPerson(officePerson);
        applicationOfBoss.setStatus(WAITING);
        applicationOfBoss.setStartDate(LocalDate.MAX);
        applicationOfBoss.setEndDate(LocalDate.MAX);

        final Person secondStagePerson = new Person();
        secondStagePerson.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        final Application applicationOfSecondStage = new Application();
        applicationOfSecondStage.setId(3L);
        applicationOfSecondStage.setVacationType(anyVacationType());
        applicationOfSecondStage.setPerson(secondStagePerson);
        applicationOfSecondStage.setStatus(WAITING);
        applicationOfSecondStage.setStartDate(LocalDate.MAX);
        applicationOfSecondStage.setEndDate(LocalDate.MAX);

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10L);
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
            .andExpect(model().attribute("canAccessApplicationStatistics", is(true)))
            .andExpect(model().attribute("canAccessCancellationRequests", is(false)))
            .andExpect(model().attribute("canAccessOtherApplications", is(true)))
            .andExpect(model().attribute("canAccessSickNoteSubmissions", is(false)))
            .andExpect(model().attribute("userApplications", hasSize(1)))
            .andExpect(model().attribute("userApplications", hasItem(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(10L)),
                    hasProperty("cancellationRequested", is(true)),
                    hasProperty("cancelAllowed", is(false)),
                    hasProperty("approveAllowed", is(false))
                )
            )))
            .andExpect(model().attribute("otherApplications", hasSize(2)))
            .andExpect(model().attribute("otherApplications", hasItem(hasProperty("person", hasProperty("name", equalTo("office"))))))
            .andExpect(model().attribute("otherApplications", hasItem(hasProperty("person", hasProperty("name", equalTo("person"))))))
            .andExpect(model().attribute("otherApplications", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(1L)),
                    hasProperty("cancellationRequested", is(false)),
                    hasProperty("cancelAllowed", is(false)),
                    hasProperty("approveAllowed", is(true))
                ),
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(2L)),
                    hasProperty("cancellationRequested", is(false)),
                    hasProperty("cancelAllowed", is(false)),
                    hasProperty("approveAllowed", is(true))
                )
            )))
            .andExpect(model().attributeDoesNotExist("applications_cancellation_request"))
            .andExpect(model().attribute("applications_holiday_replacements", hasSize(0)))
            .andExpect(view().name("application/application-overview"));
    }

    @Test
    void getApplicationForSecondStageWithCancellationRequested() throws Exception {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("");

        final Person person = new Person();
        person.setFirstName("person");
        final Application application = new Application();
        application.setId(1L);
        application.setVacationType(anyVacationType());
        application.setPerson(person);
        application.setStatus(TEMPORARY_ALLOWED);
        application.setStartDate(LocalDate.MAX);
        application.setEndDate(LocalDate.MAX);
        application.setDayLength(FULL);

        final Application applicationCancellationRequestPerson = new Application();
        applicationCancellationRequestPerson.setId(11L);
        applicationCancellationRequestPerson.setVacationType(anyVacationType());
        applicationCancellationRequestPerson.setPerson(person);
        applicationCancellationRequestPerson.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationCancellationRequestPerson.setStartDate(LocalDate.MAX);
        applicationCancellationRequestPerson.setEndDate(LocalDate.MAX);
        applicationCancellationRequestPerson.setDayLength(FULL);

        final Person officePerson = new Person();
        officePerson.setFirstName("office");
        officePerson.setPermissions(List.of(OFFICE));
        final Application applicationOfBoss = new Application();
        applicationOfBoss.setId(2L);
        applicationOfBoss.setVacationType(anyVacationType());
        applicationOfBoss.setPerson(officePerson);
        applicationOfBoss.setStatus(WAITING);
        applicationOfBoss.setStartDate(LocalDate.MAX);
        applicationOfBoss.setEndDate(LocalDate.MAX);

        final Person secondStagePerson = new Person();
        secondStagePerson.setPermissions(List.of(SECOND_STAGE_AUTHORITY, APPLICATION_CANCELLATION_REQUESTED));
        final Application applicationOfSecondStage = new Application();
        applicationOfSecondStage.setId(3L);
        applicationOfSecondStage.setVacationType(anyVacationType());
        applicationOfSecondStage.setPerson(secondStagePerson);
        applicationOfSecondStage.setStatus(WAITING);
        applicationOfSecondStage.setStartDate(LocalDate.MAX);
        applicationOfSecondStage.setEndDate(LocalDate.MAX);

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10L);
        applicationCancellationRequest.setVacationType(anyVacationType());
        applicationCancellationRequest.setPerson(secondStagePerson);
        applicationCancellationRequest.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationCancellationRequest.setStartDate(LocalDate.MAX);
        applicationCancellationRequest.setEndDate(LocalDate.MAX);
        applicationCancellationRequest.setDayLength(FULL);

        when(personService.getSignedInUser()).thenReturn(secondStagePerson);

        when(departmentService.getMembersForSecondStageAuthority(secondStagePerson))
            .thenReturn(List.of(secondStagePerson, person, officePerson));

        when(applicationService.getForStatesAndPerson(List.of(ALLOWED_CANCELLATION_REQUESTED), List.of(secondStagePerson, person, officePerson)))
            .thenReturn(List.of(applicationCancellationRequestPerson));

        // applications for signed-in user
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(secondStagePerson)))
            .thenReturn(List.of(applicationCancellationRequest));

        // other applications
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED), List.of(secondStagePerson, person, officePerson)))
            .thenReturn(List.of(application, applicationOfBoss, applicationOfSecondStage));

        perform(get("/web/application")).andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(secondStagePerson)))
            .andExpect(model().attribute("canAccessApplicationStatistics", is(true)))
            .andExpect(model().attribute("canAccessCancellationRequests", is(true)))
            .andExpect(model().attribute("canAccessOtherApplications", is(true)))
            .andExpect(model().attribute("canAccessSickNoteSubmissions", is(false)))
            .andExpect(model().attribute("userApplications", hasSize(1)))
            .andExpect(model().attribute("userApplications", hasItem(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(10L)),
                    hasProperty("cancellationRequested", is(true)),
                    hasProperty("cancelAllowed", is(false)),
                    hasProperty("approveAllowed", is(false))
                )
            )))
            .andExpect(model().attribute("otherApplications", hasSize(2)))
            .andExpect(model().attribute("otherApplications", hasItem(hasProperty("person", hasProperty("name", equalTo("office"))))))
            .andExpect(model().attribute("otherApplications", hasItem(hasProperty("person", hasProperty("name", equalTo("person"))))))
            .andExpect(model().attribute("otherApplications", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(1L)),
                    hasProperty("cancellationRequested", is(false)),
                    hasProperty("cancelAllowed", is(false)),
                    hasProperty("approveAllowed", is(true))
                ),
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(2L)),
                    hasProperty("cancellationRequested", is(false)),
                    hasProperty("cancelAllowed", is(false)),
                    hasProperty("approveAllowed", is(true))
                )
            )))
            .andExpect(model().attribute("applications_cancellation_request", hasSize(1)))
            .andExpect(model().attribute("applications_cancellation_request", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(11L))
                )
            )))
            .andExpect(model().attribute("applications_holiday_replacements", hasSize(0)))
            .andExpect(view().name("application/application-overview"));
    }

    @Test
    void departmentHeadAndSecondStageAuthorityOfDifferentDepartmentsGrantsApplications() throws Exception {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("");

        final Person departmentHeadAndSecondStageAuth = new Person();
        departmentHeadAndSecondStageAuth.setId(1L);
        departmentHeadAndSecondStageAuth.setFirstName("departmentHeadAndSecondStageAuth");
        departmentHeadAndSecondStageAuth.setPermissions(List.of(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));

        when(personService.getSignedInUser()).thenReturn(departmentHeadAndSecondStageAuth);

        final Person userOfDepartmentA = new Person();
        userOfDepartmentA.setId(2L);
        userOfDepartmentA.setFirstName("userOfDepartmentA");
        userOfDepartmentA.setPermissions(List.of(USER));
        final Application applicationOfUserA = new Application();
        applicationOfUserA.setId(1L);
        applicationOfUserA.setVacationType(anyVacationType());
        applicationOfUserA.setPerson(userOfDepartmentA);
        applicationOfUserA.setStatus(TEMPORARY_ALLOWED);
        applicationOfUserA.setStartDate(LocalDate.MAX);
        applicationOfUserA.setEndDate(LocalDate.MAX);

        final Person userOfDepartmentB = new Person();
        userOfDepartmentB.setId(3L);
        userOfDepartmentB.setFirstName("userOfDepartmentB");
        userOfDepartmentB.setPermissions(List.of(USER));
        final Application applicationOfUserB = new Application();
        applicationOfUserB.setId(2L);
        applicationOfUserB.setVacationType(anyVacationType());
        applicationOfUserB.setPerson(userOfDepartmentB);
        applicationOfUserB.setStatus(WAITING);
        applicationOfUserB.setStartDate(LocalDate.MAX);
        applicationOfUserB.setEndDate(LocalDate.MAX);

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10L);
        applicationCancellationRequest.setVacationType(anyVacationType());
        applicationCancellationRequest.setPerson(departmentHeadAndSecondStageAuth);
        applicationCancellationRequest.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationCancellationRequest.setStartDate(LocalDate.MAX);
        applicationCancellationRequest.setEndDate(LocalDate.MAX);
        applicationCancellationRequest.setDayLength(FULL);

        when(departmentService.getMembersForSecondStageAuthority(departmentHeadAndSecondStageAuth))
            .thenReturn(List.of(departmentHeadAndSecondStageAuth));

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
                    hasProperty("id", is(10L)),
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
            .andExpect(view().name("application/application-overview"));
    }

    @Test
    void departmentHeadAndSecondStageAuthorityOfDifferentDepartmentsGrantsApplicationsWithCancellationRequested() throws Exception {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("");

        final Person departmentHeadAndSecondStageAuth = new Person();
        departmentHeadAndSecondStageAuth.setId(1L);
        departmentHeadAndSecondStageAuth.setFirstName("departmentHeadAndSecondStageAuth");
        departmentHeadAndSecondStageAuth.setPermissions(List.of(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY, APPLICATION_CANCELLATION_REQUESTED));
        when(personService.getSignedInUser()).thenReturn(departmentHeadAndSecondStageAuth);

        final Person userOfDepartmentA = new Person();
        userOfDepartmentA.setId(2L);
        userOfDepartmentA.setFirstName("userOfDepartmentA");
        userOfDepartmentA.setPermissions(List.of(USER));
        final Application applicationOfUserA = new Application();
        applicationOfUserA.setId(1L);
        applicationOfUserA.setVacationType(anyVacationType());
        applicationOfUserA.setPerson(userOfDepartmentA);
        applicationOfUserA.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationOfUserA.setStartDate(LocalDate.MAX);
        applicationOfUserA.setEndDate(LocalDate.MAX);

        final Person userOfDepartmentB = new Person();
        userOfDepartmentB.setId(3L);
        userOfDepartmentB.setFirstName("userOfDepartmentB");
        userOfDepartmentB.setPermissions(List.of(USER));
        final Application applicationOfUserB = new Application();
        applicationOfUserB.setId(2L);
        applicationOfUserB.setVacationType(anyVacationType());
        applicationOfUserB.setPerson(userOfDepartmentB);
        applicationOfUserB.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationOfUserB.setStartDate(LocalDate.MAX);
        applicationOfUserB.setEndDate(LocalDate.MAX);

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10L);
        applicationCancellationRequest.setVacationType(anyVacationType());
        applicationCancellationRequest.setPerson(departmentHeadAndSecondStageAuth);
        applicationCancellationRequest.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationCancellationRequest.setStartDate(LocalDate.MAX);
        applicationCancellationRequest.setEndDate(LocalDate.MAX);
        applicationCancellationRequest.setDayLength(FULL);

        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(departmentHeadAndSecondStageAuth))).thenReturn(List.of());
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED), List.of(userOfDepartmentB))).thenReturn(List.of());
        when(applicationService.getForStatesAndPerson(List.of(WAITING), List.of(userOfDepartmentA))).thenReturn(List.of());

        when(departmentService.getMembersForDepartmentHead(departmentHeadAndSecondStageAuth)).thenReturn(List.of(userOfDepartmentA));
        when(applicationService.getForStatesAndPerson(List.of(ALLOWED_CANCELLATION_REQUESTED), List.of(userOfDepartmentA))).thenReturn(List.of(applicationOfUserA));

        when(departmentService.getMembersForSecondStageAuthority(departmentHeadAndSecondStageAuth)).thenReturn(List.of(userOfDepartmentB));
        when(applicationService.getForStatesAndPerson(List.of(ALLOWED_CANCELLATION_REQUESTED), List.of(userOfDepartmentB))).thenReturn(List.of(applicationOfUserB));

        perform(get("/web/application"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(departmentHeadAndSecondStageAuth)))
            .andExpect(model().attribute("userApplications", hasSize(0)))
            .andExpect(model().attribute("otherApplications", hasSize(0)))
            .andExpect(model().attribute("applications_cancellation_request", hasSize(2)))
            .andExpect(model().attribute("applications_cancellation_request", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(1L)),
                    hasProperty("person",
                        hasProperty("name", equalTo("userOfDepartmentA"))
                    )
                ),
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(2L)),
                    hasProperty("person",
                        hasProperty("name", equalTo("userOfDepartmentB"))
                    )
                )
            )))
            .andExpect(view().name("application/application-overview"));
    }

    @Test
    void ensureDistinctCancellationRequests() throws Exception {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("");

        final Person departmentHeadAndSecondStageAuth = new Person();
        departmentHeadAndSecondStageAuth.setId(1L);
        departmentHeadAndSecondStageAuth.setFirstName("departmentHeadAndSecondStageAuth");
        departmentHeadAndSecondStageAuth.setPermissions(List.of(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY, APPLICATION_CANCELLATION_REQUESTED));
        when(personService.getSignedInUser()).thenReturn(departmentHeadAndSecondStageAuth);

        final Person userOfDepartmentA = new Person();
        userOfDepartmentA.setId(2L);
        userOfDepartmentA.setFirstName("userOfDepartmentA");
        userOfDepartmentA.setPermissions(List.of(USER));
        final Application applicationOfUserA = new Application();
        applicationOfUserA.setId(1L);
        applicationOfUserA.setVacationType(anyVacationType());
        applicationOfUserA.setPerson(userOfDepartmentA);
        applicationOfUserA.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationOfUserA.setStartDate(LocalDate.MAX);
        applicationOfUserA.setEndDate(LocalDate.MAX);

        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(departmentHeadAndSecondStageAuth))).thenReturn(List.of());
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED), List.of(userOfDepartmentA))).thenReturn(List.of());
        when(applicationService.getForStatesAndPerson(List.of(WAITING), List.of(userOfDepartmentA))).thenReturn(List.of());

        when(departmentService.getMembersForDepartmentHead(departmentHeadAndSecondStageAuth)).thenReturn(List.of(userOfDepartmentA));
        when(applicationService.getForStatesAndPerson(List.of(ALLOWED_CANCELLATION_REQUESTED), List.of(userOfDepartmentA))).thenReturn(List.of(applicationOfUserA));

        when(departmentService.getMembersForSecondStageAuthority(departmentHeadAndSecondStageAuth)).thenReturn(List.of(userOfDepartmentA));
        when(applicationService.getForStatesAndPerson(List.of(ALLOWED_CANCELLATION_REQUESTED), List.of(userOfDepartmentA))).thenReturn(List.of(applicationOfUserA));

        perform(get("/web/application"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(departmentHeadAndSecondStageAuth)))
            .andExpect(model().attribute("userApplications", hasSize(0)))
            .andExpect(model().attribute("otherApplications", hasSize(0)))
            .andExpect(model().attribute("applications_cancellation_request", hasSize(1)))
            .andExpect(model().attribute("applications_cancellation_request", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(1L)),
                    hasProperty("person",
                        hasProperty("name", equalTo("userOfDepartmentA"))
                    )
                )
            )))
            .andExpect(view().name("application/application-overview"));
    }

    @Test
    void departmentHeadAndSecondStageAuthorityOfSameDepartmentsGrantsApplications() throws Exception {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("");

        final Person departmentHeadAndSecondStageAuth = new Person();
        departmentHeadAndSecondStageAuth.setFirstName("departmentHeadAndSecondStageAuth");
        departmentHeadAndSecondStageAuth.setPermissions(List.of(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));
        when(personService.getSignedInUser()).thenReturn(departmentHeadAndSecondStageAuth);

        final Person userOfDepartment = new Person();
        userOfDepartment.setFirstName("userOfDepartment");
        userOfDepartment.setPermissions(List.of(USER));

        final Application temporaryAllowedApplication = new Application();
        temporaryAllowedApplication.setId(1L);
        temporaryAllowedApplication.setVacationType(anyVacationType());
        temporaryAllowedApplication.setPerson(userOfDepartment);
        temporaryAllowedApplication.setStatus(TEMPORARY_ALLOWED);
        temporaryAllowedApplication.setStartDate(LocalDate.MAX);
        temporaryAllowedApplication.setEndDate(LocalDate.MAX);

        final Application waitingApplication = new Application();
        waitingApplication.setId(2L);
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
            .andExpect(view().name("application/application-overview"));
    }

    @Test
    void ensureOvertimeMoreThan24hAreDisplayedCorrectly() throws Exception {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("");

        final Person userPerson = new Person();
        userPerson.setFirstName("person");
        userPerson.setPermissions(List.of(USER));
        final Application applicationOfUser = new Application();
        applicationOfUser.setId(3L);
        applicationOfUser.setVacationType(anyVacationType());
        applicationOfUser.setPerson(userPerson);
        applicationOfUser.setStatus(WAITING);
        applicationOfUser.setHours(Duration.ofHours(35));
        applicationOfUser.setStartDate(LocalDate.MAX);
        applicationOfUser.setEndDate(LocalDate.MAX);

        when(personService.getSignedInUser()).thenReturn(userPerson);
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(userPerson))).thenReturn(List.of(applicationOfUser));

        perform(get("/web/application")).andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(userPerson)))
            .andExpect(model().attribute("userApplications", hasSize(1)))
            .andExpect(model().attribute("userApplications", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(3L)),
                    hasProperty("duration", is("35 "))
                ))
            ))
            .andExpect(view().name("application/application-overview"));
    }

    @Test
    void getApplicationForDepartmentHeadAndOfficeRoleAndNotAllAreInHisDepartment() throws Exception {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("");

        final Person person = new Person();
        person.setId(1L);
        person.setFirstName("Atticus");
        final Application application = new Application();
        application.setId(1L);
        application.setVacationType(anyVacationType());
        application.setPerson(person);
        application.setStatus(WAITING);
        application.setStartDate(LocalDate.MAX);
        application.setEndDate(LocalDate.MAX);
        application.setDayLength(FULL);

        final Person personNotMember = new Person();
        personNotMember.setId(11L);
        personNotMember.setFirstName("Not Member");
        final Application applicationNotMember = new Application();
        applicationNotMember.setId(11L);
        applicationNotMember.setVacationType(anyVacationType());
        applicationNotMember.setPerson(personNotMember);
        applicationNotMember.setStatus(WAITING);
        applicationNotMember.setStartDate(LocalDate.MAX);
        applicationNotMember.setEndDate(LocalDate.MAX);
        applicationNotMember.setDayLength(FULL);

        final Person headAndOfficePerson = new Person();
        headAndOfficePerson.setId(2L);
        headAndOfficePerson.setPermissions(List.of(USER, DEPARTMENT_HEAD, OFFICE));
        final Application applicationOfHeadAndOffice = new Application();
        applicationOfHeadAndOffice.setId(2L);
        applicationOfHeadAndOffice.setStatus(WAITING);
        applicationOfHeadAndOffice.setVacationType(anyVacationType());
        applicationOfHeadAndOffice.setPerson(headAndOfficePerson);
        applicationOfHeadAndOffice.setStartDate(LocalDate.MAX);
        applicationOfHeadAndOffice.setEndDate(LocalDate.MAX);

        when(personService.getSignedInUser()).thenReturn(headAndOfficePerson);

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10L);
        applicationCancellationRequest.setVacationType(anyVacationType());
        applicationCancellationRequest.setPerson(headAndOfficePerson);
        applicationCancellationRequest.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationCancellationRequest.setStartDate(LocalDate.MAX);
        applicationCancellationRequest.setEndDate(LocalDate.MAX);
        applicationCancellationRequest.setDayLength(FULL);

        when(departmentService.getMembersForDepartmentHead(headAndOfficePerson))
            .thenReturn(List.of(headAndOfficePerson, person));

        // applications for signed-in user
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(headAndOfficePerson)))
            .thenReturn(List.of(applicationOfHeadAndOffice, applicationCancellationRequest));

        // other as office
        when(applicationService.getForStates(List.of(WAITING, TEMPORARY_ALLOWED)))
            .thenReturn(List.of(application, applicationNotMember));

        perform(get("/web/application")).andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(headAndOfficePerson)))
            .andExpect(model().attribute("otherApplications", hasSize(2)))
            .andExpect(model().attribute("otherApplications", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(1L)),
                    hasProperty("approveAllowed", equalTo(true)),
                    hasProperty("rejectAllowed", equalTo(true))
                ),
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(11L)),
                    hasProperty("approveAllowed", equalTo(false)),
                    hasProperty("rejectAllowed", equalTo(false))
                )
            )))
            .andExpect(view().name("application/application-overview"));
    }

    @Test
    void getApplicationForSecondStageAuthorityAndOfficeRoleAndNotAllAreInHisDepartment() throws Exception {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("");

        final Person person = new Person();
        person.setId(1L);
        person.setFirstName("Atticus");
        final Application application = new Application();
        application.setId(1L);
        application.setVacationType(anyVacationType());
        application.setPerson(person);
        application.setStatus(WAITING);
        application.setStartDate(LocalDate.MAX);
        application.setEndDate(LocalDate.MAX);
        application.setDayLength(FULL);

        final Person personNotMember = new Person();
        personNotMember.setId(11L);
        personNotMember.setFirstName("Not Member");
        final Application applicationNotMember = new Application();
        applicationNotMember.setId(11L);
        applicationNotMember.setVacationType(anyVacationType());
        applicationNotMember.setPerson(personNotMember);
        applicationNotMember.setStatus(WAITING);
        applicationNotMember.setStartDate(LocalDate.MAX);
        applicationNotMember.setEndDate(LocalDate.MAX);
        applicationNotMember.setDayLength(FULL);

        final Person ssaAndOfficePerson = new Person();
        ssaAndOfficePerson.setId(2L);
        ssaAndOfficePerson.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY, OFFICE));
        final Application applicationOfSsaAndOffice = new Application();
        applicationOfSsaAndOffice.setId(2L);
        applicationOfSsaAndOffice.setStatus(WAITING);
        applicationOfSsaAndOffice.setVacationType(anyVacationType());
        applicationOfSsaAndOffice.setPerson(ssaAndOfficePerson);
        applicationOfSsaAndOffice.setStartDate(LocalDate.MAX);
        applicationOfSsaAndOffice.setEndDate(LocalDate.MAX);

        when(personService.getSignedInUser()).thenReturn(ssaAndOfficePerson);

        final Application applicationCancellationRequest = new Application();
        applicationCancellationRequest.setId(10L);
        applicationCancellationRequest.setVacationType(anyVacationType());
        applicationCancellationRequest.setPerson(ssaAndOfficePerson);
        applicationCancellationRequest.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationCancellationRequest.setStartDate(LocalDate.MAX);
        applicationCancellationRequest.setEndDate(LocalDate.MAX);
        applicationCancellationRequest.setDayLength(FULL);

        when(departmentService.getMembersForSecondStageAuthority(ssaAndOfficePerson))
            .thenReturn(List.of(ssaAndOfficePerson, person));

        // applications for signed-in user
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(ssaAndOfficePerson)))
            .thenReturn(List.of(applicationOfSsaAndOffice, applicationCancellationRequest));

        // other as office
        when(applicationService.getForStates(List.of(WAITING, TEMPORARY_ALLOWED)))
            .thenReturn(List.of(application, applicationNotMember));

        perform(get("/web/application")).andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(ssaAndOfficePerson)))
            .andExpect(model().attribute("otherApplications", hasSize(2)))
            .andExpect(model().attribute("otherApplications", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(1L)),
                    hasProperty("approveAllowed", equalTo(true)),
                    hasProperty("rejectAllowed", equalTo(true))
                ),
                allOf(
                    instanceOf(ApplicationForLeaveDto.class),
                    hasProperty("id", is(11L)),
                    hasProperty("approveAllowed", equalTo(false)),
                    hasProperty("rejectAllowed", equalTo(false))
                )
            )))
            .andExpect(view().name("application/application-overview"));
    }

    @ValueSource(strings = {"/web/application", "/web/application/replacement"})
    @ParameterizedTest
    void ensureReplacementItem(String path) throws Exception {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("");

        final Person signedInUser = new Person();
        signedInUser.setId(1337L);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");
        signedInUser.setLastName("Wayne");

        final Person applicationPerson = new Person();
        applicationPerson.setId(1L);
        applicationPerson.setFirstName("Alfred");
        applicationPerson.setLastName("Pennyworth");
        applicationPerson.setPermissions(List.of(USER));

        final HolidayReplacementEntity holidayReplacement = new HolidayReplacementEntity();
        holidayReplacement.setPerson(signedInUser);
        holidayReplacement.setNote("awesome, thanks dude!");

        final Application application = new Application();
        application.setId(3L);
        application.setPerson(applicationPerson);
        application.setStartDate(LocalDate.now(clock).plusDays(1));
        application.setEndDate(LocalDate.now(clock).plusDays(1));
        application.setHolidayReplacements(List.of(holidayReplacement));

        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(applicationService.getForHolidayReplacement(signedInUser, LocalDate.now(clock)))
            .thenReturn(List.of(application));

        perform(get(path))
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
            .andExpect(view().name("application/application-overview"));
    }

    @Test
    void ensureSecondStageAuthorityViewsAllowButton() throws Exception {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("");

        final Person departmentHeadAndSecondStageAuth = new Person();
        departmentHeadAndSecondStageAuth.setId(1L);
        departmentHeadAndSecondStageAuth.setFirstName("departmentHeadAndSecondStageAuth");
        departmentHeadAndSecondStageAuth.setPermissions(List.of(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));
        when(personService.getSignedInUser()).thenReturn(departmentHeadAndSecondStageAuth);

        final Person userOfDepartmentA = new Person();
        userOfDepartmentA.setId(2L);
        userOfDepartmentA.setFirstName("userOfDepartmentA");
        userOfDepartmentA.setPermissions(List.of(USER));

        final Application applicationOfUserA = new Application();
        applicationOfUserA.setId(1L);
        applicationOfUserA.setVacationType(anyVacationType());
        applicationOfUserA.setPerson(userOfDepartmentA);
        applicationOfUserA.setStatus(WAITING);
        applicationOfUserA.setStartDate(LocalDate.MAX);
        applicationOfUserA.setEndDate(LocalDate.MAX);
        applicationOfUserA.setTwoStageApproval(true);
        when(departmentService.getMembersForDepartmentHead(departmentHeadAndSecondStageAuth)).thenReturn(List.of(userOfDepartmentA));

        final List<Person> membersDepartment = List.of(userOfDepartmentA);
        when(departmentService.getMembersForSecondStageAuthority(departmentHeadAndSecondStageAuth)).thenReturn(membersDepartment);

        // #getApplicationsForLeaveForSecondStageAuthority
        when(applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(departmentHeadAndSecondStageAuth)))
            .thenReturn(List.of());

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
            .andExpect(view().name("application/application-overview"));
    }

    @ParameterizedTest
    @EnumSource(value = ApplicationStatus.class, names = {"WAITING", "TEMPORARY_ALLOWED"})
    void ensureReplacementItemIsPendingForApplicationStatus(ApplicationStatus status) throws Exception {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("");

        final Person signedInUser = new Person();
        signedInUser.setId(1337L);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");

        final Person applicationPerson = new Person();
        applicationPerson.setId(1L);
        applicationPerson.setFirstName("Alfred");
        applicationPerson.setPermissions(List.of(USER));

        final HolidayReplacementEntity holidayReplacement = new HolidayReplacementEntity();
        holidayReplacement.setPerson(signedInUser);

        final Application application = new Application();
        application.setId(3L);
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
            .andExpect(view().name("application/application-overview"));
    }

    @ParameterizedTest
    @EnumSource(value = ApplicationStatus.class, names = {"ALLOWED", "ALLOWED_CANCELLATION_REQUESTED", "REJECTED", "CANCELLED", "REVOKED"})
    void ensureReplacementItemIsNotPendingForApplicationStatus(ApplicationStatus status) throws Exception {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("");

        final Person signedInUser = new Person();
        signedInUser.setId(1337L);
        signedInUser.setPermissions(List.of(USER));
        signedInUser.setFirstName("Bruce");
        signedInUser.setLastName("Wayne");

        final Person applicationPerson = new Person();
        applicationPerson.setId(1L);
        applicationPerson.setFirstName("Alfred");
        applicationPerson.setLastName("Pennyworth");
        applicationPerson.setPermissions(List.of(USER));

        final HolidayReplacementEntity holidayReplacement = new HolidayReplacementEntity();
        holidayReplacement.setPerson(signedInUser);
        holidayReplacement.setNote("awesome, thanks dude!");

        final Application application = new Application();
        application.setId(3L);
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
            .andExpect(view().name("application/application-overview"));
    }

    @ValueSource(strings = {"/web/application", "/web/sicknote/submitted"})
    @ParameterizedTest
    void getSubmittedSickNotesForOffice(String path) throws Exception {

        final Person person = new Person();
        person.setFirstName("Hans");
        person.setLastName("Dampf");
        final LocalDate startDate = LocalDate.of(2024, 1, 4);
        final LocalDate endDate = LocalDate.of(2024, 1, 5);
        final Map<LocalDate, WorkingTimeCalendar.WorkingDayInformation> workingDays = Map.of(
            startDate, new WorkingTimeCalendar.WorkingDayInformation(FULL, WORKDAY, WORKDAY),
            endDate, new WorkingTimeCalendar.WorkingDayInformation(FULL, WORKDAY, WORKDAY)
        );
        final SubmittedSickNote submittedSickNote = new SubmittedSickNote(
            SickNote.builder()
                .id(1L)
                .sickNoteType(anySickNoteType())
                .person(person)
                .status(SUBMITTED)
                .startDate(startDate)
                .endDate(endDate)
                .dayLength(FULL)
                .workingTimeCalendar(new WorkingTimeCalendar(workingDays))
                .build()
        );

        final Person officePerson = new Person();
        officePerson.setPermissions(List.of(OFFICE));

        when(personService.getSignedInUser()).thenReturn(officePerson);
        when(personService.getActivePersons()).thenReturn(List.of(person));

        // other sicknotes
        userIsAllowedToSubmitSickNotes(true);
        when(submittedSickNoteService.findSubmittedSickNotes(List.of(person))).thenReturn(List.of(submittedSickNote));

        perform(get(path)).andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(officePerson)))
            .andExpect(model().attribute("canAccessSickNoteSubmissions", is(true)))
            .andExpect(model().attribute("otherSickNotes", hasSize(1)))
            .andExpect(model().attribute("otherSickNotes", hasItems(
                allOf(
                    instanceOf(SubmittedSickNoteDto.class),
                    hasProperty("id", equalTo("1")),
                    hasProperty("workDays", equalTo(BigDecimal.valueOf(2L))),
                    hasProperty("person",
                        hasProperty("name", equalTo("Hans Dampf"))
                    ),
                    hasProperty("type", equalTo("sickNoteTypeMessageKey")),
                    hasProperty("durationOfAbsenceDescription")
                )
            )))
            .andExpect(view().name("application/application-overview"));
    }

    @Test
    void getSubmittedSickNotesForDepartmentHeadWithSickNoteEdit() throws Exception {

        final Person person = new Person();
        person.setFirstName("Hans");
        person.setLastName("Dampf");
        final LocalDate startDate = LocalDate.of(2024, 1, 4);
        final LocalDate endDate = LocalDate.of(2024, 1, 5);
        final Map<LocalDate, WorkingTimeCalendar.WorkingDayInformation> workingDays = Map.of(
            startDate, new WorkingTimeCalendar.WorkingDayInformation(FULL, WORKDAY, WORKDAY),
            endDate, new WorkingTimeCalendar.WorkingDayInformation(FULL, WORKDAY, WORKDAY)
        );
        final SubmittedSickNote submittedSickNote = new SubmittedSickNote(
            SickNote.builder()
                .id(1L)
                .sickNoteType(anySickNoteType())
                .person(person)
                .status(SUBMITTED)
                .startDate(startDate)
                .endDate(endDate)
                .dayLength(FULL)
                .workingTimeCalendar(new WorkingTimeCalendar(workingDays))
                .build()
        );

        final Person departmentHead = new Person();
        departmentHead.setPermissions(List.of(DEPARTMENT_HEAD, SICK_NOTE_EDIT));

        when(personService.getSignedInUser()).thenReturn(departmentHead);
        when(departmentService.getMembersForDepartmentHead(departmentHead)).thenReturn(List.of(person));

        // other sicknotes
        userIsAllowedToSubmitSickNotes(true);
        when(submittedSickNoteService.findSubmittedSickNotes(List.of(person))).thenReturn(List.of(submittedSickNote));

        perform(get("/web/application")).andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(departmentHead)))
            .andExpect(model().attribute("canAccessSickNoteSubmissions", is(true)))
            .andExpect(model().attribute("otherSickNotes", hasSize(1)))
            .andExpect(model().attribute("otherSickNotes", hasItems(
                allOf(
                    instanceOf(SubmittedSickNoteDto.class),
                    hasProperty("id", equalTo("1")),
                    hasProperty("workDays", equalTo(BigDecimal.valueOf(2L))),
                    hasProperty("person",
                        hasProperty("name", equalTo("Hans Dampf"))
                    ),
                    hasProperty("type", equalTo("sickNoteTypeMessageKey")),
                    hasProperty("durationOfAbsenceDescription")
                )
            )))
            .andExpect(view().name("application/application-overview"));
    }

    @Test
    void getSubmittedSickNotesForSecondStageAuthorityWithSickNoteEdit() throws Exception {

        final Person person = new Person();
        person.setFirstName("Hans");
        person.setLastName("Dampf");
        final LocalDate startDate = LocalDate.of(2024, 1, 4);
        final LocalDate endDate = LocalDate.of(2024, 1, 5);
        final Map<LocalDate, WorkingTimeCalendar.WorkingDayInformation> workingDays = Map.of(
            startDate, new WorkingTimeCalendar.WorkingDayInformation(FULL, WORKDAY, WORKDAY),
            endDate, new WorkingTimeCalendar.WorkingDayInformation(FULL, WORKDAY, WORKDAY)
        );
        final SubmittedSickNote submittedSickNote = new SubmittedSickNote(
            SickNote.builder()
                .id(1L)
                .sickNoteType(anySickNoteType())
                .person(person)
                .status(SUBMITTED)
                .startDate(startDate)
                .endDate(endDate)
                .dayLength(FULL)
                .workingTimeCalendar(new WorkingTimeCalendar(workingDays))
                .build()
        );

        final Person secondStageAuthority = new Person();
        secondStageAuthority.setPermissions(List.of(SECOND_STAGE_AUTHORITY, SICK_NOTE_EDIT));

        when(personService.getSignedInUser()).thenReturn(secondStageAuthority);
        when(departmentService.getMembersForSecondStageAuthority(secondStageAuthority)).thenReturn(List.of(person));

        // other sicknotes
        userIsAllowedToSubmitSickNotes(true);
        when(submittedSickNoteService.findSubmittedSickNotes(List.of(person))).thenReturn(List.of(submittedSickNote));

        perform(get("/web/application")).andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(secondStageAuthority)))
            .andExpect(model().attribute("canAccessSickNoteSubmissions", is(true)))
            .andExpect(model().attribute("otherSickNotes", hasSize(1)))
            .andExpect(model().attribute("otherSickNotes", hasItems(
                allOf(
                    instanceOf(SubmittedSickNoteDto.class),
                    hasProperty("id", equalTo("1")),
                    hasProperty("workDays", equalTo(BigDecimal.valueOf(2L))),
                    hasProperty("person",
                        hasProperty("name", equalTo("Hans Dampf"))
                    ),
                    hasProperty("type", equalTo("sickNoteTypeMessageKey")),
                    hasProperty("durationOfAbsenceDescription")
                )
            )))
            .andExpect(view().name("application/application-overview"));
    }

    @Test
    void getSubmittedSickNotesForBossWithSickNoteEdit() throws Exception {

        final Person person = new Person();
        person.setFirstName("Hans");
        person.setLastName("Dampf");
        final LocalDate startDate = LocalDate.of(2024, 1, 4);
        final LocalDate endDate = LocalDate.of(2024, 1, 5);
        final Map<LocalDate, WorkingTimeCalendar.WorkingDayInformation> workingDays = Map.of(
            startDate, new WorkingTimeCalendar.WorkingDayInformation(FULL, WORKDAY, WORKDAY),
            endDate, new WorkingTimeCalendar.WorkingDayInformation(FULL, WORKDAY, WORKDAY)
        );
        final SubmittedSickNote submittedSickNote = new SubmittedSickNote(
            SickNote.builder()
                .id(1L)
                .sickNoteType(anySickNoteType())
                .person(person)
                .status(SUBMITTED)
                .startDate(startDate)
                .endDate(endDate)
                .dayLength(FULL)
                .workingTimeCalendar(new WorkingTimeCalendar(workingDays))
                .build()
        );

        final Person boss = new Person();
        boss.setPermissions(List.of(BOSS, SICK_NOTE_EDIT));

        when(personService.getSignedInUser()).thenReturn(boss);
        when(personService.getActivePersons()).thenReturn(List.of(person, boss));

        // other sicknotes
        userIsAllowedToSubmitSickNotes(true);
        when(submittedSickNoteService.findSubmittedSickNotes(List.of(person))).thenReturn(List.of(submittedSickNote));

        perform(get("/web/application")).andExpect(status().isOk())
            .andExpect(model().attribute("signedInUser", is(boss)))
            .andExpect(model().attribute("canAccessSickNoteSubmissions", is(true)))
            .andExpect(model().attribute("otherSickNotes", hasSize(1)))
            .andExpect(model().attribute("otherSickNotes", hasItems(
                allOf(
                    instanceOf(SubmittedSickNoteDto.class),
                    hasProperty("id", equalTo("1")),
                    hasProperty("workDays", equalTo(BigDecimal.valueOf(2L))),
                    hasProperty("person",
                        hasProperty("name", equalTo("Hans Dampf"))
                    ),
                    hasProperty("type", equalTo("sickNoteTypeMessageKey"))
                )
            )))
            .andExpect(view().name("application/application-overview"));
    }

    private static SickNoteType anySickNoteType() {
        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SICK_NOTE);
        sickNoteType.setMessageKey("sickNoteTypeMessageKey");
        return sickNoteType;
    }

    private VacationType<?> anyVacationType() {
        return ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(VacationCategory.HOLIDAY)
            .messageKey("vacationTypeMessageKey")
            .build();
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }

    private void userIsAllowedToSubmitSickNotes(boolean userIsAllowedToSubmit) {
        final Settings settings = new Settings();
        final SickNoteSettings sickNoteSettings = new SickNoteSettings();
        sickNoteSettings.setUserIsAllowedToSubmitSickNotes(userIsAllowedToSubmit);
        settings.setSickNoteSettings(sickNoteSettings);
        when(settingsService.getSettings()).thenReturn(settings);
    }
}
