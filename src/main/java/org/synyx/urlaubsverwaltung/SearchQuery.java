package org.synyx.urlaubsverwaltung;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

public class SearchQuery<T> {

    private final Class<T> type;
    private final Pageable pageable;
    private final String query;

    public SearchQuery(Class<T> type, Pageable pageable, String query) {
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

    public Comparator<T> getComparator() {
        final Iterator<Sort.Order> orderIterator = pageable.getSort().iterator();
        final Sort.Order order = orderIterator.next();

        Comparator<T> comparator = sortComparable(order);
        while (orderIterator.hasNext()) {
            comparator = comparator.thenComparing(sortComparable(orderIterator.next()));
        }

        return comparator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchQuery<?> that = (SearchQuery<?>) o;
        return Objects.equals(type, that.type) && Objects.equals(pageable, that.pageable) && Objects.equals(query, that.query);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, pageable, query);
    }

    private Comparator<T> sortComparable(Sort.Order order) {
        final Comparator<T> comparator = Comparator.comparing((T entity) -> {
            try {
                return (Comparable) BeanUtils.getPropertyDescriptor(type, order.getProperty()).getReadMethod().invoke(entity);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });

        return order.isDescending() ? comparator.reversed() : comparator;
    }
}
