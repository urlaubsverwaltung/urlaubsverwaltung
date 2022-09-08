package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
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

    final ArgumentCaptor<Mail> mailArgumentCaptor = ArgumentCaptor.forClass(Mail.class);

    @Test
    void ensureNoReminderForZeroLeftVacationDays() {

        final Clock clock = Clock.fixed(Instant.parse("2022-10-31T06:00:00Z"), ZoneId.of("UTC"));
        final VacationDaysReminderService sut = new VacationDaysReminderService(personService, accountService, vacationDaysService, mailService, clock);

        final Person person = person();
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Account account = new Account();
        account.setExpiryDate(LocalDate.of(2022, 4, 1));
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account));
        when(vacationDaysService.calculateTotalLeftVacationDays(account)).thenReturn(ZERO);

        sut.remindForCurrentlyLeftVacationDays();

        verifyNoInteractions(mailService);
    }

    @Test
    void ensureReminderForLeftVacationDays() {

        final Clock clock = Clock.fixed(Instant.parse("2022-10-31T06:00:00Z"), ZoneId.of("UTC"));
        final VacationDaysReminderService sut = new VacationDaysReminderService(personService, accountService, vacationDaysService, mailService, clock);

        final Person person = person();
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Account account = new Account();
        account.setExpiryDate(LocalDate.of(2022, 4, 1));
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
            entry("personId", 42),
            entry("vacationDaysLeft", TEN),
            entry("nextYear", 2023)
        );
    }

    @Test
    void ensureNoReminderWithoutRemainingVacationDays() {

        final Clock clock = Clock.fixed(Instant.parse("2022-01-01T06:00:00Z"), ZoneId.of("UTC"));
        final VacationDaysReminderService sut = new VacationDaysReminderService(personService, accountService, vacationDaysService, mailService, clock);

        final Person person = person();
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Account account2022 = new Account();
        account2022.setDoRemainingVacationDaysExpireLocally(true);
        account2022.setExpiryDate(LocalDate.of(2022, 4, 1));
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account2022));

        final Account account2023 = new Account();
        account2023.setDoRemainingVacationDaysExpireLocally(true);
        account2023.setExpiryDate(LocalDate.of(2023, 4, 1));
        when(accountService.getHolidaysAccount(2023, person)).thenReturn(Optional.of(account2023));

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(ZERO)
            .withRemainingVacation(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(account2022, Optional.of(account2023))).thenReturn(vacationDaysLeft);

        sut.remindForRemainingVacationDays();

        verifyNoInteractions(mailService);
    }

    @Test
    void ensureReminderForRemainingVacationDays() {

        final Clock clock = Clock.fixed(Instant.parse("2022-01-01T06:00:00Z"), ZoneId.of("UTC"));
        final VacationDaysReminderService sut = new VacationDaysReminderService(personService, accountService, vacationDaysService, mailService, clock);

        final Person person = person();
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Account account2022 = new Account();
        account2022.setDoRemainingVacationDaysExpireLocally(true);
        account2022.setExpiryDate(LocalDate.of(2022, 4, 1));
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account2022));

        final Account account2023 = new Account();
        account2023.setDoRemainingVacationDaysExpireLocally(true);
        account2023.setExpiryDate(LocalDate.of(2023, 4, 1));
        when(accountService.getHolidaysAccount(2023, person)).thenReturn(Optional.of(account2023));

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(ZERO)
            .withRemainingVacation(TEN)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(account2022, Optional.of(account2023))).thenReturn(vacationDaysLeft);

        sut.remindForRemainingVacationDays();

        verify(mailService).send(mailArgumentCaptor.capture());

        final Mail capturedMail = mailArgumentCaptor.getValue();
        assertThat(capturedMail.getMailAddressRecipients()).contains(List.of(person));
        assertThat(capturedMail.getSubjectMessageKey()).isEqualTo("subject.account.remindForRemainingVacationDays");
        assertThat(capturedMail.getTemplateName()).isEqualTo("remind_remaining_vacation_days");
        assertThat(capturedMail.getTemplateModel()).contains(
            entry("recipientNiceName", "Marlene Muster"),
            entry("personId", 42),
            entry("remainingVacationDays", TEN),
            entry("dayBeforeExpiryDate", LocalDate.of(2022, 3, 31))
        );
    }

    @Test
    void ensureNoNotificationWhenExpireDateNotEqualOfAfter() {

        final Clock clock = Clock.fixed(Instant.parse("2022-03-31T06:00:00Z"), ZoneId.of("UTC"));
        final VacationDaysReminderService sut = new VacationDaysReminderService(personService, accountService, vacationDaysService, mailService, clock);

        final Person person = person();
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Account account2022 = new Account();
        account2022.setPerson(person);
        account2022.setExpiryDate(LocalDate.of(2022, 4, 1));
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account2022));

        sut.notifyForExpiredRemainingVacationDays();
        verifyNoInteractions(mailService);
    }

    @Test
    void ensureNoNotificationWhenNotificationWasAlreadySent() {

        final Clock clock = Clock.fixed(Instant.parse("2022-04-02T06:00:00Z"), ZoneId.of("UTC"));
        final VacationDaysReminderService sut = new VacationDaysReminderService(personService, accountService, vacationDaysService, mailService, clock);

        final Person person = person();
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Account account2022 = new Account();
        account2022.setPerson(person);
        account2022.setExpiryDate(LocalDate.of(2022, 4, 2));
        account2022.setExpiryNotificationSentDate(LocalDate.of(2022, 4, 1));
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account2022));

        sut.notifyForExpiredRemainingVacationDays();

        verifyNoInteractions(mailService);
    }

    @Test
    void ensureNoNotificationWithoutExpiredRemainingVacationDays() {

        final Clock clock = Clock.fixed(Instant.parse("2022-04-01T06:00:00Z"), ZoneId.of("UTC"));
        final VacationDaysReminderService sut = new VacationDaysReminderService(personService, accountService, vacationDaysService, mailService, clock);

        final Person person = person();
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Account account2022 = new Account();
        account2022.setDoRemainingVacationDaysExpireLocally(true);
        account2022.setExpiryDate(LocalDate.of(2022, 4, 1));
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account2022));

        final Account account2023 = new Account();
        account2023.setDoRemainingVacationDaysExpireLocally(true);
        account2023.setExpiryDate(LocalDate.of(2023, 4, 1));
        when(accountService.getHolidaysAccount(2023, person)).thenReturn(Optional.of(account2023));

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(ZERO)
            .withRemainingVacation(TEN)
            .notExpiring(TEN)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(account2022, Optional.of(account2023))).thenReturn(vacationDaysLeft);

        sut.notifyForExpiredRemainingVacationDays();

        verifyNoInteractions(mailService);
    }

    @Test
    void ensureNoNotificationWhenExpireIsDisabled() {

        final Clock clock = Clock.fixed(Instant.parse("2022-04-01T06:00:00Z"), ZoneId.of("UTC"));
        final VacationDaysReminderService sut = new VacationDaysReminderService(personService, accountService, vacationDaysService, mailService, clock);

        final Person person = person();
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Account account2022 = new Account();
        account2022.setDoRemainingVacationDaysExpireLocally(false);
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account2022));

        sut.notifyForExpiredRemainingVacationDays();

        verifyNoInteractions(mailService);
    }

    @Test
    void ensureNotificationForExpiredRemainingVacationDays() {

        final Clock clock = Clock.fixed(Instant.parse("2022-04-01T06:00:00Z"), ZoneId.of("UTC"));
        final VacationDaysReminderService sut = new VacationDaysReminderService(personService, accountService, vacationDaysService, mailService, clock);

        final Person person = person();
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Account account2022 = new Account();
        account2022.setDoRemainingVacationDaysExpireLocally(true);
        account2022.setExpiryDate(LocalDate.of(2022, 4, 1));
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account2022));

        final Account account2023 = new Account();
        account2023.setDoRemainingVacationDaysExpireLocally(true);
        account2023.setExpiryDate(LocalDate.of(2023, 4, 1));
        when(accountService.getHolidaysAccount(2023, person)).thenReturn(Optional.of(account2023));

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(TEN)
            .notExpiring(ONE)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(account2022, Optional.of(account2023))).thenReturn(vacationDaysLeft);
        when(vacationDaysService.calculateTotalLeftVacationDays(account2022)).thenReturn(BigDecimal.valueOf(11L));

        sut.notifyForExpiredRemainingVacationDays();

        verify(mailService).send(mailArgumentCaptor.capture());

        final Mail capturedMail = mailArgumentCaptor.getValue();
        assertThat(capturedMail.getMailAddressRecipients()).contains(List.of(person));
        assertThat(capturedMail.getSubjectMessageKey()).isEqualTo("subject.account.notifyForExpiredRemainingVacationDays");
        assertThat(capturedMail.getTemplateName()).isEqualTo("notify_expired_remaining_vacation_days");
        assertThat(capturedMail.getTemplateModel()).contains(
            entry("recipientNiceName", "Marlene Muster"),
            entry("personId", 42),
            entry("expiredRemainingVacationDays", BigDecimal.valueOf(9L)),
            entry("totalLeftVacationDays", BigDecimal.valueOf(11L)),
            entry("remainingVacationDaysNotExpiring", ONE),
            entry("expiryDate", LocalDate.of(2022, 4, 1))
        );
    }

    private Person person() {
        final Person person = new Person();
        person.setFirstName("Marlene");
        person.setLastName("Muster");
        person.setId(42);
        return person;
    }
}
