package org.synyx.urlaubsverwaltung.application.application;

import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_ADD;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;

/**
 * Single source of truth for the question "who may interact with an application for leave?".
 *
 * <p>Two tiers of permissions exist:
 *
 * <ul>
 *     <li><em>deciding</em> about an application - allowing, rejecting, referring and commenting it - is reserved for
 *     the management of the person, which is {@link org.synyx.urlaubsverwaltung.person.Role#BOSS} or the
 *     {@link org.synyx.urlaubsverwaltung.person.Role#DEPARTMENT_HEAD} /
 *     {@link org.synyx.urlaubsverwaltung.person.Role#SECOND_STAGE_AUTHORITY} of that person. Deciding needs no further
 *     role, it is what these roles are for.</li>
 *     <li><em>administering</em> an application of somebody else - adding, editing, cancelling it and declining a
 *     cancellation request - additionally requires the matching {@code APPLICATION_*} role.
 *     {@link org.synyx.urlaubsverwaltung.person.Role#OFFICE} may administer the applications of everyone.</li>
 * </ul>
 *
 * <p>Everyone may apply for leave for themselves, edit their own application as long as nobody decided about it and ask
 * for the cancellation of an application that was allowed already.
 */
@Component
public class ApplicationForLeavePermissionEvaluator {

    private final DepartmentService departmentService;

    public ApplicationForLeavePermissionEvaluator(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * Permissions of the given user on the given application. The department memberships that the rules depend on are
     * read once and answer every question of the returned instance, which is therefore meant to be used for a single
     * request.
     *
     * @param signedInUser user asking for permissions
     * @param application  application the permissions are asked for
     * @return permissions of {@code signedInUser} on {@code application}
     */
    public ApplicationForLeavePermissions of(Person signedInUser, Application application) {

        final Person person = application.getPerson();

        return new ApplicationForLeavePermissions(signedInUser, application,
            departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, person),
            departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, person),
            isPersonSecondStageAuthorityOfSignedInUser(signedInUser, person));
    }

    /**
     * Permissions of the given user on each of the given applications. In contrast to
     * {@link #of(Person, Application)} the memberships are read for all applications at once, therefore use this one to
     * decide permissions for a list of applications.
     *
     * @param signedInUser user asking for permissions
     * @param applications applications the permissions are asked for
     * @return permissions of {@code signedInUser} per application
     */
    public Function<Application, ApplicationForLeavePermissions> of(Person signedInUser, Collection<? extends Application> applications) {

        final Set<Person> membersAsDepartmentHead = signedInUser.hasRole(DEPARTMENT_HEAD)
            ? toSet(departmentService.getMembersForDepartmentHead(signedInUser))
            : Set.of();

        final Set<Person> membersAsSecondStageAuthority = signedInUser.hasRole(SECOND_STAGE_AUTHORITY)
            ? toSet(departmentService.getMembersForSecondStageAuthority(signedInUser))
            : Set.of();

        final Set<Person> secondStageAuthoritiesOfSignedInUser = secondStageAuthoritiesOfSignedInUser(signedInUser, applications);

        return application -> {
            final Person person = application.getPerson();
            return new ApplicationForLeavePermissions(signedInUser, application,
                membersAsDepartmentHead.contains(person),
                membersAsSecondStageAuthority.contains(person),
                secondStageAuthoritiesOfSignedInUser.contains(person));
        };
    }

    /**
     * Whether the given user may apply for leave for the given person. Everyone may apply for themselves, applying for
     * somebody else is administering and therefore requires
     * {@link org.synyx.urlaubsverwaltung.person.Role#APPLICATION_ADD} on top of being responsible for that person.
     *
     * @param signedInUser user asking for permissions
     * @param person       person the application would belong to
     * @return {@code true} if the user may apply for leave for the person, {@code false} otherwise
     */
    public boolean isAllowedToApplyForPerson(Person signedInUser, Person person) {
        return signedInUser.equals(person)
            || signedInUser.hasRole(OFFICE)
            || (signedInUser.hasRole(APPLICATION_ADD)
            && (signedInUser.hasRole(BOSS)
            || departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, person)
            || departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, person)));
    }

    /**
     * Whether the person is a second stage authority of the signed in user - that person decides about the user,
     * therefore the user does not decide about them. Only a department head is held back by that rule, for everybody
     * else the answer is not worth a query.
     */
    private boolean isPersonSecondStageAuthorityOfSignedInUser(Person signedInUser, Person person) {
        return signedInUser.hasRole(DEPARTMENT_HEAD)
            && departmentService.isSecondStageAuthorityAllowedToManagePerson(person, signedInUser);
    }

    /**
     * The persons of the given applications that are a second stage authority of the signed in user, see
     * {@link #isPersonSecondStageAuthorityOfSignedInUser(Person, Person)}.
     */
    private Set<Person> secondStageAuthoritiesOfSignedInUser(Person signedInUser, Collection<? extends Application> applications) {

        if (!signedInUser.hasRole(DEPARTMENT_HEAD)) {
            return Set.of();
        }

        final List<Person> personsOfApplications = applications.stream().map(Application::getPerson).distinct().toList();
        if (personsOfApplications.stream().noneMatch(person -> person.hasRole(SECOND_STAGE_AUTHORITY))) {
            return Set.of();
        }

        return toSet(departmentService.getSecondStageAuthoritiesAllowedToManagePerson(personsOfApplications, signedInUser));
    }

    private static Set<Person> toSet(List<Person> persons) {
        return persons.stream().collect(toUnmodifiableSet());
    }
}
