package org.synyx.urlaubsverwaltung.core.mail;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.MessageSource;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.overtime.Overtime;
import org.synyx.urlaubsverwaltung.core.overtime.OvertimeComment;
import org.synyx.urlaubsverwaltung.core.person.MailNotification;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.MailSettings;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Implementation of interface {@link MailService}.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
@Service("mailService")
class MailServiceImpl implements MailService {

    protected static final Locale LOCALE = Locale.GERMAN;

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
    public void sendNewApplicationNotification(Application application, ApplicationComment comment) {

        MailSettings mailSettings = getMailSettings();

        Map<String, Object> model = createModelForApplicationStatusChangeMail(mailSettings, application,
                Optional.ofNullable(comment));
        model.put("departmentVacations",
            departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(application.getPerson(),
                application.getStartDate(), application.getEndDate()));

        List<Person> recipients = recipientService.getRecipientsForAllowAndRemind(application);
        String subject = getTranslation("subject.application.applied.boss", application.getPerson().getNiceName());

        sendMailToEachRecipient(model, recipients, "new_applications", subject);
    }


    private String getTranslation(String key, Object... args) {

        return messageSource.getMessage(key, args, LOCALE);
    }


    private void sendMailToEachRecipient(Map<String, Object> model, List<Person> recipients, String template,
        String subject) {

        MailSettings mailSettings = getMailSettings();

        for (Person recipient : recipients) {
            model.put("recipient", recipient);

            String text = mailBuilder.buildMailBody(template, model);
            mailSender.sendEmail(mailSettings, RecipientUtil.getMailAddresses(recipient), subject, text);
        }
    }


    private MailSettings getMailSettings() {

        return settingsService.getSettings().getMailSettings();
    }


    private Map<String, Object> createModelForApplicationStatusChangeMail(MailSettings mailSettings,
        Application application, Optional<ApplicationComment> optionalComment) {

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("dayLength", getTranslation(application.getDayLength().name()));
        model.put("settings", mailSettings);

        if (optionalComment.isPresent()) {
            model.put("comment", optionalComment.get());
        }

        return model;
    }


    @Override
    public void sendRemindBossNotification(Application application) {

        MailSettings mailSettings = getMailSettings();
        Map<String, Object> model = createModelForApplicationStatusChangeMail(mailSettings, application,
                Optional.empty());

        List<Person> recipients = recipientService.getRecipientsForAllowAndRemind(application);
        sendMailToEachRecipient(model, recipients, "remind", getTranslation("subject.application.remind"));
    }


    @Override
    public void sendTemporaryAllowedNotification(Application application, ApplicationComment comment) {

        MailSettings mailSettings = getMailSettings();
        Map<String, Object> model = createModelForApplicationStatusChangeMail(mailSettings, application,
                Optional.ofNullable(comment));
        model.put("departmentVacations",
            departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(application.getPerson(),
                application.getStartDate(), application.getEndDate()));

        // Inform user that the application for leave has been allowed temporary
        String textUser = mailBuilder.buildMailBody("temporary_allowed_user", model);
        mailSender.sendEmail(mailSettings, RecipientUtil.getMailAddresses(application.getPerson()),
            getTranslation("subject.application.temporaryAllowed.user"), textUser);

        // Inform second stage authorities that there is an application for leave that must be allowed
        List<Person> recipients = recipientService.getRecipientsForTemporaryAllow(application);
        sendMailToEachRecipient(model, recipients, "temporary_allowed_second_stage_authority",
            getTranslation("subject.application.temporaryAllowed.secondStage"));
    }


    @Override
    public void sendAllowedNotification(Application application, ApplicationComment comment) {

        MailSettings mailSettings = getMailSettings();
        Map<String, Object> model = createModelForApplicationStatusChangeMail(mailSettings, application,
                Optional.ofNullable(comment));

        // Inform user that the application for leave has been allowed
        String textUser = mailBuilder.buildMailBody("allowed_user", model);
        mailSender.sendEmail(mailSettings, RecipientUtil.getMailAddresses(application.getPerson()),
            getTranslation("subject.application.allowed.user"), textUser);

        // Inform office that there is a new allowed application for leave

        String textOffice = mailBuilder.buildMailBody("allowed_office", model);
        mailSender.sendEmail(mailSettings,
            RecipientUtil.getMailAddresses(
                recipientService.getRecipientsWithNotificationType(MailNotification.NOTIFICATION_OFFICE)),
            getTranslation("subject.application.allowed.office"), textOffice);
    }


    @Override
    public void sendRejectedNotification(Application application, ApplicationComment comment) {

        MailSettings mailSettings = getMailSettings();
        Map<String, Object> model = createModelForApplicationStatusChangeMail(mailSettings, application,
                Optional.ofNullable(comment));
        String text = mailBuilder.buildMailBody("rejected", model);
        mailSender.sendEmail(mailSettings, RecipientUtil.getMailAddresses(application.getPerson()),
            getTranslation("subject.application.rejected"), text);
    }


    @Override
    public void sendReferApplicationNotification(Application application, Person recipient, Person sender) {

        MailSettings mailSettings = getMailSettings();

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("settings", mailSettings);
        model.put("recipient", recipient);
        model.put("sender", sender);

        String text = mailBuilder.buildMailBody("refer", model);
        mailSender.sendEmail(mailSettings, RecipientUtil.getMailAddresses(recipient),
            getTranslation("subject.application.refer"), text);
    }


    @Override
    public void sendConfirmation(Application application, ApplicationComment comment) {

        MailSettings mailSettings = getMailSettings();
        Map<String, Object> model = createModelForApplicationStatusChangeMail(mailSettings, application,
                Optional.ofNullable(comment));
        String text = mailBuilder.buildMailBody("confirm", model);
        mailSender.sendEmail(mailSettings, RecipientUtil.getMailAddresses(application.getPerson()),
            getTranslation("subject.application.applied.user"), text);
    }


    @Override
    public void sendAppliedForLeaveByOfficeNotification(Application application, ApplicationComment comment) {

        MailSettings mailSettings = getMailSettings();
        Map<String, Object> model = createModelForApplicationStatusChangeMail(mailSettings, application,
                Optional.ofNullable(comment));
        String text = mailBuilder.buildMailBody("new_application_by_office", model);
        mailSender.sendEmail(mailSettings, RecipientUtil.getMailAddresses(application.getPerson()),
            getTranslation("subject.application.appliedByOffice"), text);
    }


    @Override
    public void sendCancelledByOfficeNotification(Application application, ApplicationComment comment) {

        MailSettings mailSettings = getMailSettings();
        Map<String, Object> model = createModelForApplicationStatusChangeMail(mailSettings, application,
                Optional.ofNullable(comment));

        String text = mailBuilder.buildMailBody("cancelled_by_office", model);

        mailSender.sendEmail(mailSettings, RecipientUtil.getMailAddresses(application.getPerson()),
            getTranslation("subject.application.cancelled.user"), text);
    }


    /**
     * Sends an email to the manager of the application to inform about a technical event, e.g. if an error occurred.
     *
     * @param  subject  of the email
     * @param  text  of the body of the email
     */
    private void sendTechnicalNotification(final String subject, final String text) {

        MailSettings mailSettings = settingsService.getSettings().getMailSettings();

        mailSender.sendEmail(mailSettings, Collections.singletonList(mailSettings.getAdministrator()), subject, text);
    }


    @Override
    public void sendCalendarSyncErrorNotification(String calendarName, Absence absence, String exception) {

        Map<String, Object> model = new HashMap<>();
        model.put("calendar", calendarName);
        model.put("absence", absence);
        model.put("exception", exception);

        String text = mailBuilder.buildMailBody("error_calendar_sync", model);

        sendTechnicalNotification(getTranslation("subject.error.calendar.sync"), text);
    }


    @Override
    public void sendCalendarUpdateErrorNotification(String calendarName, Absence absence, String eventId,
        String exception) {

        Map<String, Object> model = new HashMap<>();
        model.put("calendar", calendarName);
        model.put("absence", absence);
        model.put("eventId", eventId);
        model.put("exception", exception);

        String text = mailBuilder.buildMailBody("error_calendar_update", model);

        sendTechnicalNotification(getTranslation("subject.error.calendar.update"), text);
    }


    @Override
    public void sendCalendarDeleteErrorNotification(String calendarName, String eventId, String exception) {

        Map<String, Object> model = new HashMap<>();
        model.put("calendar", calendarName);
        model.put("eventId", eventId);
        model.put("exception", exception);

        String text = mailBuilder.buildMailBody("error_calendar_delete", model);

        sendTechnicalNotification(getTranslation("subject.error.calendar.delete"), text);
    }


    @Override
    public void sendSuccessfullyUpdatedAccountsNotification(List<Account> updatedAccounts) {

        Map<String, Object> model = new HashMap<>();
        model.put("accounts", updatedAccounts);
        model.put("year", DateMidnight.now().getYear());

        String text = mailBuilder.buildMailBody("updated_accounts", model);

        // send email to office for printing statistic
        mailSender.sendEmail(getMailSettings(),
            RecipientUtil.getMailAddresses(
                recipientService.getRecipientsWithNotificationType(MailNotification.NOTIFICATION_OFFICE)),
            getTranslation("subject.account.updatedRemainingDays"), text);

        // send email to manager to notify about update of accounts
        sendTechnicalNotification(getTranslation("subject.account.updatedRemainingDays"), text);
    }


    @Override
    public void sendSuccessfullyUpdatedSettingsNotification(Settings settings) {

        Map<String, Object> model = new HashMap<>();
        model.put("settings", settings);

        String text = mailBuilder.buildMailBody("updated_settings", model);
        sendTechnicalNotification(getTranslation("subject.settings.updated"), text);
    }


    @Override
    public void sendSickNoteConvertedToVacationNotification(Application application) {

        MailSettings mailSettings = getMailSettings();

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("settings", mailSettings);

        String text = mailBuilder.buildMailBody("sicknote_converted", model);
        mailSender.sendEmail(mailSettings, RecipientUtil.getMailAddresses(application.getPerson()),
            getTranslation("subject.sicknote.converted"), text);
    }


    @Override
    public void sendEndOfSickPayNotification(SickNote sickNote) {

        Map<String, Object> model = new HashMap<>();
        model.put("sickNote", sickNote);

        String text = mailBuilder.buildMailBody("sicknote_end_of_sick_pay", model);

        mailSender.sendEmail(getMailSettings(), RecipientUtil.getMailAddresses(sickNote.getPerson()),
            getTranslation("subject.sicknote.endOfSickPay"), text);
        mailSender.sendEmail(getMailSettings(),
            RecipientUtil.getMailAddresses(
                recipientService.getRecipientsWithNotificationType(MailNotification.NOTIFICATION_OFFICE)),
            getTranslation("subject.sicknote.endOfSickPay"), text);
    }


    @Override
    public void notifyHolidayReplacement(Application application) {

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("dayLength", messageSource.getMessage(application.getDayLength().name(), null, LOCALE));

        String text = mailBuilder.buildMailBody("notify_holiday_replacement", model);

        mailSender.sendEmail(getMailSettings(), RecipientUtil.getMailAddresses(application.getHolidayReplacement()),
            getTranslation("subject.application.holidayReplacement"), text);
    }


    @Override
    public void sendUserCreationNotification(Person person, String rawPassword) {

        Map<String, Object> model = new HashMap<>();
        model.put("person", person);
        model.put("rawPassword", rawPassword);
        model.put("applicationUrl", "");

        String text = mailBuilder.buildMailBody("user_creation", model);

        mailSender.sendEmail(getMailSettings(), RecipientUtil.getMailAddresses(person),
            getTranslation("subject.userCreation"), text);
    }


    @Override
    public void sendCancellationRequest(Application application, ApplicationComment createdComment) {

        MailSettings mailSettings = getMailSettings();

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("comment", createdComment);
        model.put("settings", mailSettings);

        String text = mailBuilder.buildMailBody("application_cancellation_request", model);

        mailSender.sendEmail(mailSettings,
            RecipientUtil.getMailAddresses(
                recipientService.getRecipientsWithNotificationType(MailNotification.NOTIFICATION_OFFICE)),
            getTranslation("subject.application.cancellationRequest"), text);
    }


    @Override
    public void sendOvertimeNotification(Overtime overtime, OvertimeComment overtimeComment) {

        MailSettings mailSettings = getMailSettings();

        Map<String, Object> model = new HashMap<>();
        model.put("overtime", overtime);
        model.put("comment", overtimeComment);
        model.put("settings", mailSettings);

        String textOffice = mailBuilder.buildMailBody("overtime_office", model);

        List<Person> recipients = recipientService.getRecipientsWithNotificationType(
                MailNotification.OVERTIME_NOTIFICATION_OFFICE);

        mailSender.sendEmail(mailSettings, RecipientUtil.getMailAddresses(recipients),
            getTranslation("subject.overtime.created"), textOffice);
    }


    @Override
    public void sendRemindForWaitingApplicationsReminderNotification(List<Application> waitingApplications) {

        /**
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
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        for (Map.Entry<Person, List<Application>> entry : applicationsPerRecipient.entrySet()) {
            MailSettings mailSettings = getMailSettings();

            List<Application> applications = entry.getValue();
            Person recipient = entry.getKey();

            Map<String, Object> model = new HashMap<>();
            model.put("applicationList", applications);
            model.put("recipient", recipient);
            model.put("settings", mailSettings);

            String msg = mailBuilder.buildMailBody("cron_remind", model);

            mailSender.sendEmail(mailSettings, RecipientUtil.getMailAddresses(recipient),
                getTranslation("subject.application.cronRemind"), msg);
        }
    }
}
