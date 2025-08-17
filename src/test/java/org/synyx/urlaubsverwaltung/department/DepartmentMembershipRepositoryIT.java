package org.synyx.urlaubsverwaltung.department;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Clock;
import java.time.Year;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest
@Transactional
class DepartmentMembershipRepositoryIT extends SingleTenantTestContainersBase {

    @Autowired
    private DepartmentMembershipRepository sut;

    @Autowired
    private Clock clock;
    @Autowired
    private PersonService personService;
    @Autowired
    private DepartmentService departmentService;

    @Nested
    class FindAllActiveInYear {

        @Test
        void ensureReturnsMembershipsHavingNoEndDate() {

            final Year currentYear = Year.now(clock);
            final Year requestYear = currentYear.minusYears(5);

            final Person person = personService.create("max", "Max", "Mustermann", "mustermann@example.org", List.of(), List.of(USER, DEPARTMENT_HEAD));
            final Department department = createDepartment(List.of(person));

            final DepartmentMembershipEntity membershipStartingInYear = new DepartmentMembershipEntity();
            membershipStartingInYear.setDepartmentId(department.getId());
            membershipStartingInYear.setPersonId(person.getId());
            membershipStartingInYear.setMembershipKind(DepartmentMembershipKind.MEMBER);
            membershipStartingInYear.setValidFrom(ZonedDateTime.now(clock).withYear(requestYear.getValue()).toInstant());
            membershipStartingInYear.setValidTo(null);

            final DepartmentMembershipEntity membershipStartingInPast = new DepartmentMembershipEntity();
            membershipStartingInPast.setDepartmentId(department.getId());
            membershipStartingInPast.setPersonId(person.getId());
            membershipStartingInPast.setMembershipKind(DepartmentMembershipKind.MEMBER);
            membershipStartingInPast.setValidFrom(ZonedDateTime.now(clock).withYear(requestYear.minusYears(1).getValue()).toInstant());
            membershipStartingInPast.setValidTo(null);

            final DepartmentMembershipEntity membershipStartingInFuture = new DepartmentMembershipEntity();
            membershipStartingInFuture.setDepartmentId(department.getId());
            membershipStartingInFuture.setPersonId(person.getId());
            membershipStartingInFuture.setMembershipKind(DepartmentMembershipKind.MEMBER);
            membershipStartingInFuture.setValidFrom(ZonedDateTime.now(clock).withYear(requestYear.plusYears(1).getValue()).toInstant());
            membershipStartingInFuture.setValidTo(null);

            sut.saveAll(List.of(membershipStartingInYear, membershipStartingInPast, membershipStartingInFuture));

            final List<DepartmentMembershipEntity> actual = sut.findAllActiveInYear(requestYear.getValue());
            assertThat(actual).containsExactlyInAnyOrder(membershipStartingInYear, membershipStartingInPast);

            // ensure all memberships, 1 memberships from initial department creation and 3 historical memberships created above
            assertThat(sut.findAll()).hasSize(4);
        }

        @Test
        void ensureReturnsMembershipStartingInYearEndingInYear() {

            final Year currentYear = Year.now(clock);
            final Year requestYear = currentYear.minusYears(5);

            final Person person = personService.create("max", "Max", "Mustermann", "mustermann@example.org", List.of(), List.of(USER, DEPARTMENT_HEAD));
            final Department department = createDepartment(List.of(person));

            final DepartmentMembershipEntity membershipStartingInYearEndingInYear = new DepartmentMembershipEntity();
            membershipStartingInYearEndingInYear.setDepartmentId(department.getId());
            membershipStartingInYearEndingInYear.setPersonId(person.getId());
            membershipStartingInYearEndingInYear.setMembershipKind(DepartmentMembershipKind.MEMBER);
            membershipStartingInYearEndingInYear.setValidFrom(ZonedDateTime.now(clock).withDayOfYear(1).withYear(requestYear.getValue()).toInstant());
            membershipStartingInYearEndingInYear.setValidTo(ZonedDateTime.now(clock).withDayOfYear(100).withYear(requestYear.getValue()).toInstant());

            sut.save(membershipStartingInYearEndingInYear);

            final List<DepartmentMembershipEntity> actual = sut.findAllActiveInYear(requestYear.getValue());
            assertThat(actual).containsExactlyInAnyOrder(membershipStartingInYearEndingInYear);

            // ensure all memberships, 1 memberships from initial department creation and 1 historical memberships created above
            assertThat(sut.findAll()).hasSize(2);
        }

        @Test
        void ensureReturnsMembershipStartingInPastEndingInYear() {

            final Year currentYear = Year.now(clock);
            final Year requestYear = currentYear.minusYears(5);

            final Person person = personService.create("max", "Max", "Mustermann", "mustermann@example.org", List.of(), List.of(USER, DEPARTMENT_HEAD));
            final Department department = createDepartment(List.of(person));

            final DepartmentMembershipEntity membershipStartingInPastEndingInYear = new DepartmentMembershipEntity();
            membershipStartingInPastEndingInYear.setDepartmentId(department.getId());
            membershipStartingInPastEndingInYear.setPersonId(person.getId());
            membershipStartingInPastEndingInYear.setMembershipKind(DepartmentMembershipKind.MEMBER);
            membershipStartingInPastEndingInYear.setValidFrom(ZonedDateTime.now(clock).withDayOfYear(1).withYear(requestYear.minusYears(2).getValue()).toInstant());
            membershipStartingInPastEndingInYear.setValidTo(ZonedDateTime.now(clock).withDayOfYear(100).withYear(requestYear.getValue()).toInstant());

            sut.save(membershipStartingInPastEndingInYear);

            final List<DepartmentMembershipEntity> actual = sut.findAllActiveInYear(requestYear.getValue());
            assertThat(actual).containsExactlyInAnyOrder(membershipStartingInPastEndingInYear);

            // ensure all memberships, 1 memberships from initial department creation and 1 historical memberships created above
            assertThat(sut.findAll()).hasSize(2);
        }

        @Test
        void ensureDoesNotReturnMembershipEndingInPast() {

            final Year currentYear = Year.now(clock);
            final Year requestYear = currentYear.minusYears(5);

            final Person person = personService.create("max", "Max", "Mustermann", "mustermann@example.org", List.of(), List.of(USER, DEPARTMENT_HEAD));
            final Department department = createDepartment(List.of(person));

            final DepartmentMembershipEntity membershipStartingInPastEndingInPast = new DepartmentMembershipEntity();
            membershipStartingInPastEndingInPast.setDepartmentId(department.getId());
            membershipStartingInPastEndingInPast.setPersonId(person.getId());
            membershipStartingInPastEndingInPast.setMembershipKind(DepartmentMembershipKind.MEMBER);
            membershipStartingInPastEndingInPast.setValidFrom(ZonedDateTime.now(clock).withDayOfYear(1).withYear(requestYear.minusYears(2).getValue()).toInstant());
            membershipStartingInPastEndingInPast.setValidTo(ZonedDateTime.now(clock).withDayOfYear(100).withYear(requestYear.minusYears(2).getValue()).toInstant());

            sut.save(membershipStartingInPastEndingInPast);

            final List<DepartmentMembershipEntity> actual = sut.findAllActiveInYear(requestYear.getValue());
            assertThat(actual).isEmpty();

            // ensure all memberships, 1 memberships from initial department creation and 1 historical memberships created above
            assertThat(sut.findAll()).hasSize(2);
        }

        @Test
        void ensureDoesNotReturnMembershipStartingInFutureEndingInFuture() {

            final Year currentYear = Year.now(clock);
            final Year requestYear = currentYear.minusYears(5);

            final Person person = personService.create("max", "Max", "Mustermann", "mustermann@example.org", List.of(), List.of(USER, DEPARTMENT_HEAD));
            final Department department = createDepartment(List.of(person));

            final DepartmentMembershipEntity membershipStartingInFutureEndingInFuture = new DepartmentMembershipEntity();
            membershipStartingInFutureEndingInFuture.setDepartmentId(department.getId());
            membershipStartingInFutureEndingInFuture.setPersonId(person.getId());
            membershipStartingInFutureEndingInFuture.setMembershipKind(DepartmentMembershipKind.MEMBER);
            membershipStartingInFutureEndingInFuture.setValidFrom(ZonedDateTime.now(clock).withDayOfYear(1).withYear(requestYear.plusYears(1).getValue()).toInstant());
            membershipStartingInFutureEndingInFuture.setValidTo(ZonedDateTime.now(clock).withDayOfYear(100).withYear(requestYear.plusYears(1).getValue()).toInstant());

            sut.save(membershipStartingInFutureEndingInFuture);

            final List<DepartmentMembershipEntity> actual = sut.findAllActiveInYear(requestYear.getValue());
            assertThat(actual).isEmpty();

            // ensure all memberships, 1 memberships from initial department creation
            assertThat(sut.findAll()).hasSize(2);
        }

        @Test
        void ensureDoesNotReturnMembershipStartingInFutureEndingIsNull() {

            final Year currentYear = Year.now(clock);
            final Year requestYear = currentYear.minusYears(5);

            final Person person = personService.create("max", "Max", "Mustermann", "mustermann@example.org", List.of(), List.of(USER, DEPARTMENT_HEAD));
            createDepartment(List.of(person));

            // no more memberships required.
            // initial department creation membership starts in Year.now(clock) and is valid indefinitely

            final List<DepartmentMembershipEntity> actual = sut.findAllActiveInYear(requestYear.getValue());
            assertThat(actual).isEmpty();

            // ensure all memberships, 1 memberships from initial department creation
            assertThat(sut.findAll()).hasSize(1);
        }
    }

    private Department createDepartment(List<Person> members) {
        final Department department = new Department();

        department.setName("Department");
        department.setMembers(members);
        department.setDepartmentHeads(List.of());
        department.setSecondStageAuthorities(List.of());

        return departmentService.create(department);
    }
}
