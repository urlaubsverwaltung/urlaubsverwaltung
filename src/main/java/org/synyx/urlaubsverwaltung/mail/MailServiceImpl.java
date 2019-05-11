package org.synyx.urlaubsverwaltung.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.MailSettings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;


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
        sendMailToPersons(persons, subjectMessageKey, templateName, model);
    }

    @Override
    public void sendMailTo(Person person, String subjectMessageKey, String templateName, Map<String, Object> model) {

        final List<Person> persons = singletonList(person);
        sendMailToPersons(persons, subjectMessageKey, templateName, model);
    }

    @Override
    public void sendMailTo(List<Person> persons, String subjectMessageKey, String templateName, Map<String, Object> model) {

        sendMailToPersons(persons, subjectMessageKey, templateName, model);
    }

    @Override
    public void sendTechnicalMail(String subjectMessageKey, String templateName, Map<String, Object> model) {

        MailSettings mailSettings = settingsService.getSettings().getMailSettings();

        sendMailToRecipients(singletonList(mailSettings.getAdministrator()), subjectMessageKey, templateName, model);
    }

    private void sendMailToPersons(List<Person> persons, String subjectMessageKey, String templateName, Map<String, Object> model) {

        final List<String> recipients = recipientService.getMailAddresses(persons);
        sendMailToRecipients(recipients, subjectMessageKey, templateName, model);
    }

    private void sendMailToRecipients(List<String> recipients, String subjectMessageKey, String templateName, Map<String, Object> model) {

        MailSettings mailSettings = getMailSettings();
        model.put("baseLinkURL", mailSettings.getBaseLinkURL());

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
