package org.synyx.urlaubsverwaltung.sickdays.web;

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

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SickDaysOverviewViewControllerSecurityIT extends TestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PersonService personService;

    @Test
    @WithMockUser(authorities = "USER")
    void periodsSickNotesWithWrongRole() throws Exception {
        final ResultActions resultActions = perform(get("/web/sicknote"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER", "OFFICE"})
    void periodsSickNotesWithCorrectRole() throws Exception {

        when(personService.getSignedInUser()).thenReturn(new Person());

        final ResultActions resultActions = perform(get("/web/sicknote"));
        resultActions.andExpect(status().isOk());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
