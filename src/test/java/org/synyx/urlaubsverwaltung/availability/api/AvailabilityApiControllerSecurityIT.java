package org.synyx.urlaubsverwaltung.availability.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AvailabilityApiControllerSecurityIT extends TestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PersonService personService;
    @MockBean
    private AvailabilityService availabilityService;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Test
    void getAvailabilitiesIsUnauthorized() throws Exception {
        perform(
            get("/api/persons/5/availabilities")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("http://localhost/oauth2/authorization/default"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "ADMIN", "INACTIVE"})
    void getAvailabilityForOtherUserIsForbidden(final String role) throws Exception {
        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/persons/5/availabilities")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        )
            .andExpect(status().isForbidden());
    }

    @Test
    void getAvailabilitiesHasOfficeRole() throws Exception {

        final Person testPerson = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(5L)).thenReturn(Optional.of(testPerson));
        when(availabilityService.getPersonsAvailabilities(any(), any(), any())).thenReturn(new AvailabilityListDto(emptyList(), testPerson.getId()));

        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/persons/5/availabilities")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("OFFICE")))
        )
            .andExpect(status().isOk());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
