package org.synyx.urlaubsverwaltung.web.overtime;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.SpringApplicationConfiguration;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.web.context.WebApplicationContext;

import org.synyx.urlaubsverwaltung.UrlaubsverwaltungApplication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = UrlaubsverwaltungApplication.class)
@WebAppConfiguration
public class OvertimeControllerIT {

    @Autowired
    private WebApplicationContext webContext;

    private MockMvc mockMvc;

    @Before
    public void before() {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webContext).build();
    }


    @Test
    @Ignore("Currently disabled as integration tests broke during the migration to Spring Boot.")
    public void shouldReturnErrorPageForUnknownPerson() throws Exception {

        mockMvc.perform(get("/overtime").param("person", "42"))
            .andExpect(status().isBadRequest())
            .andExpect(view().name("errors"));
    }


    @Test
    @Ignore("Currently disabled as integration tests broke during the migration to Spring Boot.")
    public void shouldReturnErrorPageForUnknownOvertime() throws Exception {

        mockMvc.perform(get("/overtime/42")).andExpect(status().isBadRequest()).andExpect(view().name("errors"));
    }
}
