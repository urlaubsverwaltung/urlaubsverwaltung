package org.synyx.urlaubsverwaltung.publicholiday;

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
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BAYERN;

@SpringBootTest
class PublicHolidayApiControllerSecurityIT extends SingleTenantTestContainersBase {

    @Autowired
    private WebApplicationContext context;
    @MockitoBean
    private PersonService personService;
    @MockitoBean
    private WorkingTimeService workingTimeService;
    @MockitoBean
    private DepartmentService departmentService;

    // WorkingTimeWriteService is required for personService
    @MockitoBean
    private WorkingTimeWriteService workingTimeWriteService;

    @Test
    void ensureNotToGetHolidaysWithoutAuthentication() throws Exception {
        perform(
            get("/api/public-holidays")
        )
            .andExpect(status().is4xxClientError());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "OFFICE", "INACTIVE"})
    void ensureGetHolidaysForOtherUserIsOk(final String role) throws Exception {
        perform(
            get("/api/public-holidays")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
                .param("from", "2016-01-01")
                .param("to", "2016-01-31")
        )
            .andExpect(status().isOk());
    }

    @Test
    void personsPublicHolidaysWithoutAuthenticationIsNotPossible() throws Exception {
        perform(
            get("/api/persons/1/public-holidays")
        )
            .andExpect(status().is4xxClientError());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "INACTIVE"})
    void ensuresPersonsPublicHolidaysForOtherUserIsForbidden(final String role) throws Exception {
        perform(
            get("/api/persons/1/public-holidays")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
                .param("from", "2016-01-01")
                .param("to", "2016-01-31")
        )
            .andExpect(status().isForbidden());
    }

    @Test
    void personsPublicHolidaysAsDepartmentHeadUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final Person departmentHead = new Person();
        departmentHead.setId(2L);
        departmentHead.setPermissions(List.of(DEPARTMENT_HEAD));
        when(personService.getPersonByUsername("departmentHead")).thenReturn(Optional.of(departmentHead));
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, person)).thenReturn(true);

        when(workingTimeService.getFederalStateForPerson(eq(person), any(LocalDate.class))).thenReturn(GERMANY_BAYERN);

        perform(
            get("/api/persons/1/public-holidays")
                .with(oidcLogin().idToken(builder -> builder.subject("departmentHead")).authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("DEPARTMENT_HEAD")))
                .param("from", "2016-01-01")
                .param("to", "2016-01-31")
        )
            .andExpect(status().isOk());
    }

    @Test
    void personsPublicHolidaysAsSSAUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final Person ssa = new Person();
        ssa.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        when(personService.getPersonByUsername("ssa")).thenReturn(Optional.of(ssa));
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(ssa, person)).thenReturn(true);
        when(workingTimeService.getFederalStateForPerson(eq(person), any(LocalDate.class))).thenReturn(GERMANY_BAYERN);

        perform(
            get("/api/persons/1/public-holidays")
                .with(oidcLogin().idToken(builder -> builder.subject("ssa")).authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("SECOND_STAGE_AUTHORITY")))
                .param("from", "2016-01-01")
                .param("to", "2016-01-31")
        )
            .andExpect(status().isOk());
    }

    @Test
    void personsPublicHolidaysWithBossRoleIsOk() throws Exception {

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(new Person()));
        when(workingTimeService.getFederalStateForPerson(any(Person.class), any(LocalDate.class))).thenReturn(GERMANY_BAYERN);

        perform(
            get("/api/persons/1/public-holidays")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("BOSS")))
                .param("from", "2016-01-01")
                .param("to", "2016-01-31")
        )
            .andExpect(status().isOk());
    }

    @Test
    void personsPublicHolidaysWithOfficeRoleIsOk() throws Exception {

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(new Person()));
        when(workingTimeService.getFederalStateForPerson(any(Person.class), any(LocalDate.class))).thenReturn(GERMANY_BAYERN);

        perform(
            get("/api/persons/1/public-holidays")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("OFFICE")))
                .param("from", "2016-01-01")
                .param("to", "2016-01-31")
        )
            .andExpect(status().isOk());
    }

    @Test
    void personsPublicHolidaysWithSameUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(workingTimeService.getFederalStateForPerson(any(Person.class), any(LocalDate.class))).thenReturn(GERMANY_BAYERN);

        perform(
            get("/api/persons/1/public-holidays")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER")))
                .param("from", "2016-01-01")
                .param("to", "2016-01-31")
        )
            .andExpect(status().isOk());
    }

    @Test
    void personsPublicHolidaysWithDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(
            get("/api/persons/1/public-holidays")
                .with(oidcLogin().idToken(builder -> builder.subject("differentUser")).authorities(new SimpleGrantedAuthority("USER")))
                .param("from", "2016-01-01")
                .param("to", "2016-01-31")
        )
            .andExpect(status().isForbidden());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
