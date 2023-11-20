package org.synyx.urlaubsverwaltung.mail;


import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.simplejavamail.converter.EmailConverter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Properties;

import static jakarta.mail.Session.getInstance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailSenderServiceTest {

    private MailSenderService sut;

    @Mock
    private JavaMailSender javaMailSender;

    @BeforeEach
    void setUp() {
        sut = new MailSenderService(javaMailSender);
    }

    @Test
    void sendEmail() {
        final ArgumentCaptor<SimpleMailMessage> mailMessageArgumentCaptor = forClass(SimpleMailMessage.class);

        final String recipient = "hans@dampf.com";
        final String subject = "subject";
        final String body = "text";
        final String from = "from@example.org";
        final String replyTo = "replyTo@example.org";

        sut.sendEmail(from, replyTo, recipient, subject, body);

        verify(javaMailSender).send(mailMessageArgumentCaptor.capture());
        final SimpleMailMessage mailMessage = mailMessageArgumentCaptor.getValue();
        assertThat(mailMessage.getFrom()).contains(from);
        assertThat(mailMessage.getTo()).containsExactly(recipient);
        assertThat(mailMessage.getSubject()).isEqualTo(subject);
        assertThat(mailMessage.getText()).isEqualTo(body);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void doesNotSendMailForNullRecipients(final String recipient) {
        sut.sendEmail("from@example.org", "replyTo@example.org", recipient, "subject", "text");
        verifyNoInteractions(javaMailSender);
    }

    @Test
    void ensuresSendMailWithAttachment() throws Exception {
        final ArgumentCaptor<MimeMessage> mailMessageArgumentCaptor = forClass(MimeMessage.class);

        final String recipient = "hans@dampf.com";
        final String subject = "subject";
        final String body = "text";
        final String from = "from@example.org";
        final String replyTo = "replyTo@example.org";
        final List<MailAttachment> mailAttachments = List.of(new MailAttachment("name", new ByteArrayResource(new byte[]{})));

        final MimeMessage msg = new MimeMessage(getInstance(new Properties(), null));
        when(javaMailSender.createMimeMessage()).thenReturn(msg);

        sut.sendEmail(from, replyTo, recipient, subject, body, mailAttachments);

        verify(javaMailSender).send(mailMessageArgumentCaptor.capture());
        final MimeMessage mailMessage = mailMessageArgumentCaptor.getValue();
        assertThat(mailMessage.getFrom()).contains(new InternetAddress(from));
        assertThat(mailMessage.getAllRecipients()).containsExactly(new InternetAddress(recipient));
        assertThat(mailMessage.getSubject()).isEqualTo(subject);
        assertThat(readPlainContent(mailMessage)).hasToString(body);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void doesNotSendMailWithAttachmentToNullRecipients(final String recipient) {
        sut.sendEmail("from@example.org", "replyTo@example.org", recipient, "subject", "text", List.of());
        verifyNoInteractions(javaMailSender);
    }

    private String readPlainContent(MimeMessage message) {
        return EmailConverter.mimeMessageToEmail(message).getPlainText();
    }
}
