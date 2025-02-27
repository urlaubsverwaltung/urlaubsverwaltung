package org.synyx.urlaubsverwaltung.application.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationComment;
import org.synyx.urlaubsverwaltung.calendar.CalendarAbsence;
import org.synyx.urlaubsverwaltung.calendar.CalendarAbsenceType;
import org.synyx.urlaubsverwaltung.calendar.ICalService;
import org.synyx.urlaubsverwaltung.calendar.ICalType;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailRecipientService;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.mail.MailTemplateModelSupplier;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.calendar.CalendarAbsenceType.DEFAULT;
import static org.synyx.urlaubsverwaltung.calendar.ICalType.CANCELLED;
import static org.synyx.urlaubsverwaltung.calendar.ICalType.PUBLISHED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_CANCELLATION;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_CONVERTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_EDITED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_REJECTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_REVOKED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_UPCOMING;

@Service
class ApplicationMailService {

    private static final String APPLICATION = "application";
    private static final String VACATION_TYPE = "vacationTypeLabel";
    private static final String COMMENT = "comment";
    private static final String CALENDAR_ICS = "calendar.ics";
    private static final String HOLIDAY_REPLACEMENT = "holidayReplacement";
    private static final String HOLIDAY_REPLACEMENT_NOTE = "holidayReplacementNote";

    private final MailService mailService;
    private final DepartmentService departmentService;
    private final MailRecipientService mailRecipientService;
    private final ICalService iCalService;
    private final SettingsService settingsService;
    private final Clock clock;

    @Autowired
    ApplicationMailService(MailService mailService, DepartmentService departmentService,
                           MailRecipientService mailRecipientService, ICalService iCalService,
                           SettingsService settingsService, Clock clock) {
        this.mailService = mailService;
        this.departmentService = departmentService;
        this.mailRecipientService = mailRecipientService;
        this.iCalService = iCalService;
        this.settingsService = settingsService;
        this.clock = clock;
    }

    @Async
    void sendAllowedNotification(Application application, ApplicationComment applicationComment) {

        final ByteArrayResource calendarFile = generateCalendar(application, DEFAULT, application.getPerson());

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            APPLICATION, application,
            VACATION_TYPE, application.getVacationType().getLabel(locale),
            COMMENT, applicationComment
        );

        // Inform user that the application for leave has been allowed
        final Mail mailToApplicant = Mail.builder()
            .withRecipient(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_ALLOWED)
            .withSubject("subject.application.allowed.user")
            .withTemplate("application_allowed_to_applicant", modelSupplier)
            .withAttachment(CALENDAR_ICS, calendarFile)
            .build();
        mailService.send(mailToApplicant);

        // Inform all person of interest like boss or department head that the application for leave has been allowed
        final List<Person> relevantRecipientsToInform = mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED);
        final Mail mailToRelevantRecipients = Mail.builder()
            .withRecipient(relevantRecipientsToInform)
            .withSubject("subject.application.allowed.management", application.getPerson().getNiceName())
            .withTemplate("application_allowed_to_management", modelSupplier)
            .withAttachment(CALENDAR_ICS, calendarFile)
            .build();
        mailService.send(mailToRelevantRecipients);

        // Inform colleagues of applicant which are in same department
        final MailTemplateModelSupplier modelColleaguesSupplier = locale -> Map.of(APPLICATION, application);
        final List<Person> relevantColleaguesToInform = mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED);
        final Mail mailToRelevantColleagues = Mail.builder()
            .withRecipient(relevantColleaguesToInform)
            .withSubject("subject.application.allowed.to_colleagues", application.getPerson().getNiceName())
            .withTemplate("application_allowed_to_colleagues", modelColleaguesSupplier)
            .withAttachment(CALENDAR_ICS, calendarFile)
            .build();
        mailService.send(mailToRelevantColleagues);
    }

    /**
     * sends an email to the applicant that the application has been rejected.
     *
     * @param application the application which got rejected
     * @param comment     reason why application was rejected
     */
    @Async
    void sendRejectedNotification(Application application, ApplicationComment comment) {

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            APPLICATION, application,
            VACATION_TYPE, application.getVacationType().getLabel(locale),
            COMMENT, comment
        );

        // send reject information to the applicant
        final Mail mailToApplicant = Mail.builder()
            .withRecipient(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_REJECTED)
            .withSubject("subject.application.rejected")
            .withTemplate("application_rejected_information_to_applicant", modelSupplier)
            .build();
        mailService.send(mailToApplicant);

        // send reject information to the management
        final List<Person> relevantRecipientsToInform = mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED);
        final Mail mailToRelevantRecipients = Mail.builder()
            .withRecipient(relevantRecipientsToInform)
            .withSubject("subject.application.rejected_information")
            .withTemplate("application_rejected_information_to_management", modelSupplier)
            .build();
        mailService.send(mailToRelevantRecipients);
    }

    /**
     * If a boss is not sure about the decision of an application (reject or allow), he can ask another boss to decide
     * about this application via a generated email.
     *
     * @param application to ask for support
     * @param recipient   to request for a second opinion
     * @param sender      person that asks for a second opinion
     */
    @Async
    void sendReferredToManagementNotification(Application application, Person recipient, Person sender) {

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            APPLICATION, application,
            VACATION_TYPE, application.getVacationType().getLabel(locale),
            "sender", sender
        );

        final Mail mailToApplicant = Mail.builder()
            .withRecipient(recipient)
            .withSubject("subject.application.refer")
            .withTemplate("application_referred_to_management", modelSupplier)
            .build();

        mailService.send(mailToApplicant);
    }

    /**
     * If an applicant edited the application for leave before it was accepted/declined by a boss/department head
     * an edited notification will be sent to himself and the boss/department head
     *
     * @param application that has been edited
     * @param editor      that edited the application for leave
     */
    @Async
    void sendEditedNotification(Application application, Person editor) {

        final Mail mailToApplicant;
        if (application.getPerson().equals(editor)) {
            mailToApplicant = Mail.builder()
                .withRecipient(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_EDITED)
                .withSubject("subject.application.edited.to_applicant_by_applicant")
                .withTemplate("application_edited_by_applicant_to_applicant", locale -> Map.of(APPLICATION, application))
                .build();
        } else {
            mailToApplicant = Mail.builder()
                .withRecipient(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_EDITED)
                .withSubject("subject.application.edited.to_applicant_by_management", editor.getNiceName())
                .withTemplate("application_edited_by_management_to_applicant", locale -> Map.of(APPLICATION, application, "editor", editor))
                .build();
        }
        mailService.send(mailToApplicant);

        final List<Person> relevantRecipientsToInform = mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED);
        final Mail mailToManagement = Mail.builder()
            .withRecipient(relevantRecipientsToInform)
            .withSubject("subject.application.edited.management", application.getPerson().getNiceName(), editor.getNiceName())
            .withTemplate("application_edited_by_applicant_to_management", locale -> Map.of(APPLICATION, application, "editor", editor))
            .build();
        mailService.send(mailToManagement);
    }

    /**
     * Sends information to the office and applicant that the cancellation request was cancelled
     *
     * @param application cancellation requested application
     */
    @Async
    void sendDeclinedCancellationRequestApplicationNotification(Application application, ApplicationComment comment) {

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            APPLICATION, application,
            COMMENT, comment
        );

        // send mail to applicant
        final Mail mailToApplicant = Mail.builder()
            .withRecipient(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_CANCELLATION)
            .withSubject("subject.application.cancellationRequest.declined.applicant", application.getPerson().getNiceName())
            .withTemplate("application_cancellation_request_declined_to_applicant", modelSupplier)
            .build();
        mailService.send(mailToApplicant);

        // send cancelled cancellation request information to the office and relevant persons
        final List<Person> relevantRecipientsToInform = mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED);
        final Mail mailToManagement = Mail.builder()
            .withRecipient(relevantRecipientsToInform)
            .withSubject("subject.application.cancellationRequest.declined.management")
            .withTemplate("application_cancellation_request_declined_to_management", modelSupplier)
            .build();
        mailService.send(mailToManagement);
    }

    /**
     * Sends mail to office and informs about
     * a cancellation request of an already allowed application.
     *
     * @param application    cancelled application
     * @param createdComment additional comment for the confirming application
     */
    @Async
    void sendCancellationRequest(Application application, ApplicationComment createdComment) {

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            APPLICATION, application,
            COMMENT, createdComment
        );

        // send mail to applicant
        final Mail mailToApplicant = Mail.builder()
            .withRecipient(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_CANCELLATION)
            .withSubject("subject.application.cancellationRequest.applicant")
            .withTemplate("application_cancellation_request_to_applicant", modelSupplier)
            .build();
        mailService.send(mailToApplicant);

        // send reject information to the office or boss, dh or ssa with APPLICATION_CANCELLATION_REQUESTED
        final List<Person> recipientsOfInterest = mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED);
        final Mail mailToManagement = Mail.builder()
            .withRecipient(recipientsOfInterest)
            .withSubject("subject.application.cancellationRequest")
            .withTemplate("application_cancellation_request_to_management", modelSupplier)
            .build();
        mailService.send(mailToManagement);
    }

    /**
     * Sends mail to the affected person if sick note is converted to vacation.
     *
     * @param application the application that has been converted from sick note to vacation
     */
    @Async
    void sendSickNoteConvertedToVacationNotification(Application application) {

        final Mail mailToApplicant = Mail.builder()
            .withRecipient(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_CONVERTED)
            .withSubject("subject.sicknote.converted")
            .withTemplate("sicknote_converted", locale -> Map.of(APPLICATION, application))
            .build();
        mailService.send(mailToApplicant);

        final List<Person> relevantRecipientsToInform = mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED);
        final Mail mailToManagement = Mail.builder()
            .withRecipient(relevantRecipientsToInform)
            .withSubject("subject.sicknote.converted.management", application.getPerson().getNiceName())
            .withTemplate("sicknote_converted_to_management", locale -> Map.of(APPLICATION, application))
            .build();
        mailService.send(mailToManagement);
    }

    /**
     * Sends an email to the applicant that the application
     * has been created successfully.
     *
     * @param application confirmed application
     * @param comment     additional comment for the confirming application
     */
    @Async
    void sendConfirmationAllowedDirectly(Application application, ApplicationComment comment) {

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            APPLICATION, application,
            VACATION_TYPE, application.getVacationType().getLabel(locale),
            COMMENT, comment
        );
        final Mail mailToApplicant = Mail.builder()
            .withRecipient(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_ALLOWED)
            .withSubject("subject.application.allowedDirectly.user")
            .withTemplate("application_allowed_directly_to_applicant", modelSupplier)
            .build();
        mailService.send(mailToApplicant);

        // Inform colleagues of applicant which are in same department
        final MailTemplateModelSupplier modelColleaguesSupplier = locale -> Map.of(APPLICATION, application);
        final List<Person> relevantColleaguesToInform = mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED);
        final Mail mailToRelevantColleagues = Mail.builder()
            .withRecipient(relevantColleaguesToInform)
            .withSubject("subject.application.allowed.to_colleagues", application.getPerson().getNiceName())
            .withTemplate("application_allowed_to_colleagues", modelColleaguesSupplier)
            .build();
        mailService.send(mailToRelevantColleagues);
    }

    /**
     * Sends an email to the person of the given application
     * that some management person has entered an application directly on behalf of himself.
     *
     * @param application confirmed application on behalf
     * @param comment     additional comment for the application
     */
    @Async
    void sendConfirmationAllowedDirectlyByManagement(Application application, ApplicationComment comment) {

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            APPLICATION, application,
            VACATION_TYPE, application.getVacationType().getLabel(locale),
            COMMENT, comment
        );
        final Mail mailToApplicant = Mail.builder()
            .withRecipient(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_ALLOWED)
            .withSubject("subject.application.allowedDirectly.management")
            .withTemplate("application_allowed_directly_by_management_to_applicant", modelSupplier)
            .build();
        mailService.send(mailToApplicant);

        // Inform colleagues of applicant which are in same department
        final MailTemplateModelSupplier modelColleaguesSupplier = locale -> Map.of(APPLICATION, application);
        final List<Person> relevantColleaguesToInform = mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED);
        final Mail mailToRelevantColleagues = Mail.builder()
            .withRecipient(relevantColleaguesToInform)
            .withSubject("subject.application.allowed.to_colleagues", application.getPerson().getNiceName())
            .withTemplate("application_allowed_to_colleagues", modelColleaguesSupplier)
            .build();
        mailService.send(mailToRelevantColleagues);
    }

    /**
     * Sends an email to the management notifying
     * that there is a new directly allowed application for leave
     * which has to be allowed or rejected by a boss.
     *
     * @param application directly allowed application
     * @param comment     additional comment for the application
     */
    @Async
    void sendDirectlyAllowedNotificationToManagement(Application application, ApplicationComment comment) {

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            APPLICATION, application,
            VACATION_TYPE, application.getVacationType().getLabel(locale),
            COMMENT, comment
        );

        final List<Person> recipients = mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED);
        final Mail mailToAllowAndRemind = Mail.builder()
            .withRecipient(recipients)
            .withSubject("subject.application.allowedDirectly.boss", application.getPerson().getNiceName())
            .withTemplate("application_allowed_directly_to_management", modelSupplier)
            .build();

        mailService.send(mailToAllowAndRemind);
    }

    /**
     * Sends mail to person to inform that he/she
     * has been selected as replacement
     * for a directly allowed application
     * that stands in while someone is on holiday.
     *
     * @param application to inform the replacement
     */
    @Async
    void notifyHolidayReplacementAboutDirectlyAllowedApplication(HolidayReplacementEntity holidayReplacement, Application application) {

        final ByteArrayResource calendarFile = generateCalendar(application, CalendarAbsenceType.HOLIDAY_REPLACEMENT, holidayReplacement.getPerson());

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            APPLICATION, application,
            HOLIDAY_REPLACEMENT, holidayReplacement.getPerson(),
            HOLIDAY_REPLACEMENT_NOTE, holidayReplacement.getNote()
        );

        final Mail mailToReplacement = Mail.builder()
            .withRecipient(holidayReplacement.getPerson(), NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT)
            .withSubject("subject.application.allowedDirectly.holidayReplacement", application.getPerson().getNiceName())
            .withTemplate("application_allowed_directly_to_holiday_replacement", modelSupplier)
            .withAttachment(CALENDAR_ICS, calendarFile)
            .build();

        mailService.send(mailToReplacement);
    }

    /**
     * Sends mail to person to inform that he/she
     * has been selected as replacement
     * but that this application status is WAITING
     *
     * @param application to inform the replacement beforehand
     */
    @Async
    void notifyHolidayReplacementForApply(HolidayReplacementEntity holidayReplacement, Application application) {

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            APPLICATION, application,
            HOLIDAY_REPLACEMENT, holidayReplacement.getPerson(),
            HOLIDAY_REPLACEMENT_NOTE, holidayReplacement.getNote()
        );

        final Mail mailToReplacement = Mail.builder()
            .withRecipient(holidayReplacement.getPerson(), NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT)
            .withSubject("subject.application.holidayReplacement.apply", application.getPerson().getNiceName())
            .withTemplate("application_applied_to_holiday_replacement", modelSupplier)
            .build();

        mailService.send(mailToReplacement);
    }

    /**
     * Sends mail to person to inform that he/she
     * has been selected as replacement
     * that stands in while someone is on holiday.
     *
     * @param application to inform the replacement
     */
    @Async
    void notifyHolidayReplacementAllow(HolidayReplacementEntity holidayReplacement, Application application) {

        final ByteArrayResource calendarFile = generateCalendar(application, CalendarAbsenceType.HOLIDAY_REPLACEMENT, holidayReplacement.getPerson());

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            APPLICATION, application,
            HOLIDAY_REPLACEMENT, holidayReplacement.getPerson(),
            HOLIDAY_REPLACEMENT_NOTE, holidayReplacement.getNote()
        );

        final Mail mailToReplacement = Mail.builder()
            .withRecipient(holidayReplacement.getPerson(), NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT)
            .withSubject("subject.application.holidayReplacement.allow", application.getPerson().getNiceName())
            .withTemplate("application_allowed_to_holiday_replacement", modelSupplier)
            .withAttachment(CALENDAR_ICS, calendarFile)
            .build();

        mailService.send(mailToReplacement);
    }

    /**
     * Sends mail to person to inform that he/she
     * has been selected as replacement
     * but that the request was cancelled/rejected/revoked/...
     *
     * @param application to inform the replacement was cancelled
     */
    @Async
    void notifyHolidayReplacementAboutCancellation(HolidayReplacementEntity holidayReplacement, Application application) {

        final ByteArrayResource calendarFile = generateCalendar(application, DEFAULT, CANCELLED, holidayReplacement.getPerson());

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            APPLICATION, application,
            HOLIDAY_REPLACEMENT, holidayReplacement.getPerson()
        );

        final Mail mailToReplacement = Mail.builder()
            .withRecipient(holidayReplacement.getPerson(), NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT)
            .withSubject("subject.application.holidayReplacement.cancellation", application.getPerson().getNiceName())
            .withTemplate("application_cancelled_to_holiday_replacement", modelSupplier)
            .withAttachment(CALENDAR_ICS, calendarFile)
            .build();

        mailService.send(mailToReplacement);
    }

    /**
     * Sends mail to person to inform that he/she
     * has been selected as replacement
     * but that the request was cancelled/rejected/revoked/...
     *
     * @param application to inform the replacement was cancelled
     */
    @Async
    void notifyHolidayReplacementAboutEdit(HolidayReplacementEntity holidayReplacement, Application application) {

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            APPLICATION, application,
            HOLIDAY_REPLACEMENT, holidayReplacement.getPerson(),
            HOLIDAY_REPLACEMENT_NOTE, holidayReplacement.getNote()
        );

        final List<ApplicationStatus> allowedStatuses = List.of(ApplicationStatus.ALLOWED, ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED);
        final String messageKey = allowedStatuses.contains(application.getStatus()) ? "subject.application.holidayReplacement.allow.edit" : "subject.application.holidayReplacement.edit";

        final Mail mailToReplacement = Mail.builder()
            .withRecipient(holidayReplacement.getPerson(), NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT)
            .withSubject(messageKey, application.getPerson().getNiceName())
            .withTemplate("application_edited_to_holiday_replacement", modelSupplier)
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
    @Async
    void sendAppliedNotification(Application application, ApplicationComment comment) {

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            APPLICATION, application,
            VACATION_TYPE, application.getVacationType().getLabel(locale),
            COMMENT, comment
        );

        final Mail mailToApplicant = Mail.builder()
            .withRecipient(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_APPLIED)
            .withSubject("subject.application.applied.user")
            .withTemplate("application_applied_by_applicant_to_applicant", modelSupplier)
            .build();

        mailService.send(mailToApplicant);
    }

    /**
     * Sends an email to the person of the given application
     * that some management person has applied for leave on behalf of himself.
     *
     * @param application confirmed application on behalf
     * @param comment     additional comment for the application
     */
    @Async
    void sendAppliedByManagementNotification(Application application, ApplicationComment comment) {

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            APPLICATION, application,
            VACATION_TYPE, application.getVacationType().getLabel(locale),
            COMMENT, comment
        );

        final Mail mailToApplicant = Mail.builder()
            .withRecipient(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_APPLIED)
            .withSubject("subject.application.applied.management")
            .withTemplate("application_applied_by_management_to_applicant", modelSupplier)
            .build();

        mailService.send(mailToApplicant);
    }

    /**
     * Send emails to the applicant and to all relevant persons if an application for leave got revoked.
     *
     * @param application the application which got cancelled
     * @param comment     describes the reason of the revocation
     */
    @Async
    void sendRevokedNotifications(Application application, ApplicationComment comment) {

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            APPLICATION, application,
            COMMENT, comment
        );

        if (application.getPerson().equals(application.getCanceller())) {
            final Mail mailToApplicant = Mail.builder()
                .withRecipient(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_REVOKED)
                .withSubject("subject.application.revoked.applicant")
                .withTemplate("application_revoked_by_applicant_to_applicant", modelSupplier)
                .build();
            mailService.send(mailToApplicant);
        } else {
            final Mail mailToNotApplicant = Mail.builder()
                .withRecipient(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_REVOKED)
                .withSubject("subject.application.revoked.notApplicant")
                .withTemplate("application_revoked_by_management_to_applicant", modelSupplier)
                .build();
            mailService.send(mailToNotApplicant);
        }

        // send reject information to all other relevant persons
        final List<Person> relevantRecipientsToInform = mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED);
        final Mail mailToRelevantPersons = Mail.builder()
            .withRecipient(relevantRecipientsToInform)
            .withSubject("subject.application.revoked.management")
            .withTemplate("application_revoked_to_management", modelSupplier)
            .build();

        mailService.send(mailToRelevantPersons);
    }

    /**
     * Sends an email to the recipients of interest notifying
     * that an application for leave was directly cancelled.
     *
     * @param application that was cancelled directly
     * @param comment     additional comment for the application
     */
    @Async
    void sendCancelledDirectlyToManagement(Application application, ApplicationComment comment) {

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            APPLICATION, application,
            VACATION_TYPE, application.getVacationType().getLabel(locale),
            COMMENT, comment
        );

        final List<Person> recipients = mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION);
        final Mail mailToAllowAndRemind = Mail.builder()
            .withRecipient(recipients)
            .withSubject("subject.application.cancelledDirectly.information.recipients_of_interest", application.getPerson().getNiceName())
            .withTemplate("application_cancelled_directly_to_management", modelSupplier)
            .build();

        mailService.send(mailToAllowAndRemind);
    }

    /**
     * Sends an email to the applicant if an application for leave got cancelled directly by himself.
     *
     * @param application the application which got cancelled directly
     * @param comment     describes the reason of the direct cancellation
     */
    @Async
    void sendCancelledDirectlyConfirmationByApplicant(Application application, ApplicationComment comment) {

        final Person recipient = application.getPerson();
        final ByteArrayResource calendarFile = generateCalendar(application, DEFAULT, CANCELLED, recipient);

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            APPLICATION, application,
            VACATION_TYPE, application.getVacationType().getLabel(locale),
            COMMENT, comment
        );

        // send cancelled by office information to the applicant
        final Mail mailToApplicant = Mail.builder()
            .withRecipient(recipient, NOTIFICATION_EMAIL_APPLICATION_CANCELLATION)
            .withSubject("subject.application.cancelledDirectly.user")
            .withTemplate("application_cancelled_directly_confirmation_by_applicant_to_applicant", modelSupplier)
            .withAttachment(CALENDAR_ICS, calendarFile)
            .build();
        mailService.send(mailToApplicant);

        // Inform colleagues of applicant which are in same department
        final MailTemplateModelSupplier modelColleaguesSupplier = locale -> Map.of(APPLICATION, application);
        final List<Person> relevantColleaguesToInform = mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION);
        final Mail mailToRelevantColleagues = Mail.builder()
            .withRecipient(relevantColleaguesToInform)
            .withSubject("subject.application.cancelled.to_colleagues", application.getPerson().getNiceName())
            .withTemplate("application_cancellation_to_colleagues", modelColleaguesSupplier)
            .withAttachment(CALENDAR_ICS, calendarFile)
            .build();
        mailService.send(mailToRelevantColleagues);
    }

    /**
     * Sends an email to the person of the given application
     * that some management person has cancelled an application directly on behalf of himself.
     *
     * @param application confirmed application on behalf
     * @param comment     additional comment for the application
     */
    @Async
    void sendCancelledDirectlyConfirmationByManagement(Application application, ApplicationComment comment) {

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            APPLICATION, application,
            VACATION_TYPE, application.getVacationType().getLabel(locale),
            COMMENT, comment
        );
        final Mail mailToApplicant = Mail.builder()
            .withRecipient(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_CANCELLATION)
            .withSubject("subject.application.cancelledDirectly.management")
            .withTemplate("application_cancelled_directly_confirmation_by_management_to_applicant", modelSupplier)
            .build();
        mailService.send(mailToApplicant);

        // Inform colleagues of applicant which are in same department
        final MailTemplateModelSupplier modelColleaguesSupplier = locale -> Map.of(APPLICATION, application);
        final List<Person> relevantColleaguesToInform = mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION);
        final Mail mailToRelevantColleagues = Mail.builder()
            .withRecipient(relevantColleaguesToInform)
            .withSubject("subject.application.cancelled.to_colleagues", application.getPerson().getNiceName())
            .withTemplate("application_cancellation_to_colleagues", modelColleaguesSupplier)
            .build();
        mailService.send(mailToRelevantColleagues);
    }

    /**
     * Sends an email to the applicant if an application for leave got cancelled by management.
     *
     * @param application the application which got cancelled
     * @param comment     describes the reason of the cancellation
     */
    @Async
    void sendCancelledConfirmationByManagement(Application application, ApplicationComment comment) {

        final ByteArrayResource calendarFile = generateCalendar(application, DEFAULT, CANCELLED, application.getPerson());

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            APPLICATION, application,
            COMMENT, comment
        );

        // send cancelled by office information to the applicant
        final Mail mailToApplicant = Mail.builder()
            .withRecipient(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_CANCELLATION)
            .withSubject("subject.application.cancelled.user")
            .withTemplate("application_cancelled_by_management_to_applicant", modelSupplier)
            .withAttachment(CALENDAR_ICS, calendarFile)
            .build();
        mailService.send(mailToApplicant);

        // send cancelled by office information to all other relevant persons
        final List<Person> recipientsOfInterest = mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION);
        final Mail mailToRelevantPersons = Mail.builder()
            .withRecipient(recipientsOfInterest)
            .withSubject("subject.application.cancelled.management")
            .withTemplate("application_cancelled_by_management_to_management", modelSupplier)
            .withAttachment(CALENDAR_ICS, calendarFile)
            .build();
        mailService.send(mailToRelevantPersons);

        // Inform colleagues of applicant which are in same department
        final MailTemplateModelSupplier modelColleaguesSupplier = locale -> Map.of(APPLICATION, application);
        final List<Person> relevantColleaguesToInform = mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION);
        final Mail mailToRelevantColleagues = Mail.builder()
            .withRecipient(relevantColleaguesToInform)
            .withSubject("subject.application.cancelled.to_colleagues", application.getPerson().getNiceName())
            .withTemplate("application_cancellation_to_colleagues", modelColleaguesSupplier)
            .withAttachment(CALENDAR_ICS, calendarFile)
            .build();
        mailService.send(mailToRelevantColleagues);
    }

    /**
     * Sends an email to the bosses notifying
     * that there is a new application for leave
     * which has to be allowed or rejected by a boss.
     *
     * @param application to allow or reject
     * @param comment     additional comment for the application
     */
    @Async
    void sendAppliedNotificationToManagement(Application application, ApplicationComment comment) {

        final List<Application> applicationsForLeave =
            departmentService.getApplicationsFromColleaguesOf(application.getPerson(), application.getStartDate(), application.getEndDate());

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            APPLICATION, application,
            VACATION_TYPE, application.getVacationType().getLabel(locale),
            COMMENT, comment,
            "departmentVacations", applicationsForLeave
        );

        final List<Person> recipients = mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED);
        final Mail mailToAllowAndRemind = Mail.builder()
            .withRecipient(recipients)
            .withSubject("subject.application.applied.boss", application.getPerson().getNiceName())
            .withTemplate("application_applied_to_management", modelSupplier)
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
    @Async
    void sendTemporaryAllowedNotification(Application application, ApplicationComment comment) {

        // Inform user that the application for leave has been allowed temporary
        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            APPLICATION, application,
            COMMENT, comment
        );

        final Mail mailToApplicant = Mail.builder()
            .withRecipient(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED)
            .withSubject("subject.application.temporaryAllowed.user")
            .withTemplate("application_temporary_allowed_to_applicant", modelSupplier)
            .build();
        mailService.send(mailToApplicant);

        // Inform second stage authorities that there is an application for leave that must be allowed
        final List<Application> applicationsForLeave =
            departmentService.getApplicationsFromColleaguesOf(application.getPerson(), application.getStartDate(), application.getEndDate());

        final MailTemplateModelSupplier modelSecondStageSupplier = locale -> Map.of(
            APPLICATION, application,
            VACATION_TYPE, application.getVacationType().getLabel(locale),
            COMMENT, comment,
            "departmentVacations", applicationsForLeave
        );

        final List<Person> recipients = mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED);
        final Mail mailToTemporaryAllow = Mail.builder()
            .withRecipient(recipients)
            .withSubject("subject.application.temporaryAllowed.management")
            .withTemplate("application_temporary_allowed_to_management", modelSecondStageSupplier)
            .build();
        mailService.send(mailToTemporaryAllow);
    }

    /**
     * If an application has status waiting and no person with management rights
     * has decided about it after a certain time, the management will receive a
     * reminding notification.
     *
     * @param application to receive a reminding notification
     */
    @Async
    void sendRemindNotificationToManagement(Application application) {

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(APPLICATION, application);

        final List<Person> recipients = mailRecipientService.getResponsibleManagersOf(application.getPerson());
        final Mail mailToAllowAndRemind = Mail.builder()
            .withRecipient(recipients)
            .withSubject("subject.application.remind")
            .withTemplate("application_remind_to_management", modelSupplier)
            .build();
        mailService.send(mailToAllowAndRemind);
    }

    @Async
    void sendRemindForUpcomingApplicationsReminderNotification(List<Application> applications) {
        for (Application application : applications) {

            final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
                APPLICATION, application,
                "daysBeforeUpcomingApplication", ChronoUnit.DAYS.between(LocalDate.now(clock), application.getStartDate())
            );

            final Mail mailToUpcomingApplicationsPersons = Mail.builder()
                .withRecipient(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_UPCOMING)
                .withSubject("subject.application.remind.upcoming")
                .withTemplate("application_cron_remind_for_upcoming_application_to_applicant", modelSupplier)
                .build();
            mailService.send(mailToUpcomingApplicationsPersons);
        }
    }

    @Async
    void sendRemindForUpcomingHolidayReplacement(List<Application> applications) {
        for (Application application : applications) {
            for (HolidayReplacementEntity holidayReplacement : application.getHolidayReplacements()) {

                final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
                    APPLICATION, application,
                    "daysBeforeUpcomingHolidayReplacement", ChronoUnit.DAYS.between(LocalDate.now(clock), application.getStartDate()),
                    "replacementNote", holidayReplacement.getNote()
                );

                final Mail mailToUpcomingHolidayReplacement = Mail.builder()
                    .withRecipient(holidayReplacement.getPerson(), NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING)
                    .withSubject("subject.application.remind.upcoming.holiday_replacement", application.getPerson().getNiceName())
                    .withTemplate("application_cron_upcoming_holiday_replacement_to_holiday_replacement", modelSupplier)
                    .build();
                mailService.send(mailToUpcomingHolidayReplacement);
            }
        }
    }

    @Async
    void sendRemindForWaitingApplicationsReminderNotification(List<Application> waitingApplications) {

        /*
         * what is happening here?
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
        final Map<Person, List<Application>> applicationsPerRecipient = waitingApplications.stream()
            .flatMap(application -> mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER).stream()
                .map(person -> new AbstractMap.SimpleEntry<>(person, application)))
            .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())));

        for (Map.Entry<Person, List<Application>> entry : applicationsPerRecipient.entrySet()) {

            final List<Application> applications = entry.getValue();
            final int numberOfApplications = applications.size();
            final MailTemplateModelSupplier modelSupplier = applicationRemindCronManagementMailTemplateModelSupplier(applications);

            final Person recipient = entry.getKey();
            final Mail mailToRemindForWaiting = Mail.builder()
                .withRecipient(recipient)
                .withSubject("subject.application.cronRemind", numberOfApplications)
                .withTemplate("application_remind_cron_to_management", modelSupplier)
                .build();
            mailService.send(mailToRemindForWaiting);
        }
    }

    private static MailTemplateModelSupplier applicationRemindCronManagementMailTemplateModelSupplier(List<Application> applications) {

        final List<Application> sortedApplications = applications.stream()
            .sorted(comparing(Application::getStartDate).reversed())
            .toList();

        // mapper is called for every mail recipient and locale could differ. therefore cache mapping by locale.
        final Function<Locale, Map<Person, List<ApplicationMailRemindCronManagementDto>>> applicationDtoMapper = cachedByKey(
            locale -> sortedApplications.stream()
                .map(application -> applicationRemindCronManagementDto(application, locale))
                .collect(groupingBy(ApplicationMailRemindCronManagementDto::person))
        );

        return locale -> Map.of(
            "applicationsByPerson", applicationDtoMapper.apply(locale),
            "numberOfApplications", applications.size()
        );
    }

    private static ApplicationMailRemindCronManagementDto applicationRemindCronManagementDto(Application application, Locale locale) {
        return new ApplicationMailRemindCronManagementDto(
            application.getId(),
            application.getPerson(),
            application.getStartDate(),
            application.getEndDate(),
            application.getDayLength(),
            application.getVacationType().getLabel(locale)
        );
    }

    private ByteArrayResource generateCalendar(Application application, CalendarAbsenceType absenceType, Person recipient) {
        return generateCalendar(application, absenceType, PUBLISHED, recipient);
    }

    private ByteArrayResource generateCalendar(Application application, CalendarAbsenceType absenceType, ICalType iCalType, Person recipient) {
        final CalendarAbsence absence = new CalendarAbsence(application.getPerson(), application.getPeriod(), getAbsenceTimeConfiguration(), absenceType);
        return iCalService.getSingleAppointment(absence, iCalType, recipient);
    }

    private AbsenceTimeConfiguration getAbsenceTimeConfiguration() {
        final TimeSettings timeSettings = settingsService.getSettings().getTimeSettings();
        return new AbsenceTimeConfiguration(timeSettings);
    }

    private static <T, R> Function<T, R> cachedByKey(Function<T, R> function) {
        final Map<T, R> cache = new ConcurrentHashMap<>();
        return key -> cache.computeIfAbsent(key, function);
    }
}
