package org.synyx.urlaubsverwaltung.application.service;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;

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

    /**
     * sends an email to the applicant that the application has been rejected.
     *
     * @param  application  the application which got rejected
     * @param  comment  reason why application was rejected
     */
    void sendRejectedNotification(Application application, ApplicationComment comment) {

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationType", getTranslation(application.getVacationType().getCategory().getMessageKey()));
        model.put("dayLength", getTranslation(application.getDayLength().name()));
        model.put("comment", comment);

        mailService.sendMailTo(application.getPerson(),"subject.application.rejected", "rejected", model);
    }

    /**
     * If a boss is not sure about the decision of an application (reject or allow), he can ask another boss to decide
     * about this application via a generated email.
     *
     * @param  application to ask for support
     * @param  recipient to request for a second opinion
     * @param  sender person that asks for a second opinion
     */
    void sendReferApplicationNotification(Application application, Person recipient, Person sender) {

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("recipient", recipient);
        model.put("sender", sender);

        mailService.sendMailTo(recipient,"subject.application.refer", "refer", model);
    }

    /**
     * Sends mail to office and informs about
     * a cancellation request of an already allowed application.
     *
     * @param  application cancelled application
     * @param  createdComment additional comment for the confirming application
     */
    void sendCancellationRequest(Application application, ApplicationComment createdComment) {

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("comment", createdComment);

        mailService.sendMailTo(NOTIFICATION_OFFICE,"subject.application.cancellationRequest", "application_cancellation_request", model);
    }

    private String getTranslation(String key, Object... args) {

        return messageSource.getMessage(key, args, LOCALE);
    }
}
