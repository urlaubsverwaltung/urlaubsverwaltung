package org.synyx.urlaubsverwaltung.search;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.synyx.urlaubsverwaltung.person.Person;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultPersonSuggestionUrlStrategyTest {

    private DefaultPersonSuggestionUrlStrategy sut;

    @BeforeEach
    void setUp() {
        sut = new DefaultPersonSuggestionUrlStrategy();
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, 3L})
    void ensureDefaultMainLink(long id) {

        final Person person = new Person();
        person.setId(id);

        assertThat(sut.buildSuggestionMainLink(person, searchContext())).isEqualTo("/web/person/%s/overview".formatted(id));
    }

    private static SearchContext searchContext() {
        return searchContext(new MockHttpServletRequest());
    }

    private static SearchContext searchContext(HttpServletRequest request) {
        return SearchContext.of(request, null);
    }
}
