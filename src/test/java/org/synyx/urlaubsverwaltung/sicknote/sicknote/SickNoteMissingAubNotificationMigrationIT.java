package org.synyx.urlaubsverwaltung.sicknote.sicknote;

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
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;

/**
 * Verifies the SQL of {@code changelog-6.15.0-missing-aub-notification.xml} that marks the sick notes ended before
 * the update as notified. The changeset already ran on the empty database when this test starts, so the SQL is read
 * from the changelog and executed again.
 */
@SpringBootTest
@Transactional
class SickNoteMissingAubNotificationMigrationIT extends SingleTenantTestContainersBase {

    private static final String CHANGELOG = "dbchangelogs/changelog-6.15.0-missing-aub-notification.xml";
    private static final String CHANGESET_ID = "mark-ended-sick-notes-without-aub-as-notified";

    @Autowired
    private SickNoteRepository sickNoteRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void ensureMigrationMarksEndedSickNoteWithoutAubAsNotified() throws Exception {

        final LocalDate today = currentDate();
        final SickNoteEntity sickNote = sickNoteRepository.save(sickNote(today.minusDays(10), today.minusDays(1), null, null));

        migrate();

        assertThat(missingAubNotificationSend(sickNote)).isEqualTo(today);
    }

    @Test
    void ensureMigrationKeepsOngoingSickNoteWithoutAubOpenForTheNotification() throws Exception {

        final LocalDate today = currentDate();
        final SickNoteEntity endsToday = sickNoteRepository.save(sickNote(today.minusDays(10), today, null, null));
        final SickNoteEntity endsTomorrow = sickNoteRepository.save(sickNote(today.minusDays(1), today.plusDays(1), null, null));

        migrate();

        assertThat(missingAubNotificationSend(endsToday)).isNull();
        assertThat(missingAubNotificationSend(endsTomorrow)).isNull();
    }

    @Test
    void ensureMigrationDoesNotMarkEndedSickNoteWithAub() throws Exception {

        final LocalDate today = currentDate();
        final SickNoteEntity sickNote = sickNoteRepository.save(sickNote(today.minusDays(10), today.minusDays(1), today.minusDays(10), today.minusDays(1)));

        migrate();

        assertThat(missingAubNotificationSend(sickNote)).isNull();
    }

    private static SickNoteEntity sickNote(LocalDate startDate, LocalDate endDate, LocalDate aubStartDate, LocalDate aubEndDate) {
        final SickNoteEntity sickNoteEntity = new SickNoteEntity();
        sickNoteEntity.setStartDate(startDate);
        sickNoteEntity.setEndDate(endDate);
        sickNoteEntity.setAubStartDate(aubStartDate);
        sickNoteEntity.setAubEndDate(aubEndDate);
        sickNoteEntity.setStatus(ACTIVE);
        return sickNoteEntity;
    }

    private void migrate() throws Exception {
        entityManager.flush();
        jdbcTemplate.update(changesetSql());
        entityManager.clear();
    }

    private LocalDate currentDate() {
        return jdbcTemplate.queryForObject("SELECT CURRENT_DATE", LocalDate.class);
    }

    private LocalDate missingAubNotificationSend(SickNoteEntity sickNote) {
        return jdbcTemplate.queryForObject("SELECT missing_aub_notification_send FROM sick_note WHERE id = ?", LocalDate.class, sickNote.getId());
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
