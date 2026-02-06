package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.synyx.urlaubsverwaltung.person.PersonPageRequest.PersonPageRequestUnpaged;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PersonPageRequestTest {

    @Nested
    class OfApiPageable {

        @ParameterizedTest
        @ValueSource(strings = {"person.firstName", "person.lastName"})
        void ensurePersonPropertyMapping(String given) {
            final Sort sort = Sort.by(given);
            final Pageable pageable = PageRequest.of(5, 20, sort);

            final PersonPageRequest actual = PersonPageRequest.ofApiPageable(pageable);

            assertThat(actual).isNotNull();
            assertThat(actual.isPaged()).isTrue();
            assertThat(actual.getPageNumber()).isEqualTo(5);
            assertThat(actual.getPageSize()).isEqualTo(20);
            assertThat(actual.getSort()).isNotNull();
            assertThat(actual.getSort().isSorted()).isTrue();
        }

        @Test
        void ensureAscendingOrder() {
            final Sort sort = Sort.by(Order.asc("person.firstName"));
            final Pageable pageable = PageRequest.of(0, 10, sort);

            final PersonPageRequest actual = PersonPageRequest.ofApiPageable(pageable);

            final Order actualOrder = actual.getSort().iterator().next();
            assertThat(actualOrder.isAscending()).isTrue();
        }

        @Test
        void ensureDescendingOrder() {
            final Sort sort = Sort.by(Order.desc("person.firstName"));
            final Pageable pageable = PageRequest.of(0, 10, sort);

            final PersonPageRequest actual = PersonPageRequest.ofApiPageable(pageable);

            final Order actualOrder = actual.getSort().iterator().next();
            assertThat(actualOrder.isDescending()).isTrue();
        }

        @Test
        void ensureCombinedOrder() {
            final Sort sort = Sort.by(
                Order.asc("person.firstName"),
                Order.desc("person.lastName")
            );
            final Pageable pageable = PageRequest.of(0, 10, sort);

            final PersonPageRequest actual = PersonPageRequest.ofApiPageable(pageable);

            assertThat(actual).isNotNull();
            assertThat(actual.isPaged()).isTrue();
            assertThat(actual.getSort()).isNotNull();
            assertThat(actual.getSort().isSorted()).isTrue();

            final List<Order> actualOrders = actual.getSort().toList();
            assertThat(actualOrders).hasSize(2);
            assertThat(actualOrders.get(0).isAscending()).isTrue();
            assertThat(actualOrders.get(1).isDescending()).isTrue();
        }

        @Test
        void ensureUnpaged() {
            final Pageable pageable = PageRequest.of(0, 10);

            final PersonPageRequest actual = PersonPageRequest.ofApiPageable(pageable);

            assertThat(actual).isNotNull();
            assertThat(actual.isPaged()).isFalse();
            assertThat(actual).isInstanceOf(PersonPageRequestUnpaged.class);
        }

        @Test
        void ensureUnpagedForUnsorted() {
            final Sort unsorted = Sort.unsorted();
            final Pageable pageable = PageRequest.of(0, 10, unsorted);

            final PersonPageRequest actual = PersonPageRequest.ofApiPageable(pageable);

            assertThat(actual).isNotNull();
            assertThat(actual.isPaged()).isFalse();
            assertThat(actual).isInstanceOf(PersonPageRequestUnpaged.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"person.invalid", "someOtherField"})
        void ensureUnpagedForUnknownProperties(String unknownProperty) {
            final Sort sort = Sort.by(unknownProperty);
            final Pageable pageable = PageRequest.of(0, 10, sort);

            final PersonPageRequest result = PersonPageRequest.ofApiPageable(pageable);

            // Invalid property is ignored, so result should be unpaged
            assertThat(result).isNotNull();
            assertThat(result.isPaged()).isFalse();
            assertThat(result).isInstanceOf(PersonPageRequestUnpaged.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"person.invalid", "someOtherField"})
        void ensureUnknownPropertiesAreIgnored(String unknownProperty) {
            final Sort sort = Sort.by(
                Order.asc("person.firstName"),
                Order.desc(unknownProperty)
            );
            final Pageable pageable = PageRequest.of(0, 10, sort);

            final PersonPageRequest actual = PersonPageRequest.ofApiPageable(pageable);

            // Should have valid sort and be paged
            assertThat(actual).isNotNull();
            assertThat(actual.isPaged()).isTrue();
            assertThat(actual.getSort()).isNotNull();
            assertThat(actual.getSort().isSorted()).isTrue();

            // Should only have one sort order (the person-prefixed one)
            final List<Order> actualOrders = actual.getSort().toList();
            assertThat(actualOrders).hasSize(1);
            assertThat(actualOrders.getFirst().isAscending()).isTrue();
        }
    }
}
