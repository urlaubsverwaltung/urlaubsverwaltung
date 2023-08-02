package org.synyx.urlaubsverwaltung.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.function.Function;
import java.util.stream.Stream;

public class PageStreamSupport {

    private PageStreamSupport() {
        // Utility classes should not have public constructors java:S1118
    }

    private static final int DEFAULT_PAGE_SIZE = 25;

    public static <E> Stream<E> stream(Function<Pageable, Page<E>> pageSupplier) {
        return stream(pageSupplier, DEFAULT_PAGE_SIZE);
    }

    public static <E> Stream<E> stream(Function<Pageable, Page<E>> pageSupplier, int pageSize) {
        final Page<E> initialPage = pageSupplier.apply(PageRequest.of(0, pageSize));

        if (initialPage == null || !initialPage.hasContent()) {
            return Stream.empty();
        }

        return Stream.iterate(
                initialPage,
                currentPage -> !currentPage.isEmpty(),
                currentPage -> currentPage.hasNext() ? pageSupplier.apply(currentPage.nextPageable()) : Page.empty()
            )
            .flatMap(p -> p.getContent().stream());
    }
}
