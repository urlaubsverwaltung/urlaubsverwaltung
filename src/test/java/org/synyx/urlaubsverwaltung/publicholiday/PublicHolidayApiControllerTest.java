package org.synyx.urlaubsverwaltung.publicholiday;

import de.jollyday.Holiday;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceExceptionHandler;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static de.jollyday.HolidayType.OFFICIAL_HOLIDAY;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.settings.FederalState.BADEN_WUERTTEMBERG;

@ExtendWith(MockitoExtension.class)
class PublicHolidayApiControllerTest {

    private PublicHolidayApiController sut;

    @Mock
    private PublicHolidaysService publicHolidayService;
    @Mock
    private PersonService personService;
    @Mock
    private WorkingTimeService workingTimeService;
    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        sut = new PublicHolidayApiController(publicHolidayService, personService, workingTimeService, settingsService);
    }

    @Test
    void getPublicHolidays() throws Exception {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(BADEN_WUERTTEMBERG);
        when(settingsService.getSettings()).thenReturn(settings);

        final LocalDate from = LocalDate.of(2016, 5, 19);
        final LocalDate to = LocalDate.of(2016, 5, 20);
        final Holiday fromHoliday = new Holiday(from, "", OFFICIAL_HOLIDAY);
        final Holiday toHoliday = new Holiday(to, "", OFFICIAL_HOLIDAY);
        when(publicHolidayService.getHolidays(from, to, BADEN_WUERTTEMBERG)).thenReturn(List.of(fromHoliday, toHoliday));

        when(publicHolidayService.getWorkingDurationOfDate(from, BADEN_WUERTTEMBERG)).thenReturn(ONE);
        when(publicHolidayService.getAbsenceTypeOfDate(from, BADEN_WUERTTEMBERG)).thenReturn(DayLength.MORNING);
        when(publicHolidayService.getWorkingDurationOfDate(to, BADEN_WUERTTEMBERG)).thenReturn(TEN);
        when(publicHolidayService.getAbsenceTypeOfDate(to, BADEN_WUERTTEMBERG)).thenReturn(DayLength.NOON);

        perform(get("/api/holidays")
            .param("from", "2016-05-19")
            .param("to", "2016-05-20"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.publicHolidays").exists())
            .andExpect(jsonPath("$.publicHolidays", hasSize(2)))
            .andExpect(jsonPath("$.publicHolidays[0].date", is("2016-05-19")))
            .andExpect(jsonPath("$.publicHolidays[0].dayLength", is(1)))
            .andExpect(jsonPath("$.publicHolidays[0].absencePeriodName", is("MORNING")))
            .andExpect(jsonPath("$.publicHolidays[1].date", is("2016-05-20")))
            .andExpect(jsonPath("$.publicHolidays[1].dayLength", is(10)))
            .andExpect(jsonPath("$.publicHolidays[1].absencePeriodName", is("NOON")));
    }

    @Test
    void getPublicHolidaysForInvalidPeriod() throws Exception {
        perform(get("/api/holidays")
            .param("from", "2016-01-01")
            .param("to", "2015-01-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getPublicHolidaysForInvalidFrom() throws Exception {
        perform(get("/api/holidays")
            .param("from", "invalid")
            .param("to", "2016-01-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getPublicHolidaysForInvalidTo() throws Exception {
        perform(get("/api/holidays")
            .param("from", "2016-01-01")
            .param("to", "invalid"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getPublicHolidaysForMissingFrom() throws Exception {
        perform(get("/api/holidays")
            .param("to", "2015-01-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getPublicHolidaysForMissingTo() throws Exception {
        perform(get("/api/holidays")
            .param("from", "2016-01-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void personsPublicHolidays() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final LocalDate from = LocalDate.of(2016, 5, 19);

        when(workingTimeService.getFederalStateForPerson(person, from)).thenReturn(BADEN_WUERTTEMBERG);

        final LocalDate to = LocalDate.of(2016, 5, 20);
        final Holiday fromHoliday = new Holiday(from, "", OFFICIAL_HOLIDAY);
        final Holiday toHoliday = new Holiday(to, "", OFFICIAL_HOLIDAY);
        when(publicHolidayService.getHolidays(from, to, BADEN_WUERTTEMBERG)).thenReturn(List.of(fromHoliday, toHoliday));

        when(publicHolidayService.getWorkingDurationOfDate(from, BADEN_WUERTTEMBERG)).thenReturn(ONE);
        when(publicHolidayService.getAbsenceTypeOfDate(from, BADEN_WUERTTEMBERG)).thenReturn(DayLength.MORNING);
        when(publicHolidayService.getWorkingDurationOfDate(to, BADEN_WUERTTEMBERG)).thenReturn(TEN);
        when(publicHolidayService.getAbsenceTypeOfDate(to, BADEN_WUERTTEMBERG)).thenReturn(DayLength.NOON);

        perform(get("/api/persons/1/holidays")
            .param("from", "2016-05-19")
            .param("to", "2016-05-20"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.publicHolidays").exists())
            .andExpect(jsonPath("$.publicHolidays", hasSize(2)))
            .andExpect(jsonPath("$.publicHolidays[0].date", is("2016-05-19")))
            .andExpect(jsonPath("$.publicHolidays[0].dayLength", is(1)))
            .andExpect(jsonPath("$.publicHolidays[0].absencePeriodName", is("MORNING")))
            .andExpect(jsonPath("$.publicHolidays[1].date", is("2016-05-20")))
            .andExpect(jsonPath("$.publicHolidays[1].dayLength", is(10)))
            .andExpect(jsonPath("$.publicHolidays[1].absencePeriodName", is("NOON")));
    }

    @Test
    void personsSickNotesForEmptyPerson() throws Exception {
        when(personService.getPersonByID(1)).thenReturn(Optional.empty());

        perform(get("/api/persons/1/holidays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void personsPublicHolidaysForInvalidPeriod() throws Exception {
        perform(get("/api/persons/1/holidays")
            .param("from", "2016-01-01")
            .param("to", "2015-01-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void personsPublicHolidaysForInvalidFrom() throws Exception {
        perform(get("/api/persons/1/holidays")
            .param("from", "invalid")
            .param("to", "2016-01-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void personsPublicHolidaysForInvalidTo() throws Exception {
        perform(get("/api/persons/1/holidays")
            .param("from", "2016-01-01")
            .param("to", "invalid"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void personsPublicHolidaysForMissingFrom() throws Exception {
        perform(get("/api/persons/1/holidays")
            .param("to", "2015-01-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void personsPublicHolidaysForMissingTo() throws Exception {
        perform(get("/api/persons/1/holidays")
            .param("from", "2016-01-01"))
            .andExpect(status().isBadRequest());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).setControllerAdvice(new RestControllerAdviceExceptionHandler()).build().perform(builder);
    }
}
