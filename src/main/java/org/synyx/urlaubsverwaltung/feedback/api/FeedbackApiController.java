package org.synyx.urlaubsverwaltung.feedback.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.synyx.urlaubsverwaltung.feedback.Feedback;
import org.synyx.urlaubsverwaltung.feedback.FeedbackService;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackApiController {

    private FeedbackService feedbackService;

    public FeedbackApiController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public void acceptFeedback(@RequestBody Feedback feedback) {
        feedbackService.handleFeedback(feedback);
    }
}
