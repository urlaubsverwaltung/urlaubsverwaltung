package org.synyx.urlaubsverwaltung.department;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.PersonId;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.groupingBy;
import static org.synyx.urlaubsverwaltung.department.DepartmentMembershipKind.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.department.DepartmentMembershipKind.MEMBER;
import static org.synyx.urlaubsverwaltung.department.DepartmentMembershipKind.SECOND_STAGE_AUTHORITY;

@Service
class DepartmentMembershipService {

    private final DepartmentMembershipRepository repository;
    private final Clock clock;

    DepartmentMembershipService(DepartmentMembershipRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /**
     * Creates the initial memberships for a department based on the provided members, department heads, and second stage authorities.
     * This method is typically used when a new department is created or when the initial membership history
     * needs to be established for an existing department.
     *
     * @param departmentId the ID of the department for which to create the memberships
     * @param memberIds the list of members to be added to the department
     * @param departmentHeadIds the list of department heads to be added to the department
     * @param secondStageAuthorityIds the list of second stage authorities to be added to the department
     * @return a list of {@link DepartmentMembership} representing the initial memberships
     */
    DepartmentMembershipBucket createInitialMemberships(
        Long departmentId,
        List<PersonId> memberIds,
        List<PersonId> departmentHeadIds,
        List<PersonId> secondStageAuthorityIds
    ) {

        final Instant now = Instant.now(clock);

        final List<DepartmentMembershipEntity> membershipEntities = new ArrayList<>();

        for (PersonId personId : memberIds) {
            membershipEntities.add(newMembershipEntity(departmentId, personId, MEMBER, now));
        }

        for (PersonId personId : departmentHeadIds) {
            membershipEntities.add(newMembershipEntity(departmentId, personId, DEPARTMENT_HEAD, now));
        }

        for (PersonId personId : secondStageAuthorityIds) {
            membershipEntities.add(newMembershipEntity(departmentId, personId, SECOND_STAGE_AUTHORITY, now));
        }

        return saveAll(departmentId, membershipEntities);
    }

    /**
     * Updates the memberships of a department based on the new membership bucket and the current membership bucket.
     *
     * @param newMembershipBucket the new membership bucket containing the updated membership information for the department
     * @param currentMembershipBucket the current membership bucket containing the existing membership information for the department
     * @return a {@link DepartmentMembershipBucket} containing the updated membership information
     */
    DepartmentMembershipBucket updateDepartmentMemberships(DepartmentMembershipBucket newMembershipBucket, DepartmentMembershipBucket currentMembershipBucket) {

        final Long departmentId = newMembershipBucket.departmentId();

        final Instant now = Instant.now(clock);
        final MemberIdsDiff memberIdsDiff = getMemberIdsDiff(newMembershipBucket, currentMembershipBucket);

        // updates existing memberships when validTo is null nor today
        // TODO use a Map instead of list with filtering
        final List<UpdateOrDelete> updatedOrDeleteMemberships = updateActiveMemberships(memberIdsDiff, departmentId, now);
        final List<DepartmentMembershipEntity> toDelete = updatedOrDeleteMemberships.stream().filter(UpdateOrDelete::delete).map(UpdateOrDelete::membershipEntity).toList();
        final List<DepartmentMembershipEntity> toUpdate = updatedOrDeleteMemberships.stream().filter(UpdateOrDelete::update).map(UpdateOrDelete::membershipEntity).toList();

        // new membership entities for members that are not yet in the history with a validTo eq null
        final List<DepartmentMembershipEntity> newMemberships = createNewMemberships(memberIdsDiff, departmentId, now);

        if (!toDelete.isEmpty()) {
            repository.deleteAll(toDelete);
        }

        final List<DepartmentMembershipEntity> toSave = Stream.concat(toUpdate.stream(), newMemberships.stream()).toList();
        return saveAll(departmentId, toSave);
    }

    DepartmentMembershipBucket getDepartmentMembershipBucket(Long departmentId) {
        return getDepartmentMembershipBucket(List.of(departmentId)).getOrDefault(departmentId, DepartmentMembershipBucket.empty(departmentId));
    }

    /**
     * Returns the current memberships for the given department IDs.
     * A membership is considered current if it has a validTo of null.
     *
     * @param departmentIds the IDs of the departments for which to retrieve current memberships
     * @return a map where the key is the department ID and the value is a list of current memberships for that department
     */
    Map<Long, DepartmentMembershipBucket> getDepartmentMembershipBucket(Collection<Long> departmentIds) {

        final List<DepartmentMembershipEntity> currentMemberships =
            repository.findAllByDepartmentIdIsInAndValidToIsNull(departmentIds);

        final Map<Long, List<DepartmentMembership>> membershipsByDepartmentId = currentMemberships.stream()
            .map(DepartmentMembershipService::toDepartmentMembership)
            .collect(groupingBy(DepartmentMembership::departmentId));

        final Map<Long, DepartmentMembershipBucket> buckets = new HashMap<>();

        for (Long departmentId : departmentIds) {
            final List<DepartmentMembership> memberships = membershipsByDepartmentId.getOrDefault(departmentId, List.of());
            buckets.put(departmentId, DepartmentMembershipBucket.ofMemberships(departmentId, memberships));
        }

        return unmodifiableMap(buckets);
    }

    private DepartmentMembershipBucket saveAll(long departmentId, List<DepartmentMembershipEntity> toSave) {

        if (toSave.isEmpty()) {
            return DepartmentMembershipBucket.empty(departmentId);
        }

        final List<DepartmentMembershipEntity> savedEntities = repository.saveAll(toSave);

        final List<DepartmentMembership> memberships = savedEntities.stream().map(DepartmentMembershipService::toDepartmentMembership).toList();
        return DepartmentMembershipBucket.ofMemberships(departmentId, memberships);
    }

    private static DepartmentMembership toDepartmentMembership(DepartmentMembershipEntity entity) {
        return new DepartmentMembership(
            new PersonId(entity.getPersonId()),
            entity.getDepartmentId(),
            entity.getMembershipKind(),
            entity.getValidFrom(),
            Optional.ofNullable(entity.getValidTo())
        );
    }

    private MemberIdsDiff getMemberIdsDiff(DepartmentMembershipBucket nextMembershipBucket, DepartmentMembershipBucket currentMembershipBucket) {

        final List<Long> nextMemberIds = personIdValues(nextMembershipBucket.members());
        final List<Long> currentMemberIds = personIdValues(currentMembershipBucket.members());
        final List<Long> nextDepartmentHeadIds = personIdValues(nextMembershipBucket.departmentHeads());
        final List<Long> currentDepartmentHeadIds = personIdValues(currentMembershipBucket.departmentHeads());
        final List<Long> nextSecondStageAuthorityIds = personIdValues(nextMembershipBucket.secondStageAuthorities());
        final List<Long> currentSecondStageAuthorityIds = personIdValues(currentMembershipBucket.secondStageAuthorities());

        return new MemberIdsDiff(nextMemberIds, currentMemberIds, nextDepartmentHeadIds, currentDepartmentHeadIds, nextSecondStageAuthorityIds, currentSecondStageAuthorityIds);
    }

    private List<Long> personIdValues(Collection<DepartmentMembership> memberships) {
        return memberships.stream().map(DepartmentMembership::personId).map(PersonId::value).toList();
    }

    private List<DepartmentMembershipEntity> createNewMemberships(MemberIdsDiff memberIdsDiff, Long departmentId, Instant timestamp) {

        final List<DepartmentMembershipEntity> newMemberships = memberIdsDiff.newMemberIds()
            .stream()
            .map(newMemberId -> newMembershipEntity(departmentId, new PersonId(newMemberId), MEMBER, timestamp))
            .toList();

        final List<DepartmentMembershipEntity> newDepartmentHHeads = memberIdsDiff.newDepartmentHeadIds()
            .stream()
            .map(newDepartmentHeadId -> newMembershipEntity(departmentId, new PersonId(newDepartmentHeadId), DEPARTMENT_HEAD, timestamp))
            .toList();

        final List<DepartmentMembershipEntity> newSecondStageAuthorities = memberIdsDiff.newSecondStageAuthorityIds()
            .stream()
            .map(newSecondStageAuthorityId -> newMembershipEntity(departmentId, new PersonId(newSecondStageAuthorityId), SECOND_STAGE_AUTHORITY, timestamp))
            .toList();

        final ArrayList<DepartmentMembershipEntity> allMemberships = new ArrayList<>();
        allMemberships.addAll(newMemberships);
        allMemberships.addAll(newDepartmentHHeads);
        allMemberships.addAll(newSecondStageAuthorities);

        return allMemberships;
    }

    private DepartmentMembershipEntity newMembershipEntity(
        Long departmentId,
        PersonId personId,
        DepartmentMembershipKind membershipKind,
        Instant validFrom
    ) {
        final DepartmentMembershipEntity membership = new DepartmentMembershipEntity();
        membership.setDepartmentId(departmentId);
        membership.setPersonId(personId.value());
        membership.setValidFrom(validFrom);
        membership.setMembershipKind(membershipKind);
        return membership;
    }

    private List<UpdateOrDelete> updateActiveMemberships(MemberIdsDiff idsDiff, Long departmentId, Instant validTo) {

        final List<DepartmentMembershipEntity> currentMemberships =
            repository.findAllByDepartmentId(departmentId);

        return currentMemberships.stream()
            .map(membershipEntity -> switch (membershipEntity.getMembershipKind()) {
                case MEMBER ->
                    updateMembershipValidTo(membershipEntity, idsDiff.currentMemberIds, idsDiff.nextMemberIds, validTo);
                case DEPARTMENT_HEAD ->
                    updateMembershipValidTo(membershipEntity, idsDiff.currentDepartmentHeadIds, idsDiff.nextDepartmentHeadIds, validTo);
                case SECOND_STAGE_AUTHORITY ->
                    updateMembershipValidTo(membershipEntity, idsDiff.currentSecondStageAuthorityIds, idsDiff.nextSecondStageAuthorityIds, validTo);
            })
            .toList();
    }

    private record UpdateOrDelete(
        DepartmentMembershipEntity membershipEntity,
        boolean update,
        boolean delete
    ) {}

    private UpdateOrDelete updateMembershipValidTo(
        DepartmentMembershipEntity membershipEntity,
        Collection<Long> currentPersonIds,
        Collection<Long> nextPersonIds,
        Instant validTo
    ) {
        boolean update = false;
        boolean delete =  false;

        if (isValidToNullOrSameDay(membershipEntity, validTo)) {
            final boolean isMemberNow = nextPersonIds.contains(membershipEntity.getPersonId());
            final boolean hasBeenMemberBefore = currentPersonIds.contains(membershipEntity.getPersonId());
             if (hasBeenMemberBefore && !isMemberNow) {
                if (isSameDay(membershipEntity.getValidFrom(), validTo)) {
                    // person has been added to department today, and removed today
                    // e.g. because someone is testing departments in the UI
                    // therefore remove the membership entry
                    delete = true;
                } else {
                    // otherwise keep the membership entry with validTo set
                    membershipEntity.setValidTo(validTo);
                    update = true;
                }
            }
        }

        return new UpdateOrDelete(membershipEntity, update, delete);
    }

    private boolean isValidToNullOrSameDay(DepartmentMembershipEntity entity, Instant timestamp) {
        if (entity.getValidTo() == null) {
            return true;
        } else {
            return isSameDay(entity.getValidTo(), timestamp);
        }
    }

    private boolean isSameDay(Instant first, Instant second) {
        final ZoneId zone = ZoneId.of("Europe/Berlin");
        return isSameDay(first.atZone(zone), second.atZone(zone));
    }

    private boolean isSameDay(ZonedDateTime first, ZonedDateTime second) {
        return first.getYear() == second.getYear() &&
               first.getDayOfYear() == second.getDayOfYear();
    }

    private record MemberIdsDiff(
        List<Long> nextMemberIds,
        List<Long> currentMemberIds,
        List<Long> nextDepartmentHeadIds,
        List<Long> currentDepartmentHeadIds,
        List<Long> nextSecondStageAuthorityIds,
        List<Long> currentSecondStageAuthorityIds
    ) {

        List<Long> newMemberIds() {
            return nextMemberIds.stream().filter(id -> !currentMemberIds.contains(id)).toList();
        }

        List<Long> newDepartmentHeadIds() {
            return nextDepartmentHeadIds.stream().filter(id -> !currentDepartmentHeadIds.contains(id)).toList();
        }

        List<Long> newSecondStageAuthorityIds() {
            return nextSecondStageAuthorityIds.stream().filter(id -> !currentSecondStageAuthorityIds.contains(id)).toList();
        }
    }
}
