package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettings;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.SEPTEMBER;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationTypeEntity;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeServiceImpl.convert;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class ApplicationForLeaveFormViewControllerTest {

    private ApplicationForLeaveFormViewController sut;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private AccountService accountService;
    @Mock
    private VacationTypeService vacationTypeService;
    @Mock
    private ApplicationInteractionService applicationInteractionService;
    @Mock
    private ApplicationForLeaveFormValidator applicationForLeaveFormValidator;
    @Mock
    private SettingsService settingsService;

    private final DateFormatAware dateFormatAware = new DateFormatAware();

    private static final int PERSON_ID = 1;
    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeaveFormViewController(personService, departmentService, accountService, vacationTypeService,
            applicationInteractionService, applicationForLeaveFormValidator, settingsService, dateFormatAware, clock);
    }

    @Test
    void overtimeIsActivated() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final int year = Year.now(clock).getValue();
        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(year, person)).thenReturn(Optional.of(account));

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key", true);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(List.of(vacationType));

        final OvertimeSettings overtimeSettings = new OvertimeSettings();
        overtimeSettings.setOvertimeActive(true);

        final Settings settings = new Settings();
        settings.setOvertimeSettings(overtimeSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        final ResultActions resultActions = perform(get("/web/application/new"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("overtimeActive", is(true)));
        resultActions.andExpect(model().attribute("vacationTypes", hasItems(vacationType)));
        resultActions.andExpect(model().attribute("showHalfDayOption", is(true)));
    }

    @Test
    void overtimeIsDeactivated() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final int year = Year.now(clock).getValue();
        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(year, person)).thenReturn(Optional.of(account));

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key", true);
        when(vacationTypeService.getActiveVacationTypesWithoutCategory(OVERTIME)).thenReturn(List.of(vacationType));

        final OvertimeSettings overtimeSettings = new OvertimeSettings();
        overtimeSettings.setOvertimeActive(false);

        final Settings settings = new Settings();
        settings.setOvertimeSettings(overtimeSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        final ResultActions resultActions = perform(get("/web/application/new"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("overtimeActive", is(false)));
        resultActions.andExpect(model().attribute("vacationTypes", hasItems(vacationType)));
        resultActions.andExpect(model().attribute("showHalfDayOption", is(true)));
    }

    @Test
    void halfdayIsDeactivated() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final int year = Year.now(clock).getValue();
        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(year, person)).thenReturn(Optional.of(account));

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key", true);
        when(vacationTypeService.getActiveVacationTypesWithoutCategory(OVERTIME)).thenReturn(List.of(vacationType));

        final OvertimeSettings overtimeSettings = new OvertimeSettings();
        overtimeSettings.setOvertimeActive(false);

        final Settings settings = new Settings();
        settings.setOvertimeSettings(overtimeSettings);
        final var appSettings = new ApplicationSettings();
        appSettings.setAllowHalfDays(false);
        settings.setApplicationSettings(appSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        final ResultActions resultActions = perform(get("/web/application/new"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("overtimeActive", is(false)));
        resultActions.andExpect(model().attribute("vacationTypes", hasItems(vacationType)));
        resultActions.andExpect(model().attribute("showHalfDayOption", is(false)));
    }

    @Test
    void getNewApplicationFormDefaultsToSignedInPersonIfPersonIdNotGiven() throws Exception {

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final Account account = new Account(signedInPerson, validFrom, validTo, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(anyInt(), eq(signedInPerson))).thenReturn(Optional.of(account));
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get("/web/application/new"))
            .andExpect(model().attribute("person", signedInPerson));
    }

    @Test
    void getNewApplicationFormUsesPersonOfGivenPersonId() throws Exception {

        final Person person = new Person();
        person.setId(1);

        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));

        final LocalDate validFrom = LocalDate.now(clock).withMonth(JANUARY.getValue()).withDayOfMonth(1);
        final LocalDate validTo = LocalDate.now(clock).withMonth(DECEMBER.getValue()).withDayOfMonth(31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(LocalDate.now(clock).getYear(), person)).thenReturn(Optional.of(account));
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get("/web/application/new")
            .param("person", "1"))
            .andExpect(model().attribute("person", person));
    }

    @Test
    void getNewApplicationFormOfficeUserCanAddForAnotherUser() throws Exception {

        final Person person = new Person();
        person.setId(1);

        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));

        final LocalDate validFrom = LocalDate.now(clock).withMonth(JANUARY.getValue()).withDayOfMonth(1);
        final LocalDate validTo = LocalDate.now(clock).withMonth(DECEMBER.getValue()).withDayOfMonth(31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(LocalDate.now(clock).getYear(), person)).thenReturn(Optional.of(account));
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get("/web/application/new")
            .param("person", "1"))
            .andExpect(model().attribute("canAddApplicationForLeaveForAnotherUser", true));
    }

    @Test
    void getNewApplicationFormForUnknownPersonIdFallsBackToSignedInUser() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setId(1337);

        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(personService.getPersonByID(1)).thenReturn(Optional.empty());

        final Account account = new Account(signedInUser, LocalDate.now(clock), LocalDate.now(clock), ZERO, ZERO, ZERO, "");
        when(accountService.getHolidaysAccount(LocalDate.now(clock).getYear(), signedInUser)).thenReturn(Optional.of(account));
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get("/web/application/new").param("person", "1"))
            .andExpect(model().attribute("person", hasProperty("id", is(1337))));
    }

    @Test
    void getNewApplicationFormThrowsAccessDeniedExceptionIfGivenPersonNotSignedInPersonAndNotOffice() {

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Person person = personWithId(PERSON_ID);
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(person));

        assertThatThrownBy(() ->
            perform(get("/web/application/new")
                .param("person", Integer.toString(PERSON_ID)))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getNewApplicationFormAccessibleIfGivenPersonIsSignedInPerson() throws Exception {

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(signedInPerson));

        perform(get("/web/application/new")
            .param("person", Integer.toString(PERSON_ID)))
            .andExpect(status().isOk());
    }

    @Test
    void getNewApplicationFormAccessibleForOfficeIfGivenPersonNotSignedInPerson() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(personWithId(PERSON_ID)));

        perform(get("/web/application/new")
            .param("person", Integer.toString(PERSON_ID)))
            .andExpect(status().isOk());
    }

    @Test
    void getNewApplicationFormWithGivenDateFrom() throws Exception {

        final Person person = new Person();
        person.setId(1);

        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));

        final LocalDate validFrom = LocalDate.now(clock).withMonth(JANUARY.getValue()).withDayOfMonth(1);
        final LocalDate validTo = LocalDate.now(clock).withMonth(DECEMBER.getValue()).withDayOfMonth(31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(LocalDate.now(clock).getYear(), person)).thenReturn(Optional.of(account));
        when(settingsService.getSettings()).thenReturn(new Settings());

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        final LocalDate givenDateFrom = LocalDate.now(clock).withMonth(SEPTEMBER.getValue()).withDayOfMonth(30);


        final ResultActions resultActions = perform(get("/web/application/new")
            .param("person", "1")
            .param("from", givenDateFrom.format(formatter)));

        resultActions
            .andExpect(model().attribute("person", person))
            .andExpect(model().attribute("showHalfDayOption", is(true)))
            .andExpect(model().attribute("application", allOf(
                hasProperty("startDate", is(givenDateFrom)),
                hasProperty("startDateIsoValue", is(givenDateFrom.format(formatter))),
                hasProperty("endDate", is(givenDateFrom)),
                hasProperty("endDateIsoValue", is(givenDateFrom.format(formatter)))
            )));
    }

    private static Stream<Arguments> datePeriodParams() {
        return Stream.of(
            Arguments.of("27.10.2020", "2020-10-27", "30.10.2020", "2020-10-30"),
            Arguments.of("2020-10-27", "2020-10-27", "2020-10-30", "2020-10-30")
        );
    }

    @ParameterizedTest
    @MethodSource("datePeriodParams")
    void getNewApplicationFormWithGivenDateFromAndDateTo(
        String givenFromString, String expectedFromString, String givenToString, String expectedToString) throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));

        final Person person = new Person();
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(person));

        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(anyInt(), eq(person))).thenReturn(Optional.of(account));
        when(settingsService.getSettings()).thenReturn(new Settings());

        final ResultActions resultActions = perform(get("/web/application/new")
            .param("person", Integer.toString(PERSON_ID))
            .param("from", givenFromString)
            .param("to", givenToString));

        resultActions
            .andExpect(model().attribute("person", person))
            .andExpect(model().attribute("showHalfDayOption", is(true)))
            .andExpect(model().attribute("application", allOf(
                hasProperty("startDate", is(LocalDate.parse(expectedFromString))),
                hasProperty("startDateIsoValue", is(expectedFromString)),
                hasProperty("endDate", is(LocalDate.parse(expectedToString))),
                hasProperty("endDateIsoValue", is(expectedToString))
            )));
    }

    @Test
    void getNewApplicationFormShowsForm() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        perform(get("/web/application/new"))
            .andExpect(view().name("application/app_form"));
    }

    @Test
    void postNewApplicationFormShowFormIfValidationFails() throws Exception {

        when(settingsService.getSettings()).thenReturn(new Settings());
        when(personService.getSignedInUser()).thenReturn(new Person());

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("reason", "errors");
            errors.reject("globalErrors");
            return null;
        }).when(applicationForLeaveFormValidator).validate(any(), any());

        perform(post("/web/application"))
            .andExpect(model().attribute("errors", instanceOf(Errors.class)))
            .andExpect(view().name("application/app_form"))
            .andExpect(model().attribute("showHalfDayOption", is(true)));
    }

    @Test
    void postNewApplicationFormCallsServiceToApplyApplication() throws Exception {

        final Person person = personWithRole(OFFICE);
        when(personService.getSignedInUser()).thenReturn(person);
        when(applicationInteractionService.apply(any(), any(), any())).thenReturn(someApplication());

        perform(post("/web/application")
            .param("vacationType.category", "HOLIDAY")
            .param("vacationType.requiresApproval", "true"));

        verify(applicationInteractionService).apply(any(), eq(person), any());
    }

    @Test
    void postNewApplicationFormCallsServiceToDirectAllowApplication() throws Exception {

        final Person person = personWithRole(OFFICE);
        when(personService.getSignedInUser()).thenReturn(person);
        when(applicationInteractionService.directAllow(any(), any(), any())).thenReturn(someApplication());

        perform(post("/web/application")
            .param("vacationType.category", "HOLIDAY")
            .param("vacationType.requiresApproval", "false"));

        verify(applicationInteractionService).directAllow(any(), eq(person), any());
    }

    @Test
    void postNewApplicationAddsFlashAttributeAndRedirectsToNewApplication() throws Exception {

        final int applicationId = 11;
        final Person person = personWithRole(OFFICE);

        when(personService.getSignedInUser()).thenReturn(person);
        when(applicationInteractionService.apply(any(), any(), any())).thenReturn(applicationWithId(applicationId));

        perform(post("/web/application")
            .param("vacationType.category", "HOLIDAY")
            .param("vacationType.requiresApproval", "true"))
            .andExpect(flash().attribute("applySuccess", true))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/application/" + applicationId));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/web/application/new", "/web/application/21"})
    void ensureReplacementAddingForOtherPersonIsNotAllowedWhenMyRoleIsUser(String url) {
        final Person signedInUser = new Person();
        signedInUser.setId(42);
        signedInUser.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(1337);
        when(personService.getPersonByID(1337)).thenReturn(Optional.of(person));

        when(personService.getPersonByID(1)).thenReturn(Optional.of(new Person()));

        assertThatThrownBy(() -> {
            perform(post(url)
                .param("holidayReplacementToAdd", "1")
                .param("person", "1337")
                .param("vacationType.category", "HOLIDAY")
                .param("holidayReplacements[0].person.id", "42")
                .param("holidayReplacements[1].person.id", "1337")
                .param("add-holiday-replacement", ""));
        }).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ensureAjaxReplacementAddingForOtherPersonIsNotAllowedWhenMyRoleIsUser() {
        final Person signedInUser = new Person();
        signedInUser.setId(42);
        signedInUser.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(1337);
        when(personService.getPersonByID(1337)).thenReturn(Optional.of(person));

        when(personService.getPersonByID(1)).thenReturn(Optional.of(new Person()));

        assertThatThrownBy(() -> perform(post("/web/application/new/replacements")
            .header("X-Requested-With", "ajax")
            .param("holidayReplacementToAdd", "1")
            .param("person", "1337")
            .param("vacationType.category", "HOLIDAY")
            .param("holidayReplacements[0].person.id", "42")
            .param("holidayReplacements[1].person.id", "1337")
            .param("add-holiday-replacement", ""))).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ensureAddingAnEmptyReplacementForNewApplicationDoesNotThrow() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final LocalDate now = LocalDate.now(clock);
        final Account account = new Account(signedInPerson, now, now, ZERO, ZERO, ZERO, "");
        when(accountService.getHolidaysAccount(now.getYear(), signedInPerson)).thenReturn(Optional.of(account));
        when(settingsService.getSettings()).thenReturn(new Settings());

        final ResultActions perform = perform(post("/web/application/new")
            .param("vacationType.category", "HOLIDAY")
            .param("holidayReplacements[0].person.id", "42")
            .param("holidayReplacements[1].person.id", "1337")
            .param("add-holiday-replacement", ""));

        perform
            .andExpect(status().isOk())
            .andExpect(model().attribute("application", allOf(
                hasProperty("id", nullValue()),
                hasProperty("holidayReplacements", allOf(
                    hasSize(2),
                    contains(
                        hasProperty("person", hasProperty("id", is(42))),
                        hasProperty("person", hasProperty("id", is(1337)))
                    )
                ))
            )))
            .andExpect(view().name("application/app_form"));
    }

    @Test
    void ensureAjaxAddingAnEmptyReplacementForNewApplicationReturnsEmptyTextResponse() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final ResultActions perform = perform(post("/web/application/new/replacements")
            .header("X-Requested-With", "ajax")
            .param("vacationType.category", "HOLIDAY")
            .param("holidayReplacements[0].person.id", "42")
            .param("holidayReplacements[1].person.id", "1337")
            .param("holidayReplacementToAdd", ""));

        perform
            .andExpect(status().isOk())
            .andExpect(model().attributeDoesNotExist("holidayReplacement"))
            .andExpect(view().name("thymeleaf/application/application-form :: replacement-item"))
            .andExpect(content().string(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/web/application/new", "/web/application/21"})
    void ensureAddingReplacementRemovesItFromSelectables(String url) throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1);

        final Person leetPerson = new Person();
        leetPerson.setId(1337);

        final Person replacmentPerson = new Person();
        replacmentPerson.setId(42);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(personService.getPersonByID(42)).thenReturn(Optional.of(replacmentPerson));
        when(personService.getActivePersons()).thenReturn(List.of(replacmentPerson, signedInPerson, leetPerson));

        final LocalDate now = LocalDate.now(clock);
        final Account account = new Account(signedInPerson, now, now, ZERO, ZERO, ZERO, "");
        when(accountService.getHolidaysAccount(now.getYear(), signedInPerson)).thenReturn(Optional.of(account));
        when(settingsService.getSettings()).thenReturn(new Settings());

        final ResultActions perform = perform(post(url)
            .param("vacationType.category", "HOLIDAY")
            .param("add-holiday-replacement", "")
            .param("holidayReplacementToAdd", "42"));

        perform
            .andExpect(status().isOk())
            .andExpect(model().attribute("application", allOf(
                hasProperty("id", nullValue()),
                hasProperty("holidayReplacements", contains(
                    hasProperty("person", hasProperty("id", is(42)))
                ))
            )))
            .andExpect(model().attribute("selectableHolidayReplacements", contains(
                hasProperty("personId", is(1337))
            )))

            .andExpect(view().name("application/app_form"));
    }

    @Test
    void ensureAjaxAddingReplacementForNewApplication() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Person replacementPerson = new Person();
        replacementPerson.setId(42);
        when(personService.getPersonByID(42)).thenReturn(Optional.of(replacementPerson));

        final ResultActions perform = perform(post("/web/application/new/replacements")
            .header("X-Requested-With", "ajax")
            .param("vacationType.category", "HOLIDAY")
            .param("holidayReplacements[0].person.id", "1337")
            .param("holidayReplacements[1].person.id", "21")
            .param("holidayReplacementToAdd", "42"));

        perform
            .andExpect(status().isOk())
            .andExpect(model().attribute("holidayReplacement", allOf(
                hasProperty("person", hasProperty("id", is(42))),
                hasProperty("note", nullValue())
            )))
            .andExpect(model().attribute("index", is(2)))
            .andExpect(model().attribute("deleteButtonFormActionValue", is("/web/application/new")))
            .andExpect(view().name("thymeleaf/application/application-form :: replacement-item"));
    }

    @Test
    void ensureAjaxAddingReplacementForExistingApplication() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Person replacementPerson = new Person();
        replacementPerson.setId(42);
        when(personService.getPersonByID(42)).thenReturn(Optional.of(replacementPerson));

        final ResultActions perform = perform(post("/web/application/7/replacements")
            .header("X-Requested-With", "ajax")
            .param("id", "7")
            .param("vacationType.category", "HOLIDAY")
            .param("holidayReplacements[0].person.id", "1337")
            .param("holidayReplacements[1].person.id", "21")
            .param("holidayReplacementToAdd", "42"));

        perform
            .andExpect(status().isOk())
            .andExpect(model().attribute("holidayReplacement", allOf(
                hasProperty("person", hasProperty("id", is(42))),
                hasProperty("note", nullValue())
            )))
            .andExpect(model().attribute("index", is(2)))
            .andExpect(model().attribute("deleteButtonFormActionValue", is("/web/application/7")))
            .andExpect(view().name("thymeleaf/application/application-form :: replacement-item"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/web/application/new", "/web/application/21"})
    void ensureReplacementDeletionForOtherPersonIsNotAllowedWhenMyRoleIsUser(String url) {
        final Person signedInUser = new Person();
        signedInUser.setId(42);
        signedInUser.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(1337);
        when(personService.getPersonByID(1337)).thenReturn(Optional.of(person));

        assertThatThrownBy(() -> {
            perform(post(url)
                .param("remove-holiday-replacement", "1")
                .param("person", "1337")
                .param("vacationType.category", "HOLIDAY"));
        }).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ensureReplacementDeletionForNewApplicationRemovesPersonFromHolidayReplacements() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final LocalDate now = LocalDate.now(clock);
        final Account account = new Account(signedInPerson, now, now, ZERO, ZERO, ZERO, "");
        when(accountService.getHolidaysAccount(now.getYear(), signedInPerson)).thenReturn(Optional.of(account));
        when(settingsService.getSettings()).thenReturn(new Settings());

        final ResultActions perform = perform(post("/web/application/new")
            .param("vacationType.category", "HOLIDAY")
            .param("holidayReplacements[0].person.id", "42")
            .param("holidayReplacements[1].person.id", "1337")
            .param("holidayReplacements[2].person.id", "21")
            .param("remove-holiday-replacement", "1337"));

        perform
            .andExpect(status().isOk())
            .andExpect(model().attribute("application", allOf(
                hasProperty("id", nullValue()),
                hasProperty("holidayReplacements", contains(
                    hasProperty("person", hasProperty("id", is(42))),
                    hasProperty("person", hasProperty("id", is(21)))
                ))
            )))
            .andExpect(view().name("application/app_form"));
    }

    @Test
    void ensureReplacementDeletionForNewApplicationAddsTheRemovedPersonToSelectableHolidayReplacements() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1);

        final Person bruce = new Person();
        bruce.setId(42);

        final Person clark = new Person();
        clark.setId(1337);

        final Person joker = new Person();
        joker.setId(21);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(personService.getActivePersons()).thenReturn(List.of(signedInPerson, bruce, clark, joker));

        final LocalDate now = LocalDate.now(clock);
        final Account account = new Account(signedInPerson, now, now, ZERO, ZERO, ZERO, "");
        when(accountService.getHolidaysAccount(now.getYear(), signedInPerson)).thenReturn(Optional.of(account));
        when(settingsService.getSettings()).thenReturn(new Settings());

        final ResultActions perform = perform(post("/web/application/new")
            .param("vacationType.category", "HOLIDAY")
            .param("holidayReplacements[0].person.id", "42")
            .param("holidayReplacements[1].person.id", "1337")
            .param("holidayReplacements[2].person.id", "21")
            .param("remove-holiday-replacement", "1337"));

        perform
            .andExpect(status().isOk())
            .andExpect(model().attribute("application", hasProperty("id", nullValue())))
            .andExpect(model().attribute("selectableHolidayReplacements", contains(
                hasProperty("personId", is(1337))
            )))
            .andExpect(view().name("application/app_form"));
    }

    @Test
    void editApplicationForm() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final int year = Year.now(clock).getValue();
        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(year, person)).thenReturn(Optional.of(account));

        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        final Integer applicationId = 1;
        final Application application = new Application();
        application.setId(applicationId);
        application.setStatus(WAITING);
        application.setVacationType(createVacationTypeEntity(HOLIDAY));
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));

        perform(get("/web/application/1/edit"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("noHolidaysAccount", is(false)))
            .andExpect(view().name("application/app_form"))
            .andExpect(model().attribute("showHalfDayOption", is(true)));
    }

    @Test
    void editApplicationFormHalfdayIsDeactivated() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final int year = Year.now(clock).getValue();
        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(year, person)).thenReturn(Optional.of(account));

        final Settings settings = new Settings();
        final var appSettings = new ApplicationSettings();
        appSettings.setAllowHalfDays(false);
        settings.setApplicationSettings(appSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        final Integer applicationId = 1;
        final Application application = new Application();
        application.setId(applicationId);
        application.setStatus(WAITING);
        application.setVacationType(createVacationTypeEntity(HOLIDAY));
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));

        perform(get("/web/application/1/edit"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("noHolidaysAccount", is(false)))
            .andExpect(view().name("application/app_form"))
            .andExpect(model().attribute("showHalfDayOption", is(false)));
    }

    @Test
    void editApplicationFormWithHalfday() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final int year = Year.now(clock).getValue();
        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(year, person)).thenReturn(Optional.of(account));

        final Settings settings = new Settings();
        final var appSettings = new ApplicationSettings();
        appSettings.setAllowHalfDays(false);
        settings.setApplicationSettings(appSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        final Integer applicationId = 1;
        final Application application = new Application();
        application.setId(applicationId);
        application.setStatus(WAITING);
        application.setDayLength(DayLength.MORNING);
        application.setVacationType(createVacationTypeEntity(HOLIDAY));
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));

        perform(get("/web/application/1/edit"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("noHolidaysAccount", is(false)))
            .andExpect(view().name("application/app_form"))
            .andExpect(model().attribute("showHalfDayOption", is(true)));
    }


    @Test
    void ensureUnknownApplicationEditingShowsNotEditablePage() throws Exception {

        when(applicationInteractionService.get(1)).thenReturn(Optional.empty());

        perform(get("/web/application/1/edit"))
            .andExpect(status().isOk())
            .andExpect(view().name("application/app_notwaiting"));
    }

    @Test
    void editApplicationFormNotWaiting() throws Exception {

        final Integer applicationId = 1;
        final Application application = new Application();
        application.setId(applicationId);
        application.setStatus(ALLOWED);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));

        perform(get("/web/application/1/edit"))
            .andExpect(status().isOk())
            .andExpect(view().name("application/app_notwaiting"));
    }

    @Test
    void editApplicationFormNoHolidayAccount() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final int year = Year.now(clock).getValue();
        when(accountService.getHolidaysAccount(year, person)).thenReturn(Optional.empty());

        final Integer applicationId = 1;
        final Application application = new Application();
        application.setId(applicationId);
        application.setStatus(WAITING);
        application.setVacationType(createVacationTypeEntity(HOLIDAY));
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));

        perform(get("/web/application/1/edit"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("noHolidaysAccount", is(true)))
            .andExpect(view().name("application/app_form"));
    }

    @Test
    void ensureAddingAnEmptyReplacementToEditedApplicationDoesNotThrow() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final LocalDate now = LocalDate.now(clock);
        final Account account = new Account(signedInPerson, now, now, ZERO, ZERO, ZERO, "");
        when(accountService.getHolidaysAccount(now.getYear(), signedInPerson)).thenReturn(Optional.of(account));
        when(settingsService.getSettings()).thenReturn(new Settings());

        final ResultActions perform = perform(post("/web/application/7")
            .param("vacationType.category", "HOLIDAY")
            .param("id", "7")
            .param("holidayReplacements[0].person.id", "42")
            .param("holidayReplacements[1].person.id", "1337")
            .param("add-holiday-replacement", ""));

        perform
            .andExpect(status().isOk())
            .andExpect(model().attribute("application", allOf(
                hasProperty("id", is(7)),
                hasProperty("holidayReplacements", allOf(
                    hasSize(2),
                    contains(
                        hasProperty("person", hasProperty("id", is(42))),
                        hasProperty("person", hasProperty("id", is(1337)))
                    )
                ))
            )))
            .andExpect(view().name("application/app_form"));
    }

    @Test
    void ensureReplacementDeletionForEditedApplicationRemovesPersonFromHolidayReplacements() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final LocalDate now = LocalDate.now(clock);
        final Account account = new Account(signedInPerson, now, now, ZERO, ZERO, ZERO, "");
        when(accountService.getHolidaysAccount(now.getYear(), signedInPerson)).thenReturn(Optional.of(account));
        when(settingsService.getSettings()).thenReturn(new Settings());

        final ResultActions perform = perform(post("/web/application/7")
            .param("vacationType.category", "HOLIDAY")
            .param("id", "7")
            .param("holidayReplacements[0].person.id", "42")
            .param("holidayReplacements[1].person.id", "1337")
            .param("holidayReplacements[2].person.id", "21")
            .param("remove-holiday-replacement", "1337"));

        perform
            .andExpect(status().isOk())
            .andExpect(model().attribute("application", allOf(
                hasProperty("id", is(7)),
                hasProperty("holidayReplacements", contains(
                    hasProperty("person", hasProperty("id", is(42))),
                    hasProperty("person", hasProperty("id", is(21)))
                ))
            )))
            .andExpect(view().name("application/app_form"));
    }

    @Test
    void ensureReplacementDeletionForEditedApplicationAddsTheRemovedPersonToSelectableHolidayReplacements() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1);

        final Person bruce = new Person();
        bruce.setId(42);

        final Person clark = new Person();
        clark.setId(1337);

        final Person joker = new Person();
        joker.setId(21);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(personService.getActivePersons()).thenReturn(List.of(signedInPerson, bruce, clark, joker));

        final LocalDate now = LocalDate.now(clock);
        final Account account = new Account(signedInPerson, now, now, ZERO, ZERO, ZERO, "");
        when(accountService.getHolidaysAccount(now.getYear(), signedInPerson)).thenReturn(Optional.of(account));
        when(settingsService.getSettings()).thenReturn(new Settings());

        final ResultActions perform = perform(post("/web/application/7")
            .param("vacationType.category", "HOLIDAY")
            .param("id", "7")
            .param("holidayReplacements[0].person.id", "42")
            .param("holidayReplacements[1].person.id", "1337")
            .param("holidayReplacements[2].person.id", "21")
            .param("remove-holiday-replacement", "1337"));

        perform
            .andExpect(status().isOk())
            .andExpect(model().attribute("application", hasProperty("id", is(7))))
            .andExpect(model().attribute("selectableHolidayReplacements", contains(
                hasProperty("personId", is(1337))
            )))
            .andExpect(view().name("application/app_form"));
    }

    @Test
    void sendEditApplicationForm() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final Integer applicationId = 1;
        final Application application = new Application();
        application.setStatus(WAITING);

        final Application editedApplication = new Application();
        editedApplication.setId(applicationId);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));
        when(applicationInteractionService.edit(eq(application), any(Application.class), eq(person), eq(Optional.of("comment")))).thenReturn(editedApplication);

        perform(post("/web/application/1")
            .param("person.id", "1")
            .param("startDate", "28.10.2020")
            .param("endDate", "28.10.2020")
            .param("vacationType.category", "HOLIDAY")
            .param("dayLength", "FULL")
            .param("comment", "comment"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/application/1"));
    }

    @Test
    void sendEditApplicationFormIsNotWaiting() throws Exception {

        final Integer applicationId = 1;
        final Application application = new Application();
        application.setId(applicationId);
        application.setStatus(ALLOWED);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));

        perform(post("/web/application/1")
            .param("person.id", "1")
            .param("startDate", "28.10.2020")
            .param("endDate", "28.10.2020")
            .param("vacationType.category", "HOLIDAY")
            .param("dayLength", "FULL")
            .param("comment", "comment"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/application/1"))
            .andExpect(flash().attribute("editError", true));
    }

    @Test
    void sendEditApplicationFormApplicationNotFound() {

        final Integer applicationId = 1;
        final Application application = new Application();
        application.setId(applicationId);
        application.setStatus(WAITING);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            perform(post("/web/application/1")
                .param("person.id", "1")
                .param("startDate", "28.10.2020")
                .param("endDate", "28.10.2020")
                .param("vacationType.category", "HOLIDAY")
                .param("dayLength", "FULL")
                .param("comment", "comment"))
        ).hasCauseInstanceOf(UnknownApplicationForLeaveException.class);
    }

    @Test
    void sendEditApplicationFormCannotBeEdited() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final Integer applicationId = 1;
        final Application application = new Application();
        application.setStatus(WAITING);
        final Application editedApplication = new Application();
        editedApplication.setId(applicationId);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));
        when(applicationInteractionService.edit(eq(application), any(Application.class), eq(person), eq(Optional.of("comment")))).thenThrow(EditApplicationForLeaveNotAllowedException.class);

        perform(post("/web/application/1")
            .param("person.id", "1")
            .param("startDate", "28.10.2020")
            .param("endDate", "28.10.2020")
            .param("vacationType.category", "HOLIDAY")
            .param("dayLength", "FULL")
            .param("comment", "comment"))
            .andExpect(status().isOk())
            .andExpect(view().name("application/app_notwaiting"));
    }

    @Test
    void sendEditApplicationFormHasErrors() throws Exception {

        final Integer applicationId = 1;
        final Application application = new Application();
        application.setId(applicationId);
        application.setStatus(WAITING);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));
        when(personService.getSignedInUser()).thenReturn(new Person());

        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        doAnswer(invocation -> {
            final Errors errors = invocation.getArgument(1);
            errors.rejectValue("reason", "errors");
            errors.reject("globalErrors");
            return null;
        }).when(applicationForLeaveFormValidator).validate(any(), any());

        perform(post("/web/application/1")
            .param("person.id", "1")
            .param("startDate", "28.10.2020")
            .param("endDate", "28.10.2020")
            .param("vacationType.category", "HOLIDAY")
            .param("dayLength", "FULL")
            .param("comment", "comment"))
            .andExpect(status().isOk())
            .andExpect(view().name("application/app_form"))
            .andExpect(model().attribute("showHalfDayOption", is(true)));
    }

    @Test
    void ensureApplicationEditReplacementsAreStillDisplayedWhenValidationFails() throws Exception {
        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("startDate", "");
            return null;
        }).when(applicationForLeaveFormValidator).validate(any(), any());

        final Person signedInPerson = new Person();
        signedInPerson.setId(1);

        final Person bruce = new Person();
        bruce.setId(42);

        final Person clark = new Person();
        clark.setId(1337);

        final Person joker = new Person();
        joker.setId(21);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(personService.getActivePersons()).thenReturn(List.of(signedInPerson, bruce, clark, joker));

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Application application = new Application();
        application.setId(7);
        application.setStatus(WAITING);
        when(applicationInteractionService.get(7)).thenReturn(Optional.of(application));

        final ResultActions perform = perform(post("/web/application/7")
            .param("vacationType.category", "HOLIDAY")
            .param("id", "7"));

        perform
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("application", "startDate"))
            .andExpect(model().attribute("selectableHolidayReplacements", contains(
                hasProperty("personId", is(42)),
                hasProperty("personId", is(1337)),
                hasProperty("personId", is(21))
            )))
            .andExpect(view().name("application/app_form"));
    }

    private Person personWithRole(Role role) {
        final Person person = new Person();
        person.setPermissions(List.of(role));

        return person;
    }

    private Person personWithId(int id) {
        final Person person = new Person();
        person.setId(id);

        return person;
    }

    private Application someApplication() {

        final VacationType holidayType = new VacationType(1000, true, HOLIDAY, "application.data.vacationType.holiday", true);

        final Application application = new Application();
        application.setVacationType(convert(holidayType));
        application.setStartDate(LocalDate.now().plusDays(10));
        application.setEndDate(LocalDate.now().plusDays(20));

        return new Application();
    }

    private Application applicationWithId(int id) {
        final Application application = someApplication();
        application.setId(id);

        return application;
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
