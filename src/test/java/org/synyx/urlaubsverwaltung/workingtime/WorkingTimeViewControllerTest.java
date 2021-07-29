package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.workingtime.settings.WorkingTimeSettings;
import org.synyx.urlaubsverwaltung.workingtime.settings.WorkingTimeSettingsService;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.DayOfWeek.MONDAY;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
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
    private WorkingTimeWriteService workingTimeWriteService;
    @Mock
    private WorkingTimeSettingsService settingsService;
    @Mock
    private WorkingTimeValidator validator;

    @BeforeEach
    void setUp() {
        sut = new WorkingTimeViewController(personService, workingTimeService, workingTimeWriteService, settingsService, validator, Clock.systemUTC());
    }

    @Test
    void editGetWorkingTimeForUnknownPersonIdThrowsUnknownPersonException() {
        assertThatThrownBy(() ->
            perform(get("/web/person/" + UNKNOWN_PERSON_ID + "/workingtime"))
        ).hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    void editWorkingTimePresetsFormWithCorrectAttributes() throws Exception {
        when(settingsService.getSettings()).thenReturn(new WorkingTimeSettings());

        final Person person = new Person();
        when(personService.getPersonByID(KNOWN_PERSON_ID)).thenReturn(Optional.of(person));

        final WorkingTime workingTime = new WorkingTime(person, LocalDate.of(2020,10,2), BERLIN);
        workingTime.setWorkingDays(List.of(MONDAY), DayLength.FULL);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class))).thenReturn(Optional.of(workingTime));
        when(workingTimeService.getByPerson(person)).thenReturn(List.of(workingTime));

        perform(get("/web/person/" + KNOWN_PERSON_ID + "/workingtime"))
            .andExpect(model().attribute("workingTime", equalTo(new WorkingTimeForm(workingTime))))
            .andExpect(model().attribute("workingTimeHistories", hasItem(hasProperty("valid", equalTo(true)))))
            .andExpect(model().attribute("workingTimeHistories", hasItem(hasProperty("federalState", equalTo("BERLIN")))))
            .andExpect(model().attribute("workingTimeHistories", hasItem(hasProperty("validFrom", equalTo(LocalDate.of(2020,10,2))))))
            .andExpect(model().attribute("workingTimeHistories", hasItem(hasProperty("workingDays", hasItem("MONDAY")))))
            .andExpect(model().attribute("defaultFederalState", equalTo(FederalState.BADEN_WUERTTEMBERG)))
            .andExpect(model().attribute("federalStateTypes", equalTo(FederalState.values())))
            .andExpect(model().attribute("weekDays", equalTo(DayOfWeek.values())));

    }

    @Test
    void editGetWorkingTimeCreatesEmptyFormIfNoExistingWorkingTimeForPerson() throws Exception {

        final Person person = new Person();
        when(personService.getPersonByID(KNOWN_PERSON_ID)).thenReturn(Optional.of(person));

        when(settingsService.getSettings()).thenReturn(new WorkingTimeSettings());
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class))).thenReturn(Optional.empty());

        perform(get("/web/person/" + KNOWN_PERSON_ID + "/workingtime"))
            .andExpect(model().attribute("workingTime", equalTo(new WorkingTimeForm())));
    }

    @Test
    void editGetWorkingTimeUsesCorrectView() throws Exception {

        when(settingsService.getSettings()).thenReturn(new WorkingTimeSettings());
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

        when(settingsService.getSettings()).thenReturn(new WorkingTimeSettings());
        when(personService.getPersonByID(KNOWN_PERSON_ID)).thenReturn(Optional.of(new Person()));

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("validFrom", "errors");
            return null;
        }).when(validator).validate(any(), any());

        perform(post("/web/person/" + KNOWN_PERSON_ID + "/workingtime"))
            .andExpect(view().name("workingtime/workingtime_form"));

        verify(workingTimeWriteService, never()).touch(any(), any(), any(), any());
    }

    @Test
    void updatePostWorkingTimeTouchPersonIfValidationSuccessful() throws Exception {

        final Person person = new Person();
        when(personService.getPersonByID(KNOWN_PERSON_ID)).thenReturn(Optional.of(person));

        perform(post("/web/person/" + KNOWN_PERSON_ID + "/workingtime"));

        verify(workingTimeWriteService).touch(any(), any(), eq(person), any());
    }

    @Test
    void updatePostWorkingTimeAddsFlashAttributeAndRedirectsToPerson() throws Exception {

        when(personService.getPersonByID(KNOWN_PERSON_ID)).thenReturn(Optional.of(new Person()));

        perform(post("/web/person/" + KNOWN_PERSON_ID + "/workingtime"))
            .andExpect(flash().attribute("updateSuccess", true))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/person/" + KNOWN_PERSON_ID));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
