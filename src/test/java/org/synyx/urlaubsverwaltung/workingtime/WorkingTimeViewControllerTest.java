package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.BERLIN;

@ExtendWith(MockitoExtension.class)
class WorkingTimeViewControllerTest {

    private WorkingTimeViewController sut;

    private static final int KNOWN_PERSON_ID = 1;
    private static final int UNKNOWN_PERSON_ID = 217;

    @Mock
    private PersonService personService;
    @Mock
    private WorkingTimeService workingTimeService;
    @Mock
    private SettingsService settingsService;
    @Mock
    private WorkingTimeValidator validator;

    @BeforeEach
    void setUp() {
        sut = new WorkingTimeViewController(personService, workingTimeService, settingsService, validator, Clock.systemUTC());
    }

    @Test
    void editGetWorkingTimeForUnknownPersonIdThrowsUnknownPersonException() {
        assertThatThrownBy(() ->
            perform(get("/web/person/" + UNKNOWN_PERSON_ID + "/workingtime"))
        ).hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    void editWorkingTimePresetsFormWithExistingWorkingTimeForPerson() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person();
        when(personService.getPersonByID(KNOWN_PERSON_ID)).thenReturn(Optional.of(person));

        final WorkingTime workingTime = someWorkingTimeOfPerson(person);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class))).thenReturn(Optional.of(workingTime));

        perform(get("/web/person/" + KNOWN_PERSON_ID + "/workingtime"))
            .andExpect(model().attribute("workingTime", equalTo(new WorkingTimeForm(workingTime))));
    }

    @Test
    void editGetWorkingTimeCreatesEmptyFormIfNoExistingWorkingTimeForPerson() throws Exception {

        final Person person = new Person();
        when(personService.getPersonByID(KNOWN_PERSON_ID)).thenReturn(Optional.of(person));

        when(settingsService.getSettings()).thenReturn(new Settings());
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class))).thenReturn(Optional.empty());

        perform(get("/web/person/" + KNOWN_PERSON_ID + "/workingtime"))
            .andExpect(model().attribute("workingTime", equalTo(new WorkingTimeForm())));
    }

    @Test
    void editGetWorkingTimeUsesCorrectView() throws Exception {

        when(settingsService.getSettings()).thenReturn(new Settings());
        when(personService.getPersonByID(KNOWN_PERSON_ID)).thenReturn(Optional.of(new Person()));

        perform(get("/web/person/" + KNOWN_PERSON_ID + "/workingtime"))
            .andExpect(view().name("workingtime/workingtime_form"));
    }

    @Test
    void updatePostWorkingTimeForUnknownPersonIdThrowsUnknownPersonException() {
        assertThatThrownBy(() ->
            perform(post("/web/person/" + UNKNOWN_PERSON_ID + "/workingtime"))
        ).hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    void updatePostWorkingTimeShowsFormIfValidationFails() throws Exception {

        when(settingsService.getSettings()).thenReturn(new Settings());
        when(personService.getPersonByID(KNOWN_PERSON_ID)).thenReturn(Optional.of(new Person()));

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("validFrom", "errors");
            return null;
        }).when(validator).validate(any(), any());

        perform(post("/web/person/" + KNOWN_PERSON_ID + "/workingtime"))
            .andExpect(view().name("workingtime/workingtime_form"));

        verify(workingTimeService, never()).touch(any(), any(), any(), any());
    }

    @Test
    void updatePostWorkingTimeTouchPersonIfValidationSuccessful() throws Exception {

        final Person person = new Person();
        when(personService.getPersonByID(KNOWN_PERSON_ID)).thenReturn(Optional.of(person));

        perform(post("/web/person/" + KNOWN_PERSON_ID + "/workingtime"));

        verify(workingTimeService).touch(any(), any(), any(), eq(person));
    }

    @Test
    void updatePostWorkingTimeAddsFlashAttributeAndRedirectsToPerson() throws Exception {

        when(personService.getPersonByID(KNOWN_PERSON_ID)).thenReturn(Optional.of(new Person()));

        perform(post("/web/person/" + KNOWN_PERSON_ID + "/workingtime"))
            .andExpect(flash().attribute("updateSuccess", true))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/person/" + KNOWN_PERSON_ID));
    }

    private WorkingTime someWorkingTimeOfPerson(final Person person) {

        WorkingTime workingTime = new WorkingTime();
        workingTime.setPerson(person);
        workingTime.setValidFrom(LocalDate.now().minusDays(10));
        workingTime.setFederalStateOverride(BERLIN);

        return workingTime;
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
