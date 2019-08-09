package org.synyx.urlaubsverwaltung.workingtime.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.settings.FederalState.BERLIN;

@RunWith(MockitoJUnitRunner.class)
public class WorkingTimeViewControllerTest {

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

    @Before
    public void setUp() {

        sut = new WorkingTimeViewController(personService, workingTimeService, settingsService, validator);

        when(settingsService.getSettings()).thenReturn(new Settings());
    }

    @Test
    public void editGetWorkingTimeForUnknownPersonIdThrowsUnknownPersonException() {

        assertThatThrownBy(() ->

            perform(get("/web/person/" + UNKNOWN_PERSON_ID + "/workingtime"))

        ).hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    public void editWorkingTimePresetsFormWithExistingWorkingTimeForPerson() throws Exception {

        final Person person = somePerson();
        when(personService.getPersonByID(KNOWN_PERSON_ID)).thenReturn(Optional.of(person));

        final WorkingTime workingTime = someWorkingTimeOfPerson(person);
        when(workingTimeService.getCurrentOne(person)).thenReturn(Optional.of(workingTime));

        perform(get("/web/person/" + KNOWN_PERSON_ID + "/workingtime"))
            .andExpect(model().attribute("workingTime", equalTo(new WorkingTimeForm(workingTime))));
    }

    @Test
    public void editGetWorkingTimeCreatesEmptyFormIfNoExistingWorkingTimeForPerson() throws Exception {

        final Person person = somePerson();
        when(personService.getPersonByID(KNOWN_PERSON_ID)).thenReturn(Optional.of(person));

        when(workingTimeService.getCurrentOne(person)).thenReturn(Optional.empty());

        perform(get("/web/person/" + KNOWN_PERSON_ID + "/workingtime"))
            .andExpect(model().attribute("workingTime", equalTo(new WorkingTimeForm())));
    }

    @Test
    public void editGetWorkingTimeUsesCorrectView() throws Exception {

        when(personService.getPersonByID(KNOWN_PERSON_ID)).thenReturn(Optional.of(somePerson()));

        perform(get("/web/person/" + KNOWN_PERSON_ID + "/workingtime"))
            .andExpect(view().name("workingtime/workingtime_form"));
    }

    @Test
    public void updatePostWorkingTimeForUnknownPersonIdThrowsUnknownPersonException() {

        assertThatThrownBy(() ->

            perform(post("/web/person/" + UNKNOWN_PERSON_ID + "/workingtime"))

        ).hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    public void updatePostWorkingTimeShowsFormIfValidationFails() throws Exception {

        when(personService.getPersonByID(KNOWN_PERSON_ID)).thenReturn(Optional.of(somePerson()));

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
    public void updatePostWorkingTimeTouchPersonIfValidationSuccessful() throws Exception {

        final Person person = somePerson();
        when(personService.getPersonByID(KNOWN_PERSON_ID)).thenReturn(Optional.of(person));

        perform(post("/web/person/" + KNOWN_PERSON_ID + "/workingtime"));

        verify(workingTimeService).touch(any(), any(), any(), eq(person));
    }

    @Test
    public void updatePostWorkingTimeAddsFlashAttributeAndRedirectsToPerson() throws Exception {

        when(personService.getPersonByID(KNOWN_PERSON_ID)).thenReturn(Optional.of(somePerson()));

        perform(post("/web/person/" + KNOWN_PERSON_ID + "/workingtime"))
            .andExpect(flash().attribute("updateSuccess", true))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/person/" + KNOWN_PERSON_ID));
    }

    private Person somePerson() {

        return new Person();
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
