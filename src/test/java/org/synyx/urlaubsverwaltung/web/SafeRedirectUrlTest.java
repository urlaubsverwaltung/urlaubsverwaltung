package org.synyx.urlaubsverwaltung.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.web.SafeRedirectUrl.ofKnownOrigin;

class SafeRedirectUrlTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "/web/application",
        "/web/application/replacement",
        "/web/sicknote/submitted",
        "/web/persons/5/sicknotes",
        "/web/person/5/overview"
    })
    void ensurePagesOfferingAnActionAreAccepted(String url) {
        assertThat(ofKnownOrigin(url)).hasValue(url);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/web/persons/5/sicknotes?year=2022",
        "/web/person/5/overview?year=2022"
    })
    void ensureQueryStringIsKeptButNotPartOfTheComparison(String url) {
        assertThat(ofKnownOrigin(url)).hasValue(url);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void ensureMissingUrlIsRejected(String url) {
        assertThat(ofKnownOrigin(url)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/web/hijacked/",
        "/web/application/57",
        "/web/person/5",
        "/web/sicknote/42",
        "/web/persons/5/sicknotes/42"
    })
    void ensurePagesNotOfferingAnActionAreRejected(String url) {
        assertThat(ofKnownOrigin(url)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "//evil.example.org/web/application",
        "http://evil.example.org/web/application",
        "https://evil.example.org",
        "web/application",
        "\\\\evil.example.org",
        "/\\evil.example.org",
        "/web/application\\..\\..",
        "javascript:alert(1)"
    })
    void ensureUrlsLeavingThisApplicationAreRejected(String url) {
        assertThat(ofKnownOrigin(url)).isEmpty();
    }

    @Test
    void ensureUrlWithControlCharacterIsRejected() {
        assertThat(ofKnownOrigin("/web/application\nLocation: https://evil.example.org")).isEmpty();
    }
}
