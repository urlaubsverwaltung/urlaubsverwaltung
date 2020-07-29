package org.synyx.urlaubsverwaltung.sicknote.api;

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
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SickNoteApiControllerSecurityIT extends TestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PersonService personService;
    @MockBean
    private SickNoteService sickNoteService;

    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Test
    void getSickNotesWithoutBasicAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/sicknotes"));
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    void getSickNotesWithBasicAuthIsOk() throws Exception {
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
    @WithMockUser(username = "user")
    void getSickNotesWithSameUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(sickNoteService.getByPersonAndPeriod(any(), any(), any())).thenReturn(List.of(new SickNote()));

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
            .param("person", "1")
        );

        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "differentUser")
    void getSickNotesWithDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
            .param("person", "1")
        );

        resultActions.andExpect(status().isForbidden());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
