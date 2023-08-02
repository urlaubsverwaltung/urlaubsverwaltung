package org.synyx.urlaubsverwaltung.notification;

import org.synyx.urlaubsverwaltung.person.PersonId;

import java.util.Collection;
import java.util.Map;

public interface UserNotificationSettingsService {

    /**
     * Find the notification settings of a person.
     *
     * @param personId id of the person
     * @return the {@linkplain UserNotificationSettings} or default settings when nothing is persisted yet.
     */
    UserNotificationSettings findNotificationSettings(PersonId personId);

    /**
     * Find the notification settings of multiple persons.
     *
     * @param personIds list of person ids
     * @return the {@linkplain UserNotificationSettings} or default settings for all asked persons
     */
    Map<PersonId, UserNotificationSettings> findNotificationSettings(Collection<PersonId> personIds);

    /**
     * Update notification settings of a person.
     *
     * @param personId              id of the person
     * @param restrictToDepartments whether to restrict mail notifications to departments or not
     * @return the updated {@linkplain UserNotificationSettings}
     */
    UserNotificationSettings updateNotificationSettings(PersonId personId, boolean restrictToDepartments);
}
