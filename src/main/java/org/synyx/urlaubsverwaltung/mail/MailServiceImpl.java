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
    private final MailContentBuilder mailContentBuilder;
    private final MailSenderService mailSenderService;
    private final MailProperties mailProperties;
    private final PersonService personService;

    @Autowired
    MailServiceImpl(MessageSource messageSource, MailContentBuilder mailContentBuilder, MailSenderService mailSenderService,
                    MailProperties mailProperties, PersonService personService) {

        this.messageSource = messageSource;
        this.mailContentBuilder = mailContentBuilder;
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

        final List<Person> recipients = getRecipients(mail);
        if (mail.isSendToEachIndividually()) {
            recipients.forEach(recipient -> {
                model.put("recipient", recipient);
                final String body = mailContentBuilder.buildMailBody(mail.getTemplateName(), model, LOCALE);
                mail.getMailAttachments().ifPresentOrElse(
                    mailAttachments -> mailSenderService.sendEmail(sender, List.of(recipient.getEmail()), subject, body, mailAttachments),
                    () -> mailSenderService.sendEmail(sender, List.of(recipient.getEmail()), subject, body));
            });
        } else {
            final String body = mailContentBuilder.buildMailBody(mail.getTemplateName(), model, LOCALE);
            mail.getMailAttachments().ifPresentOrElse(
                mailAttachments -> mailSenderService.sendEmail(sender, getMailAddresses(recipients), subject, body, mailAttachments),
                () -> mailSenderService.sendEmail(sender, getMailAddresses(recipients), subject, body));
        }
    }

    private List<Person> getRecipients(Mail mail) {

        final List<Person> recipients = new ArrayList<>();
        mail.getMailNotificationRecipients().ifPresent(mailNotification -> recipients.addAll(personService.getPersonsWithNotificationType(mailNotification)));
        mail.getMailAddressRecipients().ifPresent(recipients::addAll);

        if (mail.isSendToTechnicalMail()) {
            recipients.add(new Person(null, null, "Administrator", mailProperties.getAdministrator()));
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
