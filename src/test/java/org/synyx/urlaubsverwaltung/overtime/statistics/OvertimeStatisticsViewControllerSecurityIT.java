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
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class OvertimeStatisticsViewControllerSecurityIT extends SingleTenantTestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private OvertimeStatisticsService overtimeStatisticsService;
    @MockitoBean
    private PersonService personService;

    @Test
    void ensuresUnauthorizedPersonCannotAccess() throws Exception {
        perform(
            get("/web/overtime/statistics")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/oauth2/authorization/default"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "INACTIVE", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensuresAuthorizedPersonWithIncorrectRoleCannotAccess(final String role) throws Exception {
        perform(get("/web/overtime/statistics")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role))))
            .andExpect(status().isForbidden());
    }

    @Test
    void ensuresAuthorizedPersonWithOfficeRoleCanAccess() throws Exception {

        final Person person = new Person("user", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);

        final OvertimeStatistics statistics = new OvertimeStatistics(Year.of(2024), LocalDate.of(2024, 6, 15), List.of(), 0);
        when(overtimeStatisticsService.createStatistics(any(Year.class))).thenReturn(statistics);

        perform(get("/web/overtime/statistics")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("OFFICE")))
        )
            .andExpect(status().isOk())
            .andExpect(view().name("overtime/overtime_statistics"));
    }

    @Test
    void ensuresAuthorizedPersonWithBossRoleCanAccess() throws Exception {

        final Person person = new Person("user", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);

        final OvertimeStatistics statistics = new OvertimeStatistics(Year.of(2024), LocalDate.of(2024, 6, 15), List.of(), 0);
        when(overtimeStatisticsService.createStatistics(any(Year.class))).thenReturn(statistics);

        perform(get("/web/overtime/statistics")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("BOSS")))
        )
            .andExpect(status().isOk())
            .andExpect(view().name("overtime/overtime_statistics"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
