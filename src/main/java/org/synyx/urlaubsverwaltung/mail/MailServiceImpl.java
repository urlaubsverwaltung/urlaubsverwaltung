package org.synyx.urlaubsverwaltung.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.MailSettings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Collections.singletonList;


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
    private final SettingsService settingsService;

    @Autowired
    MailServiceImpl(MessageSource messageSource, MailBuilder mailBuilder, MailSender mailSender,
                    RecipientService recipientService, SettingsService settingsService) {

        this.messageSource = messageSource;
        this.mailBuilder = mailBuilder;
        this.mailSender = mailSender;
        this.recipientService = recipientService;
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
    public void sendMailToEach(List<Person> persons, String subjectMessageKey, String templateName, Map<String, Object> model, Object... args) {

        persons.forEach(person -> {
            model.put("recipient", person);
            final List<String> mailAddress = recipientService.getMailAddresses(person);
            sendMailToRecipients(mailAddress, subjectMessageKey, templateName, model, args);
        });
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

    private void sendMailToRecipients(List<String> recipients, String subjectMessageKey, String templateName, Map<String, Object> model, Object... args) {

        MailSettings mailSettings = getMailSettings();
        model.put("baseLinkURL", mailSettings.getBaseLinkURL());

        final String subject = getTranslation(subjectMessageKey, args);
        final String text = mailBuilder.buildMailBody(templateName, model, LOCALE);

        mailSender.sendEmail(mailSettings, recipients, subject, text);
    }

    private String getTranslation(String key, Object... args) {

        return messageSource.getMessage(key, args, LOCALE);
    }


    private MailSettings getMailSettings() {

        return settingsService.getSettings().getMailSettings();
    }
}
