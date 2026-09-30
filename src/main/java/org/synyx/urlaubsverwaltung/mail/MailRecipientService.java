package org.synyx.urlaubsverwaltung.mail;

import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;
import java.util.Map;

public interface MailRecipientService {

    /**
     * Returns all responsible managers of the given person.
     * Managers are:
     * <ul>
     *     <li>bosses</li>
     *     <li>department heads</li>
     *     <li>second stage authorities.</li>
     * </ul>
     *
     * @param personOfInterest person to get managers from
     * @return list of all responsible managers
     */
    List<Person> getResponsibleManagersOf(Person personOfInterest);

    /**
     * Returns a list of recipients of interest for a given person based on
     * <ul>
     *     <li>is Office and the given mail notification is active</li>
     *     <li>is Boss and the given mail notification is active</li>
     *     <li>is responsible Department Head and the given mail notification is active</li>
     *     <li>is responsible Second Stage Authority and the given mail notification is active</li>
     *     <li>is Boss in the same Department and the given mail notification is active</li>
     * </ul>
     *
     * @param personOfInterest person to get recipients from
     * @param mailNotification given notification that one of must be active
     * @return list of recipients of interest
     */
    List<Person> getRecipientsOfInterest(Person personOfInterest, MailNotification mailNotification);

    /**
     * Same as {@link #getRecipientsOfInterest(Person, MailNotification)} for many persons at once: the offices, bosses,
     * department heads, second stage authorities and their department memberships are loaded once for all persons.
     *
     * @param personsOfInterest persons to get recipients from
     * @param mailNotification  given notification that one of must be active
     * @return for every given person the recipients of interest, an empty list if there is none
     */
    Map<Person, List<Person>> getRecipientsOfInterest(List<Person> personsOfInterest, MailNotification mailNotification);

    /**
     * Returns a list of colleagues for a given person based on
     * <ul>
     *     <li>is in the same department</li>
     *     <li>is active</li>
     *     <li>is not department head of the department</li>
     *     <li>is not second stage authority of the department</li>
     *     <li>and the given mail notification is active</li>
     * </ul>
     *
     * @param personOfInterest person to get recipients from
     * @param mailNotification given notification that one of must be active
     * @return list of colleagues
     */
    List<Person> getColleagues(Person personOfInterest, MailNotification mailNotification);
}
