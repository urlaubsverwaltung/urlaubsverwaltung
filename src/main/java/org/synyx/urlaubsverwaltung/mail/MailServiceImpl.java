package org.synyx.urlaubsverwaltung.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;

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
    private final MailOptionProvider mailOptionProvider;
    private final RecipientService recipientService;

    @Autowired
    MailServiceImpl(MessageSource messageSource, MailBuilder mailBuilder, MailSender mailSender,
                    MailOptionProvider mailOptionProvider, RecipientService recipientService) {

        this.messageSource = messageSource;
        this.mailBuilder = mailBuilder;
        this.mailOptionProvider = mailOptionProvider;
        this.mailSender = mailSender;
        this.recipientService = recipientService;
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


        sendMailToRecipients(singletonList(mailOptionProvider.getAdministrator()), subjectMessageKey, templateName, model);
    }

    private void sendMailToPersons(List<Person> persons, String subjectMessageKey, String templateName, Map<String, Object> model) {

        final List<String> recipients = recipientService.getMailAddresses(persons);
        sendMailToRecipients(recipients, subjectMessageKey, templateName, model);
    }

    private void sendMailToRecipients(List<String> recipients, String subjectMessageKey, String templateName, Map<String, Object> model, Object... args) {

        model.put("baseLinkURL", mailOptionProvider.getApplicationUrl());

        final String subject = getTranslation(subjectMessageKey, args);
        final String text = mailBuilder.buildMailBody(templateName, model, LOCALE);

        mailSender.sendEmail(mailOptionProvider.getSender(), recipients, subject, text);
    }

    private String getTranslation(String key, Object... args) {

        return messageSource.getMessage(key, args, LOCALE);
    }

}
