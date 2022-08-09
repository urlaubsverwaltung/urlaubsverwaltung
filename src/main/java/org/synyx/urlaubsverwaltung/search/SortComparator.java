package org.synyx.urlaubsverwaltung.search;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Function;

import static java.util.Comparator.comparing;

public class SortComparator<T> implements Comparator<T> {

    private final Comparator<T> comparator;

    public SortComparator(Class<T> type, Sort sort) {
        this.comparator = buildComparator(type, sort);
    }

    @Override
    public int compare(T o1, T o2) {
        return comparator.compare(o1, o2);
    }

    private static <T> Comparator<T> buildComparator(Class<T> type, Sort sort) {
        final Iterator<Sort.Order> orderIterator = sort.iterator();
        if (!orderIterator.hasNext()) {
            return comparing(t -> 0);
        }

        Comparator<T> comparator = sortComparable(type, orderIterator.next());
        while (orderIterator.hasNext()) {
            comparator = comparator.thenComparing(sortComparable(type, orderIterator.next()));
        }

        return comparator;
    }

    private static <T> Comparator<T> sortComparable(Class<T> type, Sort.Order order) {

        final Function<? super T, Comparable<? super Comparable>> valueExtractor = (T entity) -> {
            try {
                final PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(type, order.getProperty());
                if (propertyDescriptor != null) {
                    final Method readMethod = propertyDescriptor.getReadMethod();
                    final Object invoke = readMethod.invoke(entity);
                    return (Comparable<? super Comparable>) invoke;
                }
                return null;
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };

        return order.isDescending()
            ? comparing(valueExtractor, Comparator.nullsFirst(Comparator.reverseOrder()))
            : comparing(valueExtractor, Comparator.nullsFirst(Comparator.naturalOrder()));
    }
}
