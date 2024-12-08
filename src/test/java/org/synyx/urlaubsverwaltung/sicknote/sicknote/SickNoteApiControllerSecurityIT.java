package org.synyx.urlaubsverwaltung.sicknote.sicknote;

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
import org.synyx.urlaubsverwaltung.person.Role;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_VIEW;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest
class SickNoteApiControllerSecurityIT extends SingleTenantTestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PersonService personService;
    @MockitoBean
    private SickNoteService sickNoteService;
    @MockitoBean
    private DepartmentService departmentService;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Test
    void getSickNotesWithoutBasicAuthIsUnauthorized() throws Exception {
        perform(
            get("/api/sicknotes")
        )
            .andExpect(status().is4xxClientError());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "INACTIVE"})
    void getSicknotesIsForbidden(final String role) throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        perform(
            get("/api/sicknotes")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        )
            .andExpect(status().isForbidden());
    }

    @Test
    void personsSickNotesWithoutBasicAuthIsUnauthorized() throws Exception {
        perform(
            get("/api/persons/1/sicknotes")
        )
            .andExpect(status().is4xxClientError());
    }

    @Test
    void personsSickNotesWithOfficeUserOnlyIsOk() throws Exception {

        final Person person = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final Person office = new Person();
        office.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(office);

        when(sickNoteService.getByPersonAndPeriod(any(), any(), any())).thenReturn(List.of(SickNote.builder().build()));

        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/persons/1/sicknotes")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("OFFICE")))
        )
            .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"OFFICE", "BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void personsSickNotesAndSickNoteViewAuthorityIsOk(final String role) throws Exception {

        final Person management = new Person();
        management.setPermissions(List.of(USER, SICK_NOTE_VIEW, Role.valueOf(role)));
        when(personService.getSignedInUser()).thenReturn(management);

        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/sicknotes")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("SICK_NOTE_VIEW"), new SimpleGrantedAuthority(role)))
        )
            .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void personsSickNotesWithoutSickNoteViewAuthorityIsForbidden(final String role) throws Exception {
        final Person person = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final Person signedInPerson = new Person();
        signedInPerson.setPermissions(List.of(USER, Role.valueOf(role)));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/persons/1/sicknotes")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        )
            .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "INACTIVE"})
    void personsSickNotesIsForbidden(final String role) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        perform(
            get("/api/persons/1/sicknotes")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        )
            .andExpect(status().isForbidden());
    }

    @Test
    void personsSickNotesWithSameUserIsOk() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        when(sickNoteService.getByPersonAndPeriod(any(), any(), any())).thenReturn(List.of(SickNote.builder().build()));

        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/persons/1/sicknotes")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .with(oidcLogin().idToken(builder -> builder.subject("user")).authorities(new SimpleGrantedAuthority("USER")))
        ).andExpect(status().isOk());
    }

    @Test
    void personsSickNotesWithDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final Person otherPerson = new Person();
        otherPerson.setId(1L);
        otherPerson.setUsername("other person");
        when(personService.getSignedInUser()).thenReturn(otherPerson);

        final LocalDateTime now = LocalDateTime.now();

        perform(get("/api/persons/1/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
            .with(oidcLogin().idToken(builder -> builder.subject("differentUser")).authorities(new SimpleGrantedAuthority("USER")))
        ).andExpect(status().isForbidden());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
