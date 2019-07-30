package org.synyx.urlaubsverwaltung.security;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

public class LoginControllerTest {

    private LoginController sut;

    private static final String APPLICATION_VERSION = "1.1.1";

    @Before
    public void setUp() {

        sut = new LoginController(APPLICATION_VERSION);
    }

    @Test
    public void ensureLoginHasCorrectVersionAndView() throws Exception {

        perform(get("/login"))
            .andExpect(model().attribute("version", equalTo(APPLICATION_VERSION)))
            .andExpect(view().name("login/login"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {

        return standaloneSetup(sut).build().perform(builder);
    }
}
