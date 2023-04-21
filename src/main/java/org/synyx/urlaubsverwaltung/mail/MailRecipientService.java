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
     * Returns a list of recipients of interest for a given person based on
     * <ul>
     *     <li>is Office and one of the given mail notifications is active</li>
     *     <li>is Boss and one of the given mail notifications is active</li>
     *     <li>is responsible Department Head and one of the given mail notifications is active</li>
     *     <li>is responsible Second Stage Authority and one of the given mail notifications is active</li>
     *     <li>is Boss in the same Department and one of the given mail notifications is active</li>
     * </ul>
     *
     * @param personOfInterest  person to get recipients from
     * @param mailNotifications given notifications that one of must be active
     * @return list of recipients of interest
     */
    List<Person> getRecipientsOfInterest(Person personOfInterest, List<MailNotification> mailNotifications);
}
