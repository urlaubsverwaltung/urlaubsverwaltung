package org.synyx.urlaubsverwaltung.restapi.holiday;

import org.joda.time.DateMidnight;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.restapi.ApiExceptionHandlerControllerAdvice;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class PublicHolidayControllerTest {

    private MockMvc mockMvc;

    private PublicHolidaysService publicHolidayServiceMock;
    private PersonService personServiceMock;
    private WorkingTimeService workingTimeServiceMock;
    private SettingsService settingsServiceMock;

    @Before
    public void setUp() {

        personServiceMock = mock(PersonService.class);
        publicHolidayServiceMock = mock(PublicHolidaysService.class);
        workingTimeServiceMock = mock(WorkingTimeService.class);
        settingsServiceMock = mock(SettingsService.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new PublicHolidayController(publicHolidayServiceMock,
                        personServiceMock, workingTimeServiceMock, settingsServiceMock))
                .setControllerAdvice(new ApiExceptionHandlerControllerAdvice())
                .build();

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(FederalState.BADEN_WUERTTEMBERG);
        when(settingsServiceMock.getSettings()).thenReturn(settings);
    }


    @Test
    public void ensureReturnsCorrectPublicHolidaysForYear() throws Exception {

        mockMvc.perform(get("/api/holidays").param("year", "2016")).andExpect(status().isOk());

        verify(publicHolidayServiceMock).getHolidays(2016, FederalState.BADEN_WUERTTEMBERG);
    }


    @Test
    public void ensureReturnsCorrectPublicHolidaysForYearAndMonth() throws Exception {

        mockMvc.perform(get("/api/holidays").param("year", "2016").param("month", "4")).andExpect(status().isOk());

        verify(publicHolidayServiceMock).getHolidays(2016, 4, FederalState.BADEN_WUERTTEMBERG);
    }


    @Test
    public void ensureReturnsCorrectPublicHolidaysForYearAndPersonWithOverriddenFederalState() throws Exception {

        Person person = TestDataCreator.createPerson();
        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.of(person));
        when(workingTimeServiceMock.getFederalStateForPerson(any(Person.class),
                    any(DateMidnight.class)))
            .thenReturn(FederalState.BAYERN);

        mockMvc.perform(get("/api/holidays").param("year", "2016").param("person", "23")).andExpect(status().isOk());

        verify(publicHolidayServiceMock).getHolidays(2016, FederalState.BAYERN);
        verify(workingTimeServiceMock).getFederalStateForPerson(person, new DateMidnight(2016, 1, 1));
    }


    @Test
    public void ensureReturnsCorrectPublicHolidaysForYearAndMonthAndPersonWithOverriddenFederalState()
        throws Exception {

        Person person = TestDataCreator.createPerson();
        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.of(person));
        when(workingTimeServiceMock.getFederalStateForPerson(any(Person.class),
                    any(DateMidnight.class)))
            .thenReturn(FederalState.BAYERN);

        mockMvc.perform(get("/api/holidays").param("year", "2016").param("month", "4").param("person", "23"))
            .andExpect(status().isOk());

        verify(publicHolidayServiceMock).getHolidays(2016, 4, FederalState.BAYERN);
        verify(workingTimeServiceMock).getFederalStateForPerson(person, new DateMidnight(2016, 4, 1));
    }


    @Test
    public void ensureBadRequestForMissingYearParameter() throws Exception {

        mockMvc.perform(get("/api/holidays")).andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidYearParameter() throws Exception {

        mockMvc.perform(get("/api/holidays").param("year", "foo")).andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidMonthParameter() throws Exception {

        mockMvc.perform(get("/api/holidays").param("year", "2016").param("month", "foo"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidPersonParameter() throws Exception {

        mockMvc.perform(get("/api/holidays").param("year", "2016").param("person", "foo"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestIfThereIsNoPersonForGivenID() throws Exception {

        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/holidays").param("year", "2016").param("person", "23"))
            .andExpect(status().isBadRequest());
    }
}
