package org.synyx.urlaubsverwaltung.mail;

import net.fortuna.ical4j.model.Calendar;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Mail {

    private final List<Person> mailAddressRecipients;
    private final MailNotification mailNotificationRecipients;
    private final boolean sendToTechnicalMail;

    private final String templateName;
    private final Map<String, Object> templateModel;

    private final String subjectMessageKey;
    private final Object[] subjectMessageArguments;

    private final List<MailCalendarAttachment> mailCalendarAttachments;

    Mail(List<Person> mailAddressRecipients, MailNotification mailNotificationRecipients, boolean sendToTechnicalMail,
         String templateName, Map<String, Object> templateModel, String subjectMessageKey,
         Object[] subjectMessageArguments, List<MailCalendarAttachment> mailCalendarAttachments) {
        this.mailAddressRecipients = mailAddressRecipients;
        this.mailNotificationRecipients = mailNotificationRecipients;
        this.sendToTechnicalMail = sendToTechnicalMail;
        this.templateName = templateName;
        this.templateModel = templateModel;
        this.subjectMessageKey = subjectMessageKey;
        this.subjectMessageArguments = subjectMessageArguments;
        this.mailCalendarAttachments = mailCalendarAttachments;
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

    public Optional<List<MailCalendarAttachment>> getMailAttachments() {
        return Optional.ofNullable(mailCalendarAttachments);
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

        private String templateName;
        private Map<String, Object> templateModel = new HashMap<>();

        private String subjectMessageKey;
        private Object[] subjectMessageArguments;

        private List<MailCalendarAttachment> mailCalendarAttachments;

        public Mail.Builder withTechnicalRecipient(boolean sendToTechnicalMail) {
            this.sendToTechnicalMail = sendToTechnicalMail;
            return this;
        }

        public Mail.Builder withRecipient(MailNotification mailNotification) {
            this.mailNotificationRecipients = mailNotification;
            return this;
        }

        public Mail.Builder withRecipient(Person recipient) {
            withRecipient(List.of(recipient));
            return this;
        }

        public Mail.Builder withRecipient(List<Person> recipients) {
            if (mailAddressRecipients == null) {
                mailAddressRecipients = new ArrayList<>();
            }

            this.mailAddressRecipients.addAll(recipients);
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

        public Mail.Builder withCalendarAttachment(String name, Calendar calendar) {
            if (mailCalendarAttachments == null) {
                mailCalendarAttachments = new ArrayList<>();
            }

            this.mailCalendarAttachments.add(new MailCalendarAttachment(name, calendar));
            return this;
        }

        public Mail build() {
            return new Mail(mailAddressRecipients, mailNotificationRecipients, sendToTechnicalMail,
                templateName, templateModel, subjectMessageKey, subjectMessageArguments,
                mailCalendarAttachments);
        }
    }
}
