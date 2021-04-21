package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.GravatarUtil.createImgURL;

class GravatarUtilTest {

    @ParameterizedTest
    @ValueSource(strings = {"   "})
    @NullSource
    void ensureCreateGravatarUrlFallbackOnEmptyOrNullEMail(String email) {
        final String gravatarUrl = createImgURL(email);
        assertThat(gravatarUrl).isEqualTo("https://gravatar.com/avatar/d41d8cd98f00b204e9800998ecf8427e");
    }

    @ParameterizedTest
    @ValueSource(strings = {"gary@example.org", "GARY@EXAMPLE.ORG"})
    void ensureCreateGravatarUrl(String email) {
        final String gravatarUrl = createImgURL(email);
        assertThat(gravatarUrl).isEqualTo("https://gravatar.com/avatar/f8b44b88d2e22c6f2f4f4592a9196598");
    }
}
