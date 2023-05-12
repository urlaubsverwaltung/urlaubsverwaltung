package org.synyx.urlaubsverwaltung.person.web;

import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_ABSENCE_COLLEAGUES_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_APPLIED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL;

final class PersonNotificationsMapper {

    private PersonNotificationsMapper() {
        // ok
    }

    static List<MailNotification> mapToMailNotifications(PersonNotificationsDto personNotificationsDto) {

        final List<MailNotification> mailNotifications = new ArrayList<>();
        addIfActive(mailNotifications, personNotificationsDto.getPersonNewManagementAll(), NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL);

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

        addIfActive(mailNotifications, personNotificationsDto.getOvertimeAppliedForManagement(), NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED);
        addIfActive(mailNotifications, personNotificationsDto.getOvertimeAppliedByManagement(), NOTIFICATION_EMAIL_OVERTIME_APPLIED_BY_MANAGEMENT);
        addIfActive(mailNotifications, personNotificationsDto.getOvertimeApplied(), NOTIFICATION_EMAIL_OVERTIME_APPLIED);

        addIfActive(mailNotifications, personNotificationsDto.getAbsenceForColleagues(), List.of(NOTIFICATION_EMAIL_ABSENCE_COLLEAGUES_ALLOWED));

        return mailNotifications;
    }

    static PersonNotificationsDto mapToPersonNotificationsDto(Person person) {

        final List<MailNotification> activePersonMailNotifications = new ArrayList<>(person.getNotifications());
        final PersonNotificationsDto personNotificationsDto = new PersonNotificationsDto();

        final Map<MailNotification, Consumer<PersonNotificationDto>> setterByNotification = new EnumMap<>(MailNotification.class);
        setterByNotification.put(NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL, personNotificationsDto::setPersonNewManagementAll);
        setterByNotification.put(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED, personNotificationsDto::setApplicationAppliedForManagement);
        setterByNotification.put(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED, personNotificationsDto::setApplicationAdaptedForManagement);
        setterByNotification.put(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED, personNotificationsDto::setApplicationAdaptedForManagement);
        setterByNotification.put(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED, personNotificationsDto::setApplicationCancellationForManagement);
        setterByNotification.put(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED, personNotificationsDto::setApplicationCancellationForManagement);
        setterByNotification.put(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION, personNotificationsDto::setApplicationCancellationForManagement);
        setterByNotification.put(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED, personNotificationsDto::setApplicationAllowedForManagement);
        setterByNotification.put(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED, personNotificationsDto::setApplicationTemporaryAllowedForManagement);
        setterByNotification.put(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER, personNotificationsDto::setApplicationWaitingReminderForManagement);
        setterByNotification.put(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED, personNotificationsDto::setApplicationCancellationRequestedForManagement);
        setterByNotification.put(NOTIFICATION_EMAIL_APPLICATION_APPLIED, personNotificationsDto::setApplicationAppliedAndChanges);
        setterByNotification.put(NOTIFICATION_EMAIL_APPLICATION_ALLOWED, personNotificationsDto::setApplicationAppliedAndChanges);
        setterByNotification.put(NOTIFICATION_EMAIL_APPLICATION_REVOKED, personNotificationsDto::setApplicationAppliedAndChanges);
        setterByNotification.put(NOTIFICATION_EMAIL_APPLICATION_REJECTED, personNotificationsDto::setApplicationAppliedAndChanges);
        setterByNotification.put(NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED, personNotificationsDto::setApplicationAppliedAndChanges);
        setterByNotification.put(NOTIFICATION_EMAIL_APPLICATION_CANCELLATION, personNotificationsDto::setApplicationAppliedAndChanges);
        setterByNotification.put(NOTIFICATION_EMAIL_APPLICATION_EDITED, personNotificationsDto::setApplicationAppliedAndChanges);
        setterByNotification.put(NOTIFICATION_EMAIL_APPLICATION_CONVERTED, personNotificationsDto::setApplicationAppliedAndChanges);
        setterByNotification.put(NOTIFICATION_EMAIL_APPLICATION_UPCOMING, personNotificationsDto::setApplicationUpcoming);
        setterByNotification.put(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT, personNotificationsDto::setHolidayReplacement);
        setterByNotification.put(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING, personNotificationsDto::setHolidayReplacementUpcoming);
        setterByNotification.put(NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED, personNotificationsDto::setOvertimeAppliedForManagement);
        setterByNotification.put(NOTIFICATION_EMAIL_OVERTIME_APPLIED_BY_MANAGEMENT, personNotificationsDto::setOvertimeAppliedByManagement);
        setterByNotification.put(NOTIFICATION_EMAIL_OVERTIME_APPLIED, personNotificationsDto::setOvertimeApplied);
        setterByNotification.put(NOTIFICATION_EMAIL_ABSENCE_COLLEAGUES_ALLOWED, personNotificationsDto::setAbsenceForColleagues);

        for (MailNotification mailNotificationToCheck : MailNotification.values()) {
            final boolean isVisible = mailNotificationToCheck.isValidWith(person.getPermissions());
            final boolean isActive = activePersonMailNotifications.contains(mailNotificationToCheck);
            setterByNotification.get(mailNotificationToCheck).accept(new PersonNotificationDto(isVisible, isActive));
        }

        final List<PersonNotificationDto> dtoNotifications = List.of(
            personNotificationsDto.getPersonNewManagementAll(),
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
            personNotificationsDto.getHolidayReplacementUpcoming(),
            personNotificationsDto.getOvertimeAppliedForManagement(),
            personNotificationsDto.getOvertimeAppliedByManagement(),
            personNotificationsDto.getOvertimeApplied(),
            personNotificationsDto.getAbsenceForColleagues()
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
