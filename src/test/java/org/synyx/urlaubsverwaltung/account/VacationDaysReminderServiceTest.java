package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VacationDaysReminderServiceTest {

    @Mock
    private PersonService personService;
    @Mock
    private AccountService accountService;
    @Mock
    private VacationDaysService vacationDaysService;
    @Mock
    private MailService mailService;
    @Mock
    private Clock clock;
    @InjectMocks
    private VacationDaysReminderService sut;

    final ArgumentCaptor<Mail> mailArgumentCaptor = ArgumentCaptor.forClass(Mail.class);

    @BeforeEach
    void setUp() {
        when(clock.getZone()).thenReturn(ZoneId.of("Europe/Berlin"));
    }

    @Test
    void ensureNoReminderForZeroLeftVacationDays() {

        when(clock.instant()).thenReturn(Instant.parse("2022-10-31T06:00:00Z"));

        final Person person = person();
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Account account = new Account();
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account));
        when(vacationDaysService.calculateTotalLeftVacationDays(account)).thenReturn(ZERO);

        sut.remindForCurrentlyLeftVacationDays();

        verifyNoInteractions(mailService);
    }

    @Test
    void ensureReminderForLeftVacationDays() {

        when(clock.instant()).thenReturn(Instant.parse("2022-10-31T06:00:00Z"));

        final Person person = person();
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Account account = new Account();
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account));
        when(vacationDaysService.calculateTotalLeftVacationDays(account)).thenReturn(TEN);

        sut.remindForCurrentlyLeftVacationDays();

        verify(mailService).send(mailArgumentCaptor.capture());

        final Mail capturedMail = mailArgumentCaptor.getValue();
        assertThat(capturedMail.getMailAddressRecipients()).contains(List.of(person));
        assertThat(capturedMail.getSubjectMessageKey()).isEqualTo("subject.account.remindForCurrentlyLeftVacationDays");
        assertThat(capturedMail.getTemplateName()).isEqualTo("remind_currently_left_vacation_days");
        assertThat(capturedMail.getTemplateModel()).contains(
            entry("recipientNiceName", "Marlene Muster"),
            entry("vacationDaysLeft", TEN),
            entry("nextYear", 2023)
        );
    }

    @Test
    void ensureNoReminderWithoutRemainingVacationDays() {

        when(clock.instant()).thenReturn(Instant.parse("2022-01-01T06:00:00Z"));

        final Person person = person();
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Account account2022 = new Account();
        final Optional<Account> account2023 = Optional.of(new Account());
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account2022));
        when(accountService.getHolidaysAccount(2023, person)).thenReturn(account2023);

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(ZERO)
            .withRemainingVacation(ZERO)
            .forUsedDaysBeforeApril(ZERO)
            .forUsedDaysAfterApril(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(account2022, account2023)).thenReturn(vacationDaysLeft);

        sut.remindForRemainingVacationDays();

        verifyNoInteractions(mailService);
    }

    @Test
    void ensureReminderForRemainingVacationDays() {

        when(clock.instant()).thenReturn(Instant.parse("2022-01-01T06:00:00Z"));

        final Person person = person();
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Account account2022 = new Account();
        final Optional<Account> account2023 = Optional.of(new Account());
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account2022));
        when(accountService.getHolidaysAccount(2023, person)).thenReturn(account2023);

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(ZERO)
            .withRemainingVacation(TEN)
            .notExpiring(ZERO)
            .forUsedDaysBeforeApril(ZERO)
            .forUsedDaysAfterApril(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(account2022, account2023)).thenReturn(vacationDaysLeft);

        sut.remindForRemainingVacationDays();

        verify(mailService).send(mailArgumentCaptor.capture());

        final Mail capturedMail = mailArgumentCaptor.getValue();
        assertThat(capturedMail.getMailAddressRecipients()).contains(List.of(person));
        assertThat(capturedMail.getSubjectMessageKey()).isEqualTo("subject.account.remindForRemainingVacationDays");
        assertThat(capturedMail.getTemplateName()).isEqualTo("remind_remaining_vacation_days");
        assertThat(capturedMail.getTemplateModel()).contains(
            entry("recipientNiceName", "Marlene Muster"),
            entry("remainingVacationDays", TEN),
            entry("year", 2022)
        );
    }

    @Test
    void ensureNoNotificationWithoutExpiredRemainingVacationDays() {

        when(clock.instant()).thenReturn(Instant.parse("2022-01-01T06:00:00Z"));

        final Person person = person();
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Account account2022 = new Account();
        final Optional<Account> account2023 = Optional.of(new Account());
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account2022));
        when(accountService.getHolidaysAccount(2023, person)).thenReturn(account2023);

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(ZERO)
            .withRemainingVacation(TEN)
            .notExpiring(TEN)
            .forUsedDaysBeforeApril(ZERO)
            .forUsedDaysAfterApril(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(account2022, account2023)).thenReturn(vacationDaysLeft);

        sut.notifyForExpiredRemainingVacationDays();

        verifyNoInteractions(mailService);
    }

    @Test
    void ensureNotificationForExpiredRemainingVacationDays() {

        when(clock.instant()).thenReturn(Instant.parse("2022-04-01T06:00:00Z"));

        final Person person = person();
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Account account2022 = new Account();
        final Optional<Account> account2023 = Optional.of(new Account());
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account2022));
        when(accountService.getHolidaysAccount(2023, person)).thenReturn(account2023);

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(ZERO)
            .withRemainingVacation(TEN)
            .notExpiring(ZERO)
            .forUsedDaysBeforeApril(ZERO)
            .forUsedDaysAfterApril(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(account2022, account2023)).thenReturn(vacationDaysLeft);

        sut.notifyForExpiredRemainingVacationDays();

        verify(mailService).send(mailArgumentCaptor.capture());

        final Mail capturedMail = mailArgumentCaptor.getValue();
        assertThat(capturedMail.getMailAddressRecipients()).contains(List.of(person));
        assertThat(capturedMail.getSubjectMessageKey()).isEqualTo("subject.account.notifyForExpiredRemainingVacationDays");
        assertThat(capturedMail.getTemplateName()).isEqualTo("notify_expired_remaining_vacation_days");
        assertThat(capturedMail.getTemplateModel()).contains(
            entry("recipientNiceName", "Marlene Muster"),
            entry("expiredRemainingVacationDays", TEN),
            entry("year", 2022)
        );
    }

    private Person person() {
        final Person person = new Person();
        person.setFirstName("Marlene");
        person.setLastName("Muster");
        return person;
    }
}
