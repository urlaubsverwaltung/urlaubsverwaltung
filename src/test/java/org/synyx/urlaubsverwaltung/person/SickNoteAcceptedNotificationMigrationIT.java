package org.synyx.urlaubsverwaltung.person;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_USER;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_CANCELLED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_EDITED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_USER;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

/**
 * Verifies the SQL of {@code changelog-6.14.0-restore-sick-note-accepted-notification.xml}. The changeset already ran
 * on the empty database when this test starts, so the SQL is read from the changelog and executed again against
 * persons that lost the notification by saving their notification settings.
 */
@SpringBootTest
@Transactional
class SickNoteAcceptedNotificationMigrationIT extends SingleTenantTestContainersBase {

    private static final String CHANGELOG = "dbchangelogs/changelog-6.14.0-restore-sick-note-accepted-notification.xml";
    private static final String CHANGESET_ID = "restore-sick-note-accepted-notification";

    @Autowired
    private PersonService personService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void ensureMigrationRestoresTheNotificationForPersonsWithTheOwnSickNoteNotifications() throws Exception {

        // saved "Einreichung und Änderung für mich" before the fix: everything of the checkbox except the accepted sick note
        final Person person = personService.create("saved", "Saved", "Mustermann", "saved@example.org", List.of(
            NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_USER,
            NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT,
            NOTIFICATION_EMAIL_SICK_NOTE_EDITED_BY_MANAGEMENT,
            NOTIFICATION_EMAIL_SICK_NOTE_CANCELLED_BY_MANAGEMENT
        ), List.of(USER));

        migrate();

        assertThat(countOfAcceptedNotification(person)).isOne();
    }

    @Test
    void ensureMigrationDoesNotDuplicateTheNotification() throws Exception {

        final Person person = personService.create("default", "Default", "Mustermann", "default@example.org", List.of(
            NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_USER,
            NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_USER
        ), List.of(USER));

        migrate();

        assertThat(countOfAcceptedNotification(person)).isOne();
    }

    @Test
    void ensureMigrationDoesNotRestoreTheNotificationForPersonsWithoutTheOwnSickNoteNotifications() throws Exception {

        // unchecked "Einreichung und Änderung für mich"
        final Person person = personService.create("unchecked", "Unchecked", "Mustermann", "unchecked@example.org", List.of(), List.of(USER));

        migrate();

        assertThat(countOfAcceptedNotification(person)).isZero();
    }

    private void migrate() throws Exception {
        entityManager.flush();
        jdbcTemplate.update(changesetSql());
        entityManager.clear();
    }

    private int countOfAcceptedNotification(Person person) {
        final Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM person_notifications WHERE person_id = ? AND notifications = ?",
            Integer.class, person.getId(), NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_USER.name());
        return count == null ? 0 : count;
    }

    private static String changesetSql() throws Exception {
        try (InputStream changelog = new ClassPathResource(CHANGELOG).getInputStream()) {
            final NodeList changeSets = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(changelog)
                .getElementsByTagName("changeSet");
            for (int i = 0; i < changeSets.getLength(); i++) {
                final Element changeSet = (Element) changeSets.item(i);
                if (CHANGESET_ID.equals(changeSet.getAttribute("id"))) {
                    return changeSet.getElementsByTagName("sql").item(0).getTextContent();
                }
            }
        }
        throw new IllegalStateException("changeSet id=%s not found in %s".formatted(CHANGESET_ID, CHANGELOG));
    }
}
