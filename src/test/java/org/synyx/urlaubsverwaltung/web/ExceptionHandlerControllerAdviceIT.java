package org.synyx.urlaubsverwaltung.web;

import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:META-INF/applicationContext.xml", "classpath:META-INF/web-context.xml" })
@WebAppConfiguration
public class ExceptionHandlerControllerAdviceIT {

    @Autowired
    private WebApplicationContext webContext;

    private MockMvc mockMvc;

    @Before
    public void before() {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webContext).build();
    }


    @Test
    public void shouldReturnErrorPageForUnknownPersonException() throws Exception {

        mockMvc.perform(get("/overtime").param("person", "42"))
            .andExpect(status().isBadRequest())
            .andExpect(view().name("error"));
    }


    @Test
    public void shouldReturnErrorPageForIllegalStateException() throws Exception {

        mockMvc.perform(get("/overtime").param("person", "42"))
            .andExpect(status().isBadRequest())
            .andExpect(view().name("error"));
    }
}
