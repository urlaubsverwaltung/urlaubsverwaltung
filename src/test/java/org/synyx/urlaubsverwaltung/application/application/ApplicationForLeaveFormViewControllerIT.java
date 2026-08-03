package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.MessageSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettings;
import org.synyx.urlaubsverwaltung.application.specialleave.SpecialLeaveSettingsService;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeDto;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeViewModelService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.math.BigDecimal.TEN;
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.ORANGE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

/**
 * Renders the real {@code application/application_form} Thymeleaf template (unlike
 * {@link ApplicationForLeaveFormViewControllerTest}, which uses a standalone MockMvc setup that never touches the
 * view resolver) to guard against regressions that only show up in the rendered HTML.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApplicationForLeaveFormViewControllerIT extends SingleTenantTestContainersBase {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PersonService personService;
    @MockitoBean
    private DepartmentService departmentService;
    @MockitoBean
    private AccountService accountService;
    @MockitoBean
    private VacationTypeService vacationTypeService;
    @MockitoBean
    private VacationTypeViewModelService vacationTypeViewModelService;
    @MockitoBean
    private ApplicationInteractionService applicationInteractionService;
    @MockitoBean
    private SettingsService settingsService;
    @MockitoBean
    private SpecialLeaveSettingsService specialLeaveSettingsService;

    @Test
    void editApplicationFormShowsExistingHolidayReplacementsEvenWhenNobodyIsLeftToAdd() throws Exception {

        final Locale locale = Locale.GERMAN;

        final Person person = new Person("applicant", "Applicant", "Anna", "anna@example.org");
        person.setId(1L);
        person.setPermissions(List.of(USER));

        final Person batman = new Person("batman", "Wayne", "Bruce", "bruce@example.org");
        batman.setId(2L);
        batman.setPermissions(List.of(USER));

        final Person joker = new Person("joker", "Fleck", "Arthur", "arthur@example.org");
        joker.setId(3L);
        joker.setPermissions(List.of(USER));

        when(personService.getSignedInUser()).thenReturn(person);
        // no active person is left that isn't already the applicant or an existing replacement
        when(personService.getActivePersons()).thenReturn(List.of(person, batman, joker));

        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage("message-key", new Object[]{}, locale)).thenReturn("Erholungsurlaub");
        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("message-key")
            .build();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(vacationType));
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));

        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expireDate = LocalDate.of(2014, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, true, expireDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(Year.now().getValue(), person)).thenReturn(Optional.of(account));

        final Settings settings = new Settings();
        settings.setApplicationSettings(new ApplicationSettings());
        when(settingsService.getSettings()).thenReturn(settings);

        final HolidayReplacementEntity batmanReplacement = new HolidayReplacementEntity();
        batmanReplacement.setPerson(batman);
        batmanReplacement.setNote("");

        final HolidayReplacementEntity jokerReplacement = new HolidayReplacementEntity();
        jokerReplacement.setPerson(joker);
        jokerReplacement.setNote("");

        final Application application = new Application();
        application.setId(1L);
        application.setPerson(person);
        application.setStatus(WAITING);
        application.setVacationType(vacationType);
        application.setHolidayReplacements(List.of(batmanReplacement, jokerReplacement));
        when(applicationInteractionService.get(1L)).thenReturn(Optional.of(application));

        mockMvc.perform(
                get("/web/application/1/edit")
                    .locale(locale)
                    .with(oidcSubject(person, List.of(USER)))
                    .with(csrf())
            )
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("data-test-id=\"holiday-replacement-row\"")))
            .andExpect(content().string(containsString("Bruce Wayne")))
            .andExpect(content().string(containsString("Arthur Fleck")));
    }

    private static OidcLoginRequestPostProcessor oidcSubject(Person person, List<Role> roles) {

        final OidcIdToken.Builder tokenBuilder = OidcIdToken.withTokenValue("not-empty-token-value")
            .claim("sub", person.getUsername());

        final List<SimpleGrantedAuthority> authorities = roles.stream().map(Role::name).map(SimpleGrantedAuthority::new).toList();

        final OidcUser oidcUser = new DefaultOidcUser(authorities, tokenBuilder.build());

        return oidcLogin().oidcUser(oidcUser);
    }
}
