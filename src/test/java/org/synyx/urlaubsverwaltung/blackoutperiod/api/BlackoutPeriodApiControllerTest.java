package org.synyx.urlaubsverwaltung.blackoutperiod.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;
import org.synyx.urlaubsverwaltung.blackoutperiod.BlackoutPeriod;
import org.synyx.urlaubsverwaltung.blackoutperiod.BlackoutPeriodService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class BlackoutPeriodApiControllerTest {

    private BlackoutPeriodApiController sut;

    @Mock
    private PersonService personService;
    @Mock
    private BlackoutPeriodService blackoutPeriodService;

    @BeforeEach
    void setUp() {
        sut = new BlackoutPeriodApiController(personService, blackoutPeriodService);
    }

    @Test
    void personsBlackoutPeriodsExpandsPeriodIntoDaysClampedToRequestedRange() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(42L);
        when(personService.getPersonByID(42L)).thenReturn(Optional.of(person));

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setTitle("Jahresabschluss");
        blackoutPeriod.setStartDate(LocalDate.of(2026, 12, 20));
        blackoutPeriod.setEndDate(LocalDate.of(2027, 1, 5));

        when(blackoutPeriodService.findBlackoutPeriodsForPerson(person, LocalDate.of(2026, 12, 22), LocalDate.of(2026, 12, 23)))
            .thenReturn(List.of(blackoutPeriod));

        perform(get("/api/persons/42/blackout-periods")
            .param("from", "2026-12-22")
            .param("to", "2026-12-23"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.blackoutPeriods", hasSize(2)))
            .andExpect(jsonPath("$.blackoutPeriods[0].date", is("2026-12-22")))
            .andExpect(jsonPath("$.blackoutPeriods[0].title", is("Jahresabschluss")))
            .andExpect(jsonPath("$.blackoutPeriods[1].date", is("2026-12-23")))
            .andExpect(jsonPath("$.blackoutPeriods[1].title", is("Jahresabschluss")));
    }

    @Test
    void personsBlackoutPeriodsReturnsEmptyListWhenNoneApply() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(42L);
        when(personService.getPersonByID(42L)).thenReturn(Optional.of(person));
        when(blackoutPeriodService.findBlackoutPeriodsForPerson(person, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 10)))
            .thenReturn(List.of());

        perform(get("/api/persons/42/blackout-periods")
            .param("from", "2026-06-01")
            .param("to", "2026-06-10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.blackoutPeriods", hasSize(0)));
    }

    @Test
    void personsBlackoutPeriodsBadRequestWhenStartAfterEnd() throws Exception {

        perform(get("/api/persons/42/blackout-periods")
            .param("from", "2026-06-10")
            .param("to", "2026-06-01"))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertInstanceOf(ResponseStatusException.class, result.getResolvedException()));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
