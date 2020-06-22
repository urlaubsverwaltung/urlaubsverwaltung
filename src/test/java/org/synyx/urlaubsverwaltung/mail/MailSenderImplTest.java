package org.synyx.urlaubsverwaltung.mail;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;


@RunWith(MockitoJUnitRunner.class)
public class MailSenderImplTest {

    private MailSenderImpl sut;

    @Mock
    private JavaMailSender javaMailSender;

    @Before
    public void setUp() {
        sut = new MailSenderImpl(javaMailSender);
    }

    @Test
    public void sendEmail() {
        final ArgumentCaptor<SimpleMailMessage> mailMessageArgumentCaptor = forClass(SimpleMailMessage.class);

        final String[] recipients = {"hans@dampf.com"};
        final String subject = "subject";
        final String body = "text";
        final String from = "from@example.org";

        sut.sendEmail(from, List.of(recipients), subject, body);

        verify(javaMailSender).send(mailMessageArgumentCaptor.capture());
        SimpleMailMessage mailMessage = mailMessageArgumentCaptor.getValue();
        assertThat(mailMessage.getFrom()).contains(from);
        assertThat(mailMessage.getTo()).isEqualTo(recipients);
        assertThat(mailMessage.getSubject()).isEqualTo(subject);
        assertThat(mailMessage.getText()).isEqualTo(body);
    }

    @Test
    public void doesNotSendMailForZeroRecipients() {

        sut.sendEmail("from@example.org", List.of(), "subject", "text");

        verifyZeroInteractions(javaMailSender);
    }
}
