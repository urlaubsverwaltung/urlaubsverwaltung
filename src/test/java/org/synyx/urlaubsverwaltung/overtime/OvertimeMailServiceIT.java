package org.synyx.urlaubsverwaltung.overtime;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createOvertimeRecord;
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeAction.CREATED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.OVERTIME_NOTIFICATION_OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"spring.mail.port=3025", "spring.mail.host=localhost"})
@Transactional
public class OvertimeMailServiceIT extends TestContainersBase {

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);

    @Autowired
    private OvertimeMailService sut;

    @Autowired
    private PersonService personService;

    @Test
    public void ensureOfficeWithOvertimeNotificationGetMailIfOvertimeRecorded() throws MessagingException, IOException {

        final Person person = createPerson("user", "Lieschen", "Müller", "lieschen12@example.org");
        final Overtime overtimeRecord = createOvertimeRecord(person);
        final OvertimeComment overtimeComment = new OvertimeComment(person, overtimeRecord, CREATED);

        final Person office = createPerson("office", "Marlene", "Muster", "office@example.org");
        office.setPermissions(singletonList(OFFICE));
        office.setNotifications(singletonList(OVERTIME_NOTIFICATION_OFFICE));
        personService.save(office);

        sut.sendOvertimeNotification(overtimeRecord, overtimeComment);

        // was email sent to office?
        assertThat(greenMail.getReceivedMessagesForDomain(office.getEmail()).length).isOne();

        // check attributes
        final Message msg = greenMail.getReceivedMessagesForDomain(office.getEmail())[0];
        assertThat(msg.getSubject()).contains("Es wurden Überstunden eingetragen");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        final String text = (String) msg.getContent();
        assertThat(text).contains("Hallo Office");
        assertThat(text).contains("es wurden Überstunden erfasst");
        assertThat(text).contains("/web/overtime/1234");
    }
}
