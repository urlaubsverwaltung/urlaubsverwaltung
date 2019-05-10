package org.synyx.urlaubsverwaltung.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.Absence;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.AbsenceSettings;
import org.synyx.urlaubsverwaltung.settings.MailSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;

import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;


/**
 * Implementation of interface {@link MailService}.
 */
@Service("mailService")
class MailServiceImpl implements MailService {

    private static final Locale LOCALE = Locale.GERMAN;

    private final MessageSource messageSource;
    private final MailBuilder mailBuilder;
    private final MailSender mailSender;
    private final RecipientService recipientService;
    private final DepartmentService departmentService;
    private final SettingsService settingsService;

    @Autowired
    MailServiceImpl(MessageSource messageSource, MailBuilder mailBuilder, MailSender mailSender,
                    RecipientService recipientService, DepartmentService departmentService, SettingsService settingsService) {

        this.messageSource = messageSource;
        this.mailBuilder = mailBuilder;
        this.mailSender = mailSender;
        this.recipientService = recipientService;
        this.departmentService = departmentService;
        this.settingsService = settingsService;
    }

    @Override
    public void sendMailTo(MailNotification mailNotification, String subjectMessageKey, String templateName, Map<String, Object> model) {

        final List<Person> persons = recipientService.getRecipientsWithNotificationType(mailNotification);
        sendMail(persons, subjectMessageKey, templateName, model);
    }

    @Override
    public void sendMailTo(Person person, String subjectMessageKey, String templateName, Map<String, Object> model) {

        final List<Person> persons = singletonList(person);
        sendMail(persons, subjectMessageKey, templateName, model);
    }

    @Override
    public void sendMailTo(List<Person> persons, String subjectMessageKey, String templateName, Map<String, Object> model) {

        sendMail(persons, subjectMessageKey, templateName, model);
    }

    private void sendMail(List<Person> persons, String subjectMessageKey, String templateName, Map<String, Object> model) {

        MailSettings mailSettings = getMailSettings();
        model.put("baseLinkURL", mailSettings.getBaseLinkURL());

        final List<String> recipients = recipientService.getMailAddresses(persons);
        final String subject = getTranslation(subjectMessageKey);
        final String text = mailBuilder.buildMailBody(templateName, model, LOCALE);

        mailSender.sendEmail(mailSettings, recipients, subject, text);
    }

    @Override
    public void sendNewApplicationNotification(Application application, ApplicationComment comment) {

        MailSettings mailSettings = getMailSettings();

        final List<Application> applicationsForLeave =
            departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(application.getPerson(), application.getStartDate(), application.getEndDate());

        Map<String, Object> model = createModelForApplicationStatusChangeMail(mailSettings, application, ofNullable(comment));
        model.put("departmentVacations", applicationsForLeave);

        final List<Person> recipients = recipientService.getRecipientsForAllowAndRemind(application);
        final String subject = getTranslation("subject.application.applied.boss", application.getPerson().getNiceName());

        sendMailToEachRecipient(model, recipients, "new_applications", subject);
    }


    @Override
    public void sendRemindBossNotification(Application application) {

        MailSettings mailSettings = getMailSettings();
        Map<String, Object> model = createModelForApplicationStatusChangeMail(mailSettings, application, empty());

        List<Person> recipients = recipientService.getRecipientsForAllowAndRemind(application);
        sendMailToEachRecipient(model, recipients, "remind", getTranslation("subject.application.remind"));
    }


    @Override
    public void sendTemporaryAllowedNotification(Application application, ApplicationComment comment) {

        MailSettings mailSettings = getMailSettings();
        Map<String, Object> model = createModelForApplicationStatusChangeMail(mailSettings, application, ofNullable(comment));

        final List<Application> applicationsForLeave =
            departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(application.getPerson(), application.getStartDate(), application.getEndDate());

        model.put("departmentVacations", applicationsForLeave);

        // Inform user that the application for leave has been allowed temporary
        final String textUser = mailBuilder.buildMailBody("temporary_allowed_user", model, LOCALE);
        final String subjectForUser = getTranslation("subject.application.temporaryAllowed.user");
        mailSender.sendEmail(mailSettings, recipientService.getMailAddresses(application.getPerson()), subjectForUser, textUser);

        // Inform second stage authorities that there is an application for leave that must be allowed
        final List<Person> recipients = recipientService.getRecipientsForTemporaryAllow(application);
        final String subjectForSecondStage = getTranslation("subject.application.temporaryAllowed.secondStage");

        sendMailToEachRecipient(model, recipients, "temporary_allowed_second_stage_authority", subjectForSecondStage);
    }

    @Override
    public void sendReferApplicationNotification(Application application, Person recipient, Person sender) {

        MailSettings mailSettings = getMailSettings();

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("settings", mailSettings);
        model.put("recipient", recipient);
        model.put("sender", sender);

        final String text = mailBuilder.buildMailBody("refer", model, LOCALE);
        final String subject = getTranslation("subject.application.refer");
        final List<String> recipients = recipientService.getMailAddresses(recipient);
        mailSender.sendEmail(mailSettings, recipients, subject, text);
    }


    @Override
    public void sendConfirmation(Application application, ApplicationComment comment) {

        MailSettings mailSettings = getMailSettings();
        Map<String, Object> model = createModelForApplicationStatusChangeMail(mailSettings, application, ofNullable(comment));

        final String text = mailBuilder.buildMailBody("confirm", model, LOCALE);
        final List<String> recipients = recipientService.getMailAddresses(application.getPerson());
        final String subject = getTranslation("subject.application.applied.user");
        mailSender.sendEmail(mailSettings, recipients, subject, text);
    }


    @Override
    public void sendAppliedForLeaveByOfficeNotification(Application application, ApplicationComment comment) {

        MailSettings mailSettings = getMailSettings();
        Map<String, Object> model = createModelForApplicationStatusChangeMail(mailSettings, application, ofNullable(comment));

        final List<String> recipients = recipientService.getMailAddresses(application.getPerson());
        final String subject = getTranslation("subject.application.appliedByOffice");
        final String text = mailBuilder.buildMailBody("new_application_by_office", model, LOCALE);
        mailSender.sendEmail(mailSettings, recipients, subject, text);
    }


    @Override
    public void sendCancelledByOfficeNotification(Application application, ApplicationComment comment) {

        MailSettings mailSettings = getMailSettings();
        Map<String, Object> model = createModelForApplicationStatusChangeMail(mailSettings, application, ofNullable(comment));

        final List<String> recipients = recipientService.getMailAddresses(application.getPerson());
        final String subject = getTranslation("subject.application.cancelled.user");
        final String text = mailBuilder.buildMailBody("cancelled_by_office", model, LOCALE);
        mailSender.sendEmail(mailSettings, recipients, subject, text);
    }


    /**
     * Sends an email to the manager of the application to inform about a technical event, e.g. if an error occurred.
     *
     * @param subject of the email
     * @param text    of the body of the email
     */
    private void sendTechnicalNotification(final String subject, final String text) {

        MailSettings mailSettings = settingsService.getSettings().getMailSettings();

        final List<String> recipients = singletonList(mailSettings.getAdministrator());
        mailSender.sendEmail(mailSettings, recipients, subject, text);
    }


    @Override
    public void sendCalendarSyncErrorNotification(String calendarName, Absence absence, String exception) {

        Map<String, Object> model = new HashMap<>();
        model.put("calendar", calendarName);
        model.put("absence", absence);
        model.put("exception", exception);

        final String subject = getTranslation("subject.error.calendar.sync");
        final String text = mailBuilder.buildMailBody("error_calendar_sync", model, LOCALE);
        sendTechnicalNotification(subject, text);
    }


    @Override
    public void sendCalendarUpdateErrorNotification(String calendarName, Absence absence, String eventId,
                                                    String exception) {

        Map<String, Object> model = new HashMap<>();
        model.put("calendar", calendarName);
        model.put("absence", absence);
        model.put("eventId", eventId);
        model.put("exception", exception);

        final String subject = getTranslation("subject.error.calendar.update");
        final String text = mailBuilder.buildMailBody("error_calendar_update", model, LOCALE);
        sendTechnicalNotification(subject, text);
    }


    @Override
    public void sendCalendarDeleteErrorNotification(String calendarName, String eventId, String exception) {

        Map<String, Object> model = new HashMap<>();
        model.put("calendar", calendarName);
        model.put("eventId", eventId);
        model.put("exception", exception);

        String text = mailBuilder.buildMailBody("error_calendar_delete", model, LOCALE);

        sendTechnicalNotification(getTranslation("subject.error.calendar.delete"), text);
    }


    @Override
    public void sendSuccessfullyUpdatedAccountsNotification(List<Account> updatedAccounts) {

        Map<String, Object> model = new HashMap<>();
        model.put("accounts", updatedAccounts);
        model.put("today", LocalDate.now(UTC));

        final String text = mailBuilder.buildMailBody("updated_accounts", model, LOCALE);
        final String subject = getTranslation("subject.account.updatedRemainingDays");

        // send email to office for printing statistic
        final List<String> recipients = recipientService.getMailAddresses(recipientService.getRecipientsWithNotificationType(NOTIFICATION_OFFICE));
        mailSender.sendEmail(getMailSettings(), recipients, subject, text);

        // send email to manager to notify about update of accounts
        sendTechnicalNotification(subject, text);
    }


    @Override
    public void sendSuccessfullyUpdatedSettingsNotification(Settings settings) {

        Map<String, Object> model = new HashMap<>();
        model.put("settings", settings);

        final String text = mailBuilder.buildMailBody("updated_settings", model, LOCALE);
        final String subject = getTranslation("subject.settings.updated");
        sendTechnicalNotification(subject, text);
    }


    @Override
    public void sendSickNoteConvertedToVacationNotification(Application application) {

        MailSettings mailSettings = getMailSettings();

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("settings", mailSettings);

        final String text = mailBuilder.buildMailBody("sicknote_converted", model, LOCALE);
        final String subject = getTranslation("subject.sicknote.converted");
        final List<String> recipients = recipientService.getMailAddresses(application.getPerson());
        mailSender.sendEmail(mailSettings, recipients, subject, text);
    }


    @Override
    public void sendEndOfSickPayNotification(SickNote sickNote) {

        final MailSettings mailSettings = getMailSettings();

        Map<String, Object> model = new HashMap<>();
        model.put("sickNote", sickNote);
        model.put("maximumSickPayDays", getAbsenceSettings().getMaximumSickPayDays());

        final String text = mailBuilder.buildMailBody("sicknote_end_of_sick_pay", model, LOCALE);
        final String subject = getTranslation("subject.sicknote.endOfSickPay");

        final List<String> recipientSickPerson = recipientService.getMailAddresses(sickNote.getPerson());
        mailSender.sendEmail(mailSettings, recipientSickPerson, subject, text);

        final List<String> recipientsOffice = recipientService.getMailAddresses(recipientService.getRecipientsWithNotificationType(NOTIFICATION_OFFICE));
        mailSender.sendEmail(mailSettings, recipientsOffice, subject, text);
    }


    @Override
    public void notifyHolidayReplacement(Application application) {

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("dayLength", messageSource.getMessage(application.getDayLength().name(), null, LOCALE));

        final String text = mailBuilder.buildMailBody("notify_holiday_replacement", model, LOCALE);
        final List<String> recipients = recipientService.getMailAddresses(application.getHolidayReplacement());
        final String subject = getTranslation("subject.application.holidayReplacement");

        mailSender.sendEmail(getMailSettings(), recipients, subject, text);
    }


    @Override
    public void sendUserCreationNotification(Person person, String rawPassword) {

        Map<String, Object> model = new HashMap<>();
        model.put("person", person);
        model.put("rawPassword", rawPassword);
        model.put("applicationUrl", "");

        final String text = mailBuilder.buildMailBody("user_creation", model, LOCALE);
        final List<String> recipients = recipientService.getMailAddresses(person);
        final String subject = getTranslation("subject.userCreation");

        mailSender.sendEmail(getMailSettings(), recipients, subject, text);
    }


    @Override
    public void sendCancellationRequest(Application application, ApplicationComment createdComment) {

        MailSettings mailSettings = getMailSettings();

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("comment", createdComment);
        model.put("settings", mailSettings);

        final String text = mailBuilder.buildMailBody("application_cancellation_request", model, LOCALE);
        final List<String> recipients = recipientService.getMailAddresses(recipientService.getRecipientsWithNotificationType(NOTIFICATION_OFFICE));
        final String subject = getTranslation("subject.application.cancellationRequest");

        mailSender.sendEmail(mailSettings, recipients, subject, text);
    }


    @Override
    public void sendRemindForWaitingApplicationsReminderNotification(List<Application> waitingApplications) {

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
            .flatMap(application ->
                recipientService.getRecipientsForAllowAndRemind(application)
                    .stream()
                    .map(person -> new AbstractMap.SimpleEntry<>(person, application)))
            .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())));

        for (Map.Entry<Person, List<Application>> entry : applicationsPerRecipient.entrySet()) {
            MailSettings mailSettings = getMailSettings();

            List<Application> applications = entry.getValue();
            Person recipient = entry.getKey();

            Map<String, Object> model = new HashMap<>();
            model.put("applicationList", applications);
            model.put("recipient", recipient);
            model.put("settings", mailSettings);

            final List<String> recipients = recipientService.getMailAddresses(recipient);
            final String subject = getTranslation("subject.application.cronRemind");
            final String text = mailBuilder.buildMailBody("cron_remind", model, LOCALE);

            mailSender.sendEmail(mailSettings, recipients, subject, text);
        }
    }

    private String getTranslation(String key, Object... args) {

        return messageSource.getMessage(key, args, LOCALE);
    }


    private void sendMailToEachRecipient(Map<String, Object> model, List<Person> persons, String template,
                                         String subject) {

        MailSettings mailSettings = getMailSettings();

        for (Person recipient : persons) {
            model.put("recipient", recipient);

            final String text = mailBuilder.buildMailBody(template, model, LOCALE);
            final List<String> recipients = recipientService.getMailAddresses(recipient);

            mailSender.sendEmail(mailSettings, recipients, subject, text);
        }
    }


    private MailSettings getMailSettings() {

        return settingsService.getSettings().getMailSettings();
    }

    private AbsenceSettings getAbsenceSettings() {

        return settingsService.getSettings().getAbsenceSettings();
    }

    private Map<String, Object> createModelForApplicationStatusChangeMail(MailSettings mailSettings,
                                                                          Application application, Optional<ApplicationComment> optionalComment) {

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationType", getTranslation(application.getVacationType().getCategory().getMessageKey()));
        model.put("dayLength", getTranslation(application.getDayLength().name()));
        model.put("settings", mailSettings);

        optionalComment.ifPresent(applicationComment -> model.put("comment", applicationComment));

        return model;
    }
}
