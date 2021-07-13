package org.synyx.urlaubsverwaltung.absence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceExceptionHandler;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHoliday;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.settings.WorkingTimeSettingsEmbeddable;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AbsenceApiControllerTest {

    private AbsenceApiController sut;

    @Mock
    private PersonService personService;

    @Mock
    private AbsenceService absenceService;

    @Mock
    private PublicHolidaysService publicHolidaysService;

    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        sut = new AbsenceApiController(personService, absenceService, publicHolidaysService, settingsService);
    }

    // VACATION --------------------------------------------------------------------------------------------------------
    @Test
    void ensureCorrectConversionOfVacationFullDay() throws Exception {
        mockFederalState(FederalState.BADEN_WUERTTEMBERG);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, Month.JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, Month.DECEMBER, 31);

        final AbsencePeriod.RecordMorning recordMorningVacation = new AbsencePeriod.RecordMorningVacation(42, AbsencePeriod.AbsenceStatus.WAITING);
        final AbsencePeriod.RecordNoon recordNoonVacation = new AbsencePeriod.RecordNoonVacation(42, AbsencePeriod.AbsenceStatus.WAITING);
        final AbsencePeriod.Record fullDayVacationRecord = new AbsencePeriod.Record(startDate.plusDays(1), person, recordMorningVacation, recordNoonVacation);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(fullDayVacationRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.absences").exists())
            .andExpect(jsonPath("$.absences", hasSize(1)))
            .andExpect(jsonPath("$.absences[0].date", is("2016-01-02")))
            .andExpect(jsonPath("$.absences[0].type", is("VACATION")))
            .andExpect(jsonPath("$.absences[0].dayLength", is(1)))
            .andExpect(jsonPath("$.absences[0].absencePeriodName", is("FULL")))
            .andExpect(jsonPath("$.absences[0].href", is("42")))
        ;
    }

    @Test
    void ensureCorrectConversionOfVacationMorning() throws Exception {
        mockFederalState(FederalState.BADEN_WUERTTEMBERG);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, Month.JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, Month.DECEMBER, 31);

        final AbsencePeriod.RecordMorning recordMorningVacation = new AbsencePeriod.RecordMorningVacation(42, AbsencePeriod.AbsenceStatus.WAITING);
        final AbsencePeriod.Record fullDayVacationRecord = new AbsencePeriod.Record(startDate.plusDays(1), person, recordMorningVacation);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(fullDayVacationRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.absences").exists())
            .andExpect(jsonPath("$.absences", hasSize(1)))
            .andExpect(jsonPath("$.absences[0].date", is("2016-01-02")))
            .andExpect(jsonPath("$.absences[0].type", is("VACATION")))
            .andExpect(jsonPath("$.absences[0].dayLength", is(0.5)))
            .andExpect(jsonPath("$.absences[0].absencePeriodName", is("MORNING")))
            .andExpect(jsonPath("$.absences[0].href", is("42")))
        ;
    }

    @Test
    void ensureCorrectConversionOfVacationNoon() throws Exception {
        mockFederalState(FederalState.BADEN_WUERTTEMBERG);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, Month.JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, Month.DECEMBER, 31);

        final AbsencePeriod.RecordNoon recordNoonVacation = new AbsencePeriod.RecordNoonVacation(42, AbsencePeriod.AbsenceStatus.WAITING);
        final AbsencePeriod.Record fullDayVacationRecord = new AbsencePeriod.Record(startDate.plusDays(1), person, recordNoonVacation);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(fullDayVacationRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.absences").exists())
            .andExpect(jsonPath("$.absences", hasSize(1)))
            .andExpect(jsonPath("$.absences[0].date", is("2016-01-02")))
            .andExpect(jsonPath("$.absences[0].type", is("VACATION")))
            .andExpect(jsonPath("$.absences[0].dayLength", is(0.5)))
            .andExpect(jsonPath("$.absences[0].absencePeriodName", is("NOON")))
            .andExpect(jsonPath("$.absences[0].href", is("42")))
        ;
    }

    // SICK ------------------------------------------------------------------------------------------------------------
    @Test
    void ensureCorrectConversionOfSickFullDay() throws Exception {
        mockFederalState(FederalState.BADEN_WUERTTEMBERG);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, Month.JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, Month.DECEMBER, 31);

        final AbsencePeriod.RecordMorning recordMorningSick = new AbsencePeriod.RecordMorningSick(42);
        final AbsencePeriod.RecordNoon recordNoonSick = new AbsencePeriod.RecordNoonSick(42);
        final AbsencePeriod.Record fullDaySickRecord = new AbsencePeriod.Record(startDate.plusDays(1), person, recordMorningSick, recordNoonSick);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(fullDaySickRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.absences").exists())
            .andExpect(jsonPath("$.absences", hasSize(1)))
            .andExpect(jsonPath("$.absences[0].date", is("2016-01-02")))
            .andExpect(jsonPath("$.absences[0].type", is("SICK_NOTE")))
            .andExpect(jsonPath("$.absences[0].dayLength", is(1)))
            .andExpect(jsonPath("$.absences[0].absencePeriodName", is("FULL")))
            .andExpect(jsonPath("$.absences[0].href", is("42")))
        ;
    }

    @Test
    void ensureCorrectConversionOfSickMorning() throws Exception {
        mockFederalState(FederalState.BADEN_WUERTTEMBERG);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, Month.JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, Month.DECEMBER, 31);

        final AbsencePeriod.RecordMorning recordMorningSick = new AbsencePeriod.RecordMorningSick(42);
        final AbsencePeriod.Record morningSickRecord = new AbsencePeriod.Record(startDate.plusDays(1), person, recordMorningSick);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(morningSickRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.absences").exists())
            .andExpect(jsonPath("$.absences", hasSize(1)))
            .andExpect(jsonPath("$.absences[0].date", is("2016-01-02")))
            .andExpect(jsonPath("$.absences[0].type", is("SICK_NOTE")))
            .andExpect(jsonPath("$.absences[0].dayLength", is(0.5)))
            .andExpect(jsonPath("$.absences[0].absencePeriodName", is("MORNING")))
            .andExpect(jsonPath("$.absences[0].href", is("42")))
        ;
    }

    @Test
    void ensureCorrectConversionOfSickNoon() throws Exception {
        mockFederalState(FederalState.BADEN_WUERTTEMBERG);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, Month.JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, Month.DECEMBER, 31);

        final AbsencePeriod.RecordNoon recordNoonSick = new AbsencePeriod.RecordNoonSick(42);
        final AbsencePeriod.Record noonSickRecord = new AbsencePeriod.Record(startDate.plusDays(1), person, recordNoonSick);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(noonSickRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.absences").exists())
            .andExpect(jsonPath("$.absences", hasSize(1)))
            .andExpect(jsonPath("$.absences[0].date", is("2016-01-02")))
            .andExpect(jsonPath("$.absences[0].type", is("SICK_NOTE")))
            .andExpect(jsonPath("$.absences[0].dayLength", is(0.5)))
            .andExpect(jsonPath("$.absences[0].absencePeriodName", is("NOON")))
            .andExpect(jsonPath("$.absences[0].href", is("42")))
        ;
    }

    // VACATION / SICK - COMBINATION -----------------------------------------------------------------------------------
    @Test
    void ensureCorrectConversionOfVacationMorningAndSickNoon() throws Exception {
        mockFederalState(FederalState.BADEN_WUERTTEMBERG);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, Month.JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, Month.DECEMBER, 31);

        final AbsencePeriod.RecordMorning recordMorningVacation = new AbsencePeriod.RecordMorningVacation(1337, AbsencePeriod.AbsenceStatus.WAITING);
        final AbsencePeriod.RecordNoon recordNoonSick = new AbsencePeriod.RecordNoonSick(42);
        final AbsencePeriod.Record absenceRecord = new AbsencePeriod.Record(startDate.plusDays(1), person, recordMorningVacation, recordNoonSick);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(absenceRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.absences").exists())
            .andExpect(jsonPath("$.absences", hasSize(2)))
            .andExpect(jsonPath("$.absences[0].date", is("2016-01-02")))
            .andExpect(jsonPath("$.absences[0].type", is("SICK_NOTE")))
            .andExpect(jsonPath("$.absences[0].dayLength", is(0.5)))
            .andExpect(jsonPath("$.absences[0].absencePeriodName", is("NOON")))
            .andExpect(jsonPath("$.absences[0].href", is("42")))
            .andExpect(jsonPath("$.absences[1].date", is("2016-01-02")))
            .andExpect(jsonPath("$.absences[1].type", is("VACATION")))
            .andExpect(jsonPath("$.absences[1].dayLength", is(0.5)))
            .andExpect(jsonPath("$.absences[1].absencePeriodName", is("MORNING")))
            .andExpect(jsonPath("$.absences[1].href", is("1337")))
        ;
    }

    @Test
    void ensureCorrectConversionOfVacationNoonAndSickMorning() throws Exception {
        mockFederalState(FederalState.BADEN_WUERTTEMBERG);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, Month.JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, Month.DECEMBER, 31);

        final AbsencePeriod.RecordMorning recordMorningSick = new AbsencePeriod.RecordMorningSick(42);
        final AbsencePeriod.RecordNoon recordNoonVacation = new AbsencePeriod.RecordNoonVacation(1337, AbsencePeriod.AbsenceStatus.WAITING);
        final AbsencePeriod.Record absenceRecord = new AbsencePeriod.Record(startDate.plusDays(1), person, recordMorningSick, recordNoonVacation);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(absenceRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.absences").exists())
            .andExpect(jsonPath("$.absences", hasSize(2)))
            .andExpect(jsonPath("$.absences[0].date", is("2016-01-02")))
            .andExpect(jsonPath("$.absences[0].type", is("SICK_NOTE")))
            .andExpect(jsonPath("$.absences[0].dayLength", is(0.5)))
            .andExpect(jsonPath("$.absences[0].absencePeriodName", is("MORNING")))
            .andExpect(jsonPath("$.absences[0].href", is("42")))
            .andExpect(jsonPath("$.absences[1].date", is("2016-01-02")))
            .andExpect(jsonPath("$.absences[1].type", is("VACATION")))
            .andExpect(jsonPath("$.absences[1].dayLength", is(0.5)))
            .andExpect(jsonPath("$.absences[1].absencePeriodName", is("NOON")))
            .andExpect(jsonPath("$.absences[1].href", is("1337")))
        ;
    }

    // VACATION / PUBLIC-HOLIDAY - COMBINATION -------------------------------------------------------------------------
    @Test
    void ensureCorrectConversionOfVacationFullDayWithHalfDayChristmasEve() throws Exception {
        mockFederalState(FederalState.BADEN_WUERTTEMBERG);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, Month.JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, Month.DECEMBER, 31);

        final AbsencePeriod.RecordMorning recordMorningVacation = new AbsencePeriod.RecordMorningVacation(42, AbsencePeriod.AbsenceStatus.WAITING);
        final AbsencePeriod.RecordNoon recordNoonVacation = new AbsencePeriod.RecordNoonVacation(42, AbsencePeriod.AbsenceStatus.WAITING);
        final AbsencePeriod.Record fullDayVacationRecord = new AbsencePeriod.Record(LocalDate.of(2016, Month.DECEMBER, 24), person, recordMorningVacation, recordNoonVacation);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(fullDayVacationRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        final LocalDate date = LocalDate.of(2016, Month.DECEMBER, 24);
        final PublicHoliday christmasEve = new PublicHoliday(date, DayLength.NOON);

        when(publicHolidaysService.getPublicHolidays(startDate, endDate, FederalState.BADEN_WUERTTEMBERG))
            .thenReturn(List.of(christmasEve));

        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.absences").exists())
            .andExpect(jsonPath("$.absences", hasSize(1)))
            .andExpect(jsonPath("$.absences[0].date", is("2016-12-24")))
            .andExpect(jsonPath("$.absences[0].type", is("VACATION")))
            .andExpect(jsonPath("$.absences[0].dayLength", is(1)))
            .andExpect(jsonPath("$.absences[0].absencePeriodName", is("FULL")))
            .andExpect(jsonPath("$.absences[0].href", is("42")))
        ;
    }

    @Test
    void ensureCorrectConversionOfVacationMorningWithHalfDayChristmasEve() throws Exception {
        mockFederalState(FederalState.BADEN_WUERTTEMBERG);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, Month.JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, Month.DECEMBER, 31);

        final AbsencePeriod.RecordMorning recordMorningVacation = new AbsencePeriod.RecordMorningVacation(42, AbsencePeriod.AbsenceStatus.WAITING);
        final AbsencePeriod.Record fullDayVacationRecord = new AbsencePeriod.Record(LocalDate.of(2016, Month.DECEMBER, 24), person, recordMorningVacation);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(fullDayVacationRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        final LocalDate date = LocalDate.of(2016, Month.DECEMBER, 24);
        final PublicHoliday christmasEve = new PublicHoliday(date, DayLength.NOON);

        when(publicHolidaysService.getPublicHolidays(startDate, endDate, FederalState.BADEN_WUERTTEMBERG))
            .thenReturn(List.of(christmasEve));

        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.absences").exists())
            .andExpect(jsonPath("$.absences", hasSize(1)))
            .andExpect(jsonPath("$.absences[0].date", is("2016-12-24")))
            .andExpect(jsonPath("$.absences[0].type", is("VACATION")))
            .andExpect(jsonPath("$.absences[0].dayLength", is(1)))
            .andExpect(jsonPath("$.absences[0].absencePeriodName", is("FULL")))
            .andExpect(jsonPath("$.absences[0].href", is("42")))
        ;
    }

    // SICK / PUBLIC-HOLIDAY - COMBINATION -----------------------------------------------------------------------------
    @Test
    void ensureCorrectConversionOfSickFullDayWithHalfDayChristmasEve() throws Exception {
        mockFederalState(FederalState.BADEN_WUERTTEMBERG);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, Month.JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, Month.DECEMBER, 31);

        final AbsencePeriod.RecordMorning recordMorningSick = new AbsencePeriod.RecordMorningSick(42);
        final AbsencePeriod.RecordNoon recordNoonSick = new AbsencePeriod.RecordNoonSick(42);
        final AbsencePeriod.Record fullDaySick = new AbsencePeriod.Record(LocalDate.of(2016, Month.DECEMBER, 24), person, recordMorningSick, recordNoonSick);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(fullDaySick));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        final LocalDate date = LocalDate.of(2016, Month.DECEMBER, 24);
        final PublicHoliday christmasEve = new PublicHoliday(date, DayLength.NOON);

        when(publicHolidaysService.getPublicHolidays(startDate, endDate, FederalState.BADEN_WUERTTEMBERG))
            .thenReturn(List.of(christmasEve));

        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.absences").exists())
            .andExpect(jsonPath("$.absences", hasSize(1)))
            .andExpect(jsonPath("$.absences[0].date", is("2016-12-24")))
            .andExpect(jsonPath("$.absences[0].type", is("SICK_NOTE")))
            .andExpect(jsonPath("$.absences[0].dayLength", is(1)))
            .andExpect(jsonPath("$.absences[0].absencePeriodName", is("FULL")))
            .andExpect(jsonPath("$.absences[0].href", is("42")))
        ;
    }

    @Test
    void ensureCorrectConversionOfSickMorningWithHalfDayChristmasEve() throws Exception {
        mockFederalState(FederalState.BADEN_WUERTTEMBERG);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, Month.JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, Month.DECEMBER, 31);

        final AbsencePeriod.RecordMorning recordMorningSick = new AbsencePeriod.RecordMorningSick(42);
        final AbsencePeriod.Record morningSick = new AbsencePeriod.Record(LocalDate.of(2016, Month.DECEMBER, 24), person, recordMorningSick);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(morningSick));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        final LocalDate date = LocalDate.of(2016, Month.DECEMBER, 24);
        final PublicHoliday christmasEve = new PublicHoliday(date, DayLength.NOON);

        when(publicHolidaysService.getPublicHolidays(startDate, endDate, FederalState.BADEN_WUERTTEMBERG))
            .thenReturn(List.of(christmasEve));

        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.absences").exists())
            .andExpect(jsonPath("$.absences", hasSize(1)))
            .andExpect(jsonPath("$.absences[0].date", is("2016-12-24")))
            .andExpect(jsonPath("$.absences[0].type", is("SICK_NOTE")))
            .andExpect(jsonPath("$.absences[0].dayLength", is(1)))
            .andExpect(jsonPath("$.absences[0].absencePeriodName", is("FULL")))
            .andExpect(jsonPath("$.absences[0].href", is("42")))
        ;
    }

    // PARAMETER HANDLING ----------------------------------------------------------------------------------------------
    @Test
    void ensureTypeFilterIsWorkingForVacationOnly() throws Exception {
        mockFederalState(FederalState.BADEN_WUERTTEMBERG);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, Month.JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, Month.DECEMBER, 31);

        final AbsencePeriod.Record vacationRecord = anyVacationRecord(person, LocalDate.of(2016, Month.JANUARY, 12));
        final AbsencePeriod.Record sickRecord = anySickRecord(person, LocalDate.of(2016, Month.FEBRUARY, 12));
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(vacationRecord, sickRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31")
            .param("type", "VACATION"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.absences").exists())
            .andExpect(jsonPath("$.absences", hasSize(1)))
            .andExpect(jsonPath("$.absences[0].type", is("VACATION")));
    }

    @Test
    void ensureTypeFilterIsWorkingForSickNoteOnly() throws Exception {
        mockFederalState(FederalState.BADEN_WUERTTEMBERG);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, Month.JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, Month.DECEMBER, 31);

        final AbsencePeriod.Record vacationRecord = anyVacationRecord(person, LocalDate.of(2016, Month.JANUARY, 12));
        final AbsencePeriod.Record sickRecord = anySickRecord(person, LocalDate.of(2016, Month.FEBRUARY, 12));
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(vacationRecord, sickRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31")
            .param("type", "SICK_NOTE"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.absences").exists())
            .andExpect(jsonPath("$.absences", hasSize(1)))
            .andExpect(jsonPath("$.absences[0].type", is("SICK_NOTE")));
    }

    @Test
    void ensureTypeFilterFallbackIsEverything() throws Exception {
        mockFederalState(FederalState.BADEN_WUERTTEMBERG);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, Month.JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, Month.DECEMBER, 31);

        final AbsencePeriod.Record vacationRecord = anyVacationRecord(person, LocalDate.of(2016, Month.JANUARY, 12));
        final AbsencePeriod.Record sickRecord = anySickRecord(person, LocalDate.of(2016, Month.FEBRUARY, 12));
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(vacationRecord, sickRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.absences").exists())
            .andExpect(jsonPath("$.absences", hasSize(2)))
            .andExpect(jsonPath("$.absences[0].type", is("VACATION")))
            .andExpect(jsonPath("$.absences[1].type", is("SICK_NOTE")));
    }

    @Test
    void ensureBadRequestForInvalidFromParameter() throws Exception {
        perform(get("/api/persons/23/absences")
            .param("from", "2016-01")
            .param("to", "2016-01-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidToParameter() throws Exception {
        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidPersonParameter() throws Exception {
        perform(get("/api/persons/foo/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForMissingFromParameter() throws Exception {
        perform(get("/api/persons/23/absences")
            .param("to", "2016-01-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForMissingToParameter() throws Exception {

        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForMissingPersonParameter() throws Exception {
        perform(get("/api/persons//absences")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31")
        ).andExpect(status().isNotFound());
    }

    @Test
    void ensureBadRequestIfThereIsNoPersonForGivenID() throws Exception {
        when(personService.getPersonByID(anyInt())).thenReturn(Optional.empty());

        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidTypeParameter() throws Exception {
        when(personService.getPersonByID(anyInt()))
            .thenReturn(Optional.of(new Person("muster", "Muster", "Marlene", "muster@example.org")));

        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31")
            .param("type", "FOO"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidPeriod() throws Exception {
        perform(get("/api/persons/23/absences")
            .param("from", "2016-01-01")
            .param("to", "2015-01-01"))
            .andExpect(status().isBadRequest());
    }

    private void mockFederalState(FederalState federalState) {
        final WorkingTimeSettingsEmbeddable workingTimeSettings = new WorkingTimeSettingsEmbeddable();
        workingTimeSettings.setFederalState(federalState);

        final Settings settings = new Settings();
        settings.setWorkingTimeSettings(workingTimeSettings);

        when(settingsService.getSettings()).thenReturn(settings);
    }

    private static AbsencePeriod.Record anyVacationRecord(Person person, LocalDate date) {
        final AbsencePeriod.RecordMorning morning = new AbsencePeriod.RecordMorningVacation(42, AbsencePeriod.AbsenceStatus.WAITING);
        return new AbsencePeriod.Record(date, person, morning);
    }

    private static AbsencePeriod.Record anySickRecord(Person person, LocalDate date) {
        final AbsencePeriod.RecordMorning morning = new AbsencePeriod.RecordMorningSick(42);
        return new AbsencePeriod.Record(date, person, morning);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.standaloneSetup(sut).setControllerAdvice(new RestControllerAdviceExceptionHandler()).build().perform(builder);
    }
}
