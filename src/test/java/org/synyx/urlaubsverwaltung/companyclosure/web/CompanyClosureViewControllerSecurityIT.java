package org.synyx.urlaubsverwaltung.companyclosure.web;

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

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class CompanyClosureViewControllerSecurityIT extends SingleTenantTestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PersonService personService;

    @ParameterizedTest
    @ValueSource(strings = {"OFFICE", "BOSS"})
    void listIsAllowedForOfficeAndBoss(String role) throws Exception {
        givenSignedInUser();

        perform(get("/web/company-closure")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role))))
            .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"OFFICE", "BOSS"})
    void formsAreAllowedForOfficeAndBoss(String role) throws Exception {
        givenSignedInUser();

        perform(get("/web/company-closure/new")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role))))
            .andExpect(status().isOk());
        perform(get("/web/company-closure/12-24/edit")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role))))
            .andExpect(status().isOk());
    }

    @Test
    void userIsForbidden() throws Exception {
        givenSignedInUser();

        perform(get("/web/company-closure")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"))))
            .andExpect(status().isForbidden());
        perform(get("/web/company-closure/new")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"))))
            .andExpect(status().isForbidden());
        perform(get("/web/company-closure/12-24/edit")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"))))
            .andExpect(status().isForbidden());
        perform(post("/web/company-closure").with(csrf())
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"))))
            .andExpect(status().isForbidden());
        perform(post("/web/company-closure/12-24").with(csrf())
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"))))
            .andExpect(status().isForbidden());
    }

    private void givenSignedInUser() {
        final Person person = new Person();
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
