package org.synyx.urlaubsverwaltung.absence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.json.JsonCompareMode.STRICT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.absence.AbsencePeriod.AbsenceStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.absence.AbsencePeriod.AbsenceStatus.WAITING;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class AbsenceApiControllerIT extends SingleTenantTestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PersonService personService;
    @MockitoBean
    private AbsenceService absenceService;

    @Test
    void ensureEmptyAbsences() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyLong())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, JANUARY, 7);

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of());

        perform(
            get("/api/persons/23/absences")
                .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
                .param("from", "2016-01-01")
                .param("to", "2016-01-07")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                {
                  "absences": []
                }
                """, STRICT));
    }

    // VACATION --------------------------------------------------------------------------------------------------------
    @Test
    void ensureCorrectConversionOfVacationFullDay() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyLong())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, DECEMBER, 31);

        final AbsencePeriod.RecordMorning recordMorningVacation = new AbsencePeriod.RecordMorningVacation(person, 42L, WAITING, "HOLIDAY", 1L, false);
        final AbsencePeriod.RecordNoon recordNoonVacation = new AbsencePeriod.RecordNoonVacation(person, 42L, WAITING, "HOLIDAY", 1L, false);
        final AbsencePeriod.Record fullDayVacationRecord = new AbsencePeriod.Record(startDate.plusDays(1), person, recordMorningVacation, recordNoonVacation);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(fullDayVacationRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(
            get("/api/persons/23/absences")
                .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
                .param("from", "2016-01-01")
                .param("to", "2016-12-31")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                {
                    "absences": [
                        {
                          "date": "2016-01-02",
                          "id": 42,
                          "absent": "FULL",
                          "absentNumeric": 1,
                          "absenceType": "VACATION",
                          "category": "HOLIDAY",
                          "typeId": 1,
                          "status": "WAITING"
                        }
                    ]
                }
                """, STRICT));
    }

    @Test
    void ensureCorrectConversionOfVacationMorning() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyLong())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, DECEMBER, 31);

        final AbsencePeriod.RecordMorning recordMorningVacation = new AbsencePeriod.RecordMorningVacation(person, 42L, WAITING, "HOLIDAY", 1L, false);
        final AbsencePeriod.Record fullDayVacationRecord = new AbsencePeriod.Record(startDate.plusDays(1), person, recordMorningVacation);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(fullDayVacationRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(
            get("/api/persons/23/absences")
                .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
                .param("from", "2016-01-01")
                .param("to", "2016-12-31")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                {
                    "absences": [
                        {
                          "date": "2016-01-02",
                          "id": 42,
                          "absent": "MORNING",
                          "absentNumeric": 0.5,
                          "absenceType": "VACATION",
                          "category": "HOLIDAY",
                          "typeId": 1,
                          "status": "WAITING"
                        }
                    ]
                }
                """, STRICT));
    }

    @Test
    void ensureCorrectConversionOfVacationNoon() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyLong())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, DECEMBER, 31);

        final AbsencePeriod.RecordNoon recordNoonVacation = new AbsencePeriod.RecordNoonVacation(person, 42L, WAITING, "HOLIDAY", 1L, false);
        final AbsencePeriod.Record fullDayVacationRecord = new AbsencePeriod.Record(startDate.plusDays(1), person, recordNoonVacation);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(fullDayVacationRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(
            get("/api/persons/23/absences")
                .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
                .param("from", "2016-01-01")
                .param("to", "2016-12-31")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                {
                    "absences": [
                        {
                          "date": "2016-01-02",
                          "id": 42,
                          "absent": "NOON",
                          "absentNumeric": 0.5,
                          "absenceType": "VACATION",
                          "category": "HOLIDAY",
                          "typeId": 1,
                          "status": "WAITING"
                        }
                    ]
                }
                """, STRICT));
    }

    @Test
    void ensureCorrectConversionOfOvertimeVacationNoon() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(23L);
        when(personService.getPersonByID(23L)).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, DECEMBER, 31);

        final AbsencePeriod.RecordNoon recordNoonVacation = new AbsencePeriod.RecordNoonVacation(person, 42L, WAITING, "OVERTIME", 1L, false);
        final AbsencePeriod.Record fullDayVacationRecord = new AbsencePeriod.Record(startDate.plusDays(1), person, recordNoonVacation);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(fullDayVacationRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(
            get("/api/persons/23/absences")
                .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
                .param("from", "2016-01-01")
                .param("to", "2016-12-31")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                {
                    "absences": [
                        {
                          "date": "2016-01-02",
                          "id": 42,
                          "absent": "NOON",
                          "absentNumeric": 0.5,
                          "absenceType": "VACATION",
                          "category": "OVERTIME",
                          "typeId": 1,
                          "status": "WAITING",
                          "_links": {
                             "overtime": {
                                "href": "http://localhost/api/persons/23/absences/42/overtime"
                              }
                          }
                        }
                    ]
                }
                """, STRICT));
    }

    // SICK ------------------------------------------------------------------------------------------------------------
    @Test
    void ensureCorrectConversionOfSickFullDay() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyLong())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, DECEMBER, 31);

        final AbsencePeriod.RecordMorning recordMorningSick = new AbsencePeriod.RecordMorningSick(person, 42L, ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.RecordNoon recordNoonSick = new AbsencePeriod.RecordNoonSick(person, 42L, ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record fullDaySickRecord = new AbsencePeriod.Record(startDate.plusDays(1), person, recordMorningSick, recordNoonSick);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(fullDaySickRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(
            get("/api/persons/23/absences")
                .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
                .param("from", "2016-01-01")
                .param("to", "2016-12-31")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                {
                    "absences": [
                        {
                          "date": "2016-01-02",
                          "id": 42,
                          "absent": "FULL",
                          "absentNumeric": 1,
                          "absenceType": "SICK_NOTE",
                          "category": "SICK_NOTE",
                          "typeId": 1,
                          "status": "ACTIVE"
                        }
                    ]
                }
                """, STRICT));
    }

    @Test
    void ensureCorrectConversionOfSickMorning() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyLong())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, DECEMBER, 31);

        final AbsencePeriod.RecordMorning recordMorningSick = new AbsencePeriod.RecordMorningSick(person, 42L, ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record morningSickRecord = new AbsencePeriod.Record(startDate.plusDays(1), person, recordMorningSick);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(morningSickRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(
            get("/api/persons/23/absences")
                .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
                .param("from", "2016-01-01")
                .param("to", "2016-12-31")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                {
                    "absences": [
                        {
                          "date": "2016-01-02",
                          "id": 42,
                          "absent": "MORNING",
                          "absentNumeric": 0.5,
                          "absenceType": "SICK_NOTE",
                          "category": "SICK_NOTE",
                          "typeId": 1,
                          "status": "ACTIVE"
                        }
                    ]
                }
                """, STRICT));
    }

    @Test
    void ensureCorrectConversionOfSickNoon() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyLong())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, DECEMBER, 31);

        final AbsencePeriod.RecordNoon recordNoonSick = new AbsencePeriod.RecordNoonSick(person, 42L, ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record noonSickRecord = new AbsencePeriod.Record(startDate.plusDays(1), person, recordNoonSick);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(noonSickRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(
            get("/api/persons/23/absences")
                .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
                .param("from", "2016-01-01")
                .param("to", "2016-12-31")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                {
                    "absences": [
                        {
                          "date": "2016-01-02",
                          "id": 42,
                          "absent": "NOON",
                          "absentNumeric": 0.5,
                          "absenceType": "SICK_NOTE",
                          "category": "SICK_NOTE",
                          "typeId": 1,
                          "status": "ACTIVE"
                        }
                    ]
                }
                """, STRICT));
    }

    // VACATION / SICK - COMBINATION -----------------------------------------------------------------------------------
    @Test
    void ensureCorrectConversionOfVacationMorningAndSickNoon() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyLong())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, DECEMBER, 31);

        final AbsencePeriod.RecordMorning recordMorningVacation = new AbsencePeriod.RecordMorningVacation(person, 1337L, WAITING, "HOLIDAY", 1L, false);
        final AbsencePeriod.RecordNoon recordNoonSick = new AbsencePeriod.RecordNoonSick(person, 42L, ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.Record absenceRecord = new AbsencePeriod.Record(startDate.plusDays(1), person, recordMorningVacation, recordNoonSick);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(absenceRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(
            get("/api/persons/23/absences")
                .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
                .param("from", "2016-01-01")
                .param("to", "2016-12-31")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                {
                    "absences": [
                        {
                          "date": "2016-01-02",
                          "id": 1337,
                          "absent": "MORNING",
                          "absentNumeric": 0.5,
                          "absenceType": "VACATION",
                          "category": "HOLIDAY",
                          "typeId": 1,
                          "status": "WAITING"
                        },
                        {
                          "date": "2016-01-02",
                          "id": 42,
                          "absent": "NOON",
                          "absentNumeric": 0.5,
                          "absenceType": "SICK_NOTE",
                          "category": "SICK_NOTE",
                          "typeId": 1,
                          "status": "ACTIVE"
                        }
                    ]
                }
                """, STRICT));
    }

    @Test
    void ensureCorrectConversionOfVacationNoonAndSickMorning() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyLong())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, DECEMBER, 31);

        final AbsencePeriod.RecordMorning recordMorningSick = new AbsencePeriod.RecordMorningSick(person, 42L, ACTIVE, "SICK_NOTE", 1L);
        final AbsencePeriod.RecordNoon recordNoonVacation = new AbsencePeriod.RecordNoonVacation(person, 1337L, WAITING, "HOLIDAY", 1L, false);
        final AbsencePeriod.Record absenceRecord = new AbsencePeriod.Record(startDate.plusDays(1), person, recordMorningSick, recordNoonVacation);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(absenceRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(
            get("/api/persons/23/absences")
                .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
                .param("from", "2016-01-01")
                .param("to", "2016-12-31")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                {
                    "absences": [
                        {
                          "date": "2016-01-02",
                          "id": 42,
                          "absent": "MORNING",
                          "absentNumeric": 0.5,
                          "absenceType": "SICK_NOTE",
                          "category": "SICK_NOTE",
                          "typeId": 1,
                          "status": "ACTIVE"
                        },
                        {
                          "date": "2016-01-02",
                          "id": 1337,
                          "absent": "NOON",
                          "absentNumeric": 0.5,
                          "absenceType": "VACATION",
                          "category": "HOLIDAY",
                          "typeId": 1,
                          "status": "WAITING"
                        }
                    ]
                }
                """, STRICT));
    }

    // VACATION / PUBLIC-HOLIDAY - COMBINATION -------------------------------------------------------------------------
    @Test
    void ensureCorrectConversionOfVacationMorningWithHalfDayChristmasEve() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyLong())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, DECEMBER, 31);

        final AbsencePeriod.RecordMorning recordMorningVacation = new AbsencePeriod.RecordMorningVacation(person, 42L, WAITING, "HOLIDAY", 1L, false);
        final AbsencePeriod.RecordNoon recordNoonPublicHoliday = new AbsencePeriod.RecordNoonPublicHoliday(person);
        final AbsencePeriod.Record fullDayVacationRecord = new AbsencePeriod.Record(LocalDate.of(2016, DECEMBER, 24), person, recordMorningVacation, recordNoonPublicHoliday);
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(fullDayVacationRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(
            get("/api/persons/23/absences")
                .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
                .param("from", "2016-01-01")
                .param("to", "2016-12-31")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                {
                    "absences": [
                        {
                          "date": "2016-12-24",
                          "id": 42,
                          "absent": "MORNING",
                          "absentNumeric": 0.5,
                          "absenceType": "VACATION",
                          "category": "HOLIDAY",
                          "typeId": 1,
                          "status": "WAITING"
                        },
                        {
                          "date": "2016-12-24",
                          "id": null,
                          "absent": "NOON",
                          "absentNumeric": 0.5,
                          "absenceType": "PUBLIC_HOLIDAY",
                          "category": null,
                          "typeId": null,
                          "status": "ACTIVE"
                        }
                    ]
                }
                """, STRICT));
    }


    // PARAMETER HANDLING ----------------------------------------------------------------------------------------------
    @Test
    void ensureTypeFilterIsWorkingForVacationOnly() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyLong())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, DECEMBER, 31);

        final AbsencePeriod.Record vacationRecord = anyVacationRecord(person, LocalDate.of(2016, JANUARY, 12));
        final AbsencePeriod.Record sickRecord = anySickRecord(person, LocalDate.of(2016, FEBRUARY, 12));
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(vacationRecord, sickRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(
            get("/api/persons/23/absences")
                .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
                .param("from", "2016-01-01")
                .param("to", "2016-12-31")
                .param("absence-types", "VACATION")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                {
                    "absences": [
                        {
                          "date": "2016-01-12",
                          "id": 42,
                          "absent": "MORNING",
                          "absentNumeric": 0.5,
                          "absenceType": "VACATION",
                          "category": "HOLIDAY",
                          "typeId": 1,
                          "status": "WAITING"
                        }
                    ]
                }
                """, STRICT));
    }

    @Test
    void ensureTypeFilterIsWorkingForSickNoteOnly() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyLong())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, DECEMBER, 31);

        final AbsencePeriod.Record vacationRecord = anyVacationRecord(person, LocalDate.of(2016, JANUARY, 12));
        final AbsencePeriod.Record sickRecord = anySickRecord(person, LocalDate.of(2016, FEBRUARY, 12));
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(vacationRecord, sickRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(
            get("/api/persons/23/absences")
                .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
                .param("from", "2016-01-01")
                .param("to", "2016-12-31")
                .param("absence-types", "SICK_NOTE")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                {
                    "absences": [
                        {
                          "date": "2016-02-12",
                          "id": 42,
                          "absent": "MORNING",
                          "absentNumeric": 0.5,
                          "absenceType": "SICK_NOTE",
                          "category": "SICK_NOTE",
                          "typeId": 1,
                          "status": "ACTIVE"
                        }
                    ]
                }
                """, STRICT));
    }

    @Test
    void ensureTypeFilterIsWorkingForMultipleTypes() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyLong())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, DECEMBER, 31);

        final AbsencePeriod.Record vacationRecord = anyVacationRecord(person, LocalDate.of(2016, JANUARY, 12));
        final AbsencePeriod.Record sickRecord = anySickRecord(person, LocalDate.of(2016, FEBRUARY, 12));

        final AbsencePeriod.RecordMorning publicHolidayMorning = new AbsencePeriod.RecordMorningPublicHoliday(person);
        final AbsencePeriod.RecordNoon publicHolidayNoon = new AbsencePeriod.RecordNoonPublicHoliday(person);
        final AbsencePeriod.Record publicHoliday = new AbsencePeriod.Record(LocalDate.of(2016, JANUARY, 6), person, publicHolidayMorning, publicHolidayNoon);

        final AbsencePeriod.RecordMorning noWorkdayMorning = new AbsencePeriod.RecordMorningNoWorkday(person);
        final AbsencePeriod.RecordNoon noWorkdayNoon = new AbsencePeriod.RecordNoonNoWorkday(person);
        final AbsencePeriod.Record noWorkday = new AbsencePeriod.Record(LocalDate.of(2016, JANUARY, 7), person, noWorkdayMorning, noWorkdayNoon);

        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(vacationRecord, sickRecord, publicHoliday, noWorkday));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(
            get("/api/persons/23/absences")
                .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
                .param("from", "2016-01-01")
                .param("to", "2016-12-31")
                .param("absence-types", "PUBLIC_HOLIDAY", "NO_WORKDAY")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                {
                    "absences": [
                        {
                          "date": "2016-01-06",
                          "id": null,
                          "absent": "FULL",
                          "absentNumeric": 1,
                          "absenceType": "PUBLIC_HOLIDAY",
                          "category": null,
                          "typeId": null,
                          "status": "ACTIVE"
                        },
                        {
                          "date": "2016-01-07",
                          "id": null,
                          "absent": "FULL",
                          "absentNumeric": 1,
                          "absenceType": "NO_WORKDAY",
                          "category": null,
                          "typeId": null,
                          "status": "ACTIVE"
                        }
                    ]
                }
                """, STRICT));
    }

    @Test
    void ensureTypeFilterFallbackIsEverything() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyLong())).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, DECEMBER, 31);

        final AbsencePeriod.Record vacationRecord = anyVacationRecord(person, LocalDate.of(2016, JANUARY, 12));
        final AbsencePeriod.Record sickRecord = anySickRecord(person, LocalDate.of(2016, FEBRUARY, 12));
        final AbsencePeriod absencePeriod = new AbsencePeriod(List.of(vacationRecord, sickRecord));

        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(List.of(absencePeriod));

        perform(
            get("/api/persons/23/absences")
                .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
                .param("from", "2016-01-01")
                .param("to", "2016-12-31")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                {
                    "absences": [
                        {
                          "date": "2016-01-12",
                          "id": 42,
                          "absent": "MORNING",
                          "absentNumeric": 0.5,
                          "absenceType": "VACATION",
                          "category": "HOLIDAY",
                          "typeId": 1,
                          "status": "WAITING"
                        },
                        {
                          "date": "2016-02-12",
                          "id": 42,
                          "absent": "MORNING",
                          "absentNumeric": 0.5,
                          "absenceType": "SICK_NOTE",
                          "category": "SICK_NOTE",
                          "typeId": 1,
                          "status": "ACTIVE"
                        }
                    ]
                }
                """, STRICT));
    }

    @Test
    void ensureNoWorkdaysInclusiveDoesNotMissOnTwoHalfDays() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(anyLong())).thenReturn(Optional.of(person));

        final LocalDate date = LocalDate.of(2016, JANUARY, 1);

        final AbsencePeriod.RecordMorning morning = new AbsencePeriod.RecordMorningVacation(person, 42L, WAITING, "HOLIDAY", 1L, false);
        final AbsencePeriod.Record recordMorning = new AbsencePeriod.Record(date, person, morning);
        final AbsencePeriod absencePeriodMorning = new AbsencePeriod(List.of(recordMorning));
        final AbsencePeriod.RecordNoon noon = new AbsencePeriod.RecordNoonVacation(person, 43L, WAITING, "HOLIDAY", 1L, false);
        final AbsencePeriod.Record recordNoon = new AbsencePeriod.Record(date, person, noon);
        final AbsencePeriod absencePeriodNoon = new AbsencePeriod(List.of(recordNoon));

        when(absenceService.getOpenAbsences(person, date, date)).thenReturn(List.of(absencePeriodMorning, absencePeriodNoon));

        perform(get("/api/persons/23/absences")
            .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
            .param("from", "2016-01-01")
            .param("to", "2016-01-01")
            .param("noWorkdaysInclusive", "true"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                {
                    "absences": [
                        {
                          "date": "2016-01-01",
                          "id": 42,
                          "absent": "MORNING",
                          "absentNumeric": 0.5,
                          "absenceType": "VACATION",
                          "category": "HOLIDAY",
                          "typeId": 1,
                          "status": "WAITING"
                        },
                        {
                          "date": "2016-01-01",
                          "id": 43,
                          "absent": "NOON",
                          "absentNumeric": 0.5,
                          "absenceType": "VACATION",
                          "status": "WAITING",
                          "category": "HOLIDAY",
                          "typeId": 1
                        }
                    ]
                }
                """, STRICT));
    }

    @Test
    void ensureBadRequestForInvalidFromParameter() throws Exception {
        perform(get("/api/persons/23/absences")
            .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
            .param("from", "2016-01")
            .param("to", "2016-01-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidToParameter() throws Exception {
        perform(get("/api/persons/23/absences")
            .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
            .param("from", "2016-01-01")
            .param("to", "2016-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidPersonParameter() throws Exception {
        perform(get("/api/persons/foo/absences")
            .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForMissingFromParameter() throws Exception {
        perform(get("/api/persons/23/absences")
            .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
            .param("to", "2016-01-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForMissingToParameter() throws Exception {

        perform(get("/api/persons/23/absences")
            .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
            .param("from", "2016-01-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForMissingPersonParameter() throws Exception {
        perform(get("/api/persons//absences")
            .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
            .param("from", "2016-01-01")
            .param("to", "2016-01-31")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void ensureForbiddenIfThereIsNoPersonForGivenID() throws Exception {
        when(personService.getPersonByID(anyLong())).thenReturn(Optional.empty());

        perform(get("/api/persons/23/absences")
            .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"))
            .andExpect(status().isForbidden());
    }

    @Test
    void ensureBadRequestForInvalidTypeParameter() throws Exception {
        when(personService.getPersonByID(anyLong()))
            .thenReturn(Optional.of(new Person("muster", "Muster", "Marlene", "muster@example.org")));

        perform(get("/api/persons/23/absences")
            .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
            .param("from", "2016-01-01")
            .param("to", "2016-01-31")
            .param("absence-types", "FOO"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensureBadRequestForInvalidPeriod() throws Exception {
        when(personService.getPersonByID(anyLong()))
            .thenReturn(Optional.of(new Person("muster", "Muster", "Marlene", "muster@example.org")));

        perform(get("/api/persons/23/absences")
            .with(oidcLogin().idToken(builder -> builder.subject("muster")).authorities(new SimpleGrantedAuthority("USER")))
            .param("from", "2016-01-01")
            .param("to", "2015-01-01"))
            .andExpect(status().isBadRequest());
    }

    private static AbsencePeriod.Record anyVacationRecord(Person person, LocalDate date) {
        final AbsencePeriod.RecordMorning morning = new AbsencePeriod.RecordMorningVacation(person, 42L, WAITING, "HOLIDAY", 1L, false);
        return new AbsencePeriod.Record(date, person, morning);
    }

    private static AbsencePeriod.Record anySickRecord(Person person, LocalDate date) {
        final AbsencePeriod.RecordMorning morning = new AbsencePeriod.RecordMorningSick(person, 42L, ACTIVE, "SICK_NOTE", 1L);
        return new AbsencePeriod.Record(date, person, morning);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
