package org.synyx.urlaubsverwaltung.application.service;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.mail.MailService;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;

@Service
class ApplicationMailService {

    private static final Locale LOCALE = Locale.GERMAN;

    private final MailService mailService;
    private final MessageSource messageSource;

    ApplicationMailService(MailService mailService, MessageSource messageSource) {
        this.mailService = mailService;
        this.messageSource = messageSource;
    }

    void sendAllowedNotification(Application application, ApplicationComment applicationComment) {

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationType", getTranslation(application.getVacationType().getCategory().getMessageKey()));
        model.put("dayLength", getTranslation(application.getDayLength().name()));
        model.put("comment", applicationComment);

        // Inform user that the application for leave has been allowed
        mailService.sendMailTo(application.getPerson(), "subject.application.allowed.user", "allowed_user", model);

        // Inform office that there is a new allowed application for leave
        mailService.sendMailTo(NOTIFICATION_OFFICE, "subject.application.allowed.office", "allowed_office", model);
    }

    private String getTranslation(String key, Object... args) {

        return messageSource.getMessage(key, args, LOCALE);
    }
}
