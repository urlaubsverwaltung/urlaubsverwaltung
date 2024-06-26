package org.synyx.urlaubsverwaltung.account;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.icegreen.greenmail.util.ServerSetupTest.SMTP_IMAP;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.WORKDAY;

@SpringBootTest(properties = {"spring.mail.port=3025", "spring.mail.host=localhost"})
@Transactional
class VacationDaysReminderServiceIT extends SingleTenantTestContainersBase {

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(SMTP_IMAP);

    @MockitoBean
    private PersonService personService;
    @MockitoBean
    private AccountService accountService;
    @MockitoBean
    private VacationDaysService vacationDaysService;
    @MockBean
    private WorkingTimeCalendarService workingTimeCalendarService;

    @Autowired
    private MailService mailService;

    @Test
    void ensureReminderForLeftVacationDays() throws MessagingException, IOException {

        final Clock clock = Clock.fixed(Instant.parse("2022-10-31T06:00:00Z"), ZoneId.of("UTC"));
        final VacationDaysReminderService sut = new VacationDaysReminderService(personService, accountService, vacationDaysService, workingTimeCalendarService, mailService, clock);

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(42L);
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Account account = new Account();
        account.setPerson(person);
        account.setExpiryDateLocally(LocalDate.of(2022, 4, 1));
        account.setDoRemainingVacationDaysExpireLocally(true);
        when(accountService.getHolidaysAccount(2022, List.of(person))).thenReturn(List.of(account));
        when(vacationDaysService.calculateTotalLeftVacationDays(account)).thenReturn(TEN);

        sut.remindForCurrentlyLeftVacationDays();

        // was email sent?
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an offenen Urlaubsanspruch");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        final String content = readPlainContent(msg);
        assertThat(content).isEqualTo("""
            Hallo Lieschen Müller,

            Du hast noch 10 Tage Urlaub für dieses Jahr offen, bitte denke daran, deinen Urlaub zu planen.

            Mehr Informationen zu deinem Urlaubsanspruch findest du hier: https://localhost:8080/web/person/42/overview""");
    }

    @Test
    void ensureReminderForRemainingVacationDays() throws MessagingException, IOException {

        final Clock clock = Clock.fixed(Instant.parse("2022-01-01T06:00:00Z"), ZoneId.of("UTC"));
        final VacationDaysReminderService sut = new VacationDaysReminderService(personService, accountService, vacationDaysService, workingTimeCalendarService, mailService, clock);

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(42L);
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Account account = new Account();
        account.setPerson(person);
        account.setExpiryDateLocally(LocalDate.of(2022, 4, 1));
        account.setDoRemainingVacationDaysExpireLocally(true);
        when(accountService.getHolidaysAccount(2022, List.of(person))).thenReturn(List.of(account));

        final LocalDate startDate = LocalDate.of(2022, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2022, DECEMBER, 31);

        final Map<LocalDate, WorkingTimeCalendar.WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(startDate, endDate, date -> fullWorkDay());
        final WorkingTimeCalendar personWorkingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        final Map<Person, WorkingTimeCalendar> workingTimeCalendarByPerson = Map.of(person, personWorkingTimeCalendar);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), Year.of(2022))).thenReturn(workingTimeCalendarByPerson);

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(ZERO)
            .withRemainingVacation(TEN)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account), workingTimeCalendarByPerson, Year.of(2022)))
            .thenReturn(Map.of(account, new HolidayAccountVacationDays(account, vacationDaysLeft, vacationDaysLeft)));

        sut.remindForRemainingVacationDays();

        // was email sent?
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an offenen Resturlaub");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        final String content = readPlainContent(msg);
        assertThat(content).isEqualTo("""
            Hallo Lieschen Müller,

            Du hast noch 10 Tage Resturlaub aus dem Vorjahr, bitte denke daran den Urlaub bis zum 31.03.2022 zu nehmen.

            Mehr Informationen zu deinem Urlaubsanspruch findest du hier: https://localhost:8080/web/person/42/overview""");
    }

    @Test
    void ensureReminderForExpiredRemainingVacationDays() throws MessagingException, IOException {

        final Clock clock = Clock.fixed(Instant.parse("2022-04-01T06:00:00Z"), ZoneId.of("UTC"));
        final VacationDaysReminderService sut = new VacationDaysReminderService(personService, accountService, vacationDaysService, workingTimeCalendarService, mailService, clock);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Account account = new Account();
        account.setPerson(person);
        account.setExpiryDateLocally(LocalDate.of(2022, 4, 1));
        account.setDoRemainingVacationDaysExpireLocally(true);
        when(accountService.getHolidaysAccount(2022, List.of(person))).thenReturn(List.of(account));

        final LocalDate startDate = LocalDate.of(2022, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2022, DECEMBER, 31);

        final Map<LocalDate, WorkingTimeCalendar.WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(startDate, endDate, date -> fullWorkDay());
        final WorkingTimeCalendar personWorkingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);
        final Map<Person, WorkingTimeCalendar> workingTimeCalendarByPerson = Map.of(person, personWorkingTimeCalendar);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), Year.of(2022))).thenReturn(workingTimeCalendarByPerson);

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(ZERO)
            .withRemainingVacation(TEN)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account), workingTimeCalendarByPerson, Year.of(2022)))
            .thenReturn(Map.of(account, new HolidayAccountVacationDays(account, vacationDaysLeft, vacationDaysLeft)));
        when(vacationDaysService.calculateTotalLeftVacationDays(account)).thenReturn(TEN);

        sut.notifyForExpiredRemainingVacationDays();

        // was email sent?
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Verfall des Resturlaubs");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Marlene Muster,

            leider ist dein Resturlaub zum 01.04.2022 in Höhe von 10 Tagen verfallen.

            Dein aktueller Urlaubsanspruch:
                10 Tage

            Mehr Informationen zu deinem Urlaubsanspruch findest du hier: https://localhost:8080/web/person/1/overview""");
    }

    private String readPlainContent(Message message) throws MessagingException, IOException {
        return message.getContent().toString().replaceAll("\\r", "");
    }

    private static WorkingTimeCalendar.WorkingDayInformation fullWorkDay() {
        return new WorkingTimeCalendar.WorkingDayInformation(FULL, WORKDAY, WORKDAY);
    }

    private Map<LocalDate, WorkingTimeCalendar.WorkingDayInformation> buildWorkingTimeByDate(LocalDate from, LocalDate to, Function<LocalDate, WorkingTimeCalendar.WorkingDayInformation> dayLengthProvider) {
        Map<LocalDate, WorkingTimeCalendar.WorkingDayInformation> map = new HashMap<>();
        for (LocalDate date : new DateRange(from, to)) {
            map.put(date, dayLengthProvider.apply(date));
        }
        return map;
    }
}
