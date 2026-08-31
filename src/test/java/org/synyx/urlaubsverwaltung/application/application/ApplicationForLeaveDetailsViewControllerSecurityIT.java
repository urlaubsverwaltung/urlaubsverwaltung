package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentService;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static java.time.Month.AUGUST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;

/**
 * The unit tests of {@link ApplicationForLeaveDetailsViewController} use a standalone {@code MockMvc} setup, which does
 * not apply method security. This test therefore covers the {@code @PreAuthorize} layer of the endpoints and the
 * permission to access the data of the person of an application.
 */
@SpringBootTest
class ApplicationForLeaveDetailsViewControllerSecurityIT extends SingleTenantTestContainersBase {

    private static final long APPLICATION_ID = 1L;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PersonService personService;
    @MockitoBean
    private ApplicationService applicationService;
    @MockitoBean
    private ApplicationInteractionService applicationInteractionService;
    @MockitoBean
    private ApplicationCommentService commentService;
    @MockitoBean
    private DepartmentService departmentService;

    @ParameterizedTest
    @ValueSource(strings = {"USER", "OFFICE", "APPLICATION_ADD", "APPLICATION_EDIT", "APPLICATION_CANCEL"})
    void ensureAllowIsForbiddenWithoutManagementRole(final String role) throws Exception {
        perform(post("/web/application/" + APPLICATION_ID + "/allow")
            .with(oidcLogin().authorities(signIn("USER", role)))
            .with(csrf())
        ).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "OFFICE", "APPLICATION_ADD", "APPLICATION_EDIT", "APPLICATION_CANCEL"})
    void ensureRejectIsForbiddenWithoutManagementRole(final String role) throws Exception {
        perform(post("/web/application/" + APPLICATION_ID + "/reject")
            .with(oidcLogin().authorities(signIn("USER", role)))
            .with(csrf())
        ).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "APPLICATION_ADD", "APPLICATION_EDIT", "APPLICATION_CANCEL"})
    void ensureCommentIsForbiddenWithoutManagementRole(final String role) throws Exception {
        perform(post("/web/application/" + APPLICATION_ID + "/comment")
            .with(oidcLogin().authorities(signIn("USER", role)))
            .with(csrf())
        ).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "OFFICE"})
    void ensureCommentIsAllowedForManagementOfThePerson(final String role) throws Exception {
        final SimpleGrantedAuthority[] authorities = signIn("USER", role);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(any(), any())).thenReturn(true);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(any(), any())).thenReturn(true);

        perform(post("/web/application/" + APPLICATION_ID + "/comment")
            .with(oidcLogin().authorities(authorities))
            .with(csrf())
            .param("text", "a comment")
        ).andExpect(status().is3xxRedirection());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "OFFICE"})
    void ensureCancelIsForbiddenWithoutAccessToThePersonData(final String role) throws Exception {
        // nobody may touch the application of a person whose data they may not access, no matter which role they hold
        perform(post("/web/application/" + APPLICATION_ID + "/cancel")
            .with(oidcLogin().authorities(signIn("USER", role)))
            .with(csrf())
            .param("text", "a comment")
        ).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "APPLICATION_ADD", "APPLICATION_EDIT"})
    void ensureDeclineCancellationRequestIsForbiddenWithoutTheMatchingRole(final String role) throws Exception {
        when(departmentService.isSignedInUserAllowedToAccessPersonData(any(), any())).thenReturn(true);

        perform(post("/web/application/" + APPLICATION_ID + "/decline-cancellation-request")
            .with(oidcLogin().authorities(signIn("USER", role)))
            .with(csrf())
        ).andExpect(status().isForbidden());
    }

    private SimpleGrantedAuthority[] signIn(String... roles) {

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(Arrays.stream(roles).map(Role::valueOf).toList());

        final Person applicationPerson = new Person();
        applicationPerson.setId(2L);

        final Application application = new Application();
        application.setId(APPLICATION_ID);
        application.setPerson(applicationPerson);
        application.setStatus(ALLOWED);
        application.setStartDate(LocalDate.of(2025, AUGUST, 4));
        application.setEndDate(LocalDate.of(2025, AUGUST, 8));
        application.setVacationType(ProvidedVacationType.builder(new StaticMessageSource())
            .id(1L)
            .category(VacationCategory.HOLIDAY)
            .messageKey("vacationTypeMessageKey")
            .requiresApprovalToCancel(true)
            .build());

        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(Optional.of(application));

        return Arrays.stream(roles).map(SimpleGrantedAuthority::new).toArray(SimpleGrantedAuthority[]::new);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
