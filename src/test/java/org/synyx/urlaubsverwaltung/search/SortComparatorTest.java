package org.synyx.urlaubsverwaltung.search;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SortComparatorTest {

    @Test
    void ensureSortingAsc() {
        final Sort sort = Sort.by(Sort.Direction.ASC, "integer").and(Sort.by(Sort.Direction.ASC, "string"));
        final SortComparator<SomeClassToSort> sut = new SortComparator<>(SomeClassToSort.class, sort);

        final List<SomeClassToSort> list = List.of(
            new SomeClassToSort(2, "aaa"),
            new SomeClassToSort(1, "aaa"),
            new SomeClassToSort(3, "bbb"),
            new SomeClassToSort(3, "aaa")
        );

        final List<SomeClassToSort> sorted = list.stream().sorted(sut).collect(toList());

        assertThat(sorted).containsExactly(
            new SomeClassToSort(1, "aaa"),
            new SomeClassToSort(2, "aaa"),
            new SomeClassToSort(3, "aaa"),
            new SomeClassToSort(3, "bbb")
        );
    }

    @Test
    void ensureSortingDesc() {
        final Sort sort = Sort.by(Sort.Direction.ASC, "integer").and(Sort.by(Sort.Direction.DESC, "string"));
        final SortComparator<SomeClassToSort> sut = new SortComparator<>(SomeClassToSort.class, sort);

        final List<SomeClassToSort> list = List.of(
            new SomeClassToSort(2, "aaa"),
            new SomeClassToSort(1, "aaa"),
            new SomeClassToSort(3, "bbb"),
            new SomeClassToSort(3, "aaa")
        );

        final List<SomeClassToSort> sorted = list.stream().sorted(sut).collect(toList());

        assertThat(sorted).containsExactly(
            new SomeClassToSort(1, "aaa"),
            new SomeClassToSort(2, "aaa"),
            new SomeClassToSort(3, "bbb"),
            new SomeClassToSort(3, "aaa")
        );
    }

    @Test
    void ensureEmptyComparatorForUnsorted() {
        final SortComparator<SomeClassToSort> sut = new SortComparator<>(SomeClassToSort.class, Sort.unsorted());

        final List<SomeClassToSort> list = List.of(
            new SomeClassToSort(2, "aaa"),
            new SomeClassToSort(1, "bbb")
        );

        final List<SomeClassToSort> sorted = list.stream().sorted(sut).collect(toList());

        assertThat(sorted).containsExactly(
            new SomeClassToSort(2, "aaa"),
            new SomeClassToSort(1, "bbb")
        );
    }

    @Test
    void ensureRobustHandlingForUnknownSortProperty() {
        final Sort sortAscByInteger = Sort.by("unknownAttribute");
        final SortComparator<SomeClassToSort> sut = new SortComparator<>(SomeClassToSort.class, sortAscByInteger);
        final List<SomeClassToSort> list = List.of(new SomeClassToSort(2, "aaa"), new SomeClassToSort(1, "bbb"));

        final List<SomeClassToSort> sorted = list.stream().sorted(sut).collect(toList());
        assertThat(sorted).containsExactly(
            new SomeClassToSort(2, "aaa"),
            new SomeClassToSort(1, "bbb")
        );
    }

    static class SomeClassToSort {
        private final int integer;
        private final String string;

        SomeClassToSort(int integer, String string) {
            this.integer = integer;
            this.string = string;
        }

        public int getInteger() {
            return integer;
        }

        public String getString() {
            return string;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SomeClassToSort that = (SomeClassToSort) o;
            return integer == that.integer && Objects.equals(string, that.string);
        }

        @Override
        public int hashCode() {
            return Objects.hash(integer, string);
        }
    }
}
