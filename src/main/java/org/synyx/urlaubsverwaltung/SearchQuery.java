package org.synyx.urlaubsverwaltung;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Iterator;

public class SearchQuery<T> {

    private final Class<T> type;
    private final Pageable pageable;

    public SearchQuery(Class<T> type, Pageable pageable) {
        this.type = type;
        this.pageable = pageable;
    }

    public Pageable getPageable() {
        return pageable;
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
