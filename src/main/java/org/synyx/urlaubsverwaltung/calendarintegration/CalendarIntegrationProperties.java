package org.synyx.urlaubsverwaltung.calendarintegration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("uv.calendar-integration")
public class CalendarIntegrationProperties {

    /**
     * Secret used to encrypt sensitive calendar integration settings at rest
     * (google oauth client secret, access token and refresh token).
     * If not set, these values are stored in plaintext.
     * <p>
     * Values that were stored in plaintext before the secret was configured are
     * encrypted transparently with the next save of the calendar settings.
     * Changing or removing the secret afterwards makes already encrypted values unreadable.
     */
    private String encryptionSecret;

    public String getEncryptionSecret() {
        return encryptionSecret;
    }

    public void setEncryptionSecret(String encryptionSecret) {
        this.encryptionSecret = encryptionSecret;
    }
}
