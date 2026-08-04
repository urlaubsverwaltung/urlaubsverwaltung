package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.person.Person;

/**
 * Who is responsible for whom, which is what the permissions on an application for leave depend on. Answering it hits
 * the database, therefore it is asked either for a single person - lazily, see
 * {@link ApplicationForLeavePermissionEvaluator#of(Person, Application)} - or resolved for a whole list of applications
 * at once, see {@link ApplicationForLeavePermissionEvaluator#of(Person, java.util.Collection)}.
 */
interface ApplicationResponsibility {

    /**
     * @param person person to check
     * @return {@code true} if the signed in user is a department head of the person, {@code false} otherwise
     */
    boolean isDepartmentHeadOf(Person person);

    /**
     * @param person person to check
     * @return {@code true} if the signed in user is a second stage authority of the person, {@code false} otherwise
     */
    boolean isSecondStageAuthorityOf(Person person);

    /**
     * @param person person to check
     * @return {@code true} if the person is a second stage authority of the signed in user, {@code false} otherwise
     */
    boolean isSecondStageAuthorityOfSignedInUser(Person person);
}
