package org.synyx.urlaubsverwaltung.search;

import org.springframework.data.domain.Pageable;

import java.util.Objects;

public class PageableSearchQuery {

    private final Pageable pageable;
    private final String query;

    public PageableSearchQuery(Pageable pageable) {
        this(pageable, "");
    }

    public PageableSearchQuery(Pageable pageable, String query) {
        this.pageable = pageable;
        this.query = query;
    }

    public Pageable getPageable() {
        return pageable;
    }

    public String getQuery() {
        return query.strip().replaceAll("\\s+", " ");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageableSearchQuery that = (PageableSearchQuery) o;
        return Objects.equals(pageable, that.pageable) && Objects.equals(query, that.query);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageable, query);
    }

    @Override
    public String toString() {
        return "PageableSearchQuery{" +
            "pageable=" + pageable +
            ", query='" + query + '\'' +
            '}';
    }
}
