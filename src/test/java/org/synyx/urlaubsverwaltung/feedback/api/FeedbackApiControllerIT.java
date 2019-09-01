package org.synyx.urlaubsverwaltung.feedback.api;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FeedbackApiControllerIT {

    @Autowired
    private FeedbackApiController feedbackApiController;

    @MockBean
    private FeedbackService feedbackService;

    @Test
    public void acceptFeedbackWithoutText() throws Exception {

        ResultActions result = perform(
            post("/api/feedback")
                .content("{ \"type\": \"AWESOME\" }")
                .contentType(MediaType.APPLICATION_JSON)
        );

        result.andExpect(status().isOk());

        verify(feedbackService).handleFeedback(new Feedback(FeedbackType.AWESOME));
    }

    @Test
    public void acceptFeedbackWithText() throws Exception {

        ResultActions result = perform(
            post("/api/feedback")
                .content("{ \"type\": \"AWESOME\", \"text\": \"awesome stuff here\" }")
                .contentType(MediaType.APPLICATION_JSON)
        );

        result.andExpect(status().isOk());

        verify(feedbackService).handleFeedback(new Feedback(FeedbackType.AWESOME, "awesome stuff here"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.standaloneSetup(feedbackApiController).build().perform(builder);
    }
}
