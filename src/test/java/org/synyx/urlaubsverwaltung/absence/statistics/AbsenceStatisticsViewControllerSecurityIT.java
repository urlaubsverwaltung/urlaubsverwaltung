package org.synyx.urlaubsverwaltung.absence.statistics;

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
import org.thymeleaf.exceptions.TemplateInputException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that {@code @PreAuthorize} on {@link AbsenceStatisticsViewController} actually blocks the request
 * before it reaches the controller - {@code standaloneSetup}-based {@link AbsenceStatisticsViewControllerTest}
 * cannot prove this, since it never wires in Spring Security.
 *
 * <p>
 * The allowed-role cases only assert that the controller method ran (a signed-in user was fetched), not that the
 * page renders successfully - the template lands in Task 07, and asserting on it here would just test whether
 * Thymeleaf finds a file, not whether authorization works.
 */
@SpringBootTest
class AbsenceStatisticsViewControllerSecurityIT extends SingleTenantTestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PersonService personService;

    @ParameterizedTest
    @ValueSource(strings = {"USER", "INACTIVE"})
    void ensureNoAccessForRolesWithoutPermission(final String role) throws Exception {

        perform(get("/web/absence/statistics")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority(role)))
        ).andExpect(status().isForbidden());

        verifyNoInteractions(personService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"OFFICE", "BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensureAccessForPermittedRoles(final String role) {

        final Person person = new Person("user", "Reichenbach", "Marie", "person@example.org");
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);

        try {
            perform(get("/web/absence/statistics")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
            );
        } catch (Exception e) {
            // expected until Task 07 adds the template: the controller method itself already ran by the time
            // rendering fails, which is exactly what this test needs to prove - anything else is a real failure.
            assertThat(e).hasRootCauseInstanceOf(TemplateInputException.class);
        }

        // proves @PreAuthorize let the request through into the controller, regardless of how the (still
        // template-less, pre-Task-07) response then renders. Called more than once in a full context - the
        // person-search infrastructure (HasPersonSearch) also resolves the signed-in user.
        verify(personService, atLeastOnce()).getSignedInUser();
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
