package org.synyx.urlaubsverwaltung.overtime.statistics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class OvertimeStatisticsViewControllerSecurityIT extends SingleTenantTestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PersonService personService;

    @MockitoBean
    private SettingsService settingsService;

    @ParameterizedTest
    @ValueSource(strings = {"USER", "INACTIVE", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensureNoAccessForRolesWithoutCompanyWidePermission(final String role) throws Exception {

        signedInUser();
        overtimeFeature(true);

        perform(get("/web/overtime/statistics")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority(role)))
        ).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"OFFICE", "BOSS"})
    void ensureAccessAndRenderedPageForOfficeAndBoss(final String role) throws Exception {

        signedInUser();
        overtimeFeature(true);

        perform(get("/web/overtime/statistics")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        )
            .andExpect(status().isOk())
            // proves the page is actually rendered, including the year selector fragment
            // and the mount point of the chart
            .andExpect(content().string(containsString("/web/overtime/statistics?year=")))
            .andExpect(content().string(containsString("id=\"overtime-statistics-chart\"")))
            .andExpect(content().string(containsString("statistic-summary-card")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"OFFICE", "BOSS"})
    void ensureNotFoundWhenOvertimeFeatureIsDeactivated(final String role) throws Exception {

        signedInUser();
        overtimeFeature(false);

        perform(get("/web/overtime/statistics")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        ).andExpect(status().isNotFound());
    }

    private void signedInUser() {
        final Person person = new Person("user", "Reichenbach", "Marie", "person@example.org");
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);
        // the real statistics service runs here, an empty company renders the page with zeros everywhere
        when(personService.getAllPersonsHavingAccountInYear(any())).thenReturn(List.of());
    }

    private void overtimeFeature(boolean active) {
        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(active);
        when(settingsService.getSettings()).thenReturn(settings);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
