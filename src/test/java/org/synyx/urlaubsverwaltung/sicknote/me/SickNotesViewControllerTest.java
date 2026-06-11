package org.synyx.urlaubsverwaltung.sicknote.me;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.search.SearchContext;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.sicknote.me.SickNotesViewController.MY_SICKNOTES_ANONYMOUS_PATH;
import static org.synyx.urlaubsverwaltung.sicknote.me.SickNotesViewController.MY_SICKNOTES_PATH;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.SUBMITTED;

@ExtendWith(MockitoExtension.class)
class SickNotesViewControllerTest {

    private SickNotesViewController sut;

    @Mock
    private PersonService personService;
    @Mock
    private WorkDaysCountService workDaysCountService;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private SettingsService settingsService;
    @Mock
    private PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier;

    private final Clock clock = Clock.fixed(ZonedDateTime.of(LocalDate.of(2022, 6, 15).atStartOfDay(), ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    @BeforeEach
    void setUp() {
        sut = new SickNotesViewController(personService, workDaysCountService, sickNoteService, departmentService,
            settingsService, personSearchUiFragmentSupplier, clock);
    }

    @Nested
    class PersonSearch {

        @Test
        void personSearchUiFragmentSupplier() {
            assertThat(sut.personSearchUiFragmentSupplier()).isSameAs(personSearchUiFragmentSupplier);
        }

        @Test
        void linksToSickNotesOfPerson() {

            final Person suggestion = new Person();
            suggestion.setId(42L);

            final PersonSuggestionUrlStrategy strategy = sut.personSuggestionUrlStrategy();

            final String actual = strategy.buildSuggestionMainLink(suggestion, searchContext());
            assertThat(actual).isEqualTo("/web/persons/42/sicknotes");
        }

        @Test
        void preservesYear() {

            final MockHttpServletRequest request = new MockHttpServletRequest();
            request.setParameter("year", "2026");

            final Person suggestion = new Person();
            suggestion.setId(42L);

            final PersonSuggestionUrlStrategy strategy = sut.personSuggestionUrlStrategy();

            final String actual = strategy.buildSuggestionMainLink(suggestion, searchContext(request));
            assertThat(actual).isEqualTo("/web/persons/42/sicknotes?year=2026");
        }

        @Test
        void ignoresBlankYear() {

            final MockHttpServletRequest request = new MockHttpServletRequest();
            request.setParameter("year", "  ");

            final Person suggestion = new Person();
            suggestion.setId(42L);

            final PersonSuggestionUrlStrategy strategy = sut.personSuggestionUrlStrategy();

            final String actual = strategy.buildSuggestionMainLink(suggestion, searchContext(request));
            assertThat(actual).isEqualTo("/web/persons/42/sicknotes");
        }
    }

    @Test
    void showMySickNotesAnonymousRedirectsToPersonSickNotesWithoutYear() throws Exception {
        final Person signedIn = new Person();
        signedIn.setId(11L);
        when(personService.getSignedInUser()).thenReturn(signedIn);

        perform(get(MY_SICKNOTES_ANONYMOUS_PATH))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/persons/11/sicknotes"));
    }

    @Test
    void showMySickNotesAnonymousRedirectsToPersonSickNotesWithYear() throws Exception {
        final Person signedIn = new Person();
        signedIn.setId(12L);
        when(personService.getSignedInUser()).thenReturn(signedIn);

        perform(get(MY_SICKNOTES_ANONYMOUS_PATH).param("year", "2020"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/persons/12/sicknotes?year=2020"));
    }

    @Test
    void showMySickNotesForPersonShowsViewAndEmptyList() throws Exception {
        final Person person = new Person();
        person.setId(5L);

        when(personService.getPersonByID(5L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "5")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("person", equalTo(person)))
            .andExpect(model().attribute("sickNotes", hasSize(0)));
    }

    @Test
    void showMySickNotesForPersonWithSickNotesProvidesSummary() throws Exception {
        final Person person = new Person();
        person.setId(6L);

        when(personService.getPersonByID(6L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(settingsService.getSettings()).thenReturn(new Settings());

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setId(1L);
        sickNoteType.setCategory(SICK_NOTE);
        sickNoteType.setMessageKey("key");

        final SickNote sickNote = SickNote.builder()
            .id(42L)
            .person(person)
            .startDate(LocalDate.of(2022, 1, 2))
            .endDate(LocalDate.of(2022, 1, 4))
            .dayLength(FULL)
            .sickNoteType(sickNoteType)
            .status(ACTIVE)
            .build();

        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(sickNote));
        when(workDaysCountService.getWorkDaysCount(any(), any(LocalDate.class), any(LocalDate.class), eq(person))).thenReturn(ONE);

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "6")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("sickNotes", hasSize(1)))
            .andExpect(model().attributeExists("sickDaysOverview"))
            .andExpect(model().attribute("sickNotes",
                hasItem(hasProperty("id", is(42L)))));
    }

    @Test
    void ensureThatUserIsAllowedToSubmitSickNotesIsSetToTrue() throws Exception {
        final Person person = new Person();
        person.setId(7L);

        when(personService.getPersonByID(7L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());

        final Settings settings = new Settings();
        final SickNoteSettings sickNoteSettings = new SickNoteSettings();
        sickNoteSettings.setUserIsAllowedToSubmitSickNotes(true);
        settings.setSickNoteSettings(sickNoteSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "7")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("userIsAllowedToSubmitSickNotes", true));
    }

    @Test
    void ensureThatUserIsAllowedToSubmitSickNotesIsSetToFalse() throws Exception {
        final Person person = new Person();
        person.setId(8L);

        when(personService.getPersonByID(8L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());

        final Settings settings = new Settings();
        final SickNoteSettings sickNoteSettings = new SickNoteSettings();
        sickNoteSettings.setUserIsAllowedToSubmitSickNotes(false);
        settings.setSickNoteSettings(sickNoteSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "8")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("userIsAllowedToSubmitSickNotes", false));
    }

    @Test
    void ensureCurrentYearAndSelectedYearAreSet() throws Exception {
        final Person person = new Person();
        person.setId(10L);

        when(personService.getPersonByID(10L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "10")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("currentYear", 2022))
            .andExpect(model().attribute("selectedYear", 2022));
    }

    @Test
    void ensureSelectedYearIsSetToRequestParamYear() throws Exception {
        final Person person = new Person();
        person.setId(10L);

        when(personService.getPersonByID(10L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "10")).param("year", "2020"))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("currentYear", 2022))
            .andExpect(model().attribute("selectedYear", 2020));
    }

    @Test
    void ensureSignedInUserIsSetInModel() throws Exception {
        final Person person = new Person();
        person.setId(10L);

        when(personService.getPersonByID(10L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "10")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("signedInUser", equalTo(person)));
    }

    @Test
    void ensureDepartmentsOfPersonAreSetInModel() throws Exception {
        final Person person = new Person();
        person.setId(10L);

        when(personService.getPersonByID(10L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "10")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attributeExists("departmentsOfPerson"));
    }

    @Test
    void ensureCanViewSickNoteAnotherUserIsTrueForOffice() throws Exception {
        final Person person = new Person();
        person.setId(20L);

        final Person signedInUser = new Person();
        signedInUser.setId(21L);
        signedInUser.setUsername("office");
        signedInUser.setPermissions(List.of(Role.OFFICE));

        when(personService.getPersonByID(20L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "20")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("canViewSickNoteAnotherUser", true));
    }

    @Test
    void ensureCanViewSickNoteAnotherUserIsTrueForSickNoteViewRoleWithBoss() throws Exception {
        final Person person = new Person();
        person.setId(20L);

        final Person signedInUser = new Person();
        signedInUser.setId(21L);
        signedInUser.setUsername("sicknoteviewer");
        signedInUser.setPermissions(List.of(Role.SICK_NOTE_VIEW, Role.BOSS));

        when(personService.getPersonByID(20L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "20")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("canViewSickNoteAnotherUser", true));
    }

    @Test
    void ensureCanViewSickNoteAnotherUserIsFalseForSickNoteViewRoleWithoutBossOrDepartmentHead() throws Exception {
        final Person person = new Person();
        person.setId(20L);

        final Person signedInUser = new Person();
        signedInUser.setId(21L);
        signedInUser.setUsername("sicknoteviewer");
        signedInUser.setPermissions(List.of(Role.SICK_NOTE_VIEW));

        when(personService.getPersonByID(20L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, person)).thenReturn(false);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, person)).thenReturn(false);
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "20")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("canViewSickNoteAnotherUser", false));
    }

    @Test
    void ensureCanViewSickNoteAnotherUserIsTrueForDepartmentHead() throws Exception {
        final Person person = new Person();
        person.setId(20L);

        final Person signedInUser = new Person();
        signedInUser.setId(21L);
        signedInUser.setUsername("departhead");
        signedInUser.setPermissions(List.of(Role.DEPARTMENT_HEAD));

        when(personService.getPersonByID(20L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, person)).thenReturn(true);
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "20")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("canViewSickNoteAnotherUser", true));
    }

    @Test
    void ensureCanViewSickNoteAnotherUserIsTrueForSecondStageAuthority() throws Exception {
        final Person person = new Person();
        person.setId(20L);

        final Person signedInUser = new Person();
        signedInUser.setId(21L);
        signedInUser.setUsername("secondstage");
        signedInUser.setPermissions(List.of(Role.SECOND_STAGE_AUTHORITY));

        when(personService.getPersonByID(20L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, person)).thenReturn(false);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, person)).thenReturn(true);
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "20")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("canViewSickNoteAnotherUser", true));
    }

    @Test
    void ensureCanViewSickNoteAnotherUserIsFalseForRegularUser() throws Exception {
        final Person person = new Person();
        person.setId(20L);

        final Person signedInUser = new Person();
        signedInUser.setId(21L);
        signedInUser.setUsername("user");
        signedInUser.setPermissions(List.of(Role.USER));

        when(personService.getPersonByID(20L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, person)).thenReturn(false);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, person)).thenReturn(false);
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "20")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("canViewSickNoteAnotherUser", false));
    }

    @Test
    void ensureSickNoteAllowedToEditIsTrueForSamePersonWithSubmittedStatus() throws Exception {
        final Person person = new Person();
        person.setId(30L);

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setId(1L);
        sickNoteType.setCategory(SICK_NOTE);
        sickNoteType.setMessageKey("key");

        final SickNote sickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .startDate(LocalDate.of(2022, 1, 2))
            .endDate(LocalDate.of(2022, 1, 4))
            .dayLength(FULL)
            .sickNoteType(sickNoteType)
            .status(SUBMITTED)
            .build();

        when(personService.getPersonByID(30L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(sickNote));
        when(workDaysCountService.getWorkDaysCount(any(), any(LocalDate.class), any(LocalDate.class), eq(person))).thenReturn(ONE);
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "30")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("sickNotes", hasSize(1)))
            .andExpect(model().attribute("sickNotes",
                hasItem(hasProperty("allowedToEdit", is(true)))));
    }

    @Test
    void ensureSickNoteAllowedToEditIsFalseForSamePersonWithActiveStatus() throws Exception {
        final Person person = new Person();
        person.setId(31L);

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setId(1L);
        sickNoteType.setCategory(SICK_NOTE);
        sickNoteType.setMessageKey("key");

        final SickNote sickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .startDate(LocalDate.of(2022, 1, 2))
            .endDate(LocalDate.of(2022, 1, 4))
            .dayLength(FULL)
            .sickNoteType(sickNoteType)
            .status(ACTIVE)
            .build();

        when(personService.getPersonByID(31L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(sickNote));
        when(workDaysCountService.getWorkDaysCount(any(), any(LocalDate.class), any(LocalDate.class), eq(person))).thenReturn(ONE);
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "31")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("sickNotes", hasSize(1)))
            .andExpect(model().attribute("sickNotes",
                hasItem(hasProperty("allowedToEdit", is(false)))));
    }

    @Test
    void ensureSickNoteAllowedToEditIsTrueForOfficeUser() throws Exception {
        final Person person = new Person();
        person.setId(32L);

        final Person signedInUser = new Person();
        signedInUser.setId(33L);
        signedInUser.setPermissions(List.of(Role.OFFICE));

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setId(1L);
        sickNoteType.setCategory(SICK_NOTE);
        sickNoteType.setMessageKey("key");

        final SickNote sickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .startDate(LocalDate.of(2022, 1, 2))
            .endDate(LocalDate.of(2022, 1, 4))
            .dayLength(FULL)
            .sickNoteType(sickNoteType)
            .status(ACTIVE)
            .build();

        when(personService.getPersonByID(32L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(sickNote));
        when(workDaysCountService.getWorkDaysCount(any(), any(LocalDate.class), any(LocalDate.class), eq(person))).thenReturn(ONE);
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "32")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("sickNotes", hasSize(1)))
            .andExpect(model().attribute("sickNotes",
                hasItem(hasProperty("allowedToEdit", is(true)))));
    }

    @Test
    void ensureSickNoteAllowedToEditIsTrueForSickNoteEditRoleWithBoss() throws Exception {
        final Person person = new Person();
        person.setId(34L);

        final Person signedInUser = new Person();
        signedInUser.setId(35L);
        signedInUser.setPermissions(List.of(Role.SICK_NOTE_EDIT, Role.BOSS));

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setId(1L);
        sickNoteType.setCategory(SICK_NOTE);
        sickNoteType.setMessageKey("key");

        final SickNote sickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .startDate(LocalDate.of(2022, 1, 2))
            .endDate(LocalDate.of(2022, 1, 4))
            .dayLength(FULL)
            .sickNoteType(sickNoteType)
            .status(ACTIVE)
            .build();

        when(personService.getPersonByID(34L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, person)).thenReturn(false);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, person)).thenReturn(false);
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(sickNote));
        when(workDaysCountService.getWorkDaysCount(any(), any(LocalDate.class), any(LocalDate.class), eq(person))).thenReturn(ONE);
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "34")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("sickNotes", hasSize(1)))
            .andExpect(model().attribute("sickNotes",
                hasItem(hasProperty("allowedToEdit", is(true)))));
    }

    @Test
    void ensureSickNoteAllowedToCancelIsTrueForOfficeUser() throws Exception {
        final Person person = new Person();
        person.setId(36L);

        final Person signedInUser = new Person();
        signedInUser.setId(37L);
        signedInUser.setPermissions(List.of(Role.OFFICE));

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setId(1L);
        sickNoteType.setCategory(SICK_NOTE);
        sickNoteType.setMessageKey("key");

        final SickNote sickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .startDate(LocalDate.of(2022, 1, 2))
            .endDate(LocalDate.of(2022, 1, 4))
            .dayLength(FULL)
            .sickNoteType(sickNoteType)
            .status(ACTIVE)
            .build();

        when(personService.getPersonByID(36L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(sickNote));
        when(workDaysCountService.getWorkDaysCount(any(), any(LocalDate.class), any(LocalDate.class), eq(person))).thenReturn(ONE);
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "36")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("sickNotes", hasSize(1)))
            .andExpect(model().attribute("sickNotes",
                hasItem(hasProperty("allowedToCancel", is(true)))));
    }

    @Test
    void ensureSickNoteAllowedToCancelIsTrueForSickNoteCancelRoleWithBoss() throws Exception {
        final Person person = new Person();
        person.setId(38L);

        final Person signedInUser = new Person();
        signedInUser.setId(39L);
        signedInUser.setPermissions(List.of(Role.SICK_NOTE_CANCEL, Role.BOSS));

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setId(1L);
        sickNoteType.setCategory(SICK_NOTE);
        sickNoteType.setMessageKey("key");

        final SickNote sickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .startDate(LocalDate.of(2022, 1, 2))
            .endDate(LocalDate.of(2022, 1, 4))
            .dayLength(FULL)
            .sickNoteType(sickNoteType)
            .status(ACTIVE)
            .build();

        when(personService.getPersonByID(38L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, person)).thenReturn(false);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, person)).thenReturn(false);
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(sickNote));
        when(workDaysCountService.getWorkDaysCount(any(), any(LocalDate.class), any(LocalDate.class), eq(person))).thenReturn(ONE);
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "38")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("sickNotes", hasSize(1)))
            .andExpect(model().attribute("sickNotes",
                hasItem(hasProperty("allowedToCancel", is(true)))));
    }

    @Test
    void ensureSickNoteAllowedToCancelIsFalseWithoutPermission() throws Exception {
        final Person person = new Person();
        person.setId(40L);

        final Person signedInUser = new Person();
        signedInUser.setId(41L);
        signedInUser.setPermissions(List.of(Role.USER));

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setId(1L);
        sickNoteType.setCategory(SICK_NOTE);
        sickNoteType.setMessageKey("key");

        final SickNote sickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .startDate(LocalDate.of(2022, 1, 2))
            .endDate(LocalDate.of(2022, 1, 4))
            .dayLength(FULL)
            .sickNoteType(sickNoteType)
            .status(ACTIVE)
            .build();

        when(personService.getPersonByID(40L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, person)).thenReturn(false);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, person)).thenReturn(false);
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(sickNote));
        when(workDaysCountService.getWorkDaysCount(any(), any(LocalDate.class), any(LocalDate.class), eq(person))).thenReturn(ONE);
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "40")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("sickNotes", hasSize(1)))
            .andExpect(model().attribute("sickNotes",
                hasItem(hasProperty("allowedToCancel", is(false)))));
    }

    @Test
    void ensureSickNotesAreSortedByStartDateDescending() throws Exception {
        final Person person = new Person();
        person.setId(42L);

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setId(1L);
        sickNoteType.setCategory(SICK_NOTE);
        sickNoteType.setMessageKey("key");

        final SickNote sickNote1 = SickNote.builder()
            .id(1L)
            .person(person)
            .startDate(LocalDate.of(2022, 3, 1))
            .endDate(LocalDate.of(2022, 3, 3))
            .dayLength(FULL)
            .sickNoteType(sickNoteType)
            .status(ACTIVE)
            .build();

        final SickNote sickNote2 = SickNote.builder()
            .id(2L)
            .person(person)
            .startDate(LocalDate.of(2022, 1, 1))
            .endDate(LocalDate.of(2022, 1, 3))
            .dayLength(FULL)
            .sickNoteType(sickNoteType)
            .status(ACTIVE)
            .build();

        when(personService.getPersonByID(42L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(sickNote2, sickNote1));
        when(workDaysCountService.getWorkDaysCount(any(), any(LocalDate.class), any(LocalDate.class), eq(person))).thenReturn(ONE);
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "42")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("sickNotes", hasSize(2)))
            .andExpect(model().attribute("sickNotes",
                hasItem(hasProperty("id", is(1L)))));
    }

    @Test
    void ensureSickNoteDtoContainsCorrectAttributes() throws Exception {
        final Person person = new Person();
        person.setId(43L);

        final Person signedInUser = new Person();
        signedInUser.setId(44L);
        signedInUser.setPermissions(List.of(Role.OFFICE));

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setId(1L);
        sickNoteType.setCategory(SICK_NOTE);
        sickNoteType.setMessageKey("key");

        final SickNote sickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .startDate(LocalDate.of(2022, 1, 2))
            .endDate(LocalDate.of(2022, 1, 4))
            .dayLength(FULL)
            .sickNoteType(sickNoteType)
            .status(ACTIVE)
            .build();

        when(personService.getPersonByID(43L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(sickNote));
        when(workDaysCountService.getWorkDaysCount(any(), any(LocalDate.class), any(LocalDate.class), eq(person))).thenReturn(ONE);
        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "43")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("sickNotes", hasSize(1)))
            .andExpect(model().attribute("sickNotes",
                hasItems(
                    hasProperty("id", is(1L)),
                    hasProperty("startDate", is(LocalDate.of(2022, 1, 2))),
                    hasProperty("endDate", is(LocalDate.of(2022, 1, 4))),
                    hasProperty("dayLength", is(FULL)),
                    hasProperty("status", is(ACTIVE)),
                    hasProperty("allowedToEdit", is(true)),
                    hasProperty("allowedToCancel", is(true))
                )));
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
