package org.synyx.urlaubsverwaltung.mail;

import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;

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
