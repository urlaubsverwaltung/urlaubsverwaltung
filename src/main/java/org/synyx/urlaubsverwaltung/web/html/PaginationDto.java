package org.synyx.urlaubsverwaltung.web.html;

import org.springframework.data.domain.Page;

import java.util.Objects;

public class PaginationDto<T> {

    private final Page<T> page;
    private final String pageLinkPrefix;

    public PaginationDto(Page<T> page, String pageLinkPrefix) {
        this.page = page;
        this.pageLinkPrefix = pageLinkPrefix;
    }

    public Page<T> getPage() {
        return page;
    }

    public String hrefForPage(int pageNumber) {
        // TODO "page" is configurable in application properties with `spring.data.web.pageable.page-parameter=page`
        return pageLinkPrefix + "&page=" + pageNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaginationDto<?> that = (PaginationDto<?>) o;
        return Objects.equals(page, that.page) && Objects.equals(pageLinkPrefix, that.pageLinkPrefix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, pageLinkPrefix);
    }
}
