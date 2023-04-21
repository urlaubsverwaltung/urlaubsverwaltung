package org.synyx.urlaubsverwaltung.person.web;

import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.ArrayList;
import java.util.List;

import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_CANCELLATION;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_CONVERTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_EDITED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_REJECTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_REVOKED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_UPCOMING;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL;

final class PersonNotificationsMapper {

    private PersonNotificationsMapper() {
        // ok
    }

    static List<MailNotification> mapToMailNotifications(PersonNotificationsDto personNotificationsDto) {

        final List<MailNotification> mailNotifications = new ArrayList<>();
        addIfActive(mailNotifications, personNotificationsDto.getPersonNewManagementAll(), NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL);
        addIfActive(mailNotifications, personNotificationsDto.getOvertimeManagementAll(), NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_ALL);

        addIfActive(mailNotifications, personNotificationsDto.getApplicationAppliedForManagement(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED);
        addIfActive(mailNotifications, personNotificationsDto.getApplicationAdaptedForManagement(), List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED));
        addIfActive(mailNotifications, personNotificationsDto.getApplicationCancellationForManagement(), List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION));
        addIfActive(mailNotifications, personNotificationsDto.getApplicationAllowedForManagement(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED);
        addIfActive(mailNotifications, personNotificationsDto.getApplicationTemporaryAllowedForManagement(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED);
        addIfActive(mailNotifications, personNotificationsDto.getApplicationWaitingReminderForManagement(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER);
        addIfActive(mailNotifications, personNotificationsDto.getApplicationCancellationRequestedForManagement(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED);

        final List<MailNotification> notificationEmailApplications = List.of(NOTIFICATION_EMAIL_APPLICATION_APPLIED,
            NOTIFICATION_EMAIL_APPLICATION_ALLOWED, NOTIFICATION_EMAIL_APPLICATION_REVOKED, NOTIFICATION_EMAIL_APPLICATION_REJECTED,
            NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED, NOTIFICATION_EMAIL_APPLICATION_CANCELLATION, NOTIFICATION_EMAIL_APPLICATION_EDITED,
            NOTIFICATION_EMAIL_APPLICATION_CONVERTED);
        addIfActive(mailNotifications, personNotificationsDto.getApplicationAppliedAndChanges(), notificationEmailApplications);
        addIfActive(mailNotifications, personNotificationsDto.getApplicationUpcoming(), NOTIFICATION_EMAIL_APPLICATION_UPCOMING);
        addIfActive(mailNotifications, personNotificationsDto.getHolidayReplacement(), NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT);
        addIfActive(mailNotifications, personNotificationsDto.getHolidayReplacementUpcoming(), NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING);

        return mailNotifications;
    }

    static PersonNotificationsDto mapToPersonNotificationsDto(Person person) {

        final boolean isBossOrOffice = person.hasRole(Role.BOSS) || person.hasRole(Role.OFFICE);
        final boolean isBossOrDHOrSSA = person.hasRole(Role.BOSS) || person.hasRole(Role.DEPARTMENT_HEAD) || person.hasRole(Role.SECOND_STAGE_AUTHORITY);

        final List<MailNotification> activePersonMailNotifications = new ArrayList<>(person.getNotifications());

        final PersonNotificationsDto personNotificationsDto = new PersonNotificationsDto();
        for (MailNotification mailNotificationToCheck : MailNotification.values()) {
            switch (mailNotificationToCheck) {
                case NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL: {
                    personNotificationsDto.setPersonNewManagementAll(new PersonNotificationDto(
                        isBossOrOffice,
                        activePersonMailNotifications.contains(mailNotificationToCheck)
                    ));
                    break;
                }
                case NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_ALL: {
                    personNotificationsDto.setOvertimeManagementAll(new PersonNotificationDto(
                        isBossOrOffice,
                        activePersonMailNotifications.contains(mailNotificationToCheck)
                    ));
                    break;
                }

                case NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED: {
                    personNotificationsDto.setApplicationAppliedForManagement(new PersonNotificationDto(
                        isBossOrDHOrSSA,
                        activePersonMailNotifications.contains(mailNotificationToCheck)
                    ));
                    break;
                }
                case NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED:
                case NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED: {
                    personNotificationsDto.setApplicationAdaptedForManagement(new PersonNotificationDto(
                        isBossOrDHOrSSA,
                        activePersonMailNotifications.contains(mailNotificationToCheck)
                    ));
                    break;
                }
                case NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED:
                case NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED:
                case NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION: {
                    personNotificationsDto.setApplicationCancellationForManagement(new PersonNotificationDto(
                        isBossOrDHOrSSA,
                        activePersonMailNotifications.contains(mailNotificationToCheck)
                    ));
                    break;
                }
                case NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED: {
                    personNotificationsDto.setApplicationAllowedForManagement(new PersonNotificationDto(
                        isBossOrDHOrSSA,
                        activePersonMailNotifications.contains(mailNotificationToCheck)
                    ));
                    break;
                }
                case NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED: {
                    personNotificationsDto.setApplicationTemporaryAllowedForManagement(new PersonNotificationDto(
                        isBossOrDHOrSSA,
                        activePersonMailNotifications.contains(mailNotificationToCheck)
                    ));
                    break;
                }
                case NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER: {
                    personNotificationsDto.setApplicationWaitingReminderForManagement(new PersonNotificationDto(
                        isBossOrDHOrSSA,
                        activePersonMailNotifications.contains(mailNotificationToCheck)
                    ));
                    break;
                }
                case NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED: {
                    personNotificationsDto.setApplicationCancellationRequestedForManagement(new PersonNotificationDto(
                        person.hasRole(Role.OFFICE) || ((isBossOrDHOrSSA) && person.hasRole(Role.APPLICATION_CANCELLATION_REQUESTED)),
                        activePersonMailNotifications.contains(mailNotificationToCheck)
                    ));
                    break;
                }

                case NOTIFICATION_EMAIL_APPLICATION_APPLIED:
                case NOTIFICATION_EMAIL_APPLICATION_ALLOWED:
                case NOTIFICATION_EMAIL_APPLICATION_REVOKED:
                case NOTIFICATION_EMAIL_APPLICATION_REJECTED:
                case NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED:
                case NOTIFICATION_EMAIL_APPLICATION_CANCELLATION:
                case NOTIFICATION_EMAIL_APPLICATION_EDITED:
                case NOTIFICATION_EMAIL_APPLICATION_CONVERTED: {
                    personNotificationsDto.setApplicationAppliedAndChanges(new PersonNotificationDto(
                        true,
                        activePersonMailNotifications.contains(mailNotificationToCheck)
                    ));
                    break;
                }
                case NOTIFICATION_EMAIL_APPLICATION_UPCOMING: {
                    personNotificationsDto.setApplicationUpcoming(new PersonNotificationDto(
                        true,
                        activePersonMailNotifications.contains(mailNotificationToCheck)
                    ));
                    break;
                }
                case NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT: {
                    personNotificationsDto.setHolidayReplacement(new PersonNotificationDto(
                        true,
                        activePersonMailNotifications.contains(mailNotificationToCheck)
                    ));
                    break;
                }
                case NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING: {
                    personNotificationsDto.setHolidayReplacementUpcoming(new PersonNotificationDto(
                        true,
                        activePersonMailNotifications.contains(mailNotificationToCheck)
                    ));
                    break;
                }
            }
        }

        final List<PersonNotificationDto> dtoNotifications = List.of(
            personNotificationsDto.getPersonNewManagementAll(),
            personNotificationsDto.getOvertimeManagementAll(),
            personNotificationsDto.getApplicationAppliedForManagement(),
            personNotificationsDto.getApplicationAdaptedForManagement(),
            personNotificationsDto.getApplicationCancellationForManagement(),
            personNotificationsDto.getApplicationAllowedForManagement(),
            personNotificationsDto.getApplicationTemporaryAllowedForManagement(),
            personNotificationsDto.getApplicationWaitingReminderForManagement(),
            personNotificationsDto.getApplicationCancellationRequestedForManagement(),
            personNotificationsDto.getApplicationAppliedAndChanges(),
            personNotificationsDto.getApplicationUpcoming(),
            personNotificationsDto.getHolidayReplacement(),
            personNotificationsDto.getHolidayReplacementUpcoming()
        );

        final long visibleCount = dtoNotifications.stream().filter(PersonNotificationDto::isVisible).count();
        final long activeCount = dtoNotifications.stream().filter(PersonNotificationDto::isVisible).filter(PersonNotificationDto::isActive).count();
        personNotificationsDto.setAll(visibleCount == activeCount);

        personNotificationsDto.setPersonId(person.getId());

        return personNotificationsDto;
    }

    private static void addIfActive(List<MailNotification> activatedMailNotifications, PersonNotificationDto personNotificationDto, MailNotification mailNotification) {
        addIfActive(activatedMailNotifications, personNotificationDto, List.of(mailNotification));
    }

    private static void addIfActive(List<MailNotification> activatedMailNotifications, PersonNotificationDto personNotificationDto, List<MailNotification> mailNotifications) {
        if (personNotificationDto != null && personNotificationDto.isActive()) {
            activatedMailNotifications.addAll(mailNotifications);
        }
    }
}
