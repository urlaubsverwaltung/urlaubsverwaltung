package org.synyx.urlaubsverwaltung.mail;

import java.util.Locale;

/**
 * Supplies an attachment for the locale of a recipient, e.g. to translate its content or name.
 */
@FunctionalInterface
public interface MailAttachmentSupplier {
    MailAttachment getMailAttachment(Locale locale);
}
