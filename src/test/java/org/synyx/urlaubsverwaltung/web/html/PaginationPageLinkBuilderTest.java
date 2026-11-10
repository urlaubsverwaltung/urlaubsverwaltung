package org.synyx.urlaubsverwaltung.web.html;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.synyx.urlaubsverwaltung.web.html.PaginationPageLinkBuilder.QueryParam;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.web.html.PaginationPageLinkBuilder.buildPageLinkPrefix;

class PaginationPageLinkBuilderTest {

    @Test
    void ensureQueryParameterValuesAreEncoded() {
        final String prefix = buildPageLinkPrefix(PageRequest.of(0, 20), List.of(new QueryParam("query", "M&M max+test@example.org #1")));
        assertThat(prefix).isEqualTo("?query=M%26M+max%2Btest%40example.org+%231&size=20");
    }
}
