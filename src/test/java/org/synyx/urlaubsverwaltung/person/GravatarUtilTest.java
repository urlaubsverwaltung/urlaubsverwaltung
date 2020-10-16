package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.synyx.urlaubsverwaltung.person.GravatarUtil.createImgURL;

class GravatarUtilTest {

    @Test
    void testCreateImgURL() {
        assertThat(createImgURL("Jim.Medina@example.org"), is("https://gravatar.com/avatar/bfe7859d16a06b3a1b70848a40993f5d"));
    }
}
