package org.synyx.urlaubsverwaltung.mail;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import java.util.List;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;

class MailTest {

    @Test
    void ensureAttachmentWithNameAndResourceIsTheSameForEveryLocale() {

        final ByteArrayResource iCal = new ByteArrayResource(new byte[]{1}, "calendar.ics");

        final Mail mail = Mail.builder()
            .withAttachment("calendar.ics", iCal)
            .build();

        assertThat(mail.getMailAttachments(GERMAN)).hasValue(List.of(new MailAttachment("calendar.ics", iCal)));
        assertThat(mail.getMailAttachments(ENGLISH)).hasValue(List.of(new MailAttachment("calendar.ics", iCal)));
    }

    @Test
    void ensureAttachmentSupplierIsResolvedWithTheGivenLocale() {

        final ByteArrayResource csv = new ByteArrayResource(new byte[]{});

        final Mail mail = Mail.builder()
            .withAttachment(locale -> new MailAttachment("file_" + locale.getLanguage() + ".csv", csv))
            .build();

        assertThat(mail.getMailAttachments(GERMAN)).hasValue(List.of(new MailAttachment("file_de.csv", csv)));
        assertThat(mail.getMailAttachments(ENGLISH)).hasValue(List.of(new MailAttachment("file_en.csv", csv)));
    }

    @Test
    void ensureNoAttachments() {
        assertThat(Mail.builder().build().getMailAttachments(GERMAN)).isEmpty();
    }
}
