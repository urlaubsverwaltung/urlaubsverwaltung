package org.synyx.urlaubsverwaltung.branding;

import org.junit.jupiter.api.Test;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "uv.branding.name=Abwesenheiten")
class BrandingIT extends SingleTenantTestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PersonService personService;

    @Test
    void ensureConfiguredApplicationNameIsUsedAsWordmarkAndPageTitle() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);

        perform(get("/web/sickdays")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("OFFICE")))
        )
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("– Abwesenheiten</title>")))
            .andExpect(content().string(containsString(">Abwesenheiten</a>")))
            .andExpect(content().string(not(containsString(">Urlaubsverwaltung</a>"))));
    }

    @Test
    void ensureConfiguredApplicationNameIsUsedInWebManifest() throws Exception {

        perform(get("/site.webmanifest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("Abwesenheiten")))
            .andExpect(jsonPath("$.short_name", is("Abwesenheiten")));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
