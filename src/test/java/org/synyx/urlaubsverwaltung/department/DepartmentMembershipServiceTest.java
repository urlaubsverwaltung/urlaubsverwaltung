package org.synyx.urlaubsverwaltung.department;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.Role;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentMembershipServiceTest {

    private DepartmentMembershipService sut;

    @Mock
    private DepartmentMembershipRepository repository;

    private final Clock fixedClock = Clock.fixed(Instant.now(), ZoneOffset.UTC);

    @Captor
    @SuppressWarnings("unchecked")
    private final ArgumentCaptor<List<DepartmentMembershipEntity>> saveAllCaptor = ArgumentCaptor.forClass(List.class);

    @Captor
    @SuppressWarnings("unchecked")
    private final ArgumentCaptor<List<DepartmentMembershipEntity>> deleteAllCaptor = ArgumentCaptor.forClass(List.class);


    @BeforeEach
    void setUp() {
        sut = new DepartmentMembershipService(repository, fixedClock);
    }

    @Nested
    class CreateInitialMemberships {

        @Test
        void ensureNothingForEmptyDepartment() {
            sut.createInitialMemberships(1L, List.of(), List.of(), List.of());
            verifyNoInteractions(repository);
        }

        @Test
        void ensureMembersAndDepartmentHeadRelation() {

            final PersonId personId = new PersonId(1L);

            sut.createInitialMemberships(42L, List.of(personId), List.of(personId), List.of());

            verify(repository).saveAll(saveAllCaptor.capture());

            final List<DepartmentMembershipEntity> actualSaved = saveAllCaptor.getValue();
            assertThat(actualSaved).satisfiesExactly(
                entry -> {
                    assertThat(entry.getId()).isNull();
                    assertThat(entry.getPersonId()).isEqualTo(1L);
                    assertThat(entry.getDepartmentId()).isEqualTo(42L);
                    assertThat(entry.getMembershipKind()).isEqualByComparingTo(DepartmentMembershipKind.MEMBER);
                    assertThat(entry.getValidFrom()).isEqualTo(Instant.now(fixedClock));
                    assertThat(entry.getValidTo()).isNull();
                },
                entry -> {
                    assertThat(entry.getId()).isNull();
                    assertThat(entry.getPersonId()).isEqualTo(1L);
                    assertThat(entry.getDepartmentId()).isEqualTo(42L);
                    assertThat(entry.getMembershipKind()).isEqualByComparingTo(DepartmentMembershipKind.DEPARTMENT_HEAD);
                    assertThat(entry.getValidFrom()).isEqualTo(Instant.now(fixedClock));
                    assertThat(entry.getValidTo()).isNull();
                }
            );
        }

        @Test
        void ensureMembersAndSecondStageAuthorityRelation() {

            final PersonId personId = new PersonId(1L);

            sut.createInitialMemberships(42L, List.of(personId), List.of(), List.of(personId));

            verify(repository).saveAll(saveAllCaptor.capture());

            final List<DepartmentMembershipEntity> actualSaved = saveAllCaptor.getValue();
            assertThat(actualSaved).satisfiesExactly(
                entry -> {
                    assertThat(entry.getId()).isNull();
                    assertThat(entry.getPersonId()).isEqualTo(1L);
                    assertThat(entry.getDepartmentId()).isEqualTo(42L);
                    assertThat(entry.getMembershipKind()).isEqualByComparingTo(DepartmentMembershipKind.MEMBER);
                    assertThat(entry.getValidFrom()).isEqualTo(Instant.now(fixedClock));
                    assertThat(entry.getValidTo()).isNull();
                },
                entry -> {
                    assertThat(entry.getId()).isNull();
                    assertThat(entry.getPersonId()).isEqualTo(1L);
                    assertThat(entry.getDepartmentId()).isEqualTo(42L);
                    assertThat(entry.getMembershipKind()).isEqualByComparingTo(DepartmentMembershipKind.SECOND_STAGE_AUTHORITY);
                    assertThat(entry.getValidFrom()).isEqualTo(Instant.now(fixedClock));
                    assertThat(entry.getValidTo()).isNull();
                }
            );
        }
    }

    @Nested
    class UpdateDepartmentMemberships {

        @Test
        void ensureMemberNewEntry() {

            final PersonId personId = new PersonId(1L);

            when(repository.findAllByDepartmentId(42L)).thenReturn(List.of());

            final DepartmentStaff currentStaff = DepartmentStaff.empty(42L);

            sut.updateDepartmentMemberships(42L, currentStaff, List.of(personId), List.of(), List.of());

            verify(repository).saveAll(saveAllCaptor.capture());
            verify(repository, times(0)).deleteAll(anyList());

            final List<DepartmentMembershipEntity> actualSaved = saveAllCaptor.getValue();
            assertThat(actualSaved).satisfiesExactly(
                entry -> {
                    assertThat(entry.getId()).isNull();
                    assertThat(entry.getPersonId()).isEqualTo(1L);
                    assertThat(entry.getDepartmentId()).isEqualTo(42L);
                    assertThat(entry.getMembershipKind()).isEqualByComparingTo(DepartmentMembershipKind.MEMBER);
                    assertThat(entry.getValidFrom()).isEqualTo(Instant.now(fixedClock));
                    assertThat(entry.getValidTo()).isNull();
                }
            );
        }

        @Test
        void ensureMemberIgnoredBecauseOfExistingEntry() {

            final Instant now = Instant.now(fixedClock);
            final PersonId personId = new PersonId(1L);

            final DepartmentMembershipEntity currentMembershipEntity = new DepartmentMembershipEntity();
            currentMembershipEntity.setId(42L);
            currentMembershipEntity.setPersonId(1L);
            currentMembershipEntity.setDepartmentId(1L);
            currentMembershipEntity.setMembershipKind(DepartmentMembershipKind.MEMBER);
            currentMembershipEntity.setValidFrom(Instant.now(fixedClock).minus(Duration.ofDays(10)));
            currentMembershipEntity.setValidTo(null);

            when(repository.findAllByDepartmentId(1L)).thenReturn(List.of(currentMembershipEntity));

            final DepartmentMembership currentMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.MEMBER, now);
            final DepartmentStaff currentStaff = DepartmentStaff.ofMemberships(1L, List.of(currentMembership));

            sut.updateDepartmentMemberships(1L, currentStaff, List.of(personId), List.of(), List.of());

            verify(repository, times(0)).saveAll(anyList());
            verify(repository, times(0)).deleteAll(anyList());
        }

        @Test
        void ensureMemberOldEntryIsNotTouched() {

            final Instant now = Instant.now(fixedClock);
            final PersonId personId = new PersonId(1L);

            final DepartmentMembershipEntity oldMembership = new DepartmentMembershipEntity();
            oldMembership.setId(42L);
            oldMembership.setPersonId(1L);
            oldMembership.setDepartmentId(1L);
            oldMembership.setMembershipKind(DepartmentMembershipKind.MEMBER);
            oldMembership.setValidFrom(Instant.now(fixedClock).minus(Duration.ofDays(10)));
            oldMembership.setValidTo(Instant.now(fixedClock).minus(Duration.ofDays(5)));

            when(repository.findAllByDepartmentId(1L)).thenReturn(List.of(oldMembership));

            final DepartmentMembership currentMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.MEMBER, now);
            final DepartmentStaff currentStaff = DepartmentStaff.ofMemberships(1L, List.of(currentMembership));

            sut.updateDepartmentMemberships(1L, currentStaff, List.of(), List.of(), List.of());

            verify(repository, times(0)).saveAll(anyList());
            verify(repository, times(0)).deleteAll(anyList());
        }

        @Test
        void ensureMemberEntryValidToIsSetToTodayWhenMemberHasBeenRemoved() {

            final Instant now = Instant.now(fixedClock);
            final PersonId personId = new PersonId(1L);

            final Instant existingValidFrom = Instant.now(fixedClock).minus(Duration.ofDays(10));
            final DepartmentMembershipEntity existingMembership = new DepartmentMembershipEntity();
            existingMembership.setId(42L);
            existingMembership.setPersonId(1L);
            existingMembership.setDepartmentId(1L);
            existingMembership.setMembershipKind(DepartmentMembershipKind.MEMBER);
            existingMembership.setValidFrom(existingValidFrom);

            when(repository.findAllByDepartmentId(1L)).thenReturn(List.of(existingMembership));

            final DepartmentMembership currentMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.MEMBER, now);
            final DepartmentStaff currentStaff = DepartmentStaff.ofMemberships(1L, List.of(currentMembership));

            sut.updateDepartmentMemberships(1L, currentStaff, List.of(), List.of(), List.of());

            verify(repository).saveAll(saveAllCaptor.capture());
            verify(repository, times(0)).deleteAll(anyList());

            final List<DepartmentMembershipEntity> actualSaved = saveAllCaptor.getValue();
            assertThat(actualSaved).satisfiesExactly(
                entry -> {
                    assertThat(entry.getId()).isEqualTo(42L);
                    assertThat(entry.getPersonId()).isEqualTo(1L);
                    assertThat(entry.getDepartmentId()).isEqualTo(1L);
                    assertThat(entry.getMembershipKind()).isEqualByComparingTo(DepartmentMembershipKind.MEMBER);
                    assertThat(entry.getValidFrom()).isEqualTo(existingValidFrom);
                    assertThat(entry.getValidTo()).isEqualTo(Instant.now(fixedClock));
                }
            );
        }

        @Test
        void ensureMemberEntryIsDeletedWhenItHasBeenAddedTodayAndRemovedToday() {

            final Instant now = Instant.now(fixedClock);
            final PersonId personId = new PersonId(1L);

            final Instant today = Instant.now(fixedClock);
            final DepartmentMembershipEntity existingMembership = new DepartmentMembershipEntity();
            existingMembership.setId(42L);
            existingMembership.setPersonId(1L);
            existingMembership.setDepartmentId(1L);
            existingMembership.setMembershipKind(DepartmentMembershipKind.MEMBER);
            existingMembership.setValidFrom(today);

            when(repository.findAllByDepartmentId(1L)).thenReturn(List.of(existingMembership));

            final DepartmentMembership currentMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.MEMBER, now);
            final DepartmentStaff currentStaff = DepartmentStaff.ofMemberships(1L, List.of(currentMembership));

            sut.updateDepartmentMemberships(1L, currentStaff, List.of(), List.of(), List.of());

            verify(repository, times(0)).saveAll(anyList());
            verify(repository).deleteAll(deleteAllCaptor.capture());

            final List<DepartmentMembershipEntity> actualDeleted = deleteAllCaptor.getValue();
            assertThat(actualDeleted).satisfiesExactly(
                entry -> {
                    assertThat(entry.getId()).isEqualTo(42L);
                    assertThat(entry.getMembershipKind()).isEqualTo(DepartmentMembershipKind.MEMBER);
                }
            );
        }

        @Test
        void ensureDepartmentHeadNewEntry() {

             final PersonId personId = new PersonId(1L);

            when(repository.findAllByDepartmentId(1L)).thenReturn(List.of());

            final DepartmentStaff currentStaff = DepartmentStaff.empty(1L);

            sut.updateDepartmentMemberships(1L, currentStaff, List.of(), List.of(personId), List.of());

            verify(repository).saveAll(saveAllCaptor.capture());
            verify(repository, times(0)).deleteAll(anyList());

            final List<DepartmentMembershipEntity> actualSaved = saveAllCaptor.getValue();
            assertThat(actualSaved).satisfiesExactly(
                entry -> {
                    assertThat(entry.getId()).isNull();
                    assertThat(entry.getPersonId()).isEqualTo(1L);
                    assertThat(entry.getDepartmentId()).isEqualTo(1L);
                    assertThat(entry.getMembershipKind()).isEqualByComparingTo(DepartmentMembershipKind.DEPARTMENT_HEAD);
                    assertThat(entry.getValidFrom()).isEqualTo(Instant.now(fixedClock));
                    assertThat(entry.getValidTo()).isNull();
                }
            );
        }

        @Test
        void ensureDepartmentHeadEntryValidToIsSetToTodayWhenMemberHasBeenRemoved() {

            final Instant now = Instant.now(fixedClock);
            final PersonId personId = new PersonId(1L);

            final Instant existingValidFrom = Instant.now(fixedClock).minus(Duration.ofDays(10));
            final DepartmentMembershipEntity existingMembership = new DepartmentMembershipEntity();
            existingMembership.setId(42L);
            existingMembership.setPersonId(1L);
            existingMembership.setDepartmentId(1L);
            existingMembership.setMembershipKind(DepartmentMembershipKind.DEPARTMENT_HEAD);
            existingMembership.setValidFrom(existingValidFrom);

            when(repository.findAllByDepartmentId(1L)).thenReturn(List.of(existingMembership));

            final DepartmentMembership currentMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, now);
            final DepartmentStaff currentStaff = DepartmentStaff.ofMemberships(1L, List.of(currentMembership));

            sut.updateDepartmentMemberships(1L, currentStaff, List.of(), List.of(), List.of());

            verify(repository).saveAll(saveAllCaptor.capture());
            verify(repository, times(0)).deleteAll(anyList());

            final List<DepartmentMembershipEntity> actualSaved = saveAllCaptor.getValue();
            assertThat(actualSaved).satisfiesExactly(
                entry -> {
                    assertThat(entry.getId()).isEqualTo(42L);
                    assertThat(entry.getPersonId()).isEqualTo(1L);
                    assertThat(entry.getDepartmentId()).isEqualTo(1L);
                    assertThat(entry.getMembershipKind()).isEqualByComparingTo(DepartmentMembershipKind.DEPARTMENT_HEAD);
                    assertThat(entry.getValidFrom()).isEqualTo(existingValidFrom);
                    assertThat(entry.getValidTo()).isEqualTo(Instant.now(fixedClock));
                }
            );
        }

        @Test
        void ensureDepartmentHeadEntryIsDeletedWhenItHasBeenAddedTodayAndRemovedToday() {

            final Instant now = Instant.now(fixedClock);
            final PersonId personId = new PersonId(1L);

            final DepartmentMembershipEntity existingMembership = new DepartmentMembershipEntity();
            existingMembership.setId(21L);
            existingMembership.setPersonId(1L);
            existingMembership.setDepartmentId(1L);
            existingMembership.setMembershipKind(DepartmentMembershipKind.MEMBER);
            existingMembership.setValidFrom(now);

            final DepartmentMembershipEntity existingDepartmentHeadMembership = new DepartmentMembershipEntity();
            existingDepartmentHeadMembership.setId(42L);
            existingDepartmentHeadMembership.setPersonId(1L);
            existingDepartmentHeadMembership.setDepartmentId(1L);
            existingDepartmentHeadMembership.setMembershipKind(DepartmentMembershipKind.DEPARTMENT_HEAD);
            existingDepartmentHeadMembership.setValidFrom(now);

            when(repository.findAllByDepartmentId(1L)).thenReturn(List.of(existingMembership, existingDepartmentHeadMembership));

            final DepartmentMembership currentMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.MEMBER, now);
            final DepartmentMembership currentMembershipHead = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.DEPARTMENT_HEAD, now);
            final DepartmentStaff currentStaff = DepartmentStaff.ofMemberships(1L, List.of(currentMembership, currentMembershipHead));

            sut.updateDepartmentMemberships(1L, currentStaff, List.of(personId), List.of(), List.of());

            verify(repository, times(0)).saveAll(anyList());
            verify(repository).deleteAll(deleteAllCaptor.capture());

            final List<DepartmentMembershipEntity> actualDeleted = deleteAllCaptor.getValue();
            assertThat(actualDeleted).satisfiesExactly(
                entry -> {
                    assertThat(entry.getId()).isEqualTo(42L);
                    assertThat(entry.getMembershipKind()).isEqualTo(DepartmentMembershipKind.DEPARTMENT_HEAD);
                }
            );
        }

        @Test
        void ensureSecondStageAuthorityNewEntry() {

            final PersonId personId = new PersonId(1L);

            when(repository.findAllByDepartmentId(1L)).thenReturn(List.of());

            final DepartmentStaff currentStaff = DepartmentStaff.empty(1L);

            sut.updateDepartmentMemberships(1L, currentStaff, List.of(), List.of(), List.of(personId));

            verify(repository).saveAll(saveAllCaptor.capture());
            verify(repository, times(0)).deleteAll(anyList());

            final List<DepartmentMembershipEntity> actualSaved = saveAllCaptor.getValue();
            assertThat(actualSaved).satisfiesExactly(
                entry -> {
                    assertThat(entry.getId()).isNull();
                    assertThat(entry.getPersonId()).isEqualTo(1L);
                    assertThat(entry.getDepartmentId()).isEqualTo(1L);
                    assertThat(entry.getMembershipKind()).isEqualByComparingTo(DepartmentMembershipKind.SECOND_STAGE_AUTHORITY);
                    assertThat(entry.getValidFrom()).isEqualTo(Instant.now(fixedClock));
                    assertThat(entry.getValidTo()).isNull();
                }
            );
        }

        @Test
        void ensureSecondStageAuthorityEntryValidToIsSetToTodayWhenMemberHasBeenRemoved() {

            final Instant now = Instant.now(fixedClock);
            final PersonId personId = new PersonId(1L);

            final Instant existingValidFrom = Instant.now(fixedClock).minus(Duration.ofDays(10));
            final DepartmentMembershipEntity existingMembership = new DepartmentMembershipEntity();
            existingMembership.setId(42L);
            existingMembership.setPersonId(1L);
            existingMembership.setDepartmentId(1L);
            existingMembership.setMembershipKind(DepartmentMembershipKind.SECOND_STAGE_AUTHORITY);
            existingMembership.setValidFrom(existingValidFrom);

            when(repository.findAllByDepartmentId(1L)).thenReturn(List.of(existingMembership));

            final DepartmentMembership currentMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, now);
            final DepartmentStaff currentStaff = DepartmentStaff.ofMemberships(1L, List.of(currentMembership));

            sut.updateDepartmentMemberships(1L, currentStaff, List.of(), List.of(), List.of());

            verify(repository).saveAll(saveAllCaptor.capture());
            verify(repository, times(0)).deleteAll(anyList());

            final List<DepartmentMembershipEntity> actualSaved = saveAllCaptor.getValue();
            assertThat(actualSaved).satisfiesExactly(
                entry -> {
                    assertThat(entry.getId()).isEqualTo(42L);
                    assertThat(entry.getPersonId()).isEqualTo(1L);
                    assertThat(entry.getDepartmentId()).isEqualTo(1L);
                    assertThat(entry.getMembershipKind()).isEqualByComparingTo(DepartmentMembershipKind.SECOND_STAGE_AUTHORITY);
                    assertThat(entry.getValidFrom()).isEqualTo(existingValidFrom);
                    assertThat(entry.getValidTo()).isEqualTo(Instant.now(fixedClock));
                }
            );
        }

        @Test
        void ensureSecondStageAuthorityEntryIsDeletedWhenItHasBeenAddedTodayAndRemovedToday() {

            final Instant now = Instant.now(fixedClock);
            final PersonId personId = new PersonId(1L);

            final DepartmentMembershipEntity existingMembership = new DepartmentMembershipEntity();
            existingMembership.setId(21L);
            existingMembership.setPersonId(1L);
            existingMembership.setDepartmentId(1L);
            existingMembership.setMembershipKind(DepartmentMembershipKind.MEMBER);
            existingMembership.setValidFrom(now);

            final DepartmentMembershipEntity existingSecondStageMembership = new DepartmentMembershipEntity();
            existingSecondStageMembership.setId(42L);
            existingSecondStageMembership.setPersonId(1L);
            existingSecondStageMembership.setDepartmentId(1L);
            existingSecondStageMembership.setMembershipKind(DepartmentMembershipKind.SECOND_STAGE_AUTHORITY);
            existingSecondStageMembership.setValidFrom(now);

            when(repository.findAllByDepartmentId(1L)).thenReturn(List.of(existingMembership, existingSecondStageMembership));

            final DepartmentMembership currentMembership = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.MEMBER, now);
            final DepartmentMembership currentMembershipSecondStage = new DepartmentMembership(personId, 1L, DepartmentMembershipKind.SECOND_STAGE_AUTHORITY, now);
            final DepartmentStaff currentStaff = DepartmentStaff.ofMemberships(1L, List.of(currentMembership, currentMembershipSecondStage));

            sut.updateDepartmentMemberships(1L, currentStaff, List.of(personId), List.of(), List.of());

            verify(repository, times(0)).saveAll(anyList());
            verify(repository).deleteAll(deleteAllCaptor.capture());

            final List<DepartmentMembershipEntity> actualDeleted = deleteAllCaptor.getValue();
            assertThat(actualDeleted).satisfiesExactly(
                entry -> {
                    assertThat(entry.getId()).isEqualTo(42L);
                    assertThat(entry.getMembershipKind()).isEqualTo(DepartmentMembershipKind.SECOND_STAGE_AUTHORITY);
                }
            );
        }

        @Test
        void ensureMemberAndDepartmentHeadNewEntry() {

            final PersonId personId = new PersonId(1L);

            when(repository.findAllByDepartmentId(1L)).thenReturn(List.of());

            final DepartmentStaff currentStaff = DepartmentStaff.empty(1L);

            sut.updateDepartmentMemberships(1L, currentStaff, List.of(personId), List.of(personId), List.of());

            verify(repository).saveAll(saveAllCaptor.capture());
            verify(repository, times(0)).deleteAll(anyList());

            final List<DepartmentMembershipEntity> actualSaved = saveAllCaptor.getValue();
            assertThat(actualSaved).satisfiesExactly(
                entry -> {
                    assertThat(entry.getId()).isNull();
                    assertThat(entry.getPersonId()).isEqualTo(1L);
                    assertThat(entry.getDepartmentId()).isEqualTo(1L);
                    assertThat(entry.getMembershipKind()).isEqualByComparingTo(DepartmentMembershipKind.MEMBER);
                    assertThat(entry.getValidFrom()).isEqualTo(Instant.now(fixedClock));
                    assertThat(entry.getValidTo()).isNull();
                },
                entry -> {
                    assertThat(entry.getId()).isNull();
                    assertThat(entry.getPersonId()).isEqualTo(1L);
                    assertThat(entry.getDepartmentId()).isEqualTo(1L);
                    assertThat(entry.getMembershipKind()).isEqualByComparingTo(DepartmentMembershipKind.DEPARTMENT_HEAD);
                    assertThat(entry.getValidFrom()).isEqualTo(Instant.now(fixedClock));
                    assertThat(entry.getValidTo()).isNull();
                }
            );
        }

        @Test
        void ensureMemberAndSecondStageAuthorityNewEntry() {

            final PersonId personId = new PersonId(1L);

            final Person person = new Person();
            person.setId(1L);
            person.setPermissions(List.of(Role.USER, Role.SECOND_STAGE_AUTHORITY));

            final Department newDepartment = anyDepartment();
            newDepartment.setId(1L);
            newDepartment.setMembers(List.of(person));
            newDepartment.setSecondStageAuthorities(List.of(person));

            final Department currentDepartment = anyDepartment();
            currentDepartment.setId(1L);
            currentDepartment.setMembers(List.of());
            currentDepartment.setSecondStageAuthorities(List.of());

            when(repository.findAllByDepartmentId(1L)).thenReturn(List.of());

            final DepartmentStaff currentStaff = DepartmentStaff.empty(1L);

            sut.updateDepartmentMemberships(1L, currentStaff, List.of(personId), List.of(), List.of(personId));

            verify(repository).saveAll(saveAllCaptor.capture());
            verify(repository, times(0)).deleteAll(anyList());

            final List<DepartmentMembershipEntity> actualSaved = saveAllCaptor.getValue();
            assertThat(actualSaved).satisfiesExactly(
                entry -> {
                    assertThat(entry.getId()).isNull();
                    assertThat(entry.getPersonId()).isEqualTo(1L);
                    assertThat(entry.getDepartmentId()).isEqualTo(1L);
                    assertThat(entry.getMembershipKind()).isEqualByComparingTo(DepartmentMembershipKind.MEMBER);
                    assertThat(entry.getValidFrom()).isEqualTo(Instant.now(fixedClock));
                    assertThat(entry.getValidTo()).isNull();
                },
                entry -> {
                    assertThat(entry.getId()).isNull();
                    assertThat(entry.getPersonId()).isEqualTo(1L);
                    assertThat(entry.getDepartmentId()).isEqualTo(1L);
                    assertThat(entry.getMembershipKind()).isEqualByComparingTo(DepartmentMembershipKind.SECOND_STAGE_AUTHORITY);
                    assertThat(entry.getValidFrom()).isEqualTo(Instant.now(fixedClock));
                    assertThat(entry.getValidTo()).isNull();
                }
            );
        }
    }

    private static Department anyDepartment() {
        final Department department = new Department();
        department.setId(1L);
        department.setMembers(List.of());
        department.setSecondStageAuthorities(List.of());
        department.setDepartmentHeads(List.of());
        return department;
    }
}
