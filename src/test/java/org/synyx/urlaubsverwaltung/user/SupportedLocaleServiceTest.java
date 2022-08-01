package org.synyx.urlaubsverwaltung.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SupportedLocaleServiceTest {

    private SupportedLocaleService sut;

    @BeforeEach
    void setUp() {
        sut = new SupportedLocaleService();
    }

    @Test
    void ensuresThatAllSupportedLocalesAreProvided() {
        final Set<Locale> supportedLocales = sut.getSupportedLocales();
        assertThat(supportedLocales).contains(Locale.GERMAN, Locale.forLanguageTag("de-AT"), Locale.ENGLISH, Locale.forLanguageTag("el"));
    }
}
