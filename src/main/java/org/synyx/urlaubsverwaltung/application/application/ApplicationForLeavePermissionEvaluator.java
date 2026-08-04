package org.synyx.urlaubsverwaltung.application.application;

import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

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
     * resolved lazily and only once, therefore the returned instance is meant to be used for a single request.
     *
     * @param signedInUser user asking for permissions
     * @param application  application the permissions are asked for
     * @return permissions of {@code signedInUser} on {@code application}
     */
    public ApplicationForLeavePermissions of(Person signedInUser, Application application) {
        return new ApplicationForLeavePermissions(new LazyApplicationResponsibility(departmentService, signedInUser), signedInUser, application);
    }

    /**
     * Permissions of the given user on each of the given applications. In contrast to
     * {@link #of(Person, Application)} the responsibilities of the user are resolved for all applications at once,
     * therefore use this one to decide permissions for a list of applications.
     *
     * @param signedInUser user asking for permissions
     * @param applications applications the permissions are asked for
     * @return permissions of {@code signedInUser} per application
     */
    public Function<Application, ApplicationForLeavePermissions> of(Person signedInUser, Collection<? extends Application> applications) {

        final List<Person> personsOfApplications = applications.stream().map(Application::getPerson).distinct().toList();

        final List<Person> membersAsDepartmentHead = signedInUser.hasRole(DEPARTMENT_HEAD)
            ? departmentService.getMembersForDepartmentHead(signedInUser)
            : List.of();

        final List<Person> membersAsSecondStageAuthority = signedInUser.hasRole(SECOND_STAGE_AUTHORITY)
            ? departmentService.getMembersForSecondStageAuthority(signedInUser)
            : List.of();

        // the persons that are the second stage authority of the signed in user - they decide about the user, therefore
        // the user does not decide about them
        final List<Person> secondStageAuthoritiesOfSignedInUser = personsOfApplications.stream().anyMatch(person -> person.hasRole(SECOND_STAGE_AUTHORITY))
            ? departmentService.getSecondStageAuthoritiesAllowedToManagePerson(personsOfApplications, signedInUser)
            : List.of();

        final ApplicationResponsibility responsibility = new ResolvedApplicationResponsibility(
            membersAsDepartmentHead, membersAsSecondStageAuthority, secondStageAuthoritiesOfSignedInUser);

        return application -> new ApplicationForLeavePermissions(responsibility, signedInUser, application);
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
}
