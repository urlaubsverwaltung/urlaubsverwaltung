package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


class GravatarUtilTest {

    @Test
    void testCreateImgURL() {

        String email = "FraU.LyOner@NeT.de";
        String url = GravatarUtil.createImgURL(email);

        assertThat(url, is("https://gravatar.com/avatar/3d12daeb3e1bc6dce51ff1f1a0357df6"));
    }
}
