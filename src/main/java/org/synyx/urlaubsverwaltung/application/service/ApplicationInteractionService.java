package org.synyx.urlaubsverwaltung.application.service;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Optional;


/**
 * Provides interactions with applications for leave, i.e. apply for leave, allow, cancel etc.
 */
public interface ApplicationInteractionService {

    /**
     * Sets the application's state to waiting and calculates the number of used vacation days. The bosses gets an email
     * that a new application for leave has been created and the person of the application for leave gets an email as
     * confirmation.
     *
     * @param application for leave
     * @param applier     of the application
     * @param comment     giving further information (is optional)
     * @return the saved application for leave
     */
    Application apply(Application application, Person applier, Optional<String> comment);


    /**
     * Sets the application's state to allowed or to preliminary allowed (in case of two step approval) and informs the
     * person of the application for leave that his vacation has been allowed.
     *
     * @param application    for leave
     * @param privilegedUser that allowed the application for leave
     * @param comment        giving further information to allowing of application for leave (is optional)
     * @return the allowed application for leave
     */
    Application allow(Application application, Person privilegedUser, Optional<String> comment);


    /**
     * Sets the application's state to rejected (only by privileged user) and informs the person of the application for
     * leave that his vacation has been rejected.
     *
     * @param application    for leave
     * @param privilegedUser that rejected the application for leave
     * @param comment        giving further information to rejecting of application for leave (is optional)
     * @return the rejected application for leave
     */
    Application reject(Application application, Person privilegedUser, Optional<String> comment);


    /**
     * Sets the application's state to cancelled.
     *
     * @param application for leave
     * @param canceller   executes the application's cancellation
     * @param comment     giving further information to cancellation of application for leave (is optional)
     * @return the cancelled application for leave
     */
    Application cancel(Application application, Person canceller, Optional<String> comment);


    /**
     * Create a directly allowed application for leave due to a converted sick note.
     *
     * @param application to be created directly as allowed
     * @param creator     executes the creation
     * @return the created application for leave
     */
    Application createFromConvertedSickNote(Application application, Person creator);


    /**
     * Remind the persons with role {@link org.synyx.urlaubsverwaltung.person.Role#BOSS} to decide about the
     * application for leave (allow or reject it).
     *
     * @param application for leave to be checked
     * @return the application for leave that should be checked
     * @throws RemindAlreadySentException                        in case today already sent remind
     * @throws ImpatientAboutApplicationForLeaveProcessException in case try to remind too early
     */
    Application remind(Application application) throws RemindAlreadySentException,
        ImpatientAboutApplicationForLeaveProcessException;


    /**
     * Refer the given application for leave to the given person.
     *
     * @param application for leave to be referred
     * @param recipient   who should decide about the application for leave
     * @param sender      who ask another person to decide about the application for leave
     * @return the application for leave that is referred
     */
    Application refer(Application application, Person recipient, Person sender);
}
