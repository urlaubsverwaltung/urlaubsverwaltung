package org.synyx.urlaubsverwaltung.mail;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;


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

        sut.sendEmail(from, recipient, subject, body);

        verify(javaMailSender).send(mailMessageArgumentCaptor.capture());
        SimpleMailMessage mailMessage = mailMessageArgumentCaptor.getValue();
        assertThat(mailMessage.getFrom()).contains(from);
        assertThat(mailMessage.getTo()).containsExactly(recipient);
        assertThat(mailMessage.getSubject()).isEqualTo(subject);
        assertThat(mailMessage.getText()).isEqualTo(body);
    }

    @Test
    void doesNotSendMailForNullRecipients() {

        sut.sendEmail("from@example.org", null, "subject", "text");

        verifyNoInteractions(javaMailSender);
    }
}
