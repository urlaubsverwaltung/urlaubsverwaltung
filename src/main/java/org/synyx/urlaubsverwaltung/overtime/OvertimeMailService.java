package org.synyx.urlaubsverwaltung.overtime;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.mail.MailService;

import java.util.HashMap;
import java.util.Map;

import static org.synyx.urlaubsverwaltung.person.MailNotification.OVERTIME_NOTIFICATION_OFFICE;

@Service
class OvertimeMailService {

    private final MailService mailService;

    OvertimeMailService(MailService mailService) {
        this.mailService = mailService;
    }

    void sendOvertimeNotification(Overtime overtime, OvertimeComment overtimeComment) {

        final Map<String, Object> model = new HashMap<>();
        model.put("overtime", overtime);
        model.put("comment", overtimeComment);

        final String subjectMessageKey = "subject.overtime.created";
        final String templateName = "overtime_office";

        mailService.sendMailTo(OVERTIME_NOTIFICATION_OFFICE, subjectMessageKey, templateName, model);
    }
}
