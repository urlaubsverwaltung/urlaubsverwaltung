package org.synyx.urlaubsverwaltung.person.basedata;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class PersonBasedataViewControllerSecurityIT extends TestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PersonBasedataService personBasedataService;
    @MockBean
    private PersonService personService;

    @Test
    void ensuresUnauthorizedPersonCannotAccess() throws Exception {
        perform(get("/web/person/1/basedata"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void ensuresAuthorizedPersonWithoutRoleCannotAccess() throws Exception {
        perform(get("/web/person/1/basedata"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER", "DEPARTMENT_HEAD"})
    void ensuresAuthorizedPersonWithDepartmentHeadCannotAccess() throws Exception {
        perform(get("/web/person/1/basedata"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER", "SECOND_STAGE_AUTHORITY"})
    void ensuresAuthorizedPersonWithSecondStageAuthorityCannotAccess() throws Exception {
        perform(get("/web/person/1/basedata"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER", "BOSS"})
    void ensuresAuthorizedPersonWithBossRoleCannotAccess() throws Exception {
        perform(get("/web/person/1/basedata"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER", "ADMIN"})
    void ensuresAuthorizedPersonWithAdminRoleCannotAccess() throws Exception {
        perform(get("/web/person/1/basedata"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER", "INACTIVE"})
    void ensuresAuthorizedPersonWithInactiveRoleCannotAccess() throws Exception {
        perform(get("/web/person/1/basedata"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER", "OFFICE"})
    void ensuresAuthorizedPersonWithOfficeRoleCanAccess() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);

        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(personBasedataService.getBasedataByPersonId(1)).thenReturn(Optional.empty());
        when(personService.getSignedInUser()).thenReturn(person);

        perform(get("/web/person/1/basedata"))
            .andExpect(status().isOk())
            .andExpect(view().name("person/person-basedata"));
    }

    @Test
    void ensuresUnauthorizedPersonCannotPost() throws Exception {
        perform(
            post("/web/person/1/basedata")
                .with(csrf())
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void ensuresAuthorizedPersonWithoutRoleCannotPost() throws Exception {
        perform(
            post("/web/person/1/basedata")
                .with(csrf())
        )
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER", "DEPARTMENT_HEAD"})
    void ensuresAuthorizedPersonWithDepartmentHeadCannotPost() throws Exception {
        perform(
            post("/web/person/1/basedata")
                .with(csrf())
        )
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER", "SECOND_STAGE_AUTHORITY"})
    void ensuresAuthorizedPersonWithSecondStageAuthorityCannotPost() throws Exception {
        perform(
            post("/web/person/1/basedata")
                .with(csrf())
        )
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER", "BOSS"})
    void ensuresAuthorizedPersonWithBossRoleCannotPost() throws Exception {
        perform(
            post("/web/person/1/basedata")
                .with(csrf())
        )
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER", "ADMIN"})
    void ensuresAuthorizedPersonWithAdminRoleCannotPost() throws Exception {
        perform(
            post("/web/person/1/basedata")
                .with(csrf())
        )
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER", "INACTIVE"})
    void ensuresAuthorizedPersonWithInactiveRoleCannotPost() throws Exception {
        perform(
            post("/web/person/1/basedata")
                .with(csrf())
        )
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER", "OFFICE"})
    void ensuresAuthorizedPersonWithOfficeRoleCanPost() throws Exception {
        perform(
            post("/web/person/1/basedata")
                .param("personnelNumber", "1337")
                .param("additionalInfo", "Additional Information")
                .with(csrf())
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/person/1"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
