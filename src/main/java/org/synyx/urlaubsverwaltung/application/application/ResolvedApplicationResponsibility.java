package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;

/**
 * {@link ApplicationResponsibility} that was resolved for a whole list of applications at once.
 */
class ResolvedApplicationResponsibility implements ApplicationResponsibility {

    private final Set<Person> membersAsDepartmentHead;
    private final Set<Person> membersAsSecondStageAuthority;
    private final Set<Person> secondStageAuthoritiesOfSignedInUser;

    ResolvedApplicationResponsibility(List<Person> membersAsDepartmentHead, List<Person> membersAsSecondStageAuthority,
                                      List<Person> secondStageAuthoritiesOfSignedInUser) {
        this.membersAsDepartmentHead = membersAsDepartmentHead.stream().collect(toUnmodifiableSet());
        this.membersAsSecondStageAuthority = membersAsSecondStageAuthority.stream().collect(toUnmodifiableSet());
        this.secondStageAuthoritiesOfSignedInUser = secondStageAuthoritiesOfSignedInUser.stream().collect(toUnmodifiableSet());
    }

    @Override
    public boolean isDepartmentHeadOf(Person person) {
        return membersAsDepartmentHead.contains(person);
    }

    @Override
    public boolean isSecondStageAuthorityOf(Person person) {
        return membersAsSecondStageAuthority.contains(person);
    }

    @Override
    public boolean isSecondStageAuthorityOfSignedInUser(Person person) {
        return secondStageAuthoritiesOfSignedInUser.contains(person);
    }
}
