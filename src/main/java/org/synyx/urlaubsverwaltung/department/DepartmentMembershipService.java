package org.synyx.urlaubsverwaltung.department;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.PersonId;

import java.time.Clock;
import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
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
     * Returns all active memberships for a given person.
     * A membership is considered active if it has a validTo of null.
     *
     * @param personId the ID of the person for whom to retrieve active memberships
     * @return a list of {@link DepartmentMembership} representing the active memberships of the person
     */
    List<DepartmentMembership> getActiveMemberships(PersonId personId) {
        return getActiveMembershipsOfPersons(List.of(personId)).getOrDefault(personId, List.of());
    }

    /**
     * Returns all active memberships for all persons in a given year.<br/>
     * A membership is considered active if it has a {@link DepartmentMembership#validTo()} of {@code null}
     * or a {@link DepartmentMembership#validTo()} after the end of the given year.
     * And {@link DepartmentMembership#validFrom()} before the start or in the given year.
     *
     * @param year the year for which to retrieve active memberships
     * @return map of all active {@link DepartmentMembership} by {@link PersonId}
     */
    Map<PersonId, List<DepartmentMembership>> getActiveMembershipsOfYear(Year year) {
        return repository.findAllActiveInYear(year.getValue())
            .stream().map(DepartmentMembershipService::toDepartmentMembership)
            .collect(groupingBy(DepartmentMembership::personId));
    }

    /**
     * Returns all active memberships for a collection of persons.
     * A membership is considered active if it has a validTo of null.
     *
     * @param personIds the IDs of the persons for whom to retrieve active memberships
     * @return map of all active {@link DepartmentMembership} by person ID
     */
    Map<PersonId, List<DepartmentMembership>> getActiveMembershipsOfPersons(Collection<PersonId> personIds) {

        final List<DepartmentMembershipEntity> entities =
            repository.findAllByPersonIdIsInAndValidToIsNull(personIds.stream().map(PersonId::value).toList());

        final Map<PersonId, List<DepartmentMembership>> map = entities.stream()
            .map(DepartmentMembershipService::toDepartmentMembership)
            .collect(groupingBy(DepartmentMembership::personId));

        // ensure entries for all personIds, even if they have no memberships
        personIds.forEach(id -> map.putIfAbsent(id, List.of()));

        return map;
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
    DepartmentStaff createInitialMemberships(
        Long departmentId,
        Collection<PersonId> memberIds,
        Collection<PersonId> departmentHeadIds,
        Collection<PersonId> secondStageAuthorityIds
    ) {

        final Instant now = Instant.now(clock);
        final List<DepartmentMembershipEntity> membershipEntities = new ArrayList<>();

        memberIds.stream()
            .distinct()
            .map(personId -> newMembershipEntity(departmentId, personId, MEMBER, now))
            .forEach(membershipEntities::add);

        departmentHeadIds.stream()
            .distinct()
            .map(personId -> newMembershipEntity(departmentId, personId, DEPARTMENT_HEAD, now))
            .forEach(membershipEntities::add);

        secondStageAuthorityIds.stream()
            .distinct()
            .map(personId -> newMembershipEntity(departmentId, personId, SECOND_STAGE_AUTHORITY, now))
            .forEach(membershipEntities::add);

        return saveAll(departmentId, membershipEntities);
    }

    /**
     * Updates the memberships of a department based on the new staff and the current staff.
     *
     * @param departmentId the ID of the department for which to update the memberships
     * @param currentStaff the current staff containing the existing membership information for the department
     * @param nextMembers person IDs of the members that should be in the department after the update
     * @param nextDepartmentHeads person IDs of the department heads that should be in the department after the update
     * @param nextSecondStageAuthorities person IDs of the second stage authorities that should be in the department after the update
     * @return a {@link DepartmentStaff} containing the updated membership information
     */
    DepartmentStaff updateDepartmentMemberships(
        Long departmentId,
        DepartmentStaff currentStaff,
        Collection<PersonId> nextMembers,
        Collection<PersonId> nextDepartmentHeads,
        Collection<PersonId> nextSecondStageAuthorities
    ) {

        final Instant now = Instant.now(clock);
        final MemberIdsDiff memberIdsDiff = getMemberIdsDiff(currentStaff, nextMembers, nextDepartmentHeads, nextSecondStageAuthorities);

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

    DepartmentStaff getDepartmentStaff(Long departmentId) {
        return getDepartmentStaff(List.of(departmentId)).getOrDefault(departmentId, DepartmentStaff.empty(departmentId));
    }

    /**
     * Returns the current staff for the given department IDs.
     * A membership is considered current if it has a validTo of null.
     *
     * @param departmentIds the IDs of the departments for which to retrieve current memberships
     * @return a map where the key is the department ID and the value is a list of current memberships for that department
     */
    Map<Long, DepartmentStaff> getDepartmentStaff(Collection<Long> departmentIds) {

        final List<DepartmentMembershipEntity> currentMemberships =
            repository.findAllByDepartmentIdIsInAndValidToIsNull(departmentIds);

        final Map<Long, List<DepartmentMembership>> membershipsByDepartmentId = currentMemberships.stream()
            .map(DepartmentMembershipService::toDepartmentMembership)
            .collect(groupingBy(DepartmentMembership::departmentId));

        final Map<Long, DepartmentStaff> buckets = new HashMap<>();

        for (Long departmentId : departmentIds) {
            final List<DepartmentMembership> memberships = membershipsByDepartmentId.getOrDefault(departmentId, List.of());
            buckets.put(departmentId, DepartmentStaff.ofMemberships(departmentId, memberships));
        }

        return unmodifiableMap(buckets);
    }

    private DepartmentStaff saveAll(long departmentId, Collection<DepartmentMembershipEntity> toSave) {

        if (toSave.isEmpty()) {
            return DepartmentStaff.empty(departmentId);
        }

        final List<DepartmentMembershipEntity> savedEntities = repository.saveAll(toSave);

        final List<DepartmentMembership> memberships = savedEntities.stream().map(DepartmentMembershipService::toDepartmentMembership).toList();
        return DepartmentStaff.ofMemberships(departmentId, memberships);
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

    private MemberIdsDiff getMemberIdsDiff(DepartmentStaff currentStaff, Collection<PersonId> nextMembers, Collection<PersonId> nextDepartmentHeads, Collection<PersonId> nextSecondStageAuthorities) {

        final Set<Long> nextMemberIds = nextMembers.stream().map(PersonId::value).collect(toSet());
        final Set<Long> nextDepartmentHeadIds = nextDepartmentHeads.stream().map(PersonId::value).collect(toSet());
        final Set<Long> nextSecondStageAuthorityIds = nextSecondStageAuthorities.stream().map(PersonId::value).collect(toSet());

        final Set<Long> currentMemberIds = personIdValues(currentStaff.members());
        final Set<Long> currentDepartmentHeadIds = personIdValues(currentStaff.departmentHeads());
        final Set<Long> currentSecondStageAuthorityIds = personIdValues(currentStaff.secondStageAuthorities());

        return new MemberIdsDiff(nextMemberIds, currentMemberIds, nextDepartmentHeadIds, currentDepartmentHeadIds, nextSecondStageAuthorityIds, currentSecondStageAuthorityIds);
    }

    private Set<Long> personIdValues(Collection<DepartmentMembership> memberships) {
        return memberships.stream().map(DepartmentMembership::personId).map(PersonId::value).collect(toSet());
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
        Set<Long> nextMemberIds,
        Set<Long> currentMemberIds,
        Set<Long> nextDepartmentHeadIds,
        Set<Long> currentDepartmentHeadIds,
        Set<Long> nextSecondStageAuthorityIds,
        Set<Long> currentSecondStageAuthorityIds
    ) {

        Set<Long> newMemberIds() {
            return nextMemberIds.stream().filter(id -> !currentMemberIds.contains(id)).collect(toSet());
        }

        Set<Long> newDepartmentHeadIds() {
            return nextDepartmentHeadIds.stream().filter(id -> !currentDepartmentHeadIds.contains(id)).collect(toSet());
        }

        Set<Long> newSecondStageAuthorityIds() {
            return nextSecondStageAuthorityIds.stream().filter(id -> !currentSecondStageAuthorityIds.contains(id)).collect(toSet());
        }
    }
}
