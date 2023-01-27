package org.synyx.urlaubsverwaltung.search;


import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PageStreamSupportTest {

    @Test
    void happyPath() {

        Function<Pageable, Page<String>> pageSupplier = pageable -> {
            int pageNumber = pageable.getPageNumber();
            if (pageNumber == 0) {
                return new PageImpl(List.of("a"), pageable, 3);
            } else if (pageNumber == 1) {
                return new PageImpl(List.of("b"), pageable, 3);
            } else if (pageNumber == 2) {
                return new PageImpl(List.of("c"), pageable, 3);
            } else {
                return Page.empty();
            }
        };

        Stream<String> stream = PageStreamSupport.stream(pageSupplier, 1);

        assertThat(stream).contains("a", "b", "c");
    }

    @Test
    void ensureHandlesEmptyPage() {

        Stream<String> stream = PageStreamSupport.stream(Page::empty, 1);

        assertThat(stream).isEmpty();
    }
}
