package org.synyx.urlaubsverwaltung.overtime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.mail.MailService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.synyx.urlaubsverwaltung.person.MailNotification.OVERTIME_NOTIFICATION_OFFICE;

@RunWith(MockitoJUnitRunner.class)
public class OvertimeMailServiceTest {

    private OvertimeMailService sut;

    @Mock
    private MailService mailService;

    @Before
    public void setUp() {
        sut = new OvertimeMailService(mailService);
    }

    @Test
    public void sendOvertimeNotification() {
        final Overtime overtime = new Overtime();
        final OvertimeComment overtimeComment = new OvertimeComment();

        final Map<String, Object> model = new HashMap<>();
        model.put("overtime", overtime);
        model.put("comment", overtimeComment);

        sut.sendOvertimeNotification(overtime, overtimeComment);

        verify(mailService).sendMailTo(OVERTIME_NOTIFICATION_OFFICE, "subject.overtime.created", "overtime_office", model);
    }
}
