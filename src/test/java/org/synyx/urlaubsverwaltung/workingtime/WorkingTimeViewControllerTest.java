package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeDto;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeViewModelService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.ORANGE;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BADEN_WUERTTEMBERG;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BERLIN;

@ExtendWith(MockitoExtension.class)
class WorkingTimeViewControllerTest {

    private WorkingTimeViewController sut;

    private static final long KNOWN_PERSON_ID = 1;
    private static final long UNKNOWN_PERSON_ID = 217;

    @Mock
    private PersonService personService;
    @Mock
    private WorkingTimeService workingTimeService;
    @Mock
    private WorkingTimeWriteService workingTimeWriteService;
    @Mock
    private VacationTypeViewModelService vacationTypeViewModelService;
    @Mock
    private SettingsService settingsService;
    @Mock
    private WorkingTimeValidator validator;

    @BeforeEach
    void setUp() {
        sut = new WorkingTimeViewController(personService, workingTimeService, workingTimeWriteService, vacationTypeViewModelService, settingsService, validator, Clock.systemUTC());
    }

    @Test
    void editGetWorkingTimeForUnknownPersonIdThrowsUnknownPersonException() {
        assertThatThrownBy(() ->
            perform(get("/web/person/" + UNKNOWN_PERSON_ID + "/workingtime"))
        ).hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    void editWorkingTimePresetsFormWithCorrectAttributes() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person();
        when(personService.getPersonByID(KNOWN_PERSON_ID)).thenReturn(Optional.of(person));

        final WorkingTime workingTime = new WorkingTime(person, LocalDate.of(2020, 10, 2), GERMANY_BERLIN, false);
        workingTime.setWorkingDays(List.of(MONDAY), DayLength.FULL);
        when(workingTimeService.getWorkingTime(eq(person), any(LocalDate.class))).thenReturn(Optional.of(workingTime));
        when(workingTimeService.getByPerson(person)).thenReturn(List.of(workingTime));

        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));

        perform(get("/web/person/" + KNOWN_PERSON_ID + "/workingtime"))
            .andExpect(model().attribute("workingTime", equalTo(new WorkingTimeForm(workingTime))))
            .andExpect(model().attribute("workingTimeHistories", hasItem(hasProperty("valid", equalTo(true)))))
            .andExpect(model().attribute("workingTimeHistories", hasItem(hasProperty("federalState", equalTo("GERMANY_BERLIN")))))
            .andExpect(model().attribute("workingTimeHistories", hasItem(hasProperty("validFrom", equalTo(LocalDate.of(2020, 10, 2))))))
            .andExpect(model().attribute("workingTimeHistories", hasItem(hasProperty("validTo", equalTo(null)))))
            .andExpect(model().attribute("workingTimeHistories", hasItem(hasProperty("workingDays", hasItem("MONDAY")))))
            .andExpect(model().attribute("defaultFederalState", equalTo(GERMANY_BADEN_WUERTTEMBERG)))
            .andExpect(model().attribute("federalStateTypes", equalTo(FederalState.federalStatesTypesByCountry())))
            .andExpect(model().attribute("vacationTypeColors", equalTo(List.of(new VacationTypeDto(1L, ORANGE)))))
            .andExpect(model().attribute("weekDays", equalTo(DayOfWeek.values())));
    }

    @Test
    void editWorkingTimePresetsFormWorksWithActualInvalidState() throws Exception {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person();
        when(personService.getPersonByID(KNOWN_PERSON_ID)).thenReturn(Optional.of(person));

        final WorkingTime workingTime = new WorkingTime(person, LocalDate.of(2020, 10, 2), GERMANY_BERLIN, false);
        workingTime.setWorkingDays(List.of(MONDAY), DayLength.FULL);

        // this results in `null` in the implementation at time of writing this.
        // and we want to ensure that no NullPointer is thrown anywhere
        when(workingTimeService.getWorkingTime(eq(person), any(LocalDate.class))).thenReturn(Optional.empty());

        when(workingTimeService.getByPerson(person)).thenReturn(List.of(workingTime));

        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));

        perform(get("/web/person/" + KNOWN_PERSON_ID + "/workingtime"))
            .andExpect(status().isOk())
            .andExpect(view().name("workingtime/workingtime_form"));
    }

    @Test
    void editGetWorkingTimeCreatesEmptyFormIfNoExistingWorkingTimeForPerson() throws Exception {

        final Person person = new Person();
        when(personService.getPersonByID(KNOWN_PERSON_ID)).thenReturn(Optional.of(person));

        when(settingsService.getSettings()).thenReturn(new Settings());
        when(workingTimeService.getWorkingTime(eq(person), any(LocalDate.class))).thenReturn(Optional.empty());

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
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("validFrom", "errors");
            return null;
        }).when(validator).validate(any(), any());

        perform(post("/web/person/" + KNOWN_PERSON_ID + "/workingtime"))
            .andExpect(model().attribute("vacationTypeColors", equalTo(List.of(new VacationTypeDto(1L, ORANGE)))))
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

    private static Stream<Arguments> dateInputAndLocalDateTuple() {
        return Stream.of(
            Arguments.of("25.03.2022", LocalDate.of(2022, 3, 25)),
            Arguments.of("25.03.22", LocalDate.of(2022, 3, 25)),
            Arguments.of("25.3.2022", LocalDate.of(2022, 3, 25)),
            Arguments.of("25.3.22", LocalDate.of(2022, 3, 25)),
            Arguments.of("1.4.22", LocalDate.of(2022, 4, 1))
        );
    }

    @ParameterizedTest
    @MethodSource("dateInputAndLocalDateTuple")
    void updateAccountSucceedsWithValidFrom(String givenDate, LocalDate givenLocalDate) throws Exception {

        final Person person = new Person();
        person.setId(1L);

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(
            post("/web/person/1/workingtime")
                .param("validFrom", givenDate)
                .param("workingDays", "1", "2", "3", "4", "5")
                .param("federalState", "GERMANY_BADEN_WUERTTEMBERG")
        )
            .andExpect(redirectedUrl("/web/person/1"));

        verify(workingTimeWriteService).touch(List.of(1, 2, 3, 4, 5), givenLocalDate, person, GERMANY_BADEN_WUERTTEMBERG);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
