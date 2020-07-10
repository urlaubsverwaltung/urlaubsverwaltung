package org.synyx.urlaubsverwaltung.availability.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createPerson;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AvailabilityApiControllerSecurityIT {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PersonService personService;
    @MockBean
    private AvailabilityService availabilityService;

    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Test
    public void getAvailabilitiesIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons/5/availabilities"));
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void getAvailabilityAsAuthenticatedUserForOtherUserIsForbidden() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/5/availabilities")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
        );

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    public void getAvailabilityAsDepartmentHeadUserForOtherUserIsForbidden() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/5/availabilities")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
        );

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    public void getAvailabilityAsSecondStageAuthorityUserForOtherUserIsForbidden() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/5/availabilities")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
        );

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    public void getAvailabilityAsBossUserForOtherUserIsForbidden() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/5/availabilities")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
        );

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void getAvailabilityAsAdminUserForOtherUserIsForbidden() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/5/availabilities")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
        );

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    public void getAvailabilityAsInactiveUserForOtherUserIsForbidden() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/5/availabilities")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
        );

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void getAvailabilitiesHasOfficeRole() throws Exception {

        final Person testPerson = createPerson("testPerson");
        when(personService.getPersonByID(5)).thenReturn(Optional.of(testPerson));
        when(availabilityService.getPersonsAvailabilities(any(), any(), any())).thenReturn(new AvailabilityListDto(emptyList(), testPerson.getId()));

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/5/availabilities")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
        );

        resultActions.andExpect(status().isOk());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
