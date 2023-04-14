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
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL;
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
    }

    static List<MailNotification> mapToMailNotifications(PersonNotificationsDto personNotificationsDto) {

        final List<MailNotification> mailNotifications = new ArrayList<>();

        if (personNotificationsDto.getApplicationManagementAll() != null && personNotificationsDto.getApplicationManagementAll().isActive()) {
            mailNotifications.add(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL);
        }
        if (personNotificationsDto.getPersonNewManagementAll() != null && personNotificationsDto.getPersonNewManagementAll().isActive()) {
            mailNotifications.add(NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL);
        }
        if (personNotificationsDto.getOvertimeManagementAll() != null && personNotificationsDto.getOvertimeManagementAll().isActive()) {
            mailNotifications.add(NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_ALL);
        }

        if (personNotificationsDto.getApplicationAppliedAndChangesForManagement() != null && personNotificationsDto.getApplicationAppliedAndChangesForManagement().isActive()) {
            mailNotifications.add(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED);
            mailNotifications.add(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED);
            mailNotifications.add(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED);
            mailNotifications.add(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED);
            mailNotifications.add(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED);
            mailNotifications.add(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION);
            mailNotifications.add(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED);
            mailNotifications.add(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED);
        }
        if (personNotificationsDto.getApplicationWaitingReminderForManagement() != null && personNotificationsDto.getApplicationWaitingReminderForManagement().isActive()) {
            mailNotifications.add(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER);
        }
        if (personNotificationsDto.getApplicationCancellationRequestedForManagement() != null && personNotificationsDto.getApplicationCancellationRequestedForManagement().isActive()) {
            mailNotifications.add(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED);
        }

        if (personNotificationsDto.getApplicationAppliedAndChanges() != null && personNotificationsDto.getApplicationAppliedAndChanges().isActive()) {
            mailNotifications.add(NOTIFICATION_EMAIL_APPLICATION_APPLIED);
            mailNotifications.add(NOTIFICATION_EMAIL_APPLICATION_ALLOWED);
            mailNotifications.add(NOTIFICATION_EMAIL_APPLICATION_REVOKED);
            mailNotifications.add(NOTIFICATION_EMAIL_APPLICATION_REJECTED);
            mailNotifications.add(NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED);
            mailNotifications.add(NOTIFICATION_EMAIL_APPLICATION_CANCELLATION);
            mailNotifications.add(NOTIFICATION_EMAIL_APPLICATION_EDITED);
            mailNotifications.add(NOTIFICATION_EMAIL_APPLICATION_CONVERTED);
        }
        if (personNotificationsDto.getApplicationUpcoming() != null && personNotificationsDto.getApplicationUpcoming().isActive()) {
            mailNotifications.add(NOTIFICATION_EMAIL_APPLICATION_UPCOMING);
        }
        if (personNotificationsDto.getHolidayReplacement() != null && personNotificationsDto.getHolidayReplacement().isActive()) {
            mailNotifications.add(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT);
        }
        if (personNotificationsDto.getHolidayReplacementUpcoming() != null && personNotificationsDto.getHolidayReplacementUpcoming().isActive()) {
            mailNotifications.add(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING);
        }

        return mailNotifications;
    }

    static PersonNotificationsDto mapToPersonNotificationsDto(Person person) {

        final List<MailNotification> mailNotifications = new ArrayList<>(person.getNotifications());

        final PersonNotificationsDto personNotificationsDto = new PersonNotificationsDto();

        for (MailNotification mailNotification : MailNotification.values()) {
            switch (mailNotification) {
                case NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL: {
                    personNotificationsDto.setApplicationManagementAll(new PersonNotificationDto(
                        person.hasRole(Role.BOSS) || person.hasRole(Role.OFFICE),
                        mailNotifications.contains(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL)
                    ));
                    break;
                }
                case NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL: {
                    personNotificationsDto.setPersonNewManagementAll(new PersonNotificationDto(
                        person.hasRole(Role.BOSS) || person.hasRole(Role.OFFICE),
                        mailNotifications.contains(NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL)
                    ));
                    break;
                }
                case NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_ALL: {
                    personNotificationsDto.setOvertimeManagementAll(new PersonNotificationDto(
                        person.hasRole(Role.BOSS) || person.hasRole(Role.OFFICE),
                        mailNotifications.contains(NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_ALL)
                    ));
                    break;
                }

                case NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED:
                case NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED:
                case NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED:
                case NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED:
                case NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED:
                case NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION:
                case NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED:
                case NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED: {
                    final List<MailNotification> notificationEmailApplicationsManagement = List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED,
                        NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED,
                        NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION,
                        NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED);
                    personNotificationsDto.setApplicationAppliedAndChangesForManagement(new PersonNotificationDto(
                        person.hasRole(Role.BOSS) || person.hasRole(Role.DEPARTMENT_HEAD) || person.hasRole(Role.SECOND_STAGE_AUTHORITY),
                        mailNotifications.stream().anyMatch(notificationEmailApplicationsManagement::contains)
                    ));
                    break;
                }
                case NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER: {
                    personNotificationsDto.setApplicationWaitingReminderForManagement(new PersonNotificationDto(
                        person.hasRole(Role.BOSS) || person.hasRole(Role.DEPARTMENT_HEAD) || person.hasRole(Role.SECOND_STAGE_AUTHORITY),
                        mailNotifications.contains(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER)
                    ));
                    break;
                }
                case NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED: {
                    personNotificationsDto.setApplicationCancellationRequestedForManagement(new PersonNotificationDto(
                        person.hasRole(Role.OFFICE) || ((person.hasRole(Role.BOSS) || person.hasRole(Role.DEPARTMENT_HEAD) || person.hasRole(Role.SECOND_STAGE_AUTHORITY)) && person.hasRole(Role.APPLICATION_CANCELLATION_REQUESTED)),
                        mailNotifications.contains(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED)
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
                    final List<MailNotification> notificationEmailApplications = List.of(NOTIFICATION_EMAIL_APPLICATION_APPLIED,
                        NOTIFICATION_EMAIL_APPLICATION_ALLOWED, NOTIFICATION_EMAIL_APPLICATION_REVOKED, NOTIFICATION_EMAIL_APPLICATION_REJECTED,
                        NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED, NOTIFICATION_EMAIL_APPLICATION_CANCELLATION, NOTIFICATION_EMAIL_APPLICATION_EDITED,
                        NOTIFICATION_EMAIL_APPLICATION_CONVERTED);
                    personNotificationsDto.setApplicationAppliedAndChanges(new PersonNotificationDto(
                        true,
                        mailNotifications.stream().anyMatch(notificationEmailApplications::contains)
                    ));
                    break;
                }
                case NOTIFICATION_EMAIL_APPLICATION_UPCOMING: {
                    personNotificationsDto.setApplicationUpcoming(new PersonNotificationDto(
                        true,
                        mailNotifications.contains(NOTIFICATION_EMAIL_APPLICATION_UPCOMING)
                    ));
                    break;
                }
                case NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT: {
                    personNotificationsDto.setHolidayReplacement(new PersonNotificationDto(
                        true,
                        mailNotifications.contains(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT)
                    ));
                    break;
                }
                case NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING: {
                    personNotificationsDto.setHolidayReplacementUpcoming(new PersonNotificationDto(
                        true,
                        mailNotifications.contains(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING)
                    ));
                    break;
                }
            }
        }

        final List<PersonNotificationDto> dtoNotifications = List.of(
            personNotificationsDto.getApplicationManagementAll(),
            personNotificationsDto.getPersonNewManagementAll(),
            personNotificationsDto.getOvertimeManagementAll(),
            personNotificationsDto.getApplicationAppliedAndChangesForManagement(),
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
}
