package org.synyx.urlaubsverwaltung.web;

import de.focus_shift.launchpad.core.LaunchpadAutoConfiguration;
import de.focus_shift.launchpad.core.LaunchpadModelAttributeAppender;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Locale;

@ControllerAdvice(assignableTypes = ErrorController.class)
@Conditional(LaunchpadAutoConfiguration.LaunchpadAppsCondition.class)
class ErrorControllerAdvice {

    private final LaunchpadModelAttributeAppender launchpadModelAttributeAppender;

    ErrorControllerAdvice(LaunchpadModelAttributeAppender launchpadModelAttributeAppender) {
        this.launchpadModelAttributeAppender = launchpadModelAttributeAppender;
    }

    @ModelAttribute
    public void addAttributes(Model model, Locale locale, Authentication authentication) {
        if (authentication != null) {
            launchpadModelAttributeAppender.addModelAttribute(model, locale, authentication);
        }
    }
}
