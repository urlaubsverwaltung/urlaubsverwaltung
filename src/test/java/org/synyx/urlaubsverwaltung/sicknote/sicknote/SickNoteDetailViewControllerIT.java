package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.time.Month.JANUARY;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.SUBMITTED;

/**
 * Renders the real {@code sicknote/sick_note_detail} Thymeleaf template (unlike {@link SickNoteViewControllerTest},
 * which uses a standalone MockMvc setup that never touches the view resolver) to guard the action URLs of the
 * {@code allow} and {@code cancel} forms - they must carry the context path exactly once, and the {@code redirect}
 * query parameter must stay a plain path, because {@code SickNoteViewController} compares it literally.
 */
// the action forms sit far down the page - without buffering the whole render, the response is already committed
// when Thymeleaf asks for the session to write the CSRF token of th:action
@SpringBootTest(properties = "spring.thymeleaf.servlet.produce-partial-output-while-processing=false")
@AutoConfigureMockMvc
class SickNoteDetailViewControllerIT extends SingleTenantTestContainersBase {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PersonService personService;
    @MockitoBean
    private DepartmentService departmentService;
    @MockitoBean
    private SickNoteService sickNoteService;
    @MockitoBean
    private SettingsService settingsService;

    private Person office;

    @BeforeEach
    void setUp() {

        office = new Person("office", "Muster", "Marlene", "office@example.org");
        office.setId(1L);
        office.setPermissions(List.of(USER, OFFICE));

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SICK_NOTE);
        sickNoteType.setMessageKey("application.data.sicknotetype.sicknote");

        final SickNote sickNote = SickNote.builder()
            .id(42L)
            .person(office)
            .applier(office)
            .sickNoteType(sickNoteType)
            .startDate(LocalDate.of(2022, JANUARY, 10))
            .endDate(LocalDate.of(2022, JANUARY, 11))
            .dayLength(FULL)
            .status(SUBMITTED)
            .build();

        when(personService.getSignedInUser()).thenReturn(office);
        when(sickNoteService.getById(42L)).thenReturn(Optional.of(sickNote));
        when(departmentService.getAssignedDepartmentsOfMember(office)).thenReturn(List.of());
        when(settingsService.getSettings()).thenReturn(new Settings());
    }

    @Test
    void allowFormActionKeepsContextPathExactlyOnce() throws Exception {

        mockMvc.perform(get("/ctx/web/sicknote/42").contextPath("/ctx")
                .param("action", "allow")
                .locale(Locale.GERMAN)
                .with(oidcSubject(office, List.of(USER, OFFICE))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("action=\"/ctx/web/sicknote/42/accept?redirect=\"")))
            .andExpect(content().string(not(containsString("/ctx/ctx/"))));
    }

    @Test
    void allowFormActionCarriesTheOriginWithoutContextPathInTheQueryParameter() throws Exception {

        mockMvc.perform(get("/ctx/web/sicknote/42").contextPath("/ctx")
                .param("action", "allow")
                .param("redirect", "/web/application")
                .locale(Locale.GERMAN)
                .with(oidcSubject(office, List.of(USER, OFFICE))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(
                "action=\"/ctx/web/sicknote/42/accept?redirect=/web/application\"")));
    }

    @Test
    void anyPathOfThisApplicationIsHandedOnAsTheRedirectOfTheActionForms() throws Exception {

        // the page the action was started from decides where to return to, not a hardcoded target
        mockMvc.perform(get("/ctx/web/sicknote/42").contextPath("/ctx")
                .param("action", "allow")
                .param("redirect", "/web/persons/5/sicknotes")
                .locale(Locale.GERMAN)
                .with(oidcSubject(office, List.of(USER, OFFICE))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(
                "action=\"/ctx/web/sicknote/42/accept?redirect=/web/persons/5/sicknotes\"")))
            .andExpect(content().string(containsString(
                "action=\"/ctx/web/sicknote/42/cancel?redirect=/web/persons/5/sicknotes\"")));
    }

    @Test
    void cancelFormActionKeepsContextPathExactlyOnce() throws Exception {

        mockMvc.perform(get("/ctx/web/sicknote/42").contextPath("/ctx")
                .param("action", "cancel")
                .locale(Locale.GERMAN)
                .with(oidcSubject(office, List.of(USER, OFFICE))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("action=\"/ctx/web/sicknote/42/cancel?redirect=\"")));
    }

    @Test
    void cancelFormActionCarriesTheOriginWithoutContextPathInTheQueryParameter() throws Exception {

        mockMvc.perform(get("/ctx/web/sicknote/42").contextPath("/ctx")
                .param("action", "cancel")
                .param("redirect", "/web/application")
                .locale(Locale.GERMAN)
                .with(oidcSubject(office, List.of(USER, OFFICE))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(
                "action=\"/ctx/web/sicknote/42/cancel?redirect=/web/application\"")));
    }

    @Test
    void formActionsAreRenderedWithoutContextPathWhenDeployedAtRoot() throws Exception {

        mockMvc.perform(get("/web/sicknote/42")
                .param("action", "allow")
                .param("redirect", "/web/application")
                .locale(Locale.GERMAN)
                .with(oidcSubject(office, List.of(USER, OFFICE))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(
                "action=\"/web/sicknote/42/accept?redirect=/web/application\"")))
            .andExpect(content().string(containsString(
                "action=\"/web/sicknote/42/cancel?redirect=/web/application\"")));
    }

    private static OidcLoginRequestPostProcessor oidcSubject(Person person, List<Role> roles) {

        final OidcIdToken.Builder tokenBuilder = OidcIdToken.withTokenValue("not-empty-token-value")
            .claim("sub", person.getUsername());

        final List<SimpleGrantedAuthority> authorities = roles.stream().map(Role::name).map(SimpleGrantedAuthority::new).toList();

        final OidcUser oidcUser = new DefaultOidcUser(authorities, tokenBuilder.build());

        return oidcLogin().oidcUser(oidcUser);
    }
}
