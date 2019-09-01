package org.synyx.urlaubsverwaltung.feedback.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.synyx.urlaubsverwaltung.feedback.Feedback;
import org.synyx.urlaubsverwaltung.feedback.FeedbackService;
import org.synyx.urlaubsverwaltung.feedback.FeedbackType;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FeedbackViewControllerIT {

    @Autowired
    private FeedbackViewController feedbackViewController;

    @MockBean
    private FeedbackService feedbackService;

    @Test
    public void handlesFeedbackFormPost() throws Exception {

        ResultActions result = perform(
            post("/web/feedback")
                .param("type", "AWESOME")
                .param("text", "awesome stuff you're doing here")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .header("Referer", "/my-referer")
        );

        verify(feedbackService).handleFeedback(new Feedback(FeedbackType.AWESOME, "awesome stuff you're doing here"));
    }

    @Test
    public void redirectsToReferer() throws Exception {

        ResultActions result = perform(
            post("/web/feedback")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .header("Referer", "/my-referer")
        );

        result
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/my-referer"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.standaloneSetup(feedbackViewController).build().perform(builder);
    }
}
