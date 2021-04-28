package org.synyx.urlaubsverwaltung.mail;

import java.io.File;
import java.util.*;

public class Mail {

    private final List<Recipient> recipients;
    private final boolean sendToTechnicalMail;

    private final String templateName;
    private final Map<String, Object> templateModel;

    private final String subjectMessageKey;
    private final Object[] subjectMessageArguments;

    private final List<MailAttachment> mailAttachments;

    Mail(List<Recipient> recipients, boolean sendToTechnicalMail,
         String templateName, Map<String, Object> templateModel, String subjectMessageKey,
         Object[] subjectMessageArguments, List<MailAttachment> mailAttachments) {
        this.recipients = recipients;
        this.sendToTechnicalMail = sendToTechnicalMail;
        this.templateName = templateName;
        this.templateModel = templateModel;
        this.subjectMessageKey = subjectMessageKey;
        this.subjectMessageArguments = subjectMessageArguments;
        this.mailAttachments = mailAttachments;
    }

    public Optional<List<Recipient>> getRecipients() {
        return Optional.ofNullable(recipients);
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

        private List<Recipient> recipients = new ArrayList<>();
        private boolean sendToTechnicalMail;

        private String templateName;
        private Map<String, Object> templateModel = new HashMap<>();

        private String subjectMessageKey;
        private Object[] subjectMessageArguments;

        private List<MailAttachment> mailAttachments;

        public Mail.Builder withTechnicalRecipient(boolean sendToTechnicalMail) {
            this.sendToTechnicalMail = sendToTechnicalMail;
            return this;
        }

        public Mail.Builder withRecipient(Recipient recipient) {
            withRecipient(List.of(recipient));
            return this;
        }

        public Mail.Builder withRecipient(List<Recipient> recipients) {
            if (this.recipients == null) {
                this.recipients = new ArrayList<>();
            }

            this.recipients.addAll(recipients);
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
            return new Mail(recipients, sendToTechnicalMail,
                templateName, templateModel, subjectMessageKey, subjectMessageArguments,
                mailAttachments);
        }
    }
}
