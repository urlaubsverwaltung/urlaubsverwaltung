package org.synyx.urlaubsverwaltung.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.calendar.ICalService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.settings.TimeSettings;

import java.io.File;
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
    private final ICalService iCalService;
    private final MessageSource messageSource;
    private final SettingsService settingsService;

    @Autowired
    ApplicationMailService(MailService mailService, DepartmentService departmentService,
                           ApplicationRecipientService applicationRecipientService, ICalService iCalService,
                           MessageSource messageSource, SettingsService settingsService) {
        this.mailService = mailService;
        this.departmentService = departmentService;
        this.applicationRecipientService = applicationRecipientService;
        this.iCalService = iCalService;
        this.messageSource = messageSource;
        this.settingsService = settingsService;
    }

    void sendAllowedNotification(Application application, ApplicationComment applicationComment) {

        final Absence absence = new Absence(application.getPerson(), application.getPeriod(), getAbsenceTimeConfiguration());
        final File calendarFile = iCalService.getCalendar(application.getPerson().getNiceName(), List.of(absence));

        Map<String, Object> model = new HashMap<>();
        model.put(APPLICATION, application);
        model.put(VACATION_TYPE, getTranslation(application.getVacationType().getCategory().getMessageKey()));
        model.put(DAY_LENGTH, getTranslation(application.getDayLength().name()));
        model.put(COMMENT, applicationComment);

        // Inform user that the application for leave has been allowed
        final Mail mailToApplicant = Mail.builder()
            .withRecipient(application.getPerson())
            .withSubject("subject.application.allowed.user")
            .withTemplate("allowed_user", model)
            .withAttachment("calendar.ical", calendarFile)
            .build();
        mailService.send(mailToApplicant);

        // Inform office that there is a new allowed application for leave
        final Mail mailToOffice = Mail.builder()
            .withRecipient(NOTIFICATION_OFFICE)
            .withSubject("subject.application.allowed.office")
            .withTemplate("allowed_office", model)
            .build();
        mailService.send(mailToOffice);
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

        final Mail mailToApplicant = Mail.builder()
            .withRecipient(application.getPerson())
            .withSubject("subject.application.rejected")
            .withTemplate("rejected", model)
            .build();

        mailService.send(mailToApplicant);
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

        final Mail mailToApplicant = Mail.builder()
            .withRecipient(recipient)
            .withSubject("subject.application.refer")
            .withTemplate("refer", model)
            .build();

        mailService.send(mailToApplicant);
    }

    /**
     * If a applicant edited the application for leave before it was accepted/declined by a boss/department head
     * a edited notification will be send to himself and the boss/department head
     *
     * @param application that has been edited
     * @param recipient   that edited the application for leave
     */
    void sendEditedApplicationNotification(Application application, Person recipient) {

        Map<String, Object> model = new HashMap<>();
        model.put(APPLICATION, application);
        model.put("recipient", recipient);

        final Mail mailToApplicant = Mail.builder()
            .withRecipient(recipient)
            .withSubject("subject.application.edited", application.getPerson().getNiceName())
            .withTemplate("edited", model)
            .build();

        mailService.send(mailToApplicant);
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

        final Mail mailToOffice = Mail.builder()
            .withRecipient(NOTIFICATION_OFFICE)
            .withSubject("subject.application.cancellationRequest")
            .withTemplate("application_cancellation_request", model)
            .build();

        mailService.send(mailToOffice);
    }

    /**
     * Sends mail to the affected person if sick note is converted to vacation.
     *
     * @param application the application that has been converted from sick note to vacation
     */
    void sendSickNoteConvertedToVacationNotification(Application application) {

        Map<String, Object> model = new HashMap<>();
        model.put(APPLICATION, application);

        final Mail mailToApplicant = Mail.builder()
            .withRecipient(application.getPerson())
            .withSubject("subject.sicknote.converted")
            .withTemplate("sicknote_converted", model)
            .build();

        mailService.send(mailToApplicant);
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

        final Mail mailToReplacement = Mail.builder()
            .withRecipient(application.getHolidayReplacement())
            .withSubject("subject.application.holidayReplacement")
            .withTemplate("notify_holiday_replacement", model)
            .build();

        mailService.send(mailToReplacement);
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

        final Mail mailToApplicant = Mail.builder()
            .withRecipient(application.getPerson())
            .withSubject("subject.application.applied.user")
            .withTemplate("confirm", model)
            .build();

        mailService.send(mailToApplicant);
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

        final Mail mailToApplicant = Mail.builder()
            .withRecipient(application.getPerson())
            .withSubject("subject.application.appliedByOffice")
            .withTemplate("new_application_by_office", model)
            .build();

        mailService.send(mailToApplicant);
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

        final Mail mailToApplicant = Mail.builder()
            .withRecipient(application.getPerson())
            .withSubject("subject.application.cancelled.user")
            .withTemplate("cancelled_by_office", model)
            .build();

        mailService.send(mailToApplicant);
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
        final Mail mailToAllowAndRemind = Mail.builder()
            .withRecipient(recipients)
            .withSubject("subject.application.applied.boss", application.getPerson().getNiceName())
            .withTemplate("new_applications", model)
            .build();

        mailService.send(mailToAllowAndRemind);
    }


    /**
     * Sends an email to the applicant and to the second stage authorities that the application for leave has been
     * allowed temporary.
     *
     * @param application that has been allowed temporary by a department head
     * @param comment     contains reason why application for leave has been allowed temporary
     */
    void sendTemporaryAllowedNotification(Application application, ApplicationComment comment) {

        // Inform user that the application for leave has been allowed temporary
        Map<String, Object> model = new HashMap<>();
        model.put(APPLICATION, application);
        model.put(DAY_LENGTH, getTranslation(application.getDayLength().name()));
        model.put(COMMENT, comment);

        final Mail mailToApplicant = Mail.builder()
            .withRecipient(application.getPerson())
            .withSubject("subject.application.temporaryAllowed.user")
            .withTemplate("temporary_allowed_user", model)
            .build();
        mailService.send(mailToApplicant);

        // Inform second stage authorities that there is an application for leave that must be allowed
        final List<Application> applicationsForLeave =
            departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(application.getPerson(), application.getStartDate(), application.getEndDate());

        Map<String, Object> modelSecondStage = new HashMap<>();
        modelSecondStage.put(APPLICATION, application);
        modelSecondStage.put(VACATION_TYPE, getTranslation(application.getVacationType().getCategory().getMessageKey()));
        modelSecondStage.put(DAY_LENGTH, getTranslation(application.getDayLength().name()));
        modelSecondStage.put(COMMENT, comment);
        modelSecondStage.put("departmentVacations", applicationsForLeave);

        final List<Person> recipients = applicationRecipientService.getRecipientsForTemporaryAllow(application);
        final Mail mailToTemporaryAllow = Mail.builder()
            .withRecipient(recipients)
            .withSubject("subject.application.temporaryAllowed.secondStage")
            .withTemplate("temporary_allowed_second_stage_authority", modelSecondStage)
            .build();
        mailService.send(mailToTemporaryAllow);
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

        final List<Person> recipients = applicationRecipientService.getRecipientsForAllowAndRemind(application);
        final Mail mailToAllowAndRemind = Mail.builder()
            .withRecipient(recipients)
            .withSubject("subject.application.remind")
            .withTemplate("remind", model)
            .build();
        mailService.send(mailToAllowAndRemind);
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

            final Mail mailToRemindForWaiting = Mail.builder()
                .withRecipient(recipient)
                .withSubject("subject.application.cronRemind")
                .withTemplate("cron_remind", model)
                .build();

            mailService.send(mailToRemindForWaiting);
        }
    }

    private String getTranslation(String key, Object... args) {
        return messageSource.getMessage(key, args, LOCALE);
    }

    private AbsenceTimeConfiguration getAbsenceTimeConfiguration() {
        final TimeSettings timeSettings = settingsService.getSettings().getTimeSettings();
        return new AbsenceTimeConfiguration(timeSettings);
    }
}
