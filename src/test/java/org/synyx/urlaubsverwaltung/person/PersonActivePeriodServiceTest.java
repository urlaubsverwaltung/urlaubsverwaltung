package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonActivePeriodServiceTest {

    private PersonActivePeriodServiceImpl sut;

    @Mock
    private PersonActivePeriodRepository repository;

    @Captor
    private ArgumentCaptor<PersonActivePeriodEntity> entityCaptor;

    @BeforeEach
    void setUp() {
        sut = new PersonActivePeriodServiceImpl(repository);
    }

    @Nested
    class GetActivePeriods {

        @Test
        void ensureReturnsAllActivePeriodsOfPerson() {

            final Instant validFrom = Instant.now().minus(Duration.ofDays(10));
            final Instant validTo = Instant.now().minus(Duration.ofDays(5));

            final PersonActivePeriodEntity entity = new PersonActivePeriodEntity();
            entity.setPersonId(1L);
            entity.setValidFrom(validFrom);
            entity.setValidTo(validTo);

            when(repository.findAllByPersonIdOrderByValidFromAsc(1L)).thenReturn(List.of(entity));

            final List<PersonActivePeriod> actual = sut.getActivePeriods(new PersonId(1L));
            assertThat(actual).containsExactly(new PersonActivePeriod(new PersonId(1L), validFrom, Optional.of(validTo)));
        }
    }

    @Nested
    class GetAllActivePeriods {

        @Test
        void ensureReturnsAllActivePeriodsOfAllPersons() {

            final Instant validFrom = Instant.now().minus(Duration.ofDays(10));
            final Instant validTo = Instant.now().minus(Duration.ofDays(5));

            final PersonActivePeriodEntity entityOne = new PersonActivePeriodEntity();
            entityOne.setPersonId(1L);
            entityOne.setValidFrom(validFrom);
            entityOne.setValidTo(validTo);

            final PersonActivePeriodEntity entityTwo = new PersonActivePeriodEntity();
            entityTwo.setPersonId(2L);
            entityTwo.setValidFrom(validFrom);

            when(repository.findAll()).thenReturn(List.of(entityOne, entityTwo));

            final List<PersonActivePeriod> actual = sut.getAllActivePeriods();
            assertThat(actual).containsExactlyInAnyOrder(
                new PersonActivePeriod(new PersonId(1L), validFrom, Optional.of(validTo)),
                new PersonActivePeriod(new PersonId(2L), validFrom)
            );
        }
    }

    @Nested
    class GetActivePeriodsOverlapping {

        @Test
        void ensureReturnsOverlappingActivePeriodsGroupedByPerson() {

            final Instant from = Instant.now().minus(Duration.ofDays(30));
            final Instant to = Instant.now();
            final Instant validFrom = Instant.now().minus(Duration.ofDays(20));

            final PersonActivePeriodEntity entity = new PersonActivePeriodEntity();
            entity.setPersonId(1L);
            entity.setValidFrom(validFrom);
            entity.setValidTo(null);

            when(repository.findAllByPersonIdIsInAndOverlapping(List.of(1L, 2L), from, to)).thenReturn(List.of(entity));

            final Map<PersonId, List<PersonActivePeriod>> actual =
                sut.getActivePeriodsOverlapping(List.of(new PersonId(1L), new PersonId(2L)), from, to);

            assertThat(actual).containsOnlyKeys(new PersonId(1L), new PersonId(2L));
            assertThat(actual.get(new PersonId(1L))).containsExactly(new PersonActivePeriod(new PersonId(1L), validFrom));
            assertThat(actual.get(new PersonId(2L))).isEmpty();
        }
    }

    @Nested
    class OpenPeriod {

        @Test
        void ensureOpensNewPeriodWhenNoneIsOpen() {

            final Instant validFrom = Instant.now();

            when(repository.findByPersonIdAndValidToIsNull(1L)).thenReturn(Optional.empty());

            sut.openPeriod(new PersonId(1L), validFrom);

            verify(repository).save(entityCaptor.capture());
            final PersonActivePeriodEntity saved = entityCaptor.getValue();
            assertThat(saved.getPersonId()).isEqualTo(1L);
            assertThat(saved.getValidFrom()).isEqualTo(validFrom);
            assertThat(saved.getValidTo()).isNull();
        }

        @Test
        void ensureThrowsWhenOneIsAlreadyOpen() {

            final PersonActivePeriodEntity existingOpenPeriod = new PersonActivePeriodEntity();
            existingOpenPeriod.setPersonId(1L);
            existingOpenPeriod.setValidFrom(Instant.now().minus(Duration.ofDays(1)));

            when(repository.findByPersonIdAndValidToIsNull(1L)).thenReturn(Optional.of(existingOpenPeriod));

            assertThatThrownBy(() -> sut.openPeriod(new PersonId(1L), Instant.now()))
                .isInstanceOf(PersonActivePeriodInconsistentStateException.class);

            verify(repository, never()).save(any());
        }
    }

    @Nested
    class CloseOpenPeriod {

        @Test
        void ensureClosesTheOpenPeriod() {

            final PersonActivePeriodEntity openPeriod = new PersonActivePeriodEntity();
            openPeriod.setPersonId(1L);
            openPeriod.setValidFrom(Instant.now().minus(Duration.ofDays(1)));

            final Instant validTo = Instant.now();

            when(repository.findByPersonIdAndValidToIsNull(1L)).thenReturn(Optional.of(openPeriod));

            sut.closeOpenPeriod(new PersonId(1L), validTo);

            verify(repository).save(entityCaptor.capture());
            assertThat(entityCaptor.getValue().getValidTo()).isEqualTo(validTo);
        }

        @Test
        void ensureThrowsWhenNoOpenPeriodExists() {

            when(repository.findByPersonIdAndValidToIsNull(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.closeOpenPeriod(new PersonId(1L), Instant.now()))
                .isInstanceOf(PersonActivePeriodInconsistentStateException.class);

            verify(repository, never()).save(any());
        }
    }
}
