package org.synyx.urlaubsverwaltung.holiday.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceExceptionHandler;
import org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.settings.FederalState.BADEN_WUERTTEMBERG;

@ExtendWith(MockitoExtension.class)
class PublicHolidayApiControllerTest {

    private PublicHolidayApiController sut;

    @Mock
    private PublicHolidaysService publicHolidayServiceMock;
    @Mock
    private PersonService personServiceMock;
    @Mock
    private WorkingTimeService workingTimeServiceMock;
    @Mock
    private SettingsService settingsServiceMock;

    @BeforeEach
    void setUp() {
        sut = new PublicHolidayApiController(publicHolidayServiceMock, personServiceMock, workingTimeServiceMock, settingsServiceMock);
    }

    @Test
    void ensureReturnsCorrectPublicHolidaysForYear() throws Exception {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(BADEN_WUERTTEMBERG);
        when(settingsServiceMock.getSettings()).thenReturn(settings);

        perform(get("/api/holidays")
            .param("year", "2016"))
            .andExpect(status().isOk());

        verify(publicHolidayServiceMock).getHolidays(2016, BADEN_WUERTTEMBERG);
    }

    @Test
    void ensureReturnsCorrectPublicHolidaysForYearAndMonth() throws Exception {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(BADEN_WUERTTEMBERG);
        when(settingsServiceMock.getSettings()).thenReturn(settings);

        perform(get("/api/holidays")
            .param("year", "2016")
            .param("month", "4"))
            .andExpect(status().isOk());

        verify(publicHolidayServiceMock).getHolidays(2016, 4, BADEN_WUERTTEMBERG);
    }

    @Test
    void ensureReturnsCorrectPublicHolidaysForYearAndPersonWithOverriddenFederalState() throws Exception {

        Person person = DemoDataCreator.createPerson();
        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.of(person));
        when(workingTimeServiceMock.getFederalStateForPerson(any(Person.class),
            any(LocalDate.class)))
            .thenReturn(FederalState.BAYERN);

        perform(get("/api/holidays")
            .param("year", "2016")
            .param("person", "23"))
            .andExpect(status().isOk());

        verify(publicHolidayServiceMock).getHolidays(2016, FederalState.BAYERN);
        verify(workingTimeServiceMock).getFederalStateForPerson(person, LocalDate.of(2016, 1, 1));
    }

    @Test
    void ensureReturnsCorrectPublicHolidaysForYearAndMonthAndPersonWithOverriddenFederalState()
        throws Exception {

        Person person = DemoDataCreator.createPerson();
        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.of(person));
        when(workingTimeServiceMock.getFederalStateForPerson(any(Person.class),
            any(LocalDate.class)))
            .thenReturn(FederalState.BAYERN);

        perform(get("/api/holidays")
            .param("year", "2016")
            .param("month", "4")
            .param("person", "23"))
            .andExpect(status().isOk());

        verify(publicHolidayServiceMock).getHolidays(2016, 4, FederalState.BAYERN);
        verify(workingTimeServiceMock).getFederalStateForPerson(person, LocalDate.of(2016, 4, 1));
    }

    @Test
    void ensureBadRequestForMissingYearParameter() throws Exception {

        perform(get("/api/holidays"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidYearParameter() throws Exception {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(BADEN_WUERTTEMBERG);
        when(settingsServiceMock.getSettings()).thenReturn(settings);

        perform(get("/api/holidays")
            .param("year", "foo"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidMonthParameter() throws Exception {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(BADEN_WUERTTEMBERG);
        when(settingsServiceMock.getSettings()).thenReturn(settings);

        perform(get("/api/holidays")
            .param("year", "2016")
            .param("month", "foo"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidPersonParameter() throws Exception {

        perform(get("/api/holidays")
            .param("year", "2016")
            .param("person", "foo"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestIfThereIsNoPersonForGivenID() throws Exception {

        when(personServiceMock.getPersonByID(any())).thenReturn(Optional.empty());

        perform(get("/api/holidays")
            .param("year", "2016")
            .param("person", "23"))
            .andExpect(status().isBadRequest());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).setControllerAdvice(new RestControllerAdviceExceptionHandler()).build().perform(builder);
    }
}
