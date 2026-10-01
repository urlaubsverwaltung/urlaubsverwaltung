package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteTypeService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_MISSING_AUB_TO_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.SUBMITTED;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.NONE;

/**
 * The whole job with the database, the working time of the person and the recipients of the notification.
 */
@SpringBootTest(properties = {"spring.mail.port=3025", "spring.mail.host=localhost"})
@Transactional
class SickNoteMissingAubNotificationIT extends SingleTenantTestContainersBase {

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP_IMAP);

    @Autowired
    private SickNoteMailService sut;

    @Autowired
    private PersonService personService;
    @Autowired
    private SickNoteService sickNoteService;
    @Autowired
    private SickNoteTypeService sickNoteTypeService;
    @Autowired
    private WorkingTimeWriteService workingTimeWriteService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void ensureResponsiblePersonsAreNotifiedOnceAboutSickNotesWithoutAubReachingTheThirdWorkDay() throws Exception {

        final LocalDate today = LocalDate.now();

        final Person office = personService.create("office", "Lieschen", "Müller", "office@example.org", List.of(NOTIFICATION_EMAIL_SICK_NOTE_MISSING_AUB_TO_MANAGEMENT), List.of(USER, OFFICE));
        final Person officeWithoutNotification = personService.create("office2", "Olga", "Office", "office2@example.org", List.of(), List.of(USER, OFFICE));
        final Person bossWithoutSickNoteAdd = personService.create("boss", "Bernd", "Boss", "boss@example.org", List.of(NOTIFICATION_EMAIL_SICK_NOTE_MISSING_AUB_TO_MANAGEMENT), List.of(USER, BOSS));

        final Person sickPerson = personService.create("sick", "Marlene", "Muster", "sick@example.org", List.of(), List.of(USER));
        // works on every day of the week without public holidays, so the work days do not depend on today
        workingTimeWriteService.touch(List.of(1, 2, 3, 4, 5, 6, 7), today.minusYears(1), sickPerson, NONE);

        final SickNote thirdWorkDayToday = save(sickPerson, today.minusDays(2), today.plusDays(2), null, null, ACTIVE);
        final SickNote submittedLongAgo = save(sickPerson, today.minusDays(20), today.minusDays(10), null, null, SUBMITTED);
        final SickNote secondWorkDayToday = save(sickPerson, today.minusDays(1), today.plusDays(2), null, null, ACTIVE);
        final SickNote withAub = save(sickPerson, today.minusDays(5), today, today.minusDays(5), today, ACTIVE);

        sut.sendMissingAubNotification();

        await()
            .atMost(Duration.ofSeconds(2))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(office.getEmail())).hasSize(2));

        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(office.getEmail());
        assertThat(inbox).extracting(MimeMessage::getSubject)
            .containsOnly("Fehlende AU-Bescheinigung für die Krankmeldung von Marlene Muster");
        assertThat(greenMail.getReceivedMessagesForDomain(officeWithoutNotification.getEmail())).isEmpty();
        assertThat(greenMail.getReceivedMessagesForDomain(bossWithoutSickNoteAdd.getEmail())).isEmpty();
        assertThat(greenMail.getReceivedMessagesForDomain(sickPerson.getEmail())).isEmpty();

        assertThat(missingAubNotificationSend(thirdWorkDayToday)).isEqualTo(today);
        assertThat(missingAubNotificationSend(submittedLongAgo)).isEqualTo(today);
        assertThat(missingAubNotificationSend(secondWorkDayToday)).isNull();
        assertThat(missingAubNotificationSend(withAub)).isNull();

        // the next run does not notify again
        sut.sendMissingAubNotification();

        Thread.sleep(500);
        assertThat(greenMail.getReceivedMessagesForDomain(office.getEmail())).hasSize(2);
    }

    private SickNote save(Person person, LocalDate startDate, LocalDate endDate, LocalDate aubStartDate, LocalDate aubEndDate, SickNoteStatus status) {
        final SickNoteType sickNoteType = sickNoteTypeService.getSickNoteTypes().getFirst();
        return sickNoteService.save(SickNote.builder()
            .person(person)
            .applier(person)
            .sickNoteType(sickNoteType)
            .startDate(startDate)
            .endDate(endDate)
            .dayLength(DayLength.FULL)
            .aubStartDate(aubStartDate)
            .aubEndDate(aubEndDate)
            .status(status)
            .build());
    }

    private LocalDate missingAubNotificationSend(SickNote sickNote) {
        return jdbcTemplate.queryForObject("SELECT missing_aub_notification_send FROM sick_note WHERE id = ?", LocalDate.class, sickNote.getId());
    }
}
