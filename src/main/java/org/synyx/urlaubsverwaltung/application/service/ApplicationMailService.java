package org.synyx.urlaubsverwaltung.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;

@Service
class ApplicationMailService {

    private static final Locale LOCALE = Locale.GERMAN;

    private static final String APPLICATION = "application";
    private static final String VACATION_TYPE = "vacationType";
    private static final String DAY_LENGTH = "dayLength";
    private static final String COMMENT = "comment";

    private final MailService mailService;
    private final DepartmentService departmentService;
    private final ApplicationRecipientService applicationRecipientService;
    private final MessageSource messageSource;

    @Autowired
    ApplicationMailService(MailService mailService, DepartmentService departmentService, ApplicationRecipientService applicationRecipientService, MessageSource messageSource) {
        this.mailService = mailService;
        this.departmentService = departmentService;
        this.applicationRecipientService = applicationRecipientService;
        this.messageSource = messageSource;
    }

    void sendAllowedNotification(Application application, ApplicationComment applicationComment) {

        Map<String, Object> model = new HashMap<>();
        model.put(APPLICATION, application);
        model.put(VACATION_TYPE, getTranslation(application.getVacationType().getCategory().getMessageKey()));
        model.put(DAY_LENGTH, getTranslation(application.getDayLength().name()));
        model.put(COMMENT, applicationComment);

        // Inform user that the application for leave has been allowed
        mailService.sendMailTo(application.getPerson(), "subject.application.allowed.user", "allowed_user", model);

        // Inform office that there is a new allowed application for leave
        mailService.sendMailTo(NOTIFICATION_OFFICE, "subject.application.allowed.office", "allowed_office", model);
    }

    /**
     * sends an email to the applicant that the application has been rejected.
     *
     * @param application the application which got rejected
     * @param comment     reason why application was rejected
     */
    void sendRejectedNotification(Application application, ApplicationComment comment) {

        Map<String, Object> model = new HashMap<>();
        model.put(APPLICATION, application);
        model.put(VACATION_TYPE, getTranslation(application.getVacationType().getCategory().getMessageKey()));
        model.put(DAY_LENGTH, getTranslation(application.getDayLength().name()));
        model.put(COMMENT, comment);

        // send reject information to the applicant
        mailService.sendMailTo(application.getPerson(), "subject.application.rejected", "rejected", model);

        // send reject information to all other relevant persons
        final List<Person> relevantRecipientsToInform = applicationRecipientService.getRelevantRecipients(application);
        mailService.sendMailToEach(relevantRecipientsToInform, "subject.application.rejected_information",
            "rejected_information", model);
    }

    /**
     * If a boss is not sure about the decision of an application (reject or allow), he can ask another boss to decide
     * about this application via a generated email.
     *
     * @param application to ask for support
     * @param recipient   to request for a second opinion
     * @param sender      person that asks for a second opinion
     */
    void sendReferApplicationNotification(Application application, Person recipient, Person sender) {

        Map<String, Object> model = new HashMap<>();
        model.put(APPLICATION, application);
        model.put("recipient", recipient);
        model.put("sender", sender);

        mailService.sendMailTo(recipient, "subject.application.refer", "refer", model);
    }

    /**
     * Sends mail to office and informs about
     * a cancellation request of an already allowed application.
     *
     * @param application    cancelled application
     * @param createdComment additional comment for the confirming application
     */
    void sendCancellationRequest(Application application, ApplicationComment createdComment) {

        Map<String, Object> model = new HashMap<>();
        model.put(APPLICATION, application);
        model.put(COMMENT, createdComment);

        mailService.sendMailTo(NOTIFICATION_OFFICE, "subject.application.cancellationRequest", "application_cancellation_request", model);
    }

    /**
     * Sends mail to the affected person if sick note is converted to vacation.
     *
     * @param application the application that has been converted from sick note to vacation
     */
    void sendSickNoteConvertedToVacationNotification(Application application) {

        Map<String, Object> model = new HashMap<>();
        model.put(APPLICATION, application);

        mailService.sendMailTo(application.getPerson(), "subject.sicknote.converted", "sicknote_converted", model);
    }

    /**
     * Sends mail to person to inform that he/she
     * has been selected as holiday replacement
     * that stands in while someone is on holiday.
     *
     * @param application to inform the holiday replacement
     */
    void notifyHolidayReplacement(Application application) {

        Map<String, Object> model = new HashMap<>();
        model.put(APPLICATION, application);
        model.put(DAY_LENGTH, getTranslation(application.getDayLength().name()));

        mailService.sendMailTo(application.getHolidayReplacement(), "subject.application.holidayReplacement", "notify_holiday_replacement", model);
    }

    /**
     * Sends an email to the applicant that the application
     * has been made successfully.
     *
     * @param application confirmed application
     * @param comment     additional comment for the confirming application
     */
    void sendConfirmation(Application application, ApplicationComment comment) {

        Map<String, Object> model = new HashMap<>();
        model.put(APPLICATION, application);
        model.put(VACATION_TYPE, getTranslation(application.getVacationType().getCategory().getMessageKey()));
        model.put(DAY_LENGTH, getTranslation(application.getDayLength().name()));
        model.put(COMMENT, comment);

        final Person recipient = application.getPerson();
        mailService.sendMailTo(recipient, "subject.application.applied.user", "confirm", model);
    }

    /**
     * Sends an email to the person of the given application
     * that the office has applied for leave on behalf of himself.
     *
     * @param application confirmed application on behalf
     * @param comment     additional comment for the application
     */
    void sendAppliedForLeaveByOfficeNotification(Application application, ApplicationComment comment) {

        Map<String, Object> model = new HashMap<>();
        model.put(APPLICATION, application);
        model.put(VACATION_TYPE, getTranslation(application.getVacationType().getCategory().getMessageKey()));
        model.put(DAY_LENGTH, getTranslation(application.getDayLength().name()));
        model.put(COMMENT, comment);

        final Person recipient = application.getPerson();
        mailService.sendMailTo(recipient, "subject.application.appliedByOffice", "new_application_by_office", model);
    }

    /**
     * Send emails to the applicant and to all relevant persons if an application for leave got revoked.
     *
     * @param application the application which got cancelled
     * @param comment     describes the reason of the cancellation
     */
    void sendRevokedNotifications(Application application, ApplicationComment comment) {

        Map<String, Object> model = new HashMap<>();
        model.put(APPLICATION, application);
        model.put(COMMENT, comment);

        if (application.getPerson().equals(application.getCanceller())) {
            mailService.sendMailTo(application.getPerson(), "subject.application.revoked.applicant", "revoked_applicant", model);
        } else {
            mailService.sendMailTo(application.getPerson(), "subject.application.revoked.notApplicant", "revoked_not_applicant", model);
        }

        // send reject information to all other relevant persons
        final List<Person> relevantRecipientsToInform = applicationRecipientService.getRelevantRecipients(application);
        mailService.sendMailToEach(relevantRecipientsToInform, "subject.application.revoked.management",
            "revoked_management", model);
    }

    /**
     * Send an email to the applicant if an application for leave got cancelled by office.
     *
     * @param application the application which got cancelled
     * @param comment     describes the reason of the cancellation
     */
    void sendCancelledByOfficeNotification(Application application, ApplicationComment comment) {

        Map<String, Object> model = new HashMap<>();
        model.put(APPLICATION, application);
        model.put(COMMENT, comment);

        // send cancelled by office information to the applicant
        final Person recipient = application.getPerson();
        mailService.sendMailTo(recipient, "subject.application.cancelled.user", "cancelled_by_office", model);

        // send cancelled by office information to all other relevant persons
        final List<Person> relevantRecipientsToInform = applicationRecipientService.getRelevantRecipients(application);
        mailService.sendMailToEach(relevantRecipientsToInform, "subject.application.cancelled.management",
            "cancelled_by_office_management", model);
    }


    /**
     * Sends an email to the bosses notifying
     * that there is a new application for leave
     * which has to be allowed or rejected by a boss.
     *
     * @param application to allow or reject
     * @param comment     additional comment for the application
     */
    void sendNewApplicationNotification(Application application, ApplicationComment comment) {

        final List<Application> applicationsForLeave =
            departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(application.getPerson(), application.getStartDate(), application.getEndDate());

        Map<String, Object> model = new HashMap<>();
        model.put(APPLICATION, application);
        model.put(VACATION_TYPE, getTranslation(application.getVacationType().getCategory().getMessageKey()));
        model.put(DAY_LENGTH, getTranslation(application.getDayLength().name()));
        model.put(COMMENT, comment);
        model.put("departmentVacations", applicationsForLeave);

        final List<Person> recipients = applicationRecipientService.getRecipientsForAllowAndRemind(application);
        mailService.sendMailToEach(recipients, "subject.application.applied.boss", "new_applications", model, application.getPerson().getNiceName());
    }


    /**
     * Sends an email to the applicant and to the second stage authorities that the application for leave has been
     * allowed temporary.
     *
     * @param application that has been allowed temporary by a department head
     * @param comment     contains reason why application for leave has been allowed temporary
     */
    void sendTemporaryAllowedNotification(Application application, ApplicationComment comment) {

        Map<String, Object> model = new HashMap<>();
        model.put(APPLICATION, application);
        model.put(DAY_LENGTH, getTranslation(application.getDayLength().name()));
        model.put(COMMENT, comment);

        // Inform user that the application for leave has been allowed temporary
        final String subjectMessageKey = "subject.application.temporaryAllowed.user";
        final String templateName = "temporary_allowed_user";
        mailService.sendMailTo(application.getPerson(), subjectMessageKey, templateName, model);

        final List<Application> applicationsForLeave =
            departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(application.getPerson(), application.getStartDate(), application.getEndDate());

        Map<String, Object> modelSecondStage = new HashMap<>();
        modelSecondStage.put(APPLICATION, application);
        modelSecondStage.put(VACATION_TYPE, getTranslation(application.getVacationType().getCategory().getMessageKey()));
        modelSecondStage.put(DAY_LENGTH, getTranslation(application.getDayLength().name()));
        modelSecondStage.put(COMMENT, comment);
        modelSecondStage.put("departmentVacations", applicationsForLeave);

        // Inform second stage authorities that there is an application for leave that must be allowed
        final List<Person> recipients = applicationRecipientService.getRecipientsForTemporaryAllow(application);
        mailService.sendMailToEach(recipients, "subject.application.temporaryAllowed.secondStage", "temporary_allowed_second_stage_authority", modelSecondStage);
    }


    /**
     * If an application has status waiting and no boss has decided about it after a certain time, the bosses receive a
     * reminding notification.
     *
     * @param application to receive a reminding notification
     */
    void sendRemindBossNotification(Application application) {

        Map<String, Object> model = new HashMap<>();
        model.put(APPLICATION, application);

        List<Person> recipients = applicationRecipientService.getRecipientsForAllowAndRemind(application);
        mailService.sendMailToEach(recipients, "subject.application.remind", "remind", model);
    }


    void sendRemindForWaitingApplicationsReminderNotification(List<Application> waitingApplications) {

        /*
         * whats happening here?
         *
         * application a
         * person p
         *
         * map application to list of boss/department head
         * a_1 -> (p_1, p_2); a_2 -> (p_1, p_3)
         *
         * collect list of application grouped by boss/department head
         * p_1 -> (a_1, a_2); p_2 -> (a_1); (p_3 -> a_2)
         *
         * See: http://stackoverflow.com/questions/33086686/java-8-stream-collect-and-group-by-objects-that-map-to-multiple-keys
         */
        Map<Person, List<Application>> applicationsPerRecipient = waitingApplications.stream()
            .flatMap(application -> applicationRecipientService.getRecipientsForAllowAndRemind(application).stream()
                .map(person -> new AbstractMap.SimpleEntry<>(person, application)))
            .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())));

        for (Map.Entry<Person, List<Application>> entry : applicationsPerRecipient.entrySet()) {

            List<Application> applications = entry.getValue();
            Person recipient = entry.getKey();

            Map<String, Object> model = new HashMap<>();
            model.put("applicationList", applications);
            model.put("recipient", recipient);

            mailService.sendMailTo(recipient, "subject.application.cronRemind", "cron_remind", model);
        }
    }


    private String getTranslation(String key, Object... args) {

        return messageSource.getMessage(key, args, LOCALE);
    }
}
