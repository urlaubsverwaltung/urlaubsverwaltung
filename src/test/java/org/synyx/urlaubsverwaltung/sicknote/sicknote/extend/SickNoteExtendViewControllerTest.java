package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNotePermissionEvaluator;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import static java.time.Month.OCTOBER;
import static java.time.Month.SEPTEMBER;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_EDIT;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarFactory.workingTimeCalendarMondayToFriday;

@ExtendWith(MockitoExtension.class)
class SickNoteExtendViewControllerTest {

    private SickNoteExtendViewController sut;

    @Mock
    private PersonService personService;
    @Mock
    private WorkingTimeCalendarService workingTimeCalendarService;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private SickNoteExtensionServiceImpl sickNoteExtensionService;
    @Mock
    private SickNoteExtensionInteractionService sickNoteExtensionInteractionService;
    @Mock
    private SickNoteExtendValidator sickNoteExtendValidator;
    @Mock
    private DateFormatAware dateFormatAware;
    @Mock
    private PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy;
    @Mock
    private PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier;

    // 2024-09-25 is a wednesday
    private final Clock clock = Clock.fixed(Instant.parse("2024-09-25T00:00:00.00Z"), UTC);
    private final Settings settings = new Settings();

    @BeforeEach
    void setUp() {
        settings.getSickNoteSettings().setUserIsAllowedToSubmitSickNotes(true);
        sut = new SickNoteExtendViewController(personService, workingTimeCalendarService,
            sickNoteService, sickNoteExtensionService, sickNoteExtensionInteractionService,
            new SickNotePermissionEvaluator(mock(DepartmentService.class), settingsServiceWith(settings)),
            sickNoteExtendValidator, dateFormatAware, defaultPersonSuggestionUrlStrategy, personSearchUiFragmentSupplier,
            clock);
    }

    private static SettingsService settingsServiceWith(Settings settings) {
        final SettingsService settingsService = mock(SettingsService.class);
        lenient().when(settingsService.getSettings()).thenReturn(settings);
        return settingsService;
    }

    @Nested
    class PersonSearch {

        @Test
        void personSearchUiFragmentSupplier() {
            assertThat(sut.personSearchUiFragmentSupplier()).isSameAs(personSearchUiFragmentSupplier);
        }

        @Test
        void returnsInjectedStrategy() {
            assertThat(sut.personSuggestionUrlStrategy()).isSameAs(defaultPersonSuggestionUrlStrategy);
        }
    }

    @ParameterizedTest
    @EnumSource(value = DayLength.class, names = {"FULL", "ZERO"}, mode = EnumSource.Mode.EXCLUDE)
    void ensuresSickNoteExtendViewIsNotDisplayedForSickNotes(final DayLength dayLength) throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        when(personService.getSignedInUser()).thenReturn(person);

        final SickNote currentActiveSickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .dayLength(dayLength)
            .build();

        when(sickNoteService.getSickNoteToExtend(person))
            .thenReturn(Optional.of(currentActiveSickNote));

        perform(
            get("/web/sicknote/extend")
        )
            .andExpect(status().isOk())
            .andExpect(view().name("sicknote/sick_note_extended_not_found"));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "BOSS"})
    void ensureExtensionIsImmediatelyAcceptedWhenUserIsAllowedToAcceptSickNotes(Role role) throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role, SICK_NOTE_EDIT));

        when(personService.getSignedInUser()).thenReturn(person);

        final LocalDate endDate = LocalDate.of(2024, SEPTEMBER, 27);

        final SickNote currentActiveSickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .dayLength(FULL)
            .build();

        when(sickNoteService.getSickNoteToExtend(person))
            .thenReturn(Optional.of(currentActiveSickNote));

        perform(
            post("/web/sicknote/extend")
                .param("sickNoteId", "1")
                .param("endDate", "2024-09-27")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/sicknote/1"));

        verify(sickNoteExtensionInteractionService).submitSickNoteExtension(person, 1L, endDate);
        verify(sickNoteExtensionInteractionService).acceptSubmittedExtension(person, 1L, null);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "SICK_NOTE_ADD"})
    void ensureExtensionIsNotImmediatelyAcceptedWhenUserIsNotAllowedToAcceptSickNotes(Role role) throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));

        when(personService.getSignedInUser()).thenReturn(person);

        final LocalDate endDate = LocalDate.of(2024, SEPTEMBER, 27);

        final SickNote currentActiveSickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .dayLength(FULL)
            .build();

        when(sickNoteService.getSickNoteToExtend(person))
            .thenReturn(Optional.of(currentActiveSickNote));

        perform(
            post("/web/sicknote/extend")
                .param("sickNoteId", "1")
                .param("endDate", "2024-09-27")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/sicknote/1"));

        verify(sickNoteExtensionInteractionService).submitSickNoteExtension(person, 1L, endDate);
        verifyNoInteractions(sickNoteExtensionService);
    }

    @ParameterizedTest
    @EnumSource(value = DayLength.class, names = {"FULL", "ZERO"}, mode = EnumSource.Mode.EXCLUDE)
    void ensureExtensionDoesNoRedirectOnHalfDaySickNotes(final DayLength dayLength) throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, BOSS));

        when(personService.getSignedInUser()).thenReturn(person);

        final SickNote currentActiveSickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .dayLength(dayLength)
            .build();

        when(sickNoteService.getSickNoteToExtend(person))
            .thenReturn(Optional.of(currentActiveSickNote));

        perform(
            post("/web/sicknote/extend")
                .param("sickNoteId", "1")
                .param("endDate", "2024-09-27")
        )
            .andExpect(status().isOk())
            .andExpect(view().name("sicknote/sick_note_extended_not_found"));

        verifyNoInteractions(sickNoteExtensionService);
    }

    @Test
    void ensureExtendViewOffersTheEndOfWeekAndTodayForASickNoteThatHasEnded() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        when(personService.getSignedInUser()).thenReturn(person);
        sickNoteToExtend(person, LocalDate.of(2024, SEPTEMBER, 23), LocalDate.of(2024, SEPTEMBER, 24));

        perform(get("/web/sicknote/extend"))
            .andExpect(status().isOk())
            .andExpect(view().name("sicknote/sick_note_extend"))
            .andExpect(model().attribute("canExtendUntilEndOfWeek", true))
            .andExpect(model().attribute("earliestExtendToDate", LocalDate.of(2024, SEPTEMBER, 25)));
    }

    @Test
    void ensureExtendViewOffersNeitherTheEndOfWeekNorTodayForASickNoteRunningBeyondThisWeek() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        when(personService.getSignedInUser()).thenReturn(person);
        // the sick note runs until monday next week, the end of this week is behind its end date
        sickNoteToExtend(person, LocalDate.of(2024, SEPTEMBER, 23), LocalDate.of(2024, SEPTEMBER, 30));

        perform(get("/web/sicknote/extend"))
            .andExpect(status().isOk())
            .andExpect(view().name("sicknote/sick_note_extend"))
            .andExpect(model().attribute("canExtendUntilEndOfWeek", false))
            .andExpect(model().attribute("earliestExtendToDate", LocalDate.of(2024, OCTOBER, 1)));
    }

    private SickNote sickNoteToExtend(Person person, LocalDate startDate, LocalDate endDate) {

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SICK_NOTE);

        final SickNote sickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .dayLength(FULL)
            .startDate(startDate)
            .endDate(endDate)
            .sickNoteType(sickNoteType)
            .build();

        when(sickNoteService.getSickNoteToExtend(person)).thenReturn(Optional.of(sickNote));
        when(workingTimeCalendarService.getWorkingTimesByPersons(eq(List.of(person)), any(DateRange.class)))
            .thenReturn(Map.of(person, workingTimeCalendarMondayToFriday(startDate.minusDays(7), endDate.plusDays(14))));

        return sickNote;
    }

    @Test
    void ensureExtendViewIsForbiddenWhenSubmissionOfSickNotesIsDisabled() {

        settings.getSickNoteSettings().setUserIsAllowedToSubmitSickNotes(false);

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        when(personService.getSignedInUser()).thenReturn(person);

        assertThatThrownBy(() ->
            perform(get("/web/sicknote/extend"))
        ).hasCauseInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(sickNoteService);
    }

    @Test
    void ensureExtendingSickNoteIsForbiddenWhenSubmissionOfSickNotesIsDisabled() {

        settings.getSickNoteSettings().setUserIsAllowedToSubmitSickNotes(false);

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        when(personService.getSignedInUser()).thenReturn(person);

        assertThatThrownBy(() ->
            perform(
                post("/web/sicknote/extend")
                    .param("sickNoteId", "1")
                    .param("endDate", "2024-09-27")
            )
        ).hasCauseInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(sickNoteExtensionInteractionService);
    }

    @Test
    void ensurePreviewOfACustomDateIsTheDateItself() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        when(personService.getSignedInUser()).thenReturn(person);
        sickNoteToExtend(person, LocalDate.of(2024, SEPTEMBER, 23), LocalDate.of(2024, SEPTEMBER, 24));

        perform(
            post("/web/sicknote/extend")
                .param("sickNoteId", "1")
                .param("startDate", "2024-09-23")
                .param("extendToDate", "2024-09-27")
                .param("custom-date-preview", "")
        )
            .andExpect(status().isOk())
            .andExpect(view().name("sicknote/sick_note_extend"))
            .andExpect(model().attribute("selectedExtend", "custom"))
            // monday to friday, all of them work days
            .andExpect(model().attribute("sickNotePreviewNext", new SickNoteExtendPreviewDto(
                LocalDate.of(2024, SEPTEMBER, 23), LocalDate.of(2024, SEPTEMBER, 27), BigDecimal.valueOf(5))));
    }

    @Test
    void ensurePreviewOfAQuickSelectionIsTheNextWorkDay() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        when(personService.getSignedInUser()).thenReturn(person);
        sickNoteToExtend(person, LocalDate.of(2024, SEPTEMBER, 23), LocalDate.of(2024, SEPTEMBER, 24));

        perform(
            post("/web/sicknote/extend")
                .param("sickNoteId", "1")
                .param("startDate", "2024-09-23")
                // the datepicker submits its date with every submit, a quick selection must not use it
                .param("extendToDate", "2024-09-27")
                .param("extend", "1")
        )
            .andExpect(status().isOk())
            .andExpect(view().name("sicknote/sick_note_extend"))
            .andExpect(model().attribute("selectedExtend", "1"))
            // monday to wednesday, the work day following the end of the sick note
            .andExpect(model().attribute("sickNotePreviewNext", new SickNoteExtendPreviewDto(
                LocalDate.of(2024, SEPTEMBER, 23), LocalDate.of(2024, SEPTEMBER, 25), BigDecimal.valueOf(3))));
    }

    @Test
    void ensurePreviewWithoutACustomDateRendersThePageAgain() throws Exception {

        // the real validator, the page fails on the way into it, see #6489
        final SickNoteExtendViewController sutWithValidator = new SickNoteExtendViewController(personService,
            workingTimeCalendarService, sickNoteService, sickNoteExtensionService, sickNoteExtensionInteractionService,
            new SickNotePermissionEvaluator(mock(DepartmentService.class), settingsServiceWith(settings)),
            new SickNoteExtendValidator(sickNoteService, dateFormatAware), dateFormatAware,
            defaultPersonSuggestionUrlStrategy, personSearchUiFragmentSupplier, clock);

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        when(personService.getSignedInUser()).thenReturn(person);
        final SickNote sickNote = sickNoteToExtend(person, LocalDate.of(2024, SEPTEMBER, 23), LocalDate.of(2024, SEPTEMBER, 24));
        when(sickNoteService.getById(1L)).thenReturn(Optional.of(sickNote));

        standaloneSetup(sutWithValidator).build()
            .perform(
                post("/web/sicknote/extend")
                    .param("sickNoteId", "1")
                    // the preview button of the custom date, submitted with an empty date field
                    .param("custom-date-preview", "")
            )
            .andExpect(status().isOk())
            .andExpect(view().name("sicknote/sick_note_extend"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
