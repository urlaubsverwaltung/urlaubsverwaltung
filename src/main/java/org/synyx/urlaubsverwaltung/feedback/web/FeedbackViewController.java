package org.synyx.urlaubsverwaltung.feedback.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.feedback.Feedback;
import org.synyx.urlaubsverwaltung.feedback.FeedbackService;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/web/feedback")
public class FeedbackViewController {

    private final FeedbackService feedbackService;

    @Autowired
    public FeedbackViewController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String foo(FeedbackViewDto feedbackDto, RedirectAttributes redirectAttributes, HttpServletRequest request) {

        final Feedback feedback = new Feedback(feedbackDto.getType(), feedbackDto.getText());

        feedbackService.handleFeedback(feedback);

        redirectAttributes.addFlashAttribute("showFeedbackThankYou", true);

        final String referer = request.getHeader("Referer");
        return "redirect:"+ referer;
    }
}
