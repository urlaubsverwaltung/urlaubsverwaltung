package org.synyx.urlaubsverwaltung.absence.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.MessageSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.absence.AbsencePeriod;
import org.synyx.urlaubsverwaltung.absence.AbsenceService;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHoliday;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.ORANGE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BADEN_WUERTTEMBERG;

/**
 * Renders the real {@code absences/absences-overview} template (unlike {@link AbsenceOverviewViewControllerTest},
 * which never touches the view resolver) to guard the markup of the absence bars.
 */
@SpringBootTest(properties = "spring.thymeleaf.servlet.produce-partial-output-while-processing=false")
@AutoConfigureMockMvc
class AbsenceOverviewViewControllerIT extends SingleTenantTestContainersBase {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MessageSource messageSource;

    @MockitoBean
    private PersonService personService;
    @MockitoBean
    private DepartmentService departmentService;
    @MockitoBean
    private AbsenceService absenceService;
    @MockitoBean
    private VacationTypeService vacationTypeService;
    @MockitoBean
    private SettingsService settingsService;
    @MockitoBean
    private WorkingTimeService workingTimeService;
    @MockitoBean
    private WorkingTimeWriteService workingTimeWriteService;
    @MockitoBean
    private PublicHolidaysService publicHolidaysService;

    private Person office;

    @BeforeEach
    void setUp() {
        office = new Person("office", "Muster", "Marlene", "office@example.org");
        office.setId(1L);
        office.setPermissions(List.of(USER, OFFICE));

        when(personService.getSignedInUser()).thenReturn(office);
        when(personService.getActivePersons()).thenReturn(List.of(office));
        when(settingsService.getSettings()).thenReturn(new Settings());
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(
            ProvidedVacationType.builder(messageSource).id(42L).color(ORANGE).category(VacationCategory.HOLIDAY)
                .messageKey("application.data.vacationType.holiday").build()));

        // Thu 9th, Fri 10th, weekend, Mon 13th January 2025
        final AbsencePeriod vacation = new AbsencePeriod(IntStream.of(9, 10, 13)
            .mapToObj(day -> new AbsencePeriod.Record(LocalDate.of(2025, 1, day), office,
                new AbsencePeriod.RecordMorningVacation(office, 7L, AbsencePeriod.AbsenceStatus.ALLOWED, "HOLIDAY", 42L, false),
                new AbsencePeriod.RecordNoonVacation(office, 7L, AbsencePeriod.AbsenceStatus.ALLOWED, "HOLIDAY", 42L, false)))
            .toList());
        final AbsencePeriod weekend = new AbsencePeriod(IntStream.of(11, 12)
            .mapToObj(day -> new AbsencePeriod.Record(LocalDate.of(2025, 1, day), office,
                new AbsencePeriod.RecordMorningNoWorkday(office), new AbsencePeriod.RecordNoonNoWorkday(office)))
            .toList());
        when(absenceService.getOpenAbsences(anyList(), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(vacation, weekend));
    }

    @Test
    void rendersTheBarsOfAnAbsence() throws Exception {
        mockMvc.perform(get("/web/absences").param("year", "2025").param("month", "1")
                .locale(Locale.GERMAN)
                .with(csrf())
                .with(oidcSubject(office, List.of(USER, OFFICE))))
            .andExpect(status().isOk())
            .andExpect(content().string(matchesPattern("(?s).*absence-bar absence-bar--full absence-bar--solid absence-bar--status-allowed absence-bar--start\"\\s+style=\"--absence-bar-color:var\\(--absence-color-ORANGE\\).*")))
            .andExpect(content().string(matchesPattern("(?s).*absence-bar absence-bar--full absence-bar--bridge absence-bar--status-allowed\"\\s+style=\"--absence-bar-color:var\\(--absence-color-ORANGE\\).*")))
            .andExpect(content().string(matchesPattern("(?s).*absence-bar--solid absence-bar--status-allowed absence-bar--end\"\\s+style=\"--absence-bar-color:var\\(--absence-color-ORANGE\\).*")))
            .andExpect(content().string(containsString("--absence-bar-color:var(--absence-color-ORANGE)")))
            .andExpect(content().string(containsString("data-title=\"Erholungsurlaub\"")))
            .andExpect(content().string(containsString("data-title=\"Kein Arbeitstag\"")))
            .andExpect(content().string(containsString("style=\"--label-halves:4\"")))
            .andExpect(content().string(containsString(">Erholungsurlaub</span>")));
    }

    @Test
    void omitsTheLabelOfAHalfDayBar() throws Exception {

        // Wed 15th January 2025, morning only
        final AbsencePeriod morning = new AbsencePeriod(List.of(new AbsencePeriod.Record(LocalDate.of(2025, 1, 15), office,
            new AbsencePeriod.RecordMorningVacation(office, 8L, AbsencePeriod.AbsenceStatus.ALLOWED, "HOLIDAY", 42L, false))));
        when(absenceService.getOpenAbsences(anyList(), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(morning));

        mockMvc.perform(get("/web/absences").param("year", "2025").param("month", "1")
                .locale(Locale.GERMAN)
                .with(csrf())
                .with(oidcSubject(office, List.of(USER, OFFICE))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("absence-bar absence-bar--morning absence-bar--solid absence-bar--status-allowed absence-bar--start absence-bar--end")))
            .andExpect(content().string(not(containsString("class=\"absence-bar-label\""))));
    }

    @Test
    void announcesAHalfDayPublicHolidayNextToAMorningAbsence() throws Exception {

        // Tue 24th December 2024: vacation in the morning, Heiligabend at noon
        final DateRange december = new DateRange(LocalDate.of(2024, 12, 1), LocalDate.of(2024, 12, 31));
        when(workingTimeService.getWorkingTimesByPersonsAndDateRange(anyList(), eq(december)))
            .thenReturn(Map.of(office, Map.of(december, new WorkingTime(office, december.startDate(), GERMANY_BADEN_WUERTTEMBERG, false))));
        when(publicHolidaysService.getPublicHolidays(december.startDate(), december.endDate(), GERMANY_BADEN_WUERTTEMBERG))
            .thenReturn(List.of(new PublicHoliday(LocalDate.of(2024, 12, 24), NOON, "Heiligabend")));

        final AbsencePeriod christmasEve = new AbsencePeriod(List.of(new AbsencePeriod.Record(LocalDate.of(2024, 12, 24), office,
            new AbsencePeriod.RecordMorningVacation(office, 9L, AbsencePeriod.AbsenceStatus.ALLOWED, "HOLIDAY", 42L, false),
            new AbsencePeriod.RecordNoonPublicHoliday(office))));
        when(absenceService.getOpenAbsences(anyList(), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(christmasEve));

        mockMvc.perform(get("/web/absences").param("year", "2024").param("month", "12")
                .locale(Locale.GERMAN)
                .with(csrf())
                .with(oidcSubject(office, List.of(USER, OFFICE))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("absence-bar absence-bar--morning absence-bar--solid")))
            .andExpect(content().string(matchesPattern("(?s).*class=\"sr-only\"\\s*>Heiligabend</span>.*")));
    }

    private static OidcLoginRequestPostProcessor oidcSubject(Person person, List<Role> roles) {
        final OidcIdToken.Builder tokenBuilder = OidcIdToken.withTokenValue("not-empty-token-value")
            .claim("sub", person.getUsername());
        final List<SimpleGrantedAuthority> authorities = roles.stream().map(Role::name).map(SimpleGrantedAuthority::new).toList();
        return oidcLogin().oidcUser(new DefaultOidcUser(authorities, tokenBuilder.build()));
    }
}
