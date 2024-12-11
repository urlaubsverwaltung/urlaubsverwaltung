package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

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

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new SickNoteExtendViewController(personService, workingTimeCalendarService,
            sickNoteService, sickNoteExtensionService,
            sickNoteExtensionInteractionService,
            sickNoteExtendValidator, dateFormatAware, clock);
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

        when(sickNoteService.getSickNoteOfYesterdayOrLastWorkDay(person))
            .thenReturn(Optional.of(currentActiveSickNote));

        perform(
            get("/web/sicknote/extend")
        )
            .andExpect(status().isOk())
            .andExpect(view().name("sicknote/sick_note_extended_not_found"));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "SICK_NOTE_ADD"})
    void ensureExtensionIsImmediatelyAcceptedWhenUserHasRole(Role role) throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, role));

        when(personService.getSignedInUser()).thenReturn(person);

        final LocalDate endDate = LocalDate.of(2024, 9, 27);

        final SickNote currentActiveSickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .dayLength(FULL)
            .build();

        when(sickNoteService.getSickNoteOfYesterdayOrLastWorkDay(person))
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

    @Test
    void ensureExtensionIsNotImmediatelyAcceptedForSimpleUserOrBoss() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER, BOSS));

        when(personService.getSignedInUser()).thenReturn(person);

        final LocalDate endDate = LocalDate.of(2024, 9, 27);

        final SickNote currentActiveSickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .dayLength(FULL)
            .build();

        when(sickNoteService.getSickNoteOfYesterdayOrLastWorkDay(person))
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

        when(sickNoteService.getSickNoteOfYesterdayOrLastWorkDay(person))
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

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
