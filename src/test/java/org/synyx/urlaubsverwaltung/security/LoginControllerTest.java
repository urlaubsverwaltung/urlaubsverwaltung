package org.synyx.urlaubsverwaltung.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class LoginControllerTest {

    private LoginController sut;

    @BeforeEach
    void setUp() {
        sut = new LoginController();
    }

    @Test
    void ensureLoginHasCorrectVersionAndView() throws Exception {
        perform(get("/login"))
            .andExpect(view().name("login/login"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
