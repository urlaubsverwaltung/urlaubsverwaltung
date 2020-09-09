package org.synyx.urlaubsverwaltung.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.StringUtils.hasText;

/**
 * Implementation of interface {@link MailService}.
 */
@Service("mailService")
@EnableConfigurationProperties(MailProperties.class)
class MailServiceImpl implements MailService {

    private static final Locale LOCALE = Locale.GERMAN;

    private final MessageSource messageSource;
    private final MailBuilder mailBuilder;
    private final MailSenderService mailSenderService;
    private final MailProperties mailProperties;
    private final PersonService personService;

    @Autowired
    MailServiceImpl(MessageSource messageSource, MailBuilder mailBuilder, MailSenderService mailSenderService,
                    MailProperties mailProperties, PersonService personService) {

        this.messageSource = messageSource;
        this.mailBuilder = mailBuilder;
        this.mailProperties = mailProperties;
        this.mailSenderService = mailSenderService;
        this.personService = personService;
    }

    @Override
    public void send(Mail mail) {

        final Map<String, Object> model = mail.getTemplateModel();
        model.put("baseLinkURL", getApplicationUrl());

        final String subject = getTranslation(mail.getSubjectMessageKey(), mail.getSubjectMessageArguments());
        final String sender = mailProperties.getSender();

        if (mail.isSendToTechnicalMail()) {
            final String body = mailBuilder.buildMailBody(mail.getTemplateName(), model, LOCALE);
            mailSenderService.sendEmail(sender, List.of(mailProperties.getAdministrator()), subject, body);
        } else {
            final List<Person> recipients = getRecipients(mail);
            if (mail.getMailAttachments().isEmpty()) {
                if (mail.isSendToEachIndividually()) {
                    recipients.forEach(recipient -> {
                        model.put("recipient", recipient);
                        final String body = mailBuilder.buildMailBody(mail.getTemplateName(), model, LOCALE);
                        mailSenderService.sendEmail(sender, List.of(recipient.getEmail()), subject, body);
                    });
                } else {
                    final String body = mailBuilder.buildMailBody(mail.getTemplateName(), model, LOCALE);
                    mailSenderService.sendEmail(sender, getMailAddresses(recipients), subject, body);
                }
            } else {
                if (mail.isSendToEachIndividually()) {
                    recipients.forEach(recipient -> {
                        model.put("recipient", recipient);
                        final String body = mailBuilder.buildMailBody(mail.getTemplateName(), model, LOCALE);
                        mailSenderService.sendEmail(sender, List.of(recipient.getEmail()), subject, body, mail.getMailAttachments());
                    });
                } else {
                    final String body = mailBuilder.buildMailBody(mail.getTemplateName(), model, LOCALE);
                    mailSenderService.sendEmail(sender, getMailAddresses(recipients), subject, body, mail.getMailAttachments());
                }
            }
        }
    }

    private List<Person> getRecipients(Mail mail) {

        final List<Person> recipients = new ArrayList<>();
        if (mail.getMailNotificationRecipients() != null) {
            recipients.addAll(personService.getPersonsWithNotificationType(mail.getMailNotificationRecipients()));
        } else if (!mail.getMailAddressRecipients().isEmpty()) {
            recipients.addAll(mail.getMailAddressRecipients());
        }

        return recipients;
    }

    private List<String> getMailAddresses(List<Person> persons) {
        return persons.stream()
            .filter(person -> hasText(person.getEmail()))
            .map(Person::getEmail)
            .collect(toList());
    }

    private String getTranslation(String key, Object... args) {
        return messageSource.getMessage(key, args, LOCALE);
    }

    private String getApplicationUrl() {
        return ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath("/").build().toString();
    }
}
