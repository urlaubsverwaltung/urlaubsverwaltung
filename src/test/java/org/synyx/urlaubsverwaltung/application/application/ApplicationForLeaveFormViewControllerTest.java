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
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettings;
import org.synyx.urlaubsverwaltung.application.specialleave.SpecialLeaveSettingsItem;
import org.synyx.urlaubsverwaltung.application.specialleave.SpecialLeaveSettingsService;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeDto;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeViewModelService;
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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.SEPTEMBER;
import static java.util.Locale.GERMAN;
import static java.util.Locale.JAPANESE;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
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
import static org.mockito.Mockito.mock;
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
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.ORANGE;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.YELLOW;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_ADD;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
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
    private VacationTypeViewModelService vacationTypeViewModelService;
    @Mock
    private ApplicationInteractionService applicationInteractionService;
    @Mock
    private ApplicationForLeaveFormValidator applicationForLeaveFormValidator;
    @Mock
    private SettingsService settingsService;
    @Mock
    private SpecialLeaveSettingsService specialLeaveSettingsService;
    @Mock
    private DateFormatAware dateFormatAware;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeaveFormViewController(personService, departmentService, accountService, vacationTypeService,
            vacationTypeViewModelService, applicationInteractionService, applicationForLeaveFormValidator, settingsService,
            dateFormatAware, clock, specialLeaveSettingsService, new ApplicationMapper(vacationTypeService));
    }

    @Test
    void specialLeaveInModel() throws Exception {
        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final int year = Year.now(clock).getValue();
        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2014, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(year, person)).thenReturn(Optional.of(account));

        final OvertimeSettings overtimeSettings = new OvertimeSettings();
        final Settings settings = new Settings();
        settings.setOvertimeSettings(overtimeSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        final SpecialLeaveSettingsItem inactiveSetting = new SpecialLeaveSettingsItem(2L, false, "", 2);
        final SpecialLeaveSettingsItem activeSetting = new SpecialLeaveSettingsItem(1L, true, "", 1);
        final List<SpecialLeaveSettingsItem> specialLeaveSettings = List.of(inactiveSetting, activeSetting);
        when(specialLeaveSettingsService.getSpecialLeaveSettings()).thenReturn(specialLeaveSettings);

        final ResultActions resultActions = perform(get("/web/application/new"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("specialLeave",
            hasProperty("specialLeaveItems",
                hasItems(
                    allOf(
                        instanceOf(SpecialLeaveItemDto.class),
                        hasProperty("active", is(true)),
                        hasProperty("messageKey", is("")),
                        hasProperty("days", is(1))
                    )
                ))));
    }

    @Test
    void specialLeaveIsEmptyInModel() throws Exception {
        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final int year = Year.now(clock).getValue();
        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2014, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(year, person)).thenReturn(Optional.of(account));

        final OvertimeSettings overtimeSettings = new OvertimeSettings();
        final Settings settings = new Settings();
        settings.setOvertimeSettings(overtimeSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        when(specialLeaveSettingsService.getSpecialLeaveSettings()).thenReturn(Collections.emptyList());

        final ResultActions resultActions = perform(get("/web/application/new"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("specialLeave",
            hasProperty("specialLeaveItems", hasSize(0))));
    }


    @Test
    void overtimeIsActivated() throws Exception {

        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage("vacation-type-message-key", new Object[]{}, JAPANESE)).thenReturn("vacation type label");

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final int year = Year.now(clock).getValue();
        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2014, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(year, person)).thenReturn(Optional.of(account));

        when(vacationTypeService.getActiveVacationTypes()).thenReturn(List.of(
            ProvidedVacationType.builder(messageSource)
                .id(1L)
                .category(HOLIDAY)
                .messageKey("vacation-type-message-key")
                .build()
        ));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setCategory(HOLIDAY);
        vacationTypeDto.setLabel("vacation type label");

        final OvertimeSettings overtimeSettings = new OvertimeSettings();
        overtimeSettings.setOvertimeActive(true);

        final Settings settings = new Settings();
        settings.setOvertimeSettings(overtimeSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        perform(
            get("/web/application/new")
                .locale(JAPANESE)
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("overtimeActive", is(true)))
            .andExpect(model().attribute("vacationTypes", hasItems(vacationTypeDto)))
            .andExpect(model().attribute("showHalfDayOption", is(true)));
    }

    @Test
    void overtimeIsDeactivated() throws Exception {

        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage("vacation-type-message-key", new Object[]{}, JAPANESE)).thenReturn("vacation type label");

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final int year = Year.now(clock).getValue();
        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2014, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(year, person)).thenReturn(Optional.of(account));

        when(vacationTypeService.getActiveVacationTypesWithoutCategory(OVERTIME)).thenReturn(List.of(
            ProvidedVacationType.builder(messageSource)
                .id(1L)
                .category(HOLIDAY)
                .messageKey("vacation-type-message-key")
                .build()
        ));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setCategory(HOLIDAY);
        vacationTypeDto.setLabel("vacation type label");

        final OvertimeSettings overtimeSettings = new OvertimeSettings();
        overtimeSettings.setOvertimeActive(false);

        final Settings settings = new Settings();
        settings.setOvertimeSettings(overtimeSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        perform(
            get("/web/application/new")
                .locale(JAPANESE)
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("overtimeActive", is(false)))
            .andExpect(model().attribute("vacationTypes", hasItems(vacationTypeDto)))
            .andExpect(model().attribute("showHalfDayOption", is(true)));
    }

    @Test
    void halfdayIsDeactivated() throws Exception {

        final Locale locale = JAPANESE;
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage("vacation-type-message-key", new Object[]{}, locale)).thenReturn("vacation type label");

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final int year = Year.now(clock).getValue();
        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2014, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(year, person)).thenReturn(Optional.of(account));

        when(vacationTypeService.getActiveVacationTypesWithoutCategory(OVERTIME)).thenReturn(List.of(
            ProvidedVacationType.builder(messageSource)
                .id(1L)
                .category(HOLIDAY)
                .messageKey("vacation-type-message-key")
                .build()
        ));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setCategory(HOLIDAY);
        vacationTypeDto.setLabel("vacation type label");

        final OvertimeSettings overtimeSettings = new OvertimeSettings();
        overtimeSettings.setOvertimeActive(false);

        final Settings settings = new Settings();
        settings.setOvertimeSettings(overtimeSettings);
        final var appSettings = new ApplicationSettings();
        appSettings.setAllowHalfDays(false);
        settings.setApplicationSettings(appSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        perform(
            get("/web/application/new")
                .locale(locale)
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("overtimeActive", is(false)))
            .andExpect(model().attribute("vacationTypes", hasItems(vacationTypeDto)))
            .andExpect(model().attribute("showHalfDayOption", is(false)));
    }

    @Test
    void getNewApplicationFormDefaultsToSignedInPersonIfPersonIdNotGiven() throws Exception {

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2014, APRIL, 1);
        final Account account = new Account(signedInPerson, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(anyInt(), eq(signedInPerson))).thenReturn(Optional.of(account));
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get("/web/application/new"))
            .andExpect(model().attribute("person", signedInPerson));
    }

    @Test
    void getNewApplicationFormUsesPersonOfGivenPersonId() throws Exception {

        final Person person = new Person();
        person.setId(1L);

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));

        final LocalDate validFrom = LocalDate.now(clock).withMonth(JANUARY.getValue()).withDayOfMonth(1);
        final LocalDate validTo = LocalDate.now(clock).withMonth(DECEMBER.getValue()).withDayOfMonth(31);
        final LocalDate expiryDate = LocalDate.now(clock).withMonth(APRIL.getValue()).withDayOfMonth(1);
        final Account account = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(LocalDate.now(clock).getYear(), person)).thenReturn(Optional.of(account));
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get("/web/application/new")
            .param("personId", "1"))
            .andExpect(model().attribute("person", person));
    }

    @Test
    void getNewApplicationFormOfficeUserCanAddForAnotherUser() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final Person applier = personWithRole(USER, OFFICE);
        applier.setId(3L);
        when(personService.getSignedInUser()).thenReturn(applier);

        final LocalDate now = LocalDate.now(clock);
        final LocalDate validFrom = now.withMonth(JANUARY.getValue()).withDayOfMonth(1);
        final LocalDate validTo = now.withMonth(DECEMBER.getValue()).withDayOfMonth(31);
        final LocalDate expiryDate = LocalDate.of(now.getYear(), APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, null, expiryDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(now.getYear(), person)).thenReturn(Optional.of(account));
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person managedPerson = new Person();
        managedPerson.setId(2L);
        when(personService.getActivePersons()).thenReturn(List.of(person, managedPerson, applier));

        perform(get("/web/application/new")
            .param("personId", "1"))
            .andExpect(model().attribute("persons", List.of(person, managedPerson, applier)))
            .andExpect(model().attribute("canAddApplicationForLeaveForAnotherUser", true));
    }

    @Test
    void getNewApplicationFormBossUserCanAddForAnotherUser() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final Person applier = personWithRole(USER, BOSS, APPLICATION_ADD);
        applier.setId(3L);
        when(personService.getSignedInUser()).thenReturn(applier);

        final LocalDate now = LocalDate.now(clock);
        final LocalDate validFrom = now.withMonth(JANUARY.getValue()).withDayOfMonth(1);
        final LocalDate validTo = now.withMonth(DECEMBER.getValue()).withDayOfMonth(31);
        final LocalDate expiryDate = LocalDate.of(now.getYear(), APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, null, expiryDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(now.getYear(), person)).thenReturn(Optional.of(account));
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person managedPerson = new Person();
        managedPerson.setId(2L);
        when(personService.getActivePersons()).thenReturn(List.of(person, managedPerson, applier));

        perform(get("/web/application/new")
            .param("personId", "1"))
            .andExpect(model().attribute("persons", List.of(person, managedPerson, applier)))
            .andExpect(model().attribute("canAddApplicationForLeaveForAnotherUser", true));
    }

    @Test
    void getNewApplicationFormUserWithRoleDepartmentHeadCanAddForAnotherUser() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setFirstName("Person One");
        person.setLastName("Lastname One");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final Person applier = personWithRole(USER, DEPARTMENT_HEAD, APPLICATION_ADD);
        applier.setId(3L);
        applier.setFirstName("Applier");
        applier.setLastName("Name");
        when(personService.getSignedInUser()).thenReturn(applier);

        final LocalDate validFrom = LocalDate.now(clock).withMonth(JANUARY.getValue()).withDayOfMonth(1);
        final LocalDate validTo = LocalDate.now(clock).withMonth(DECEMBER.getValue()).withDayOfMonth(31);
        final LocalDate expiryDate = LocalDate.now(clock).withMonth(APRIL.getValue()).withDayOfMonth(1);
        final Account account = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(LocalDate.now(clock).getYear(), person)).thenReturn(Optional.of(account));
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person managedPerson = new Person();
        managedPerson.setId(2L);
        managedPerson.setFirstName("Person Two");
        managedPerson.setLastName("Lastname Two");
        when(departmentService.getManagedMembersOfDepartmentHead(applier)).thenReturn(List.of(person, managedPerson, applier));

        perform(get("/web/application/new")
            .param("personId", "1"))
            .andExpect(model().attribute("persons", List.of(applier, person, managedPerson)))
            .andExpect(model().attribute("canAddApplicationForLeaveForAnotherUser", true));
    }

    @Test
    void getNewApplicationFormUserWithRoleSecondStageAuthorityCanAddForAnotherUser() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setFirstName("Person One");
        person.setLastName("Lastname One");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final Person applier = personWithRole(USER, SECOND_STAGE_AUTHORITY, APPLICATION_ADD);
        applier.setId(3L);
        applier.setFirstName("Applier");
        applier.setLastName("Name");
        when(personService.getSignedInUser()).thenReturn(applier);

        final LocalDate now = LocalDate.now(clock);
        final LocalDate validFrom = now.withMonth(JANUARY.getValue()).withDayOfMonth(1);
        final LocalDate validTo = now.withMonth(DECEMBER.getValue()).withDayOfMonth(31);
        final LocalDate expiryDate = LocalDate.of(now.getYear(), APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, null, expiryDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(now.getYear(), person)).thenReturn(Optional.of(account));
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person managedPerson = new Person();
        managedPerson.setId(2L);
        managedPerson.setFirstName("Person Two");
        managedPerson.setLastName("Lastname Two");
        when(departmentService.getManagedMembersForSecondStageAuthority(applier)).thenReturn(List.of(person, managedPerson, applier));

        perform(get("/web/application/new")
            .param("personId", "1"))
            .andExpect(model().attribute("persons", List.of(applier, person, managedPerson)))
            .andExpect(model().attribute("canAddApplicationForLeaveForAnotherUser", true));
    }

    @Test
    void getNewApplicationFormForUnknownPersonIdFallsBackToSignedInUser() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setId(1337L);

        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(personService.getPersonByID(1L)).thenReturn(Optional.empty());

        final LocalDate now = LocalDate.now(clock);
        final Account account = new Account(signedInUser, now, now, true, LocalDate.of(now.getYear(), APRIL, 1), ZERO, ZERO, ZERO, "");
        when(accountService.getHolidaysAccount(now.getYear(), signedInUser)).thenReturn(Optional.of(account));
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get("/web/application/new").param("personId", "1"))
            .andExpect(model().attribute("person", hasProperty("id", is(1337L))));
    }

    @Test
    void getNewApplicationFormAccessibleIfGivenPersonIsSignedInPerson() throws Exception {

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(signedInPerson));

        perform(get("/web/application/new")
            .param("personId", "1"))
            .andExpect(status().isOk());
    }

    @Test
    void getNewApplicationFormAccessibleForOfficeIfGivenPersonNotSignedInPerson() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(personWithId(1)));

        perform(get("/web/application/new")
            .param("personId", "1"))
            .andExpect(status().isOk());
    }

    @Test
    void getNewApplicationFormWithGivenDateFrom() throws Exception {

        final Locale locale = GERMAN;

        final Person person = new Person();
        person.setId(1L);

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));

        final LocalDate validFrom = LocalDate.now(clock).withMonth(JANUARY.getValue()).withDayOfMonth(1);
        final LocalDate validTo = LocalDate.now(clock).withMonth(DECEMBER.getValue()).withDayOfMonth(31);
        final LocalDate expiryDate = LocalDate.now(clock).withMonth(APRIL.getValue()).withDayOfMonth(1);
        final Account account = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(LocalDate.now(clock).getYear(), person)).thenReturn(Optional.of(account));
        when(settingsService.getSettings()).thenReturn(new Settings());

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        final LocalDate givenDateFrom = LocalDate.now(clock).withMonth(SEPTEMBER.getValue()).withDayOfMonth(30);

        when(dateFormatAware.parse(givenDateFrom.format(formatter), locale)).thenReturn(Optional.of(givenDateFrom));

        perform(
            get("/web/application/new")
                .locale(locale)
                .param("personId", "1")
                .param("from", givenDateFrom.format(formatter))
        )
            .andExpect(model().attribute("person", person))
            .andExpect(model().attribute("showHalfDayOption", is(true)))
            .andExpect(model().attribute("applicationForLeaveForm", allOf(
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

        final Locale locale = GERMAN;

        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));

        final Person person = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2014, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(anyInt(), eq(person))).thenReturn(Optional.of(account));
        when(settingsService.getSettings()).thenReturn(new Settings());

        when(dateFormatAware.parse(givenFromString, locale)).thenReturn(Optional.of(LocalDate.parse(expectedFromString)));
        when(dateFormatAware.parse(givenToString, locale)).thenReturn(Optional.of(LocalDate.parse(expectedToString)));

        perform(
            get("/web/application/new")
                .locale(locale)
                .param("personId", "1")
                .param("from", givenFromString)
                .param("to", givenToString)
        )
            .andExpect(model().attribute("person", person))
            .andExpect(model().attribute("showHalfDayOption", is(true)))
            .andExpect(model().attribute("applicationForLeaveForm", allOf(
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
            .andExpect(view().name("application/application_form"));
    }

    @Test
    void getNewApplicationFormShowsFormWithHolidaysAccount() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expireDate = LocalDate.of(2014, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, true, expireDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(anyInt(), eq(person))).thenReturn(Optional.of(account));
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get("/web/application/new"))
            .andExpect(model().attribute("vacationTypeColors", equalTo(List.of(new VacationTypeDto(1L, ORANGE)))))
            .andExpect(view().name("application/application_form"));
    }

    @Test
    void postNewApplicationForSamePersonIsOk() throws Exception {

        final Person person = personWithRole(USER);
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);

        when(applicationInteractionService.directAllow(any(Application.class), eq(person), any(Optional.class)))
            .thenReturn(new Application());

        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType(1L)));

        perform(post("/web/application")
            .param("person.id", "1")
            .param("startDate", "2022-11-02")
            .param("endDate", "2022-11-03")
            .param("vacationType.id", "1")
            .param("dayLength", "FULL")
        )
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void postNewApplicationForAnyOtherPersonIsForbidden() {

        final Person signedInUser = personWithRole(USER);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        assertThatThrownBy(() -> {
            perform(post("/web/application")
                .param("person.id", "1")
                .param("startDate", "2022-11-02")
                .param("endDate", "2022-11-03")
                .param("vacationType.id", "1")
                .param("dayLength", "FULL")
            );
        }).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void postNewApplicationForAsDHForPersonWithoutApplicationAddRightIsForbidden() {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        assertThatThrownBy(() -> {
            perform(post("/web/application")
                .param("person.id", "1")
                .param("startDate", "2022-11-02")
                .param("endDate", "2022-11-03")
                .param("vacationType.id", "1")
                .param("dayLength", "FULL")
            );
        }).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void postNewApplicationForAsDHForNotMemberWithApplicationAddRightIsForbidden() {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD, APPLICATION_ADD);
        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(eq(signedInUser), any(Person.class))).thenReturn(false);

        assertThatThrownBy(() -> {
            perform(post("/web/application")
                .param("person.id", "1")
                .param("startDate", "2022-11-02")
                .param("endDate", "2022-11-03")
                .param("vacationType.id", "1")
                .param("dayLength", "FULL")
            );
        }).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void postNewApplicationForAsDHForMemberWithApplicationAddRightIsOk() throws Exception {

        final Person signedInUser = personWithRole(USER, DEPARTMENT_HEAD, APPLICATION_ADD);
        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(eq(signedInUser), any(Person.class))).thenReturn(true);

        when(applicationInteractionService.directAllow(any(Application.class), any(Person.class), any(Optional.class)))
            .thenReturn(new Application());

        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType(1L)));

        perform(post("/web/application")
            .param("person.id", "1")
            .param("startDate", "2022-11-02")
            .param("endDate", "2022-11-03")
            .param("vacationType.id", "1")
            .param("dayLength", "FULL")
        ).andExpect(status().is3xxRedirection());
    }

    @Test
    void postNewApplicationForAsSSAForPersonWithoutApplicationAddRightIsForbidden() {

        final Person signedInUser = personWithRole(USER, SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        assertThatThrownBy(() -> {
            perform(post("/web/application")
                .param("person.id", "1")
                .param("startDate", "2022-11-02")
                .param("endDate", "2022-11-03")
                .param("vacationType.id", "1")
                .param("dayLength", "FULL")
            );
        }).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void postNewApplicationForAsSSAForNotMemberWithApplicationAddRightIsForbidden() {

        final Person signedInUser = personWithRole(USER, SECOND_STAGE_AUTHORITY, APPLICATION_ADD);
        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(eq(signedInUser), any(Person.class))).thenReturn(false);

        assertThatThrownBy(() -> {
            perform(post("/web/application")
                .param("person.id", "1")
                .param("startDate", "2022-11-02")
                .param("endDate", "2022-11-03")
                .param("vacationType.id", "1")
                .param("dayLength", "FULL")
            );
        }).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void postNewApplicationForAsSSAForMemberWithApplicationAddRightIsOk() throws Exception {

        final Person signedInUser = personWithRole(USER, SECOND_STAGE_AUTHORITY, APPLICATION_ADD);
        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(eq(signedInUser), any(Person.class))).thenReturn(true);

        when(applicationInteractionService.directAllow(any(Application.class), any(Person.class), any(Optional.class)))
            .thenReturn(new Application());

        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType(1L)));

        perform(post("/web/application")
            .param("person.id", "1")
            .param("startDate", "2022-11-02")
            .param("endDate", "2022-11-03")
            .param("vacationType.id", "1")
            .param("dayLength", "FULL")
        ).andExpect(status().is3xxRedirection());
    }

    @Test
    void postNewApplicationForAsBossForPersonWithoutApplicationAddRightIsForbidden() {

        final Person signedInUser = personWithRole(USER, BOSS);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        assertThatThrownBy(() -> {
            perform(post("/web/application")
                .param("person.id", "1")
                .param("startDate", "2022-11-02")
                .param("endDate", "2022-11-03")
                .param("vacationType.id", "1")
                .param("dayLength", "FULL")
            );
        }).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void postNewApplicationFormShowFormIfValidationFails() throws Exception {

        when(settingsService.getSettings()).thenReturn(new Settings());
        when(personService.getSignedInUser()).thenReturn(new Person());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("reason", "errors");
            errors.reject("globalErrors");
            return null;
        }).when(applicationForLeaveFormValidator).validate(any(), any());

        perform(post("/web/application"))
            .andExpect(model().attribute("errors", instanceOf(Errors.class)))
            .andExpect(model().attribute("showHalfDayOption", is(true)))
            .andExpect(model().attribute("vacationTypeColors", equalTo(List.of(new VacationTypeDto(1L, ORANGE)))))
            .andExpect(view().name("application/application_form"));
    }

    @Test
    void postNewApplicationFormCallsServiceToApplyApplication() throws Exception {

        final Person person = personWithRole(OFFICE);
        when(personService.getSignedInUser()).thenReturn(person);
        when(applicationInteractionService.apply(any(), any(), any())).thenReturn(someApplication());

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).requiresApprovalToApply(true).build();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        perform(post("/web/application")
            .param("vacationType.id", "1"));

        verify(applicationInteractionService).apply(any(), eq(person), any());
    }

    @Test
    void postNewApplicationFormCallsServiceToDirectAllowApplication() throws Exception {

        final Person person = personWithRole(OFFICE);
        when(personService.getSignedInUser()).thenReturn(person);
        when(applicationInteractionService.directAllow(any(), any(), any())).thenReturn(someApplication());

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).requiresApprovalToApply(false).build();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        perform(post("/web/application")
            .param("vacationType.id", "1"));

        verify(applicationInteractionService).directAllow(any(), eq(person), any());
    }

    @Test
    void postNewApplicationAddsFlashAttributeAndRedirectsToNewApplication() throws Exception {

        final int applicationId = 11;
        final Person person = personWithRole(OFFICE);

        when(personService.getSignedInUser()).thenReturn(person);
        when(applicationInteractionService.directAllow(any(), any(), any())).thenReturn(applicationWithId(applicationId));

        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType(1L)));

        perform(post("/web/application")
            .param("vacationType.id", "1")
        )
            .andExpect(flash().attribute("applySuccess", true))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/application/" + applicationId));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/web/application/new", "/web/application/21/edit"})
    void ensureReplacementAddingForOtherPersonIsNotAllowedWhenMyRoleIsUser(String url) {
        final Person signedInUser = new Person();
        signedInUser.setId(42L);
        signedInUser.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(1337L);
        when(personService.getPersonByID(1337L)).thenReturn(Optional.of(person));

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(new Person()));

        assertThatThrownBy(() -> perform(post(url)
            .param("holidayReplacementToAdd", "1")
            .param("person", "1337")
            .param("vacationType.id", "1")
            .param("holidayReplacements[0].person.id", "42")
            .param("holidayReplacements[1].person.id", "1337")
            .param("add-holiday-replacement", ""))).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ensureAjaxReplacementAddingForOtherPersonIsNotAllowedWhenMyRoleIsUser() {
        final Person signedInUser = new Person();
        signedInUser.setId(42L);
        signedInUser.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(1337L);
        when(personService.getPersonByID(1337L)).thenReturn(Optional.of(person));

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(new Person()));

        assertThatThrownBy(() -> perform(post("/web/application/new/replacements")
            .header("X-Requested-With", "ajax")
            .param("holidayReplacementToAdd", "1")
            .param("person", "1337")
            .param("vacationType.id", "1")
            .param("holidayReplacements[0].person.id", "42")
            .param("holidayReplacements[1].person.id", "1337")
            .param("add-holiday-replacement", ""))).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ensureAddingAnEmptyReplacementForNewApplicationDoesNotThrow() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Locale locale = GERMAN;
        final MessageSource messageSource = messageSourceForVacationType("message-key", "label", locale);
        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("message-key")
            .build();

        final LocalDate now = LocalDate.now(clock);
        final Account account = new Account(signedInPerson, now, now, true, LocalDate.of(now.getYear(), APRIL, 1), ZERO, ZERO, ZERO, "");
        when(accountService.getHolidaysAccount(now.getYear(), signedInPerson)).thenReturn(Optional.of(account));

        when(settingsService.getSettings()).thenReturn(new Settings());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        perform(
            post("/web/application/new")
                .locale(locale)
                .param("vacationType.id", "1")
                .param("holidayReplacements[0].person.id", "42")
                .param("holidayReplacements[1].person.id", "1337")
                .param("add-holiday-replacement", "")
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("applicationForLeaveForm", allOf(
                hasProperty("id", nullValue()),
                hasProperty("holidayReplacements", allOf(
                    hasSize(2),
                    contains(
                        hasProperty("person", hasProperty("id", is(42L))),
                        hasProperty("person", hasProperty("id", is(1337L)))
                    )
                ))
            )))
            .andExpect(model().attribute("vacationTypeColors", equalTo(List.of(new VacationTypeDto(1L, ORANGE)))))
            .andExpect(view().name("application/application_form"));
    }

    @Test
    void ensureAjaxAddingAnEmptyReplacementForNewApplicationReturnsEmptyTextResponse() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final ResultActions perform = perform(post("/web/application/new/replacements")
            .header("X-Requested-With", "ajax")
            .param("vacationType.id", "1")
            .param("holidayReplacements[0].person.id", "42")
            .param("holidayReplacements[1].person.id", "1337")
            .param("holidayReplacementToAdd", ""));

        perform
            .andExpect(status().isOk())
            .andExpect(model().attributeDoesNotExist("holidayReplacement"))
            .andExpect(view().name("application/application-form :: replacement-item"))
            .andExpect(content().string(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/web/application/new", "/web/application/21/edit"})
    void ensureAddingReplacementRemovesItFromSelectables(String url) throws Exception {

        final Locale locale = GERMAN;
        final MessageSource messageSource = messageSourceForVacationType("message-key", "label", locale);
        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("message-key")
            .build();

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);

        final Person leetPerson = new Person();
        leetPerson.setId(1337L);

        final Person replacmentPerson = new Person();
        replacmentPerson.setId(42L);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(personService.getPersonByID(42L)).thenReturn(Optional.of(replacmentPerson));
        when(personService.getActivePersons()).thenReturn(List.of(replacmentPerson, signedInPerson, leetPerson));

        final LocalDate now = LocalDate.now(clock);
        final Account account = new Account(signedInPerson, now, now, true, LocalDate.of(now.getYear(), APRIL, 1), ZERO, ZERO, ZERO, "");
        when(accountService.getHolidaysAccount(now.getYear(), signedInPerson)).thenReturn(Optional.of(account));
        when(settingsService.getSettings()).thenReturn(new Settings());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        perform(
            post(url)
                .locale(locale)
                .param("vacationType.id", "1")
                .param("add-holiday-replacement", "")
                .param("holidayReplacementToAdd", "42")
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("applicationForLeaveForm", allOf(
                hasProperty("id", nullValue()),
                hasProperty("holidayReplacements", contains(
                    hasProperty("person", hasProperty("id", is(42L)))
                ))
            )))
            .andExpect(model().attribute("selectableHolidayReplacements", contains(
                hasProperty("personId", is(1337L))
            )))
            .andExpect(model().attribute("vacationTypeColors", equalTo(List.of(new VacationTypeDto(1L, ORANGE)))))
            .andExpect(view().name("application/application_form"));
    }

    @Test
    void ensureAjaxAddingReplacementForNewApplication() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Person replacementPerson = new Person();
        replacementPerson.setId(42L);
        when(personService.getPersonByID(42L)).thenReturn(Optional.of(replacementPerson));

        perform(post("/web/application/new/replacements")
            .header("X-Requested-With", "ajax")
            .param("vacationType.id", "1")
            .param("holidayReplacements[0].person.id", "1337")
            .param("holidayReplacements[1].person.id", "21")
            .param("holidayReplacementToAdd", "42")
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("holidayReplacement", allOf(
                hasProperty("person", hasProperty("id", is(42L))),
                hasProperty("note", nullValue())
            )))
            .andExpect(model().attribute("index", is(2)))
            .andExpect(model().attribute("deleteButtonFormActionValue", is("/web/application/new")))
            .andExpect(view().name("application/application-form :: replacement-item"));
    }

    @Test
    void ensureAjaxAddingReplacementForExistingApplication() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Person replacementPerson = new Person();
        replacementPerson.setId(42L);
        when(personService.getPersonByID(42L)).thenReturn(Optional.of(replacementPerson));

        perform(post("/web/application/7/replacements")
            .header("X-Requested-With", "ajax")
            .param("id", "7")
            .param("vacationType.id", "1")
            .param("holidayReplacements[0].person.id", "1337")
            .param("holidayReplacements[1].person.id", "21")
            .param("holidayReplacementToAdd", "42")
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("holidayReplacement", allOf(
                hasProperty("person", hasProperty("id", is(42L))),
                hasProperty("note", nullValue())
            )))
            .andExpect(model().attribute("index", is(2)))
            .andExpect(model().attribute("deleteButtonFormActionValue", is("/web/application/7/edit")))
            .andExpect(view().name("application/application-form :: replacement-item"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/web/application/new", "/web/application/21/edit"})
    void ensureReplacementDeletionForOtherPersonIsNotAllowedWhenMyRoleIsUser(String url) {
        final Person signedInUser = new Person();
        signedInUser.setId(42L);
        signedInUser.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(1337L);
        when(personService.getPersonByID(1337L)).thenReturn(Optional.of(person));

        assertThatThrownBy(() -> perform(post(url)
            .param("remove-holiday-replacement", "1")
            .param("person", "1337")
            .param("vacationType.id", "1"))).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ensureReplacementDeletionForNewApplicationRemovesPersonFromHolidayReplacements() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Locale locale = GERMAN;
        final MessageSource messageSource = messageSourceForVacationType("message-key", "label", locale);
        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .messageKey("message-key")
            .build();

        final LocalDate now = LocalDate.now(clock);
        final Account account = new Account(signedInPerson, now, now, true, LocalDate.of(now.getYear(), APRIL, 1), ZERO, ZERO, ZERO, "");
        when(accountService.getHolidaysAccount(now.getYear(), signedInPerson)).thenReturn(Optional.of(account));

        when(settingsService.getSettings()).thenReturn(new Settings());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        perform(
            post("/web/application/new")
                .locale(locale)
                .param("vacationType.id", "1")
                .param("holidayReplacements[0].person.id", "42")
                .param("holidayReplacements[1].person.id", "1337")
                .param("holidayReplacements[2].person.id", "21")
                .param("remove-holiday-replacement", "1337")
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("applicationForLeaveForm", allOf(
                hasProperty("id", nullValue()),
                hasProperty("holidayReplacements", contains(
                    hasProperty("person", hasProperty("id", is(42L))),
                    hasProperty("person", hasProperty("id", is(21L)))
                ))
            )))
            .andExpect(model().attribute("vacationTypeColors", equalTo(List.of(new VacationTypeDto(1L, ORANGE)))))
            .andExpect(view().name("application/application_form"));
    }

    @Test
    void ensureReplacementDeletionForNewApplicationAddsTheRemovedPersonToSelectableHolidayReplacements() throws Exception {

        final Locale locale = GERMAN;
        final MessageSource messageSource = messageSourceForVacationType("message-key", "label", locale);
        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("message-key")
            .build();

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);

        final Person bruce = new Person();
        bruce.setId(42L);

        final Person clark = new Person();
        clark.setId(1337L);

        final Person joker = new Person();
        joker.setId(21L);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(personService.getActivePersons()).thenReturn(List.of(signedInPerson, bruce, clark, joker));

        final LocalDate now = LocalDate.now(clock);
        final Account account = new Account(signedInPerson, now, now, true, LocalDate.of(now.getYear(), APRIL, 1), ZERO, ZERO, ZERO, "");
        when(accountService.getHolidaysAccount(now.getYear(), signedInPerson)).thenReturn(Optional.of(account));

        when(settingsService.getSettings()).thenReturn(new Settings());
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        perform(
            post("/web/application/new")
                .locale(locale)
                .param("vacationType.id", "1")
                .param("holidayReplacements[0].person.id", "42")
                .param("holidayReplacements[1].person.id", "1337")
                .param("holidayReplacements[2].person.id", "21")
                .param("remove-holiday-replacement", "1337")
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("applicationForLeaveForm", hasProperty("id", nullValue())))
            .andExpect(model().attribute("selectableHolidayReplacements", contains(
                hasProperty("personId", is(1337L))
            )))
            .andExpect(view().name("application/application_form"));
    }

    @Test
    void editApplicationForm() throws Exception {

        final Locale locale = GERMAN;
        final MessageSource messageSource = messageSourceForVacationType("message-key", "label", locale);
        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("message-key")
            .build();

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getActivePersons()).thenReturn(List.of(person));
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));

        final int year = Year.now(clock).getValue();
        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expireDate = LocalDate.of(2014, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, true, expireDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(year, person)).thenReturn(Optional.of(account));

        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        final Long applicationId = 1L;
        final Application application = new Application();
        application.setPerson(person);
        application.setId(applicationId);
        application.setStatus(WAITING);
        application.setVacationType(vacationType);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));

        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        perform(
            get("/web/application/1/edit")
                .locale(locale)
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("noHolidaysAccount", is(false)))
            .andExpect(model().attribute("showHalfDayOption", is(true)))
            .andExpect(model().attribute("vacationTypeColors", equalTo(List.of(new VacationTypeDto(1L, ORANGE)))))
            .andExpect(view().name("application/application_form"));
    }

    @Test
    void editApplicationFormForOtherWithOfficePermission() throws Exception {

        final Locale locale = GERMAN;
        final MessageSource messageSource = messageSourceForVacationType("message-key", "label", locale);
        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("message-key")
            .build();

        final Person person = new Person();
        person.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getActivePersons()).thenReturn(List.of(person));
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));

        final int year = Year.now(clock).getValue();
        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expireDate = LocalDate.of(2014, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, true, expireDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(year, person)).thenReturn(Optional.of(account));

        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        final Long applicationId = 1L;
        final Application application = new Application();
        application.setPerson(person);
        application.setId(applicationId);
        application.setStatus(ALLOWED);
        application.setVacationType(vacationType);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));

        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        perform(
            get("/web/application/1/edit")
                .locale(locale)
        )
            .andExpect(status().isOk())
            .andExpect(view().name("application/application_form"));
    }

    @Test
    void editApplicationFormForAnotherUserIsDenied() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2L);
        final Application application = new Application();
        application.setPerson(person);
        application.setId(1L);
        application.setStatus(WAITING);
        application.setVacationType(createVacationType(1L, HOLIDAY, new StaticMessageSource()));
        when(applicationInteractionService.get(1L)).thenReturn(Optional.of(application));

        perform(get("/web/application/1/edit"))
            .andExpect(status().isOk())
            .andExpect(view().name("application/application-not-editable"));
    }

    @Test
    void editApplicationFormHalfdayIsDeactivated() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Locale locale = GERMAN;
        final MessageSource messageSource = messageSourceForVacationType("message-key", "label", locale);
        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("message-key")
            .build();

        final int year = Year.now(clock).getValue();
        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expireDate = LocalDate.of(2014, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, true, expireDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(year, person)).thenReturn(Optional.of(account));

        final Settings settings = new Settings();
        final var appSettings = new ApplicationSettings();
        appSettings.setAllowHalfDays(false);
        settings.setApplicationSettings(appSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        final Long applicationId = 1L;
        final Application application = new Application();
        application.setPerson(person);
        application.setId(applicationId);
        application.setStatus(WAITING);
        application.setVacationType(vacationType);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));

        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        perform(
            get("/web/application/1/edit")
                .locale(locale)
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("noHolidaysAccount", is(false)))
            .andExpect(view().name("application/application_form"))
            .andExpect(model().attribute("showHalfDayOption", is(false)));
    }

    @Test
    void editApplicationFormWithHalfday() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Locale locale = GERMAN;
        final MessageSource messageSource = messageSourceForVacationType("message-key", "label", locale);
        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("message-key")
            .build();

        final int year = Year.now(clock).getValue();
        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expireDate = LocalDate.of(2014, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, true, expireDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(year, person)).thenReturn(Optional.of(account));

        final Settings settings = new Settings();
        final var appSettings = new ApplicationSettings();
        appSettings.setAllowHalfDays(false);
        settings.setApplicationSettings(appSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        final Long applicationId = 1L;
        final Application application = new Application();
        application.setPerson(person);
        application.setId(applicationId);
        application.setStatus(WAITING);
        application.setDayLength(DayLength.MORNING);
        application.setVacationType(vacationType);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));

        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        perform(
            get("/web/application/1/edit")
                .locale(locale)
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("noHolidaysAccount", is(false)))
            .andExpect(view().name("application/application_form"))
            .andExpect(model().attribute("showHalfDayOption", is(true)));
    }


    @Test
    void ensureUnknownApplicationEditingShowsNotEditablePage() throws Exception {

        when(applicationInteractionService.get(1L)).thenReturn(Optional.empty());

        perform(get("/web/application/1/edit"))
            .andExpect(status().isOk())
            .andExpect(view().name("application/application-not-editable"));
    }

    @Test
    void editApplicationFormNoHolidayAccount() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final Locale locale = GERMAN;
        final MessageSource messageSource = messageSourceForVacationType("message-key", "label", locale);
        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("message-key")
            .build();

        final int year = Year.now(clock).getValue();
        when(accountService.getHolidaysAccount(year, person)).thenReturn(Optional.empty());

        final Long applicationId = 1L;
        final Application application = new Application();
        application.setPerson(person);
        application.setId(applicationId);
        application.setStatus(WAITING);
        application.setVacationType(vacationType);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));

        perform(
            get("/web/application/1/edit")
                .locale(locale)
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("noHolidaysAccount", is(true)))
            .andExpect(view().name("application/application_form"));
    }

    @Test
    void ensureAddingAnEmptyReplacementToEditedApplicationDoesNotThrow() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Locale locale = GERMAN;
        final MessageSource messageSource = messageSourceForVacationType("message-key", "label", locale);
        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("message-key")
            .build();

        final LocalDate now = LocalDate.now(clock);
        final Account account = new Account(signedInPerson, now, now, true, LocalDate.of(now.getYear(), APRIL, 1), ZERO, ZERO, ZERO, "");
        when(accountService.getHolidaysAccount(now.getYear(), signedInPerson)).thenReturn(Optional.of(account));

        when(settingsService.getSettings()).thenReturn(new Settings());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        perform(
            post("/web/application/7/edit")
                .locale(locale)
                .param("vacationType.id", "1")
                .param("id", "7")
                .param("holidayReplacements[0].person.id", "42")
                .param("holidayReplacements[1].person.id", "1337")
                .param("add-holiday-replacement", "")
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("applicationForLeaveForm", allOf(
                hasProperty("id", is(7L)),
                hasProperty("holidayReplacements", allOf(
                    hasSize(2),
                    contains(
                        hasProperty("person", hasProperty("id", is(42L))),
                        hasProperty("person", hasProperty("id", is(1337L)))
                    )
                ))
            )))
            .andExpect(model().attribute("vacationTypeColors", equalTo(List.of(new VacationTypeDto(1L, ORANGE)))))
            .andExpect(view().name("application/application_form"));
    }

    @Test
    void ensureReplacementDeletionForEditedApplicationRemovesPersonFromHolidayReplacements() throws Exception {

        final Locale locale = GERMAN;
        final MessageSource messageSource = messageSourceForVacationType("message-key", "label", locale);
        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("message-key")
            .build();

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final LocalDate now = LocalDate.now(clock);
        final Account account = new Account(signedInPerson, now, now, true, LocalDate.of(now.getYear(), APRIL, 1), ZERO, ZERO, ZERO, "");
        when(accountService.getHolidaysAccount(now.getYear(), signedInPerson)).thenReturn(Optional.of(account));

        when(settingsService.getSettings()).thenReturn(new Settings());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        perform(
            post("/web/application/7/edit")
                .locale(locale)
                .param("vacationType.id", "1")
                .param("id", "7")
                .param("holidayReplacements[0].person.id", "42")
                .param("holidayReplacements[1].person.id", "1337")
                .param("holidayReplacements[2].person.id", "21")
                .param("remove-holiday-replacement", "1337")
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("applicationForLeaveForm", allOf(
                hasProperty("id", is(7L)),
                hasProperty("holidayReplacements", contains(
                    hasProperty("person", hasProperty("id", is(42L))),
                    hasProperty("person", hasProperty("id", is(21L)))
                ))
            )))
            .andExpect(model().attribute("vacationTypeColors", equalTo(List.of(new VacationTypeDto(1L, ORANGE)))))
            .andExpect(view().name("application/application_form"));
    }

    @Test
    void ensureReplacementDeletionForEditedApplicationAddsTheRemovedPersonToSelectableHolidayReplacements() throws Exception {

        final Locale locale = GERMAN;
        final MessageSource messageSource = messageSourceForVacationType("message-key", "label", locale);
        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("message-key")
            .build();

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);

        final Person bruce = new Person();
        bruce.setId(42L);

        final Person clark = new Person();
        clark.setId(1337L);

        final Person joker = new Person();
        joker.setId(21L);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(personService.getActivePersons()).thenReturn(List.of(signedInPerson, bruce, clark, joker));

        final LocalDate now = LocalDate.now(clock);
        final Account account = new Account(signedInPerson, now, now, true, LocalDate.of(now.getYear(), APRIL, 1), ZERO, ZERO, ZERO, "");
        when(accountService.getHolidaysAccount(now.getYear(), signedInPerson)).thenReturn(Optional.of(account));

        when(settingsService.getSettings()).thenReturn(new Settings());
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        perform(
            post("/web/application/7/edit")
                .locale(locale)
                .param("vacationType.id", "1")
                .param("id", "7")
                .param("holidayReplacements[0].person.id", "42")
                .param("holidayReplacements[1].person.id", "1337")
                .param("holidayReplacements[2].person.id", "21")
                .param("remove-holiday-replacement", "1337")
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("applicationForLeaveForm", hasProperty("id", is(7L))))
            .andExpect(model().attribute("selectableHolidayReplacements", contains(
                hasProperty("personId", is(1337L))
            )))
            .andExpect(view().name("application/application_form"));
    }

    @Test
    void sendEditApplicationForm() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final Long applicationId = 1L;
        final Application application = new Application();
        application.setStatus(WAITING);
        application.setPerson(person);

        final Application editedApplication = new Application();
        editedApplication.setId(applicationId);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));
        when(applicationInteractionService.edit(eq(application), any(Application.class), eq(person), eq(Optional.of("comment")))).thenReturn(editedApplication);

        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType(1L)));

        perform(post("/web/application/1/edit")
            .param("person.id", "1")
            .param("startDate", "28.10.2020")
            .param("endDate", "28.10.2020")
            .param("vacationType.id", "1")
            .param("dayLength", "FULL")
            .param("comment", "comment")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/application/1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"25.03.2022", "25.03.22", "25.3.2022", "25.3.22", "1.4.22"})
    void ensureSendEditApplicationFormSucceedsForDateFormat(String givenDate) throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final Long applicationId = 1L;
        final Application application = new Application();
        application.setStatus(WAITING);
        application.setPerson(person);

        final Application editedApplication = new Application();
        editedApplication.setId(applicationId);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));
        when(applicationInteractionService.edit(eq(application), any(Application.class), eq(person), eq(Optional.of("comment")))).thenReturn(editedApplication);

        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType(1L)));

        perform(post("/web/application/1/edit")
            .param("person.id", "1")
            .param("startDate", givenDate)
            .param("endDate", givenDate)
            .param("vacationType.id", "1")
            .param("dayLength", "FULL")
            .param("comment", "comment")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/application/1"));
    }

    @Test
    void sendEditApplicationFormIsNotWaiting() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final Long applicationId = 1L;
        final Application application = new Application();
        application.setId(applicationId);
        application.setStatus(ALLOWED);
        application.setPerson(person);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));

        perform(post("/web/application/1/edit")
            .param("person.id", "1")
            .param("startDate", "28.10.2020")
            .param("endDate", "28.10.2020")
            .param("vacationType.id", "1")
            .param("dayLength", "FULL")
            .param("comment", "comment")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/application/1"))
            .andExpect(flash().attribute("editError", true));
    }

    @Test
    void sendEditApplicationFormApplicationNotFound() {

        final Long applicationId = 1L;
        final Application application = new Application();
        application.setId(applicationId);
        application.setStatus(WAITING);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            perform(post("/web/application/1/edit")
                .param("person.id", "1")
                .param("startDate", "28.10.2020")
                .param("endDate", "28.10.2020")
                .param("vacationType.id", "1")
                .param("dayLength", "FULL")
                .param("comment", "comment"))
        ).hasCauseInstanceOf(UnknownApplicationForLeaveException.class);
    }

    @Test
    void sendEditApplicationFormCannotBeEdited() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final Long applicationId = 1L;
        final Application application = new Application();
        application.setStatus(WAITING);
        application.setPerson(person);
        final Application editedApplication = new Application();
        editedApplication.setId(applicationId);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));
        when(applicationInteractionService.edit(eq(application), any(Application.class), eq(person), eq(Optional.of("comment")))).thenThrow(EditApplicationForLeaveNotAllowedException.class);

        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(anyVacationType(1L)));

        perform(post("/web/application/1/edit")
            .param("person.id", "1")
            .param("startDate", "28.10.2020")
            .param("endDate", "28.10.2020")
            .param("vacationType.id", "1")
            .param("dayLength", "FULL")
            .param("comment", "comment")
        )
            .andExpect(status().isOk())
            .andExpect(view().name("application/application-not-editable"));
    }

    @Test
    void sendEditApplicationFormHasErrors() throws Exception {

        final Locale locale = GERMAN;
        final MessageSource messageSource = messageSourceForVacationType("message-key", "label", locale);
        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("message-key")
            .build();

        final Person person = new Person();

        final Long applicationId = 1L;
        final Application application = new Application();
        application.setId(applicationId);
        application.setStatus(WAITING);
        application.setPerson(person);
        when(applicationInteractionService.get(applicationId)).thenReturn(Optional.of(application));
        when(personService.getSignedInUser()).thenReturn(person);

        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        doAnswer(invocation -> {
            final Errors errors = invocation.getArgument(1);
            errors.rejectValue("reason", "errors");
            errors.reject("globalErrors");
            return null;
        }).when(applicationForLeaveFormValidator).validate(any(), any());

        perform(
            post("/web/application/1/edit")
                .locale(locale)
                .param("person.id", "1")
                .param("startDate", "28.10.2020")
                .param("endDate", "28.10.2020")
                .param("vacationType.id", "1")
                .param("dayLength", "FULL")
                .param("comment", "comment")
        )
            .andExpect(status().isOk())
            .andExpect(view().name("application/application_form"))
            .andExpect(model().attribute("showHalfDayOption", is(true)));
    }

    @Test
    void ensureApplicationEditReplacementsAreStillDisplayedWhenValidationFails() throws Exception {
        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("startDate", "");
            return null;
        }).when(applicationForLeaveFormValidator).validate(any(), any());

        final Locale locale = GERMAN;
        final MessageSource messageSource = messageSourceForVacationType("message-key", "label", locale);
        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("message-key")
            .build();

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);
        signedInPerson.setPermissions(List.of(USER, OFFICE));

        final Person bruce = new Person();
        bruce.setId(42L);

        final Person clark = new Person();
        clark.setId(1337L);

        final Person joker = new Person();
        joker.setId(21L);

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(personService.getActivePersons()).thenReturn(List.of(signedInPerson, bruce, clark, joker));

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Application application = new Application();
        application.setId(7L);
        application.setPerson(signedInPerson);
        application.setApplier(signedInPerson);
        application.setStatus(WAITING);
        when(applicationInteractionService.get(7L)).thenReturn(Optional.of(application));

        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));

        perform(
            post("/web/application/7/edit")
                .locale(locale)
                .param("vacationType.id", "1")
                .param("person.id", "1")
                .param("id", "7")
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("applicationForLeaveForm", "startDate"))
            .andExpect(model().attribute("selectableHolidayReplacements", contains(
                hasProperty("personId", is(42L)),
                hasProperty("personId", is(1337L)),
                hasProperty("personId", is(21L))
            )))
            .andExpect(view().name("application/application_form"));
    }

    private Person personWithRole(Role... role) {
        final Person person = new Person();
        person.setPermissions(List.of(role));

        return person;
    }

    private Person personWithId(long id) {
        final Person person = new Person();
        person.setId(id);

        return person;
    }

    private Application someApplication() {

        final VacationType<?> holidayType = ProvidedVacationType.builder(new StaticMessageSource())
            .id(1L)
            .active(true)
            .category(HOLIDAY)
            .messageKey("application.data.vacationType.holiday")
            .requiresApprovalToApply(true)
            .requiresApprovalToCancel(true)
            .color(YELLOW)
            .visibleToEveryone(false)
            .build();

        final Application application = new Application();
        application.setVacationType(holidayType);
        application.setStartDate(LocalDate.now().plusDays(10));
        application.setEndDate(LocalDate.now().plusDays(20));

        return new Application();
    }

    private VacationType<?> anyVacationType(Long id) {
        return ProvidedVacationType.builder(new StaticMessageSource()).id(id).build();
    }

    private Application applicationWithId(long id) {
        final Application application = someApplication();
        application.setId(id);

        return application;
    }

    private MessageSource messageSourceForVacationType(String messageKey, String label, Locale locale) {
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(messageKey, new Object[]{}, locale)).thenReturn(label);
        return messageSource;
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
