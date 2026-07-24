package org.synyx.urlaubsverwaltung.application.me;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.application.HolidayReplacementEntity;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeDto;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeViewModelService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.search.SearchContext;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.math.BigDecimal.ONE;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.me.ApplicationsViewController.MY_APPLICATIONS_ANONYMOUS_PATH;
import static org.synyx.urlaubsverwaltung.application.me.ApplicationsViewController.MY_APPLICATIONS_PATH;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.ORANGE;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_CANCEL;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_EDIT;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class ApplicationsViewControllerTest {

    private ApplicationsViewController sut;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private WorkDaysCountService workDaysCountService;
    @Mock
    private VacationTypeViewModelService vacationTypeViewModelService;
    @Mock
    private PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier;

    private final Clock clock = Clock.fixed(
        ZonedDateTime.of(LocalDate.of(2022, 6, 15).atStartOfDay(), ZoneId.systemDefault()).toInstant(),
        ZoneId.systemDefault()
    );

    @BeforeEach
    void setUp() {
        sut = new ApplicationsViewController(
            personService, departmentService, applicationService,
            workDaysCountService, vacationTypeViewModelService, personSearchUiFragmentSupplier, clock
        );
    }

    private void stubWorkDaysCountForApplications() {
        when(workDaysCountService.getWorkDaysCountByYearForApplications(anyCollection()))
            .thenAnswer(this::oneDayPerApplicationByYear);
    }

    private void stubWorkDaysCountForApplicationsWithUsedDaysSummary() {
        stubWorkDaysCountForApplications();
        when(workDaysCountService.getWorkDaysCountForApplications(anyCollection(), any(DateRange.class)))
            .thenAnswer(this::oneDayPerApplication);
    }

    private Map<Application, BigDecimal> oneDayPerApplication(InvocationOnMock invocation) {
        final Collection<Application> applications = invocation.getArgument(0);
        return applications.stream().collect(toMap(identity(), application -> ONE));
    }

    private Map<Application, SortedMap<Integer, BigDecimal>> oneDayPerApplicationByYear(InvocationOnMock invocation) {
        final Collection<Application> applications = invocation.getArgument(0);
        return applications.stream().collect(toMap(identity(), application -> {
            final SortedMap<Integer, BigDecimal> workDaysByYear = new TreeMap<>();
            workDaysByYear.put(application.getStartDate().getYear(), ONE);
            return workDaysByYear;
        }));
    }

    @Test
    void ensurePersonSearchSuggestionUrlStrategy() {

        final Person person = new Person();
        person.setId(1L);

        final PersonSuggestionUrlStrategy strategy = sut.personSuggestionUrlStrategy();

        assertThat(strategy.buildSuggestionMainLink(person, searchContext())).isEqualTo("/web/persons/1/applications");
    }

    @Test
    void ensurePersonSearchSuggestionUrlStrategyWithYear() {

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("year", "2026");

        final Person person = new Person();
        person.setId(1L);

        final PersonSuggestionUrlStrategy strategy = sut.personSuggestionUrlStrategy();

        assertThat(strategy.buildSuggestionMainLink(person, searchContext(request))).isEqualTo("/web/persons/1/applications?year=2026");
    }

    @Test
    void showMyApplicationsAnonymousRedirectsToPersonApplicationsWithoutYear() throws Exception {
        final Person signedIn = new Person();
        signedIn.setId(5L);
        when(personService.getSignedInUser()).thenReturn(signedIn);

        perform(get(MY_APPLICATIONS_ANONYMOUS_PATH))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/persons/5/applications"));
    }

    @Test
    void showMyApplicationsAnonymousRedirectsToPersonApplicationsWithGivenYear() throws Exception {
        final Person signedIn = new Person();
        signedIn.setId(7L);
        when(personService.getSignedInUser()).thenReturn(signedIn);

        perform(get(MY_APPLICATIONS_ANONYMOUS_PATH).param("year", "2021"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/persons/7/applications?year=2021"));
    }

    @Test
    void showMyApplicationsForPersonShowsViewAndModelAttributesWhenNoApplications() throws Exception {
        final Person person = new Person();
        person.setId(13L);

        when(personService.getPersonByID(13L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));
        when(applicationService.getApplicationsForACertainPeriodAndPerson(
            any(LocalDate.class), any(LocalDate.class), eq(person)
        )).thenReturn(List.of());

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "13")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/applications"))
            .andExpect(model().attribute("person", equalTo(person)))
            .andExpect(model().attribute("departmentsOfPerson", equalTo(List.of())))
            .andExpect(model().attribute("vacationTypeColors", hasSize(1)))
            .andExpect(model().attribute("applications", hasSize(0)));
    }

    @Test
    void showMyApplicationsForPersonWithApplicationsMapsToDto() throws Exception {
        final Person person = new Person();
        person.setId(21L);

        when(personService.getPersonByID(21L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));

        final Application application = new Application();
        application.setId(99L);
        application.setPerson(person);
        application.setStartDate(LocalDate.of(2022, 1, 10));
        application.setEndDate(LocalDate.of(2022, 1, 12));

        final VacationType<?> vacationType = mock(VacationType.class);
        when(vacationType.getLabel(any(Locale.class))).thenReturn("label");
        when(vacationType.getCategory()).thenReturn(HOLIDAY);
        when(vacationType.getColor()).thenReturn(ORANGE);
        when(vacationType.isRequiresApprovalToCancel()).thenReturn(false);
        application.setVacationType(vacationType);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(
            any(LocalDate.class), any(LocalDate.class), eq(person)
        )).thenReturn(List.of(application));
        stubWorkDaysCountForApplications();

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "21")).locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(view().name("me/applications"))
            .andExpect(model().attribute("applications", hasSize(1)))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("id", is(99L)))));
    }

    @Test
    void ensureAccessIsDeniedWhenSignedInUserIsNotAllowedToAccessPersonData() {
        final Person person = new Person();
        person.setId(1L);

        final Person signedInUser = new Person();
        signedInUser.setId(2L);
        signedInUser.setPermissions(List.of(USER));

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)).thenReturn(false);

        assertThatThrownBy(() -> perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "1"))))
            .hasCauseInstanceOf(AccessDeniedException.class);
    }

    // ---- ALLOWED TO EDIT TESTS ----

    @Test
    void ensureAllowedToEditIfPersonOwnWaitingApplication() throws Exception {
        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of());

        final Application application = createApplication(1L, person, WAITING, false);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(
            any(LocalDate.class), any(LocalDate.class), eq(person)
        )).thenReturn(List.of(application));
        stubWorkDaysCountForApplicationsWithUsedDaysSummary();

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "1")).locale(Locale.GERMANY))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("allowedToEdit", is(true)))));
    }

    @Test
    void ensureNotAllowedToEditIfPersonOwnAllowedApplication() throws Exception {
        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of());

        final Application application = createApplication(1L, person, ALLOWED, false);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(
            any(LocalDate.class), any(LocalDate.class), eq(person)
        )).thenReturn(List.of(application));
        stubWorkDaysCountForApplicationsWithUsedDaysSummary();

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "1")).locale(Locale.GERMANY))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("allowedToEdit", is(false)))));
    }

    @Test
    void ensureAllowedToEditIfOffice() throws Exception {
        final Person applicationPerson = new Person();
        applicationPerson.setId(1L);

        final Person officeUser = new Person();
        officeUser.setId(2L);
        officeUser.setPermissions(List.of(USER, OFFICE));

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(applicationPerson));
        when(personService.getSignedInUser()).thenReturn(officeUser);
        when(departmentService.isSignedInUserAllowedToAccessPersonData(officeUser, applicationPerson)).thenReturn(true);
        when(departmentService.getAssignedDepartmentsOfMember(applicationPerson)).thenReturn(List.of());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of());

        final Application application = createApplication(1L, applicationPerson, ALLOWED, false);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(
            any(LocalDate.class), any(LocalDate.class), eq(applicationPerson)
        )).thenReturn(List.of(application));
        stubWorkDaysCountForApplicationsWithUsedDaysSummary();

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "1")).locale(Locale.GERMANY))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("allowedToEdit", is(true)))));
    }

    @Test
    void ensureAllowedToEditIfDepartmentHeadWithApplicationEditRole() throws Exception {
        final Person applicationPerson = new Person();
        applicationPerson.setId(1L);

        final Person deptHead = new Person();
        deptHead.setId(2L);
        deptHead.setPermissions(List.of(USER, DEPARTMENT_HEAD, APPLICATION_EDIT));

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(applicationPerson));
        when(personService.getSignedInUser()).thenReturn(deptHead);
        when(departmentService.getAssignedDepartmentsOfMember(applicationPerson)).thenReturn(List.of());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of());
        when(departmentService.isDepartmentHeadAllowedToManagePerson(deptHead, applicationPerson)).thenReturn(true);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(deptHead, applicationPerson)).thenReturn(false);
        when(departmentService.isSignedInUserAllowedToAccessPersonData(deptHead, applicationPerson)).thenReturn(true);

        final Application application = createApplication(1L, applicationPerson, WAITING, false);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(
            any(LocalDate.class), any(LocalDate.class), eq(applicationPerson)
        )).thenReturn(List.of(application));
        stubWorkDaysCountForApplicationsWithUsedDaysSummary();

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "1")).locale(Locale.GERMANY))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("allowedToEdit", is(true)))));
    }

    @Test
    void ensureNotAllowedToEditIfDepartmentHeadWithoutApplicationEditRole() throws Exception {
        final Person applicationPerson = new Person();
        applicationPerson.setId(1L);

        final Person deptHead = new Person();
        deptHead.setId(2L);
        deptHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(applicationPerson));
        when(personService.getSignedInUser()).thenReturn(deptHead);
        when(departmentService.getAssignedDepartmentsOfMember(applicationPerson)).thenReturn(List.of());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of());
        when(departmentService.isSignedInUserAllowedToAccessPersonData(deptHead, applicationPerson)).thenReturn(true);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(deptHead, applicationPerson)).thenReturn(true);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(deptHead, applicationPerson)).thenReturn(false);

        final Application application = createApplication(1L, applicationPerson, WAITING, false);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(
            any(LocalDate.class), any(LocalDate.class), eq(applicationPerson)
        )).thenReturn(List.of(application));
        stubWorkDaysCountForApplicationsWithUsedDaysSummary();

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "1")).locale(Locale.GERMANY))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("allowedToEdit", is(false)))));
    }

    @Test
    void ensureAllowedToEditIfSecondStageAuthorityWithApplicationEditRole() throws Exception {
        final Person applicationPerson = new Person();
        applicationPerson.setId(1L);

        final Person ssa = new Person();
        ssa.setId(2L);
        ssa.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY, APPLICATION_EDIT));

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(applicationPerson));
        when(personService.getSignedInUser()).thenReturn(ssa);
        when(departmentService.getAssignedDepartmentsOfMember(applicationPerson)).thenReturn(List.of());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of());
        when(departmentService.isSignedInUserAllowedToAccessPersonData(ssa, applicationPerson)).thenReturn(true);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(ssa, applicationPerson)).thenReturn(false);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(ssa, applicationPerson)).thenReturn(true);

        final Application application = createApplication(1L, applicationPerson, WAITING, false);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(
            any(LocalDate.class), any(LocalDate.class), eq(applicationPerson)
        )).thenReturn(List.of(application));
        stubWorkDaysCountForApplicationsWithUsedDaysSummary();

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "1")).locale(Locale.GERMANY))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("allowedToEdit", is(true)))));
    }

    // ---- ALLOWED TO REVOKE TESTS ----

    @Test
    void ensureAllowedToRevokeIfPersonOwnWaitingApplicationWithApprovalToCancel() throws Exception {
        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of());

        final Application application = createApplication(1L, person, WAITING, true);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(
            any(LocalDate.class), any(LocalDate.class), eq(person)
        )).thenReturn(List.of(application));
        stubWorkDaysCountForApplicationsWithUsedDaysSummary();

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "1")).locale(Locale.GERMANY))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("allowedToRevoke", is(true)))));
    }

    @Test
    void ensureNotAllowedToRevokeIfWaitingApplicationWithoutApprovalToCancel() throws Exception {
        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of());

        final Application application = createApplication(1L, person, WAITING, false);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(
            any(LocalDate.class), any(LocalDate.class), eq(person)
        )).thenReturn(List.of(application));
        stubWorkDaysCountForApplicationsWithUsedDaysSummary();

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "1")).locale(Locale.GERMANY))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("allowedToRevoke", is(false)))));
    }

    @Test
    void ensureNotAllowedToRevokeIfAllowedApplication() throws Exception {
        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of());

        final Application application = createApplication(1L, person, ALLOWED, true);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(
            any(LocalDate.class), any(LocalDate.class), eq(person)
        )).thenReturn(List.of(application));
        stubWorkDaysCountForApplicationsWithUsedDaysSummary();

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "1")).locale(Locale.GERMANY))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("allowedToRevoke", is(false)))));
    }

    @Test
    void ensureAllowedToRevokeIfOfficeAndWaitingApplicationWithApprovalToCancel() throws Exception {
        final Person applicationPerson = new Person();
        applicationPerson.setId(1L);

        final Person officeUser = new Person();
        officeUser.setId(2L);
        officeUser.setPermissions(List.of(USER, OFFICE));

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(applicationPerson));
        when(personService.getSignedInUser()).thenReturn(officeUser);
        when(departmentService.isSignedInUserAllowedToAccessPersonData(officeUser, applicationPerson)).thenReturn(true);
        when(departmentService.getAssignedDepartmentsOfMember(applicationPerson)).thenReturn(List.of());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of());

        final Application application = createApplication(1L, applicationPerson, WAITING, true);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(
            any(LocalDate.class), any(LocalDate.class), eq(applicationPerson)
        )).thenReturn(List.of(application));
        stubWorkDaysCountForApplicationsWithUsedDaysSummary();

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "1")).locale(Locale.GERMANY))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("allowedToRevoke", is(true)))));
    }

    // ---- ALLOWED TO CANCEL TESTS ----

    @Test
    void ensureAllowedToCancelIfBossWithApplicationCancelRole() throws Exception {
        final Person applicationPerson = new Person();
        applicationPerson.setId(1L);

        final Person boss = new Person();
        boss.setId(2L);
        boss.setPermissions(List.of(USER, BOSS, APPLICATION_CANCEL));

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(applicationPerson));
        when(personService.getSignedInUser()).thenReturn(boss);
        when(departmentService.isSignedInUserAllowedToAccessPersonData(boss, applicationPerson)).thenReturn(true);
        when(departmentService.getAssignedDepartmentsOfMember(applicationPerson)).thenReturn(List.of());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of());
        when(departmentService.isDepartmentHeadAllowedToManagePerson(boss, applicationPerson)).thenReturn(false);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(boss, applicationPerson)).thenReturn(false);

        final Application application = createApplication(1L, applicationPerson, ALLOWED, false);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(
            any(LocalDate.class), any(LocalDate.class), eq(applicationPerson)
        )).thenReturn(List.of(application));
        stubWorkDaysCountForApplicationsWithUsedDaysSummary();

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "1")).locale(Locale.GERMANY))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("allowedToCancel", is(true)))));
    }

    @Test
    void ensureNotAllowedToCancelIfBossWithoutApplicationCancelRole() throws Exception {
        final Person applicationPerson = new Person();
        applicationPerson.setId(1L);

        final Person boss = new Person();
        boss.setId(2L);
        boss.setPermissions(List.of(USER, BOSS));

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(applicationPerson));
        when(personService.getSignedInUser()).thenReturn(boss);
        when(departmentService.getAssignedDepartmentsOfMember(applicationPerson)).thenReturn(List.of());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of());
        when(departmentService.isSignedInUserAllowedToAccessPersonData(boss, applicationPerson)).thenReturn(true);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(boss, applicationPerson)).thenReturn(false);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(boss, applicationPerson)).thenReturn(false);

        final Application application = createApplication(1L, applicationPerson, ALLOWED, false);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(
            any(LocalDate.class), any(LocalDate.class), eq(applicationPerson)
        )).thenReturn(List.of(application));
        stubWorkDaysCountForApplicationsWithUsedDaysSummary();

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "1")).locale(Locale.GERMANY))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("allowedToCancel", is(false)))));
    }

    @Test
    void ensureAllowedToCancelIfOffice() throws Exception {
        final Person applicationPerson = new Person();
        applicationPerson.setId(1L);

        final Person officeUser = new Person();
        officeUser.setId(2L);
        officeUser.setPermissions(List.of(USER, OFFICE));

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(applicationPerson));
        when(personService.getSignedInUser()).thenReturn(officeUser);
        when(departmentService.isSignedInUserAllowedToAccessPersonData(officeUser, applicationPerson)).thenReturn(true);
        when(departmentService.getAssignedDepartmentsOfMember(applicationPerson)).thenReturn(List.of());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of());
        when(departmentService.isDepartmentHeadAllowedToManagePerson(officeUser, applicationPerson)).thenReturn(false);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(officeUser, applicationPerson)).thenReturn(false);

        final Application application = createApplication(1L, applicationPerson, ALLOWED, false);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(
            any(LocalDate.class), any(LocalDate.class), eq(applicationPerson)
        )).thenReturn(List.of(application));
        stubWorkDaysCountForApplicationsWithUsedDaysSummary();

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "1")).locale(Locale.GERMANY))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("allowedToCancel", is(true)))));
    }

    @Test
    void ensureNotAllowedToCancelIfRegularUser() throws Exception {
        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of());

        final Application application = createApplication(1L, person, ALLOWED, false);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(
            any(LocalDate.class), any(LocalDate.class), eq(person)
        )).thenReturn(List.of(application));
        stubWorkDaysCountForApplicationsWithUsedDaysSummary();

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "1")).locale(Locale.GERMANY))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("allowedToCancel", is(false)))));
    }

    // ---- ALLOWED TO CANCEL DIRECTLY TESTS ----

    @Test
    void ensureAllowedToCancelDirectlyIfPersonOwnAllowedApplicationWithoutApprovalToCancel() throws Exception {
        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of());

        final Application application = createApplication(1L, person, ALLOWED, false);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(
            any(LocalDate.class), any(LocalDate.class), eq(person)
        )).thenReturn(List.of(application));
        stubWorkDaysCountForApplicationsWithUsedDaysSummary();

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "1")).locale(Locale.GERMANY))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("allowedToCancelDirectly", is(true)))));
    }

    @Test
    void ensureNotAllowedToCancelDirectlyIfPersonOwnAllowedApplicationWithApprovalToCancel() throws Exception {
        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of());

        final Application application = createApplication(1L, person, ALLOWED, true);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(
            any(LocalDate.class), any(LocalDate.class), eq(person)
        )).thenReturn(List.of(application));
        stubWorkDaysCountForApplicationsWithUsedDaysSummary();

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "1")).locale(Locale.GERMANY))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("allowedToCancelDirectly", is(false)))));
    }

    // ---- ALLOWED TO START CANCELLATION REQUEST TESTS ----

    @Test
    void ensureAllowedToStartCancellationRequestIfPersonOwnAllowedApplicationWithApprovalToCancel() throws Exception {
        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of());

        final Application application = createApplication(1L, person, ALLOWED, true);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(
            any(LocalDate.class), any(LocalDate.class), eq(person)
        )).thenReturn(List.of(application));
        stubWorkDaysCountForApplicationsWithUsedDaysSummary();

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "1")).locale(Locale.GERMANY))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("allowedToStartCancellationRequest", is(true)))));
    }

    @Test
    void ensureNotAllowedToStartCancellationRequestIfPersonOwnAllowedApplicationWithoutApprovalToCancel() throws Exception {
        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of());

        final Application application = createApplication(1L, person, ALLOWED, false);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(
            any(LocalDate.class), any(LocalDate.class), eq(person)
        )).thenReturn(List.of(application));
        stubWorkDaysCountForApplicationsWithUsedDaysSummary();

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "1")).locale(Locale.GERMANY))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("allowedToStartCancellationRequest", is(false)))));
    }

    // ---- HOLIDAY REPLACEMENTS TESTS ----

    @Test
    void ensureHolidayReplacementsAreMappedToDto() throws Exception {
        final Person person = new Person();
        person.setId(1L);

        final Person replacement = new Person();
        replacement.setId(100L);
        replacement.setFirstName("Max");
        replacement.setLastName("Mustermann");
        replacement.setEmail("max@example.com");
        replacement.setGravatarEnabled(true);

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of());

        final Application application = createApplication(1L, person, ALLOWED, false);

        final HolidayReplacementEntity holidayReplacement = new HolidayReplacementEntity();
        holidayReplacement.setPerson(replacement);
        application.setHolidayReplacements(List.of(holidayReplacement));

        when(applicationService.getApplicationsForACertainPeriodAndPerson(
            any(LocalDate.class), any(LocalDate.class), eq(person)
        )).thenReturn(List.of(application));
        stubWorkDaysCountForApplicationsWithUsedDaysSummary();

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "1")).locale(Locale.GERMANY))
            .andExpect(model().attribute("applications", hasSize(1)))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("holidayReplacements", hasSize(1)))))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("holidayReplacements",
                    hasItem(hasProperty("niceName", is("Max Mustermann")))))))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("holidayReplacements",
                    hasItem(hasProperty("initials", is("MM")))))))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("holidayReplacements",
                    hasItem(hasProperty("gravatarUrl", is("https://gravatar.com/avatar/27f10ba42a2d1737f88318bdcfca0e62")))))));
    }

    @Test
    void ensureHolidayReplacementsAreEmptyWhenNotSet() throws Exception {
        final Person person = new Person();
        person.setId(1L);

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of());

        final Application application = createApplication(1L, person, ALLOWED, false);
        // No holiday replacements set, defaults to empty list

        when(applicationService.getApplicationsForACertainPeriodAndPerson(
            any(LocalDate.class), any(LocalDate.class), eq(person)
        )).thenReturn(List.of(application));
        stubWorkDaysCountForApplicationsWithUsedDaysSummary();

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "1")).locale(Locale.GERMANY))
            .andExpect(model().attribute("applications", hasSize(1)))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("holidayReplacements", hasSize(0)))));
    }

    @Test
    void ensureMultipleHolidayReplacementsAreMappedToDto() throws Exception {
        final Person person = new Person();
        person.setId(1L);

        final Person replacement1 = new Person();
        replacement1.setId(100L);
        replacement1.setFirstName("Anna");
        replacement1.setLastName("Abc");
        replacement1.setEmail("anna@example.com");
        replacement1.setGravatarEnabled(true);

        final Person replacement2 = new Person();
        replacement2.setId(101L);
        replacement2.setFirstName("Bob");
        replacement2.setLastName("Bobson");
        replacement2.setEmail("bob@example.com");
        replacement2.setGravatarEnabled(true);

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of());

        final Application application = createApplication(1L, person, ALLOWED, false);

        final HolidayReplacementEntity holidayReplacement1 = new HolidayReplacementEntity();
        holidayReplacement1.setPerson(replacement1);

        final HolidayReplacementEntity holidayReplacement2 = new HolidayReplacementEntity();
        holidayReplacement2.setPerson(replacement2);

        application.setHolidayReplacements(List.of(holidayReplacement1, holidayReplacement2));

        when(applicationService.getApplicationsForACertainPeriodAndPerson(
            any(LocalDate.class), any(LocalDate.class), eq(person)
        )).thenReturn(List.of(application));
        stubWorkDaysCountForApplicationsWithUsedDaysSummary();

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "1")).locale(Locale.GERMANY))
            .andExpect(model().attribute("applications", hasSize(1)))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("holidayReplacements", hasSize(2)))))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("holidayReplacements",
                    hasItem(hasProperty("niceName", is("Anna Abc")))))))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("holidayReplacements",
                    hasItem(hasProperty("niceName", is("Bob Bobson")))))))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("holidayReplacements",
                    hasItem(hasProperty("initials", is("AA")))))))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("holidayReplacements",
                    hasItem(hasProperty("initials", is("BB")))))))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("holidayReplacements",
                    hasItem(hasProperty("gravatarUrl", is("https://gravatar.com/avatar/4b9bb80620f03eb3719e0a061c14283d")))))))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("holidayReplacements",
                    hasItem(hasProperty("gravatarUrl", is("https://gravatar.com/avatar/4b9bb80620f03eb3719e0a061c14283d")))))));
    }

    // ---- HELPER METHODS ----

    private Application createApplication(Long id, Person person, ApplicationStatus status, boolean requiresApprovalToCancel) {
        final Application application = new Application();
        application.setId(id);
        application.setPerson(person);
        application.setStatus(status);
        application.setStartDate(LocalDate.of(2022, 1, 10));
        application.setEndDate(LocalDate.of(2022, 1, 12));

        final VacationType<?> vacationType = mock(VacationType.class);
        when(vacationType.getLabel(any(Locale.class))).thenReturn("label");
        when(vacationType.getCategory()).thenReturn(HOLIDAY);
        when(vacationType.getColor()).thenReturn(ORANGE);
        when(vacationType.isRequiresApprovalToCancel()).thenReturn(requiresApprovalToCancel);
        application.setVacationType(vacationType);

        return application;
    }

    private static SearchContext searchContext() {
        return searchContext(new MockHttpServletRequest());
    }

    private static SearchContext searchContext(HttpServletRequest request) {
        return SearchContext.of(request, null);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
