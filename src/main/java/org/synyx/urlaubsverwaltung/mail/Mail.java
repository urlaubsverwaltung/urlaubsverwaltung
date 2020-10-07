package org.synyx.urlaubsverwaltung.mail;

import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Mail {

    private final List<Person> mailAddressRecipients;
    private final MailNotification mailNotificationRecipients;
    private final boolean sendToTechnicalMail;
    private final boolean sendToEachIndividually;

    private final String templateName;
    private final Map<String, Object> templateModel;

    private final String subjectMessageKey;
    private final Object[] subjectMessageArguments;

    private final List<MailAttachment> mailAttachments;

    public Mail(List<Person> mailAddressRecipients, MailNotification mailNotificationRecipients, boolean sendToTechnicalMail,
                boolean sendToEachIndividually, String templateName, Map<String, Object> templateModel, String subjectMessageKey,
                Object[] subjectMessageArguments, List<MailAttachment> mailAttachments) {
        this.mailAddressRecipients = mailAddressRecipients;
        this.mailNotificationRecipients = mailNotificationRecipients;
        this.sendToTechnicalMail = sendToTechnicalMail;
        this.sendToEachIndividually = sendToEachIndividually;
        this.templateName = templateName;
        this.templateModel = templateModel;
        this.subjectMessageKey = subjectMessageKey;
        this.subjectMessageArguments = subjectMessageArguments;
        this.mailAttachments = mailAttachments;
    }

    public Optional<List<Person>> getMailAddressRecipients() {
        return Optional.ofNullable(mailAddressRecipients);
    }

    public Optional<MailNotification> getMailNotificationRecipients() {
        return Optional.ofNullable(mailNotificationRecipients);
    }

    public boolean isSendToTechnicalMail() {
        return sendToTechnicalMail;
    }

    public boolean isSendToEachIndividually() {
        return sendToEachIndividually;
    }

    public String getTemplateName() {
        return templateName;
    }

    public Map<String, Object> getTemplateModel() {
        return templateModel;
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

        private List<Person> mailAddressRecipients = new ArrayList<>();
        private MailNotification mailNotificationRecipients;
        private boolean sendToTechnicalMail;
        private boolean sendToEachIndividually;

        private String templateName;
        private Map<String, Object> templateModel = new HashMap<>();

        private String subjectMessageKey;
        private Object[] subjectMessageArguments;

        private List<MailAttachment> mailAttachments;

        public Mail.Builder withRecipient(boolean sendToTechnicalMail) {
            this.sendToTechnicalMail = sendToTechnicalMail;
            return this;
        }

        public Mail.Builder withRecipient(MailNotification mailNotification) {
            this.mailNotificationRecipients = mailNotification;
            return this;
        }

        public Mail.Builder withRecipient(Person recipient) {
            withRecipient(List.of(recipient), this.sendToEachIndividually);
            return this;
        }

        public Mail.Builder withRecipient(List<Person> recipients) {
            withRecipient(recipients, this.sendToEachIndividually);
            return this;
        }

        public Mail.Builder withRecipient(List<Person> recipients, boolean sendToEachIndividually) {
            if (mailAddressRecipients == null) {
                mailAddressRecipients = new ArrayList<>();
            }

            this.mailAddressRecipients.addAll(recipients);
            this.sendToEachIndividually = sendToEachIndividually;
            return this;
        }

        public Mail.Builder withTemplate(String templateName, Map<String, Object> templateModel) {
            this.templateName = templateName;
            this.templateModel = templateModel;
            return this;
        }

        public Mail.Builder withSubject(String subjectMessageKey, Object... subjectMessageArguments) {
            this.subjectMessageKey = subjectMessageKey;
            this.subjectMessageArguments = subjectMessageArguments;
            return this;
        }

        public Mail.Builder withAttachment(String name, File file) {
            if (mailAttachments == null) {
                mailAttachments = new ArrayList<>();
            }

            this.mailAttachments.add(new MailAttachment(name, file));
            return this;
        }

        public Mail build() {
            return new Mail(mailAddressRecipients, mailNotificationRecipients, sendToTechnicalMail,
                sendToEachIndividually, templateName, templateModel, subjectMessageKey, subjectMessageArguments,
                mailAttachments);
        }
    }
}
