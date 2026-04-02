package org.synyx.urlaubsverwaltung.sicknote.me;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.sicknote.me.SickNotesViewController.MY_SICKNOTES_ANONYMOUS_PATH;
import static org.synyx.urlaubsverwaltung.sicknote.me.SickNotesViewController.MY_SICKNOTES_PATH;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;

@ExtendWith(MockitoExtension.class)
class SickNotesViewControllerTest {

    private SickNotesViewController sut;

    @Mock
    private PersonService personService;
    @Mock
    private WorkDaysCountService workDaysCountService;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private DepartmentService departmentService;

    private final Clock clock = Clock.fixed(ZonedDateTime.of(LocalDate.of(2022, 6, 15).atStartOfDay(), ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    @BeforeEach
    void setUp() {
        sut = new SickNotesViewController(personService, workDaysCountService, sickNoteService, departmentService, clock);
    }

    @Test
    void showMySickNotesAnonymousRedirectsToPersonSickNotesWithoutYear() throws Exception {
        final Person signedIn = new Person();
        signedIn.setId(11L);
        when(personService.getSignedInUser()).thenReturn(signedIn);

        perform(get(MY_SICKNOTES_ANONYMOUS_PATH))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/persons/11/sicknotes"));
    }

    @Test
    void showMySickNotesAnonymousRedirectsToPersonSickNotesWithYear() throws Exception {
        final Person signedIn = new Person();
        signedIn.setId(12L);
        when(personService.getSignedInUser()).thenReturn(signedIn);

        perform(get(MY_SICKNOTES_ANONYMOUS_PATH).param("year", "2020"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/persons/12/sicknotes?year=2020"));
    }

    @Test
    void showMySickNotesForPersonShowsViewAndEmptyList() throws Exception {
        final Person person = new Person();
        person.setId(5L);

        when(personService.getPersonByID(5L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "5")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("person", equalTo(person)))
            .andExpect(model().attribute("sickNotes", hasSize(0)));
    }

    @Test
    void showMySickNotesForPersonWithSickNotesProvidesSummary() throws Exception {
        final Person person = new Person();
        person.setId(6L);

        when(personService.getPersonByID(6L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setId(1L);
        sickNoteType.setCategory(SICK_NOTE);
        sickNoteType.setMessageKey("key");

        final SickNote sickNote = SickNote.builder()
            .id(42L)
            .person(person)
            .startDate(LocalDate.of(2022, 1, 2))
            .endDate(LocalDate.of(2022, 1, 4))
            .dayLength(FULL)
            .sickNoteType(sickNoteType)
            .status(ACTIVE)
            .build();

        when(sickNoteService.getByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(sickNote));
        when(workDaysCountService.getWorkDaysCount(any(), any(LocalDate.class), any(LocalDate.class), eq(person))).thenReturn(ONE);

        perform(get(MY_SICKNOTES_PATH.replace("{personId}", "6")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/sicknotes"))
            .andExpect(model().attribute("sickNotes", hasSize(1)))
            .andExpect(model().attributeExists("sickDaysOverview"))
            .andExpect(model().attribute("sickNotes",
                hasItem(hasProperty("id", is(42L)))));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}


