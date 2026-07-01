package org.synyx.urlaubsverwaltung.calendarintegration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EncryptedSecretConverterTest {

    @Test
    void ensureEncryptsAndDecrypts() {

        final EncryptedSecretConverter sut = converterWithSecret("super-secret");

        final String encrypted = sut.convertToDatabaseColumn("my-refresh-token");
        assertThat(encrypted)
            .startsWith("enc:v1:")
            .doesNotContain("my-refresh-token");

        assertThat(sut.convertToEntityAttribute(encrypted)).isEqualTo("my-refresh-token");
    }

    @Test
    void ensureEncryptionUsesRandomSaltAndIv() {

        final EncryptedSecretConverter sut = converterWithSecret("super-secret");

        final String first = sut.convertToDatabaseColumn("my-refresh-token");
        final String second = sut.convertToDatabaseColumn("my-refresh-token");

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void ensureOtherConverterInstanceWithSameSecretCanDecrypt() {

        final String encrypted = converterWithSecret("super-secret").convertToDatabaseColumn("my-refresh-token");

        final EncryptedSecretConverter sut = converterWithSecret("super-secret");
        assertThat(sut.convertToEntityAttribute(encrypted)).isEqualTo("my-refresh-token");
    }

    @Test
    void ensureNullIsPassedThrough() {

        final EncryptedSecretConverter sut = converterWithSecret("super-secret");

        assertThat(sut.convertToDatabaseColumn(null)).isNull();
        assertThat(sut.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void ensureLegacyPlaintextValueIsReadAsIs() {

        final EncryptedSecretConverter sut = converterWithSecret("super-secret");

        assertThat(sut.convertToEntityAttribute("legacy-plaintext-token")).isEqualTo("legacy-plaintext-token");
    }

    @Test
    void ensureValuesArePassedThroughWithoutConfiguredSecret() {

        final EncryptedSecretConverter sut = converterWithSecret(null);

        assertThat(sut.convertToDatabaseColumn("my-refresh-token")).isEqualTo("my-refresh-token");
        assertThat(sut.convertToEntityAttribute("my-refresh-token")).isEqualTo("my-refresh-token");
    }

    @Test
    void ensureEncryptedValueWithoutConfiguredSecretThrows() {

        final EncryptedSecretConverter withSecret = converterWithSecret("super-secret");
        final String encrypted = withSecret.convertToDatabaseColumn("my-refresh-token");

        final EncryptedSecretConverter sut = converterWithSecret(null);
        assertThatThrownBy(() -> sut.convertToEntityAttribute(encrypted))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("uv.calendar-integration.encryption-secret");
    }

    @Test
    void ensureEncryptedValueWithDifferentSecretThrows() {

        final EncryptedSecretConverter withSecret = converterWithSecret("super-secret");
        final String encrypted = withSecret.convertToDatabaseColumn("my-refresh-token");

        final EncryptedSecretConverter sut = converterWithSecret("other-secret");
        assertThatThrownBy(() -> sut.convertToEntityAttribute(encrypted))
            .isInstanceOf(IllegalStateException.class);
    }

    private static EncryptedSecretConverter converterWithSecret(String secret) {
        final CalendarIntegrationProperties properties = new CalendarIntegrationProperties();
        properties.setEncryptionSecret(secret);
        return new EncryptedSecretConverter(properties);
    }
}
