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
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_CARRIED_OVER_MANAGEMENT_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_EXPIRED_MANAGEMENT_ALL;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

/**
 * Verifies the SQL of {@code changelog-6.15.0-remaining-vacation-days-management-notifications.xml}. The
 * changeset already ran on the empty database when this test starts, so the SQL is read from the changelog and
 * executed again against the persons created here.
 */
@SpringBootTest
@Transactional
class RemainingVacationDaysManagementNotificationsMigrationIT extends SingleTenantTestContainersBase {

    private static final String CHANGELOG = "dbchangelogs/changelog-6.15.0-remaining-vacation-days-management-notifications.xml";
    private static final String CHANGESET_ID = "remaining-vacation-days-management-notifications";

    @Autowired
    private PersonService personService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void ensureMigrationEnablesTheNotificationsForOffice() throws Exception {

        final Person office = personService.create("office", "Office", "Olga", "office@example.org", List.of(), List.of(USER, OFFICE));

        migrate();

        assertThat(countOfNotification(office, NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_CARRIED_OVER_MANAGEMENT_ALL)).isOne();
        assertThat(countOfNotification(office, NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_EXPIRED_MANAGEMENT_ALL)).isOne();
    }

    @Test
    void ensureMigrationDoesNotDuplicateTheNotifications() throws Exception {

        final Person office = personService.create("office", "Office", "Olga", "office@example.org",
            List.of(NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_EXPIRED_MANAGEMENT_ALL), List.of(USER, OFFICE));

        migrate();

        assertThat(countOfNotification(office, NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_CARRIED_OVER_MANAGEMENT_ALL)).isOne();
        assertThat(countOfNotification(office, NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_EXPIRED_MANAGEMENT_ALL)).isOne();
    }

    @Test
    void ensureMigrationDoesNotEnableTheNotificationsWithoutOffice() throws Exception {

        final Person boss = personService.create("boss", "Boss", "Bruno", "boss@example.org", List.of(), List.of(USER, BOSS));

        migrate();

        assertThat(countOfNotification(boss, NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_CARRIED_OVER_MANAGEMENT_ALL)).isZero();
        assertThat(countOfNotification(boss, NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_EXPIRED_MANAGEMENT_ALL)).isZero();
    }

    private void migrate() throws Exception {
        entityManager.flush();
        jdbcTemplate.update(changesetSql());
        entityManager.clear();
    }

    private int countOfNotification(Person person, MailNotification notification) {
        final Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM person_notifications WHERE person_id = ? AND notifications = ?",
            Integer.class, person.getId(), notification.name());
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
