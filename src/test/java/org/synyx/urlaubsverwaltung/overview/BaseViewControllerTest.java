package org.synyx.urlaubsverwaltung.overview;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

public class BaseViewControllerTest {

    private BaseViewController sut;

    @Before
    public void setUp() {

        sut = new BaseViewController();
    }

    @Test
    public void indexRedirectsToOverview() throws Exception {

        perform(get("/"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/overview"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {

        return standaloneSetup(sut).build().perform(builder);
    }
}
