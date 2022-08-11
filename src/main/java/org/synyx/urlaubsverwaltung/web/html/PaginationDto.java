package org.synyx.urlaubsverwaltung.web.html;

import org.springframework.data.domain.Page;

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
}
