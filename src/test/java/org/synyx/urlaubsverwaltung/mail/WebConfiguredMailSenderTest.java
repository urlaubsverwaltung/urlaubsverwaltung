package org.synyx.urlaubsverwaltung.mail;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class WebConfiguredMailSenderTest {

    @Mock
    private JavaMailSenderImpl javaMailSender;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SettingsService settingsService;

    @InjectMocks
    private WebConfiguredMailSender mailSender;

    @Test
    public void ensureNoMailSentIfSendingMailsIsDeactivated() {

        when(settingsService.getSettings().getMailSettings().isActive()).thenReturn(false);

        mailSender.sendEmail("sender@bar.de", Collections.singletonList("foo@bar.de"), "subject", "text");

        verifyZeroInteractions(javaMailSender);
    }


    @Test
    public void ensureMailSenderAttributesAreUpdatedWhenSendingMails() {

        when(settingsService.getSettings().getMailSettings().isActive()).thenReturn(true);
        when(settingsService.getSettings().getMailSettings().getHost()).thenReturn("localhost");
        when(settingsService.getSettings().getMailSettings().getPort()).thenReturn(25);
        when(settingsService.getSettings().getMailSettings().getUsername()).thenReturn("username");
        when(settingsService.getSettings().getMailSettings().getPassword()).thenReturn("password");

        mailSender.sendEmail("sender@bar.de", Collections.singletonList("foo@bar.de"), "subject", "text");


        verify(javaMailSender).setHost("localhost");
        verify(javaMailSender).setPort(25);
        verify(javaMailSender).setUsername("username");
        verify(javaMailSender).setPassword("password");
    }


    @Test
    public void ensureMailIsSentCorrectly() {

        ArgumentCaptor<SimpleMailMessage> mailMessageArgumentCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        String subject = "subject";
        String body = "text";

        when(settingsService.getSettings().getMailSettings().isActive()).thenReturn(true);
        mailSender.sendEmail("sender@bar.de", Arrays.asList("max@firma.test", "marlene@firma.test"), subject, body);

        verify(javaMailSender).send(mailMessageArgumentCaptor.capture());

        SimpleMailMessage mailMessage = mailMessageArgumentCaptor.getValue();

        Assert.assertNotNull("There must be recipients", mailMessage.getTo());
        Assert.assertEquals("Wrong number of recipients", 2, mailMessage.getTo().length);
        Assert.assertEquals("Wrong subject", subject, mailMessage.getSubject());
        Assert.assertEquals("Wrong body", body, mailMessage.getText());
    }


    @Test
    public void ensureNoMailIsSentIfRecipientsListIsNull() {

        mailSender.sendEmail("sender@bar.de", null, "subject", "text");

        verifyZeroInteractions(javaMailSender);
    }


    @Test
    public void ensureNoMailIsSentIfRecipientsListIsEmpty() {

        mailSender.sendEmail("sender@bar.de", Collections.emptyList(), "subject", "text");

        verifyZeroInteractions(javaMailSender);
    }
}
