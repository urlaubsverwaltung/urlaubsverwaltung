package org.synyx.urlaubsverwaltung.calendarintegration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "uv.calendar-integration.encryption-secret=it-encryption-secret")
@Transactional
class GoogleCalendarSettingsEncryptionIT extends SingleTenantTestContainersBase {

    @Autowired
    private CalendarSettingsService calendarSettingsService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void ensureSecretsAreEncryptedAtRestAndDecryptedOnRead() {

        calendarSettingsService.insertDefaultCalendarSettings();

        final CalendarSettings calendarSettings = calendarSettingsService.getCalendarSettings();
        final GoogleCalendarSettings googleCalendarSettings = new GoogleCalendarSettings();
        calendarSettings.setGoogleCalendarSettings(googleCalendarSettings);
        googleCalendarSettings.setClientId("CLIENT_ID");
        googleCalendarSettings.setClientSecret("CLIENT_SECRET");
        googleCalendarSettings.setCalendarId("CALENDAR_ID");
        googleCalendarSettings.setRefreshToken("REFRESH_TOKEN");
        googleCalendarSettings.setAccessToken("ACCESS_TOKEN");
        googleCalendarSettings.setAccessTokenExpirationMillis(4102444800000L);

        final CalendarSettings saved = calendarSettingsService.save(calendarSettings);
        entityManager.flush();
        entityManager.clear();

        final var row = jdbcTemplate.queryForMap(
            "select google_client_secret, google_refresh_token, google_access_token from calendar_integration_settings where id = ?",
            saved.getId()
        );

        assertThat((String) row.get("google_client_secret")).startsWith("enc:v1:").doesNotContain("CLIENT_SECRET");
        assertThat((String) row.get("google_refresh_token")).startsWith("enc:v1:").doesNotContain("REFRESH_TOKEN");
        assertThat((String) row.get("google_access_token")).startsWith("enc:v1:").doesNotContain("ACCESS_TOKEN");

        final GoogleCalendarSettings persisted = calendarSettingsService.getCalendarSettings().getGoogleCalendarSettings();
        assertThat(persisted.getClientSecret()).isEqualTo("CLIENT_SECRET");
        assertThat(persisted.getRefreshToken()).isEqualTo("REFRESH_TOKEN");
        assertThat(persisted.getAccessToken()).isEqualTo("ACCESS_TOKEN");
        assertThat(persisted.getAccessTokenExpirationMillis()).isEqualTo(4102444800000L);
    }
}
