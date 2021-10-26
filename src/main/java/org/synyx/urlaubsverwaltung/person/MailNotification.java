package org.synyx.urlaubsverwaltung.person;

/**
 * Describes which kind of mail notifications a person can have.
 *
 * <ul>
 *     <li>
 *         {@link #NOTIFICATION_USER}: Default notifications, every user get these notifications. It's not possible to
 *         disable them.
 *         Not used for filtering recipients at the moment
 *     </li>
 *     <li>
 *         {@link #NOTIFICATION_DEPARTMENT_HEAD}: Notify {@link Role#DEPARTMENT_HEAD} users about actions of member of a
 *         department they are responsible for.
 *     </li>
 *     <li>
 *         {@link #NOTIFICATION_SECOND_STAGE_AUTHORITY}: Notify {@link Role#SECOND_STAGE_AUTHORITY} users about actions
 *         of member of a department they are responsible for.
 *     </li>
 *     <li>
 *        {@link #NOTIFICATION_BOSS_ALL}: Notify {@link Role#BOSS} users about actions of any other user.
 *     </li>
 *     <li>
 *         {@link #NOTIFICATION_BOSS_DEPARTMENTS}: Notify {@link Role#BOSS} users about actions of member of a
 *         department they are also member.
 *     </li>
 *     <li>
 *         {@link #NOTIFICATION_OFFICE}: Notify {@link Role#OFFICE} users about any actions of other users.
 *     </li>
 *     <li>
 *         {@link #OVERTIME_NOTIFICATION_OFFICE}: Notify {@link Role#OFFICE} users about any actions of other users in
 *         terms of overtime management.
 *     </li>
 * </ul>
 */
public enum MailNotification {

    NOTIFICATION_USER,
    NOTIFICATION_DEPARTMENT_HEAD,
    NOTIFICATION_SECOND_STAGE_AUTHORITY,
    NOTIFICATION_BOSS_ALL,
    NOTIFICATION_BOSS_DEPARTMENTS,
    NOTIFICATION_OFFICE,
    OVERTIME_NOTIFICATION_OFFICE
}
