package org.synyx.urlaubsverwaltung.person.basedata;

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

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class PersonBasedataViewControllerSecurityIT extends SingleTenantTestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PersonBasedataService personBasedataService;
    @MockitoBean
    private PersonService personService;

    @Test
    void ensuresUnauthorizedPersonCannotAccess() throws Exception {
        perform(
            get("/web/person/1/basedata")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("http://localhost/oauth2/authorization/default"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "INACTIVE", "USER"})
    void ensuresAuthorizedPersonWithIncorrectRoleCannotAccess(final String role) throws Exception {
        perform(get("/web/person/1/basedata")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role))))
            .andExpect(status().isForbidden());
    }

    @Test
    void ensuresAuthorizedPersonWithOfficeRoleCanAccess() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personBasedataService.getBasedataByPersonId(1)).thenReturn(Optional.empty());
        when(personService.getSignedInUser()).thenReturn(person);

        perform(get("/web/person/1/basedata")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("OFFICE")))
        )
            .andExpect(status().isOk())
            .andExpect(view().name("person/person-basedata")
            );
    }

    @Test
    void ensuresUnauthorizedPersonCannotPost() throws Exception {
        perform(
            post("/web/person/1/basedata")
                .with(csrf())
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("http://localhost/oauth2/authorization/default"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "INACTIVE", "USER"})
    void ensuresAuthorizedPersonWithoutRoleCannotPost(final String role) throws Exception {
        perform(
            post("/web/person/1/basedata")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
                .with(csrf())
        )
            .andExpect(status().isForbidden());
    }

    @Test
    void ensuresAuthorizedPersonWithOfficeRoleCanPost() throws Exception {
        perform(
            post("/web/person/1/basedata")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("OFFICE")))
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
