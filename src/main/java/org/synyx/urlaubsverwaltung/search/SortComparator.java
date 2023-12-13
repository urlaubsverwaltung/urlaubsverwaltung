package org.synyx.urlaubsverwaltung.search;

import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.Comparator.reverseOrder;
import static org.slf4j.LoggerFactory.getLogger;

public class SortComparator<T> implements Comparator<T> {

    private static final Logger LOG = getLogger(lookup().lookupClass());

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

        final Function<? super T, Comparable<? super Comparable>> valueExtractor =
            (T entity) -> extractComparableValue(type, entity, List.of(order.getProperty().split("\\.")));

        return order.isDescending()
            ? comparing(valueExtractor, nullsLast(reverseOrder()))
            : comparing(valueExtractor, nullsLast(naturalOrder()));
    }

    private static <T> Comparable<? super Comparable> extractComparableValue(Class<T> type, T entity, List<String> properties) {
        if (properties.isEmpty()) {
            return null;
        }

        final String errorMessage = format("type=\"%s\" does not contain property=\"%s\".", type, String.join(".", properties));

        final PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(type, properties.getFirst());
        if (propertyDescriptor == null) {
            LOG.debug(errorMessage);
            return null;
        }

        final Method readMethod = propertyDescriptor.getReadMethod();
        Object value;
        try {
            value = readMethod.invoke(entity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new SortComparatorException(errorMessage, e);
        }

        if (properties.size() == 1) {
            if (value instanceof String string) {
                value = string.toLowerCase();
            }
            return (Comparable<? super Comparable>) value;
        }

        return extractComparableValue((Class<T>) readMethod.getReturnType(), (T) value, properties.subList(1, properties.size()));
    }
}
