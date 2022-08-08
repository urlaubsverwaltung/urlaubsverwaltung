package org.synyx.urlaubsverwaltung.search;

import org.springframework.data.domain.Pageable;

import java.util.Objects;

public class PageableSearchQuery<T> {

    private final Class<T> type;
    private final Pageable pageable;
    private final String query;

    public PageableSearchQuery(Class<T> type, Pageable pageable, String query) {
        this.type = type;
        this.pageable = pageable;
        this.query = query;
    }

    public Pageable getPageable() {
        return pageable;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageableSearchQuery<?> that = (PageableSearchQuery<?>) o;
        return Objects.equals(type, that.type) && Objects.equals(pageable, that.pageable) && Objects.equals(query, that.query);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, pageable, query);
    }
}
