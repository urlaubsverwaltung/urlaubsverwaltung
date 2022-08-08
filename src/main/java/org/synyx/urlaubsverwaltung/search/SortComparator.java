package org.synyx.urlaubsverwaltung.search;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Iterator;

public class SortComparator<T> implements Comparator<T> {

    private final Comparator<T> comparator;

    public SortComparator(Class<T> type, Sort sort) {
        this.comparator = getComparator(type, sort);
    }

    private static <T> Comparator<T> getComparator(Class<T> type, Sort sort) {
        final Iterator<Sort.Order> orderIterator = sort.iterator();
        if (!orderIterator.hasNext()) {
            return Comparator.comparing(t -> 0);
        }

        final Sort.Order order = orderIterator.next();

        Comparator<T> comparator = sortComparable(type, order);
        while (orderIterator.hasNext()) {
            comparator = comparator.thenComparing(sortComparable(type, orderIterator.next()));
        }

        return comparator;
    }

    private static <T> Comparator<T> sortComparable(Class<T> type, Sort.Order order) {
        final Comparator<T> comparator = Comparator.comparing((T entity) -> {
            try {
                return (Comparable) BeanUtils.getPropertyDescriptor(type, order.getProperty()).getReadMethod().invoke(entity);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });

        return order.isDescending() ? comparator.reversed() : comparator;
    }

    @Override
    public int compare(T o1, T o2) {
        return comparator.compare(o1, o2);
    }
}
