package org.synyx.urlaubsverwaltung.vacationcertificate;

import org.junit.jupiter.api.Test;
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

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
class VacationCertificateViewControllerSecurityIT extends SingleTenantTestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PersonService personService;

    @Test
    void ensureUserIsForbidden() throws Exception {
        signedInPerson();

        perform(get("/web/person/1/vacation-certificate")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"))))
            .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensureManagementWithoutOfficeIsForbidden(String role) throws Exception {
        signedInPerson();

        perform(get("/web/person/1/vacation-certificate")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role))))
            .andExpect(status().isForbidden());
    }

    @Test
    void ensureOfficeIsAllowed() throws Exception {
        final Person person = signedInPerson();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(get("/web/person/1/vacation-certificate")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("OFFICE"))))
            .andExpect(status().isOk())
            // the person has no holiday account in the empty database
            .andExpect(content().string(containsString("data-test-id=\"vacation-certificate-no-account\"")));
    }

    private Person signedInPerson() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);
        return person;
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
