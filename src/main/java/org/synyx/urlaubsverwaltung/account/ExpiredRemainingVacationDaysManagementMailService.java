package org.synyx.urlaubsverwaltung.account;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.csv.CSVFile;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailAttachment;
import org.synyx.urlaubsverwaltung.mail.MailRecipientService;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_EXPIRED_MANAGEMENT_ALL;

/**
 * Informs office about the remaining vacation days of persons that expired, with the list as CSV attachment.
 */
@Service
class ExpiredRemainingVacationDaysManagementMailService {

    private final MailRecipientService mailRecipientService;
    private final PersonBasedataService personBasedataService;
    private final DepartmentService departmentService;
    private final ExpiredRemainingVacationDaysCsvExportService csvExportService;
    private final MailService mailService;
    private final Clock clock;

    ExpiredRemainingVacationDaysManagementMailService(
        MailRecipientService mailRecipientService, PersonBasedataService personBasedataService, DepartmentService departmentService,
        ExpiredRemainingVacationDaysCsvExportService csvExportService, MailService mailService, Clock clock
    ) {
        this.mailRecipientService = mailRecipientService;
        this.personBasedataService = personBasedataService;
        this.departmentService = departmentService;
        this.csvExportService = csvExportService;
        this.mailService = mailService;
        this.clock = clock;
    }

    void sendExpiredRemainingVacationDaysNotification(List<ExpiredRemainingVacationDays> expiredRemainingVacationDays) {

        final List<Person> recipients = mailRecipientService.getRecipientsWith(NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_EXPIRED_MANAGEMENT_ALL);
        if (recipients.isEmpty()) {
            return;
        }

        final List<ExpiredRemainingVacationDays> sortedByName = expiredRemainingVacationDays.stream()
            .sorted(comparing(expired -> expired.account().getPerson().getNiceName()))
            .toList();
        final List<ExpiredRemainingVacationDaysCsvRow> csvRows = toCsvRows(sortedByName);

        final LocalDate today = LocalDate.now(clock);
        final FilterPeriod runDay = new FilterPeriod(today, today);
        final Map<String, Object> model = Map.of("expiredRemainingVacationDays", sortedByName);

        final Mail mailToOffice = Mail.builder()
            .withRecipient(recipients)
            .withSubject("subject.account.expiredRemainingVacationDays.management")
            .withTemplate("account_cron_expired_remaining_vacation_days_management", _ -> model)
            .withAttachment(locale -> {
                final CSVFile csvFile = csvExportService.generateCSV(runDay, locale, csvRows);
                return new MailAttachment(csvFile.fileName(), csvFile.resource());
            })
            .build();
        mailService.send(mailToOffice);
    }

    private List<ExpiredRemainingVacationDaysCsvRow> toCsvRows(List<ExpiredRemainingVacationDays> expiredRemainingVacationDays) {

        final List<Person> persons = expiredRemainingVacationDays.stream().map(expired -> expired.account().getPerson()).toList();
        final Map<PersonId, PersonBasedata> basedataByPersonId = personBasedataService.getBasedataByPersonId(persons.stream().map(Person::getId).toList());
        final Map<PersonId, List<String>> departmentNamesByPersonId = departmentService.getDepartmentNamesByMembers(persons);

        return expiredRemainingVacationDays.stream()
            .map(expired -> {
                final PersonId personId = expired.account().getPerson().getIdAsPersonId();
                return ExpiredRemainingVacationDaysCsvRow.of(expired, basedataByPersonId.get(personId), departmentNamesByPersonId.get(personId));
            })
            .toList();
    }
}
