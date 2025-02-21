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

    static class StringBox {
        private final String value;

        StringBox(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StringBox stringBox = (StringBox) o;
            return Objects.equals(value, stringBox.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return "StringBox{" +
                "value='" + value + '\'' +
                '}';
        }
    }

    static class SomeClassToSort {
        private final int integer;
        private final String string;
        private final InnerClass innerClass;

        SomeClassToSort(int integer, String string) {
            this(integer, string, null);
        }

        SomeClassToSort(int integer, String string, InnerClass innerClass) {
            this.integer = integer;
            this.string = string;
            this.innerClass = innerClass;
        }

        public int getInteger() {
            return integer;
        }

        public String getString() {
            return string;
        }

        public InnerClass getInnerClass() {
            return innerClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SomeClassToSort that = (SomeClassToSort) o;
            return integer == that.integer && Objects.equals(string, that.string) && Objects.equals(innerClass, that.innerClass);
        }

        @Override
        public int hashCode() {
            return Objects.hash(integer, string, innerClass);
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

    static class InnerClass {
        private final BigDecimal bigDecimal;

        InnerClass(BigDecimal bigDecimal) {
            this.bigDecimal = bigDecimal;
        }

        public BigDecimal getBigDecimal() {
            return bigDecimal;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InnerClass that = (InnerClass) o;
            return Objects.equals(bigDecimal, that.bigDecimal);
        }

        @Override
        public int hashCode() {
            return Objects.hash(bigDecimal);
        }
    }
}
