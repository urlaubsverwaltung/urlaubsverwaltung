package org.synyx.urlaubsverwaltung.overtime;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.mock_javamail.Mailbox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeAction.CREATED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.OVERTIME_NOTIFICATION_OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createOvertimeRecord;
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createPerson;

@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
public class OvertimeMailServiceIT {

    @Autowired
    private OvertimeMailService sut;

    @Autowired
    private PersonService personService;

    @After
    public void tearDown() {
        Mailbox.clearAll();
    }

    @Test
    public void ensureOfficeWithOvertimeNotificationGetMailIfOvertimeRecorded() throws MessagingException, IOException {

        final Person person = createPerson("user", "Lieschen", "Müller", "lieschen12@firma.test");
        final Overtime overtimeRecord = createOvertimeRecord(person);
        final OvertimeComment overtimeComment = new OvertimeComment(person, overtimeRecord, CREATED);

        final Person office = createPerson("office", "Marlene", "Muster", "office@firma.test");
        office.setPermissions(singletonList(OFFICE));
        office.setNotifications(singletonList(OVERTIME_NOTIFICATION_OFFICE));
        personService.save(office);

        sut.sendOvertimeNotification(overtimeRecord, overtimeComment);

        // was email sent to office?
        List<Message> inboxOffice = Mailbox.get(office.getEmail());
        assertThat(inboxOffice.size()).isOne();

        // check attributes
        Message msg = inboxOffice.get(0);
        assertThat(msg.getSubject()).contains("Es wurden Überstunden eingetragen");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String text = (String) msg.getContent();
        assertThat(text).contains("Hallo Office");
        assertThat(text).contains("es wurden Überstunden erfasst");
        assertThat(text).contains("/web/overtime/1234");
    }
}
