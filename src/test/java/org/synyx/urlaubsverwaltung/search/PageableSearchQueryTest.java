package org.synyx.urlaubsverwaltung.search;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PageableSearchQueryTest {

    @ParameterizedTest
    @ValueSource(strings = {"Anne", " Anne", "Anne ", "  Anne", "Anne  "})
    void ensureToTrimWhitespacesForQueryString(final String query) {
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(mock(Pageable.class), query);
        assertThat(pageableSearchQuery.getQuery()).isEqualTo("Anne");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Anne  Lore", " Anne        Lore     "})
    void ensureToTrimDoubledWhitespacesForQueryString(final String query) {
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(mock(Pageable.class), query);
        assertThat(pageableSearchQuery.getQuery()).isEqualTo("Anne Lore");
    }
}
