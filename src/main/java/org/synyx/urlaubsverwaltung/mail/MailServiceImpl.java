package org.synyx.urlaubsverwaltung.mail;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementation of interface {@link MailService}.
 */
@Service("mailService")
@EnableConfigurationProperties(MailProperties.class)
class MailServiceImpl implements MailService {

    private static final Logger LOG = getLogger(lookup().lookupClass());
    private static final Locale LOCALE = Locale.GERMAN;

    private final MessageSource emailMessageSource;
    private final ITemplateEngine emailTemplateEngine;
    private final MailSenderService mailSenderService;
    private final MailProperties mailProperties;
    private final PersonService personService;

    @Autowired
    MailServiceImpl(MessageSource emailMessageSource, ITemplateEngine emailTemplateEngine, MailSenderService mailSenderService,
                    MailProperties mailProperties, PersonService personService) {
        this.emailMessageSource = emailMessageSource;
        this.emailTemplateEngine = emailTemplateEngine;
        this.mailProperties = mailProperties;
        this.mailSenderService = mailSenderService;
        this.personService = personService;
    }

    @Override
    public void send(Mail mail) {

        final Context context = new Context(LOCALE);
        context.setVariables(mail.getTemplateModel());
        context.setVariable("baseLinkURL", getApplicationUrl());

        final String subject = getTranslation(mail.getSubjectMessageKey(), mail.getSubjectMessageArguments());
        final String sender = generateMailAddressAndDisplayName(mailProperties.getSender(), mailProperties.getSenderDisplayName());

        getRecipients(mail).forEach(recipient -> {
            context.setVariable("recipient", recipient);
            final String body = emailTemplateEngine.process(mail.getTemplateName(), context);
            final String email = recipient.getEmail();

            if (email != null) {
                mail.getMailAttachments().ifPresentOrElse(
                    mailAttachments -> mailSenderService.sendEmail(sender, email, subject, body, mailAttachments),
                    () -> mailSenderService.sendEmail(sender, email, subject, body)
                );
            } else {
                LOG.debug("Could not send mail to E-Mail-Address of person with id {}, because email is null.", recipient.getId());
            }
        });
    }

    private List<Person> getRecipients(Mail mail) {

        final List<Person> recipients = new ArrayList<>();
        mail.getMailNotificationRecipients().ifPresent(mailNotification -> recipients.addAll(personService.getActivePersonsWithNotificationType(mailNotification)));
        mail.getMailAddressRecipients().ifPresent(recipients::addAll);

        if (mail.isSendToTechnicalMail()) {
            recipients.add(new Person(null, null, "Administrator", mailProperties.getAdministrator()));
        }

        return recipients.stream().distinct().collect(Collectors.toList());
    }

    private String getTranslation(String key, Object... args) {
        return emailMessageSource.getMessage(key, args, LOCALE);
    }

    private String getApplicationUrl() {
        final String applicationUrl = mailProperties.getApplicationUrl();
        return applicationUrl.endsWith("/") ? applicationUrl : applicationUrl + "/";
    }

    private String generateMailAddressAndDisplayName(String address, String displayName) {
        return String.format("%s <%s>", displayName, address);
    }
}
