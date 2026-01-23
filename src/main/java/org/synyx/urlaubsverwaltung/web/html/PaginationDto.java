package org.synyx.urlaubsverwaltung.web.html;

import org.springframework.boot.data.autoconfigure.web.DataWebProperties;
import org.springframework.data.domain.Page;

import java.util.Objects;

public class PaginationDto<T> {

    private final Page<T> page;
    private final String pageLinkPrefix;
    private final DataWebProperties.Pageable pageableProperties;

    public PaginationDto(Page<T> page, String pageLinkPrefix, DataWebProperties.Pageable pageableProperties) {
        this.page = page;
        this.pageLinkPrefix = pageLinkPrefix;
        this.pageableProperties = pageableProperties;
    }

    public Page<T> getPage() {
        return page;
    }

    public String hrefForPage(int pageNumberZeroBased) {
        final int page = pageableProperties.isOneIndexedParameters() ? pageNumberZeroBased + 1 : pageNumberZeroBased;
        return "%s&%s=%s".formatted(pageLinkPrefix, pageableProperties.getPageParameter(), page);
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

    @Override
    public String toString() {
        return "PaginationDto{" +
            "page=" + page +
            ", pageLinkPrefix='" + pageLinkPrefix + '\'' +
            '}';
    }
}
