package org.synyx.urlaubsverwaltung.mail;

import org.springframework.core.io.ByteArrayResource;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class Mail {

    private final List<Person> mailAddressRecipients;

    private final String templateName;
    private final MailTemplateModelSupplier templateModelSupplier;

    private final String subjectMessageKey;
    private final Object[] subjectMessageArguments;

    private final List<MailAttachment> mailAttachments;

    Mail(
        List<Person> mailAddressRecipients,
        String templateName,
        MailTemplateModelSupplier templateModelSupplier,
        String subjectMessageKey,
        Object[] subjectMessageArguments,
        List<MailAttachment> mailAttachments
    ) {
        this.mailAddressRecipients = mailAddressRecipients;
        this.templateName = templateName;
        this.templateModelSupplier = templateModelSupplier;
        this.subjectMessageKey = subjectMessageKey;
        this.subjectMessageArguments = subjectMessageArguments;
        this.mailAttachments = mailAttachments;
    }

    public Optional<List<Person>> getMailAddressRecipients() {
        return Optional.ofNullable(mailAddressRecipients);
    }

    public String getTemplateName() {
        return templateName;
    }

    public Map<String, Object> getTemplateModel(Locale locale) {
        return templateModelSupplier.getMailTemplateModel(locale);
    }

    public String getSubjectMessageKey() {
        return subjectMessageKey;
    }

    public Object[] getSubjectMessageArguments() {
        return subjectMessageArguments;
    }

    public Optional<List<MailAttachment>> getMailAttachments() {
        return Optional.ofNullable(mailAttachments);
    }

    public static Mail.Builder builder() {
        return new Mail.Builder();
    }

    /**
     * Mail Builder for an easier way to create a mail
     */
    public static class Builder {

        private final List<Person> mailAddressRecipients = new ArrayList<>();

        private String templateName;
        private MailTemplateModelSupplier templateModelSupplier = locale -> new HashMap<>();

        private String subjectMessageKey;
        private Object[] subjectMessageArguments;

        private List<MailAttachment> mailAttachments;

        public Mail.Builder withRecipient(final Person recipient) {
            withRecipient(List.of(recipient));
            return this;
        }

        public Mail.Builder withRecipient(final Person recipient, final MailNotification mailNotification) {
            withRecipient(List.of(recipient), mailNotification);
            return this;
        }

        public Mail.Builder withRecipient(final List<Person> recipients) {
            return withRecipient(recipients, null);
        }

        public Mail.Builder withRecipient(final List<Person> recipients, final MailNotification mailNotification) {
            recipients.stream()
                .filter(person -> mailNotification == null || person.hasNotificationType(mailNotification))
                .forEachOrdered(mailAddressRecipients::add);
            return this;
        }

        public Mail.Builder withTemplate(String templateName, MailTemplateModelSupplier templateModelSupplier) {
            this.templateName = templateName;
            this.templateModelSupplier = templateModelSupplier;
            return this;
        }

        public Mail.Builder withSubject(String subjectMessageKey, Object... subjectMessageArguments) {
            this.subjectMessageKey = subjectMessageKey;
            this.subjectMessageArguments = subjectMessageArguments;
            return this;
        }

        public Mail.Builder withAttachment(String name, ByteArrayResource attachment) {
            if (mailAttachments == null) {
                mailAttachments = new ArrayList<>();
            }

            this.mailAttachments.add(new MailAttachment(name, attachment));
            return this;
        }

        public Mail build() {
            return new Mail(
                mailAddressRecipients,
                templateName,
                templateModelSupplier,
                subjectMessageKey,
                subjectMessageArguments,
                mailAttachments
            );
        }
    }
}
