package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_VIEW;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest
class SickNoteApiControllerSecurityIT extends TestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PersonService personService;
    @MockBean
    private SickNoteService sickNoteService;
    @MockBean
    private DepartmentService departmentService;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Test
    void getSickNotesWithoutBasicAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/sicknotes"));
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {"OFFICE", "SICK_NOTE_VIEW"})
    void getSickNotesWithBasicAuthIsOk() throws Exception {

        final Person office = new Person();
        office.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(office);

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))));

        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getSicknotesWithNotPrivilegedUserIsForbidden() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        perform(get("/api/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    void getSicknotesWithBossUserIsForbidden() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        perform(get("/api/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    void getSicknotesWithDepartmentHeadUserIsForbidden() throws Exception {

        final LocalDateTime now = LocalDateTime.now();
        perform(get("/api/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    void getSicknotesWithSecondStageUserIsForbidden() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        perform(get("/api/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void getSicknotesWithUserIsForbidden() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        perform(get("/api/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    void getSicknotesWithInactiveIsForbidden() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        perform(get("/api/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getSicknotesWithAdminIsForbidden() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        perform(get("/api/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))))
            .andExpect(status().isForbidden());
    }

    @Test
    void personsSickNotesWithoutBasicAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons/1/sicknotes"));
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    void personsSickNotesWithOfficeUserOnlyIsOk() throws Exception {

        final Person person = new Person();
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final Person office = new Person();
        office.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(office);

        when(sickNoteService.getByPersonAndPeriod(any(), any(), any())).thenReturn(List.of(new SickNote()));

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))));

        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"BOSS", "SICK_NOTE_VIEW"})
    void personsSickNotesWithBossUserAndSickNoteViewAuthorityIsOk() throws Exception {
        final Person person = new Person();
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final Person office = new Person();
        office.setPermissions(List.of(USER, BOSS, SICK_NOTE_VIEW));
        when(personService.getSignedInUser()).thenReturn(office);

        when(sickNoteService.getByPersonAndPeriod(any(), any(), any())).thenReturn(List.of(new SickNote()));

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))));

        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"DEPARTMENT_HEAD", "SICK_NOTE_VIEW"})
    void personsSickNotesWithDepartmentHeadUserAndSickNoteViewAuthorityIsOk() throws Exception {
        final Person person = new Person();
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final Person departmentHead = new Person();
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD, SICK_NOTE_VIEW));
        when(personService.getSignedInUser()).thenReturn(departmentHead);

        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, person)).thenReturn(true);
        when(sickNoteService.getByPersonAndPeriod(any(), any(), any())).thenReturn(List.of(new SickNote()));

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))));

        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"BOSS"})
    void personsSickNotesWithBossUserWithoutSickNoteViewAuthorityIsForbidden() throws Exception {
        final Person person = new Person();
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final Person signedInPerson = new Person();
        signedInPerson.setPermissions(List.of(USER, BOSS));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"DEPARTMENT_HEAD"})
    void personsSickNotesWithDepartmentHeadUserWithoutSickNoteViewAuthorityIsForbidden() throws Exception {
        final Person person = new Person();
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final Person signedInPerson = new Person();
        signedInPerson.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"SECOND_STAGE_AUTHORITY"})
    void personsSickNotesWithSecondStageUserWithoutSickNoteViewAuthorityIsForbidden() throws Exception {
        final Person person = new Person();
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final Person signedInPerson = new Person();
        signedInPerson.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void personsSickNotesWithNotPrivilegedUserIsForbidden() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        perform(get("/api/persons/1/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void personsSickNotesWithUserIsForbidden() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        perform(get("/api/persons/1/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    void personsSickNotesWithInactiveIsForbidden() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        perform(get("/api/persons/1/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void personsSickNotesWithAdminIsForbidden() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        perform(get("/api/persons/1/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user")
    void personsSickNotesWithSameUserIsOk() throws Exception {

        final Person person = new Person();
        person.setId(1);
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        when(sickNoteService.getByPersonAndPeriod(any(), any(), any())).thenReturn(List.of(new SickNote()));

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
        );

        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "differentUser")
    void personsSickNotesWithDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setId(1);
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final Person otherPerson = new Person();
        otherPerson.setId(1);
        otherPerson.setUsername("other person");
        when(personService.getSignedInUser()).thenReturn(otherPerson);

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
        );

        resultActions.andExpect(status().isForbidden());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
