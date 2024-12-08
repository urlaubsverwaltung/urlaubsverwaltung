package org.synyx.urlaubsverwaltung.sicknote.sickdays;

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
import org.synyx.urlaubsverwaltung.util.DateAndTimeFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SickDaysOverviewViewControllerSecurityIT extends SingleTenantTestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PersonService personService;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DateAndTimeFormat.DD_MM_YYYY);

    @ParameterizedTest
    @ValueSource(strings = {"USER", "INACTIVE"})
    void periodsSickNotesWithWrongRole(final String role) throws Exception {

        final Person person = new Person();
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);

        final LocalDateTime now = LocalDateTime.now();
        perform(get("/web/sickdays")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority(role)))
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(1)))
        ).andExpect(status().isForbidden());
    }

    @Test
    void ensureToHaveAccessToPeriodsSickNotesWithOfficeRole() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);

        final LocalDateTime now = LocalDateTime.now();
        perform(get("/web/sickdays")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("OFFICE")))
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(1)))
        ).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"BOSS", "SECOND_STAGE_AUTHORITY", "DEPARTMENT_HEAD"})
    void ensureToHaveAccessToPeriodsSickNotesWithSickNoteViewRoleAndManagement(final String role) throws Exception {

        final Person person = new Person();
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);

        final LocalDateTime now = LocalDateTime.now();
        perform(get("/web/sickdays")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("SICK_NOTE_VIEW"), new SimpleGrantedAuthority(role)))
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(1)))
        ).andExpect(status().isOk());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
