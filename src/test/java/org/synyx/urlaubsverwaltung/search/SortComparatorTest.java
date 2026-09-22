package org.synyx.urlaubsverwaltung.search;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class SortComparatorTest {

    @Test
    void ensureSortingAsc() {
        final Sort sort = Sort.by(Sort.Direction.ASC, "integer").and(Sort.by(Sort.Direction.ASC, "string"));
        final SortComparator<SomeClassToSort> sut = new SortComparator<>(SomeClassToSort.class, sort);

        final List<SomeClassToSort> list = List.of(
            new SomeClassToSort(2, "aaa"),
            new SomeClassToSort(1, "AAA"),
            new SomeClassToSort(3, "bbb"),
            new SomeClassToSort(3, "aaa")
        );

        final List<SomeClassToSort> sorted = list.stream().sorted(sut).toList();

        assertThat(sorted).containsExactly(
            new SomeClassToSort(1, "AAA"),
            new SomeClassToSort(2, "aaa"),
            new SomeClassToSort(3, "aaa"),
            new SomeClassToSort(3, "bbb")
        );
    }

    @Test
    void ensureSortingAscByStringIgnoresCase() {
        final Sort sort = Sort.by("value");
        final SortComparator<StringBox> sut = new SortComparator<>(StringBox.class, sort);

        final List<StringBox> list = List.of(
            new StringBox("Bernhard"),
            new StringBox("anne Schneider"),
            new StringBox("ANne Roth"),
            new StringBox("Anne Schmidt")
        );

        final List<StringBox> actual = list.stream().sorted(sut).toList();

        assertThat(actual).containsExactly(
            new StringBox("ANne Roth"),
            new StringBox("Anne Schmidt"),
            new StringBox("anne Schneider"),
            new StringBox("Bernhard")
        );
    }

    @Test
    void ensureSortingDesc() {
        final Sort sort = Sort.by(Sort.Direction.ASC, "integer").and(Sort.by(Sort.Direction.DESC, "string"));
        final SortComparator<SomeClassToSort> sut = new SortComparator<>(SomeClassToSort.class, sort);

        final List<SomeClassToSort> list = List.of(
            new SomeClassToSort(2, "aaa"),
            new SomeClassToSort(1, "AAA"),
            new SomeClassToSort(3, "bbb"),
            new SomeClassToSort(3, "aaa")
        );

        final List<SomeClassToSort> sorted = list.stream().sorted(sut).toList();

        assertThat(sorted).containsExactly(
            new SomeClassToSort(1, "AAA"),
            new SomeClassToSort(2, "aaa"),
            new SomeClassToSort(3, "bbb"),
            new SomeClassToSort(3, "aaa")
        );
    }

    @Test
    void ensureSortingAscWithNestedProperties() {
        final Sort sort = Sort.by(Sort.Direction.ASC, "innerClass.bigDecimal");
        final SortComparator<SomeClassToSort> sut = new SortComparator<>(SomeClassToSort.class, sort);

        final List<SomeClassToSort> list = List.of(
            new SomeClassToSort(2, "aaa", new InnerClass(BigDecimal.valueOf(2))),
            new SomeClassToSort(1, "AAA", new InnerClass(BigDecimal.valueOf(1))),
            new SomeClassToSort(3, "bbb", new InnerClass(BigDecimal.valueOf(1.5))),
            new SomeClassToSort(3, "aaa", new InnerClass(BigDecimal.valueOf(3)))
        );

        final List<SomeClassToSort> sorted = list.stream().sorted(sut).toList();

        assertThat(sorted).containsExactly(
            new SomeClassToSort(1, "AAA", new InnerClass(BigDecimal.valueOf(1))),
            new SomeClassToSort(3, "bbb", new InnerClass(BigDecimal.valueOf(1.5))),
            new SomeClassToSort(2, "aaa", new InnerClass(BigDecimal.valueOf(2))),
            new SomeClassToSort(3, "aaa", new InnerClass(BigDecimal.valueOf(3)))
        );
    }

    @Test
    void ensureSortingDescWithNestedProperties() {
        final Sort sort = Sort.by(Sort.Direction.DESC, "innerClass.bigDecimal");
        final SortComparator<SomeClassToSort> sut = new SortComparator<>(SomeClassToSort.class, sort);

        final List<SomeClassToSort> list = List.of(
            new SomeClassToSort(2, "aaa", new InnerClass(BigDecimal.valueOf(2))),
            new SomeClassToSort(1, "aaa", new InnerClass(BigDecimal.valueOf(1))),
            new SomeClassToSort(3, "bbb", new InnerClass(BigDecimal.valueOf(1.5))),
            new SomeClassToSort(3, "aaa", new InnerClass(BigDecimal.valueOf(3)))
        );

        final List<SomeClassToSort> sorted = list.stream().sorted(sut).toList();

        assertThat(sorted).containsExactly(
            new SomeClassToSort(3, "aaa", new InnerClass(BigDecimal.valueOf(3))),
            new SomeClassToSort(2, "aaa", new InnerClass(BigDecimal.valueOf(2))),
            new SomeClassToSort(3, "bbb", new InnerClass(BigDecimal.valueOf(1.5))),
            new SomeClassToSort(1, "aaa", new InnerClass(BigDecimal.valueOf(1)))
        );
    }

    @Test
    void ensureEmptyComparatorForUnsorted() {
        final SortComparator<SomeClassToSort> sut = new SortComparator<>(SomeClassToSort.class, Sort.unsorted());

        final List<SomeClassToSort> list = List.of(
            new SomeClassToSort(2, "aaa"),
            new SomeClassToSort(1, "bbb"),
            new SomeClassToSort(3, "AAA")
        );

        final List<SomeClassToSort> sorted = list.stream().sorted(sut).toList();

        assertThat(sorted).containsExactly(
            new SomeClassToSort(2, "aaa"),
            new SomeClassToSort(1, "bbb"),
            new SomeClassToSort(3, "AAA")
        );
    }

    @Test
    void ensureRobustHandlingForUnknownSortProperty() {
        final Sort sortAscByInteger = Sort.by("unknownAttribute");
        final SortComparator<SomeClassToSort> sut = new SortComparator<>(SomeClassToSort.class, sortAscByInteger);
        final List<SomeClassToSort> list = List.of(
            new SomeClassToSort(2, "aaa"),
            new SomeClassToSort(1, "bbb"),
            new SomeClassToSort(3, "AAA")
        );

        final List<SomeClassToSort> sorted = list.stream().sorted(sut).toList();
        assertThat(sorted).containsExactly(
            new SomeClassToSort(2, "aaa"),
            new SomeClassToSort(1, "bbb"),
            new SomeClassToSort(3, "AAA")
        );
    }

    @Test
    void ensureNullValuesAreBasedAtTheEnd() {
        final Sort sort = Sort.by(Sort.Direction.DESC, "string");
        final SortComparator<SomeClassToSort> sut = new SortComparator<>(SomeClassToSort.class, sort);
        final List<SomeClassToSort> list = List.of(
            new SomeClassToSort(2, null),
            new SomeClassToSort(1, "bbb"),
            new SomeClassToSort(3, "BBB")
        );

        final List<SomeClassToSort> sorted = list.stream().sorted(sut).toList();
        assertThat(sorted).containsExactly(
            new SomeClassToSort(1, "bbb"),
            new SomeClassToSort(3, "BBB"),
            new SomeClassToSort(2, null)
        );
    }

    record StringBox(String value) {

        @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                StringBox stringBox = (StringBox) o;
                return Objects.equals(value, stringBox.value);
            }

        @Override
            public String toString() {
                return "StringBox{" +
                    "value='" + value + '\'' +
                    '}';
            }
        }

    record SomeClassToSort(int integer, String string, InnerClass innerClass) {
            SomeClassToSort(int integer, String string) {
                this(integer, string, null);
            }

        @Override
            public String toString() {
                return "SomeClassToSort{" +
                    "integer=" + integer +
                    ", string='" + string + '\'' +
                    ", innerClass=" + innerClass +
                    '}';
            }
        }

    record InnerClass(BigDecimal bigDecimal) {

        @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                InnerClass that = (InnerClass) o;
                return Objects.equals(bigDecimal, that.bigDecimal);
            }

    }
}
