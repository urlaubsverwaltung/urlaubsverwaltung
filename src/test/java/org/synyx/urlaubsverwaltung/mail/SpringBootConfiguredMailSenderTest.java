package org.synyx.urlaubsverwaltung.mail;


import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;


public class SpringBootConfiguredMailSenderTest {

    private JavaMailSender javaMailSender = mock(JavaMailSender.class);
    private SpringBootConfiguredMailSender sut = new SpringBootConfiguredMailSender(javaMailSender);

    @Test
    public void sendEmail() {

        String[] recipients = {"hans@dampf.com"};
        String subject = "subject";
        String body = "text";

        ArgumentCaptor<SimpleMailMessage> mailMessageArgumentCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        sut.sendEmail("lala@lala.com", Arrays.asList(recipients), subject, body);

        verify(javaMailSender).send(mailMessageArgumentCaptor.capture());

        SimpleMailMessage mailMessage = mailMessageArgumentCaptor.getValue();

        assertThat(mailMessage.getFrom()).contains("lala@lala.com");
        assertThat(mailMessage.getTo()).isEqualTo(recipients);
        assertThat(mailMessage.getSubject()).isEqualTo(subject);
        assertThat(mailMessage.getText()).isEqualTo(body);
    }

    @Test
    public void doesntSendMailForZeroRecipients() {

        sut.sendEmail("lala@lala.com", Collections.emptyList(), "subject", "text");

        verifyZeroInteractions(javaMailSender);
    }

}
