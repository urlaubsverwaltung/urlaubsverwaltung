package org.synyx.urlaubsverwaltung.person.web;

import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.function.Predicate.not;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_CANCELLATION;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION;
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
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_APPLIED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_USER;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_CANCELLED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CANCELLED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CREATED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT_TO_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_EDITED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_USER;

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

        final List<MailNotification> notificationEmailApplications = List.of(
            NOTIFICATION_EMAIL_APPLICATION_APPLIED,
            NOTIFICATION_EMAIL_APPLICATION_ALLOWED,
            NOTIFICATION_EMAIL_APPLICATION_REVOKED,
            NOTIFICATION_EMAIL_APPLICATION_REJECTED,
            NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED,
            NOTIFICATION_EMAIL_APPLICATION_CANCELLATION,
            NOTIFICATION_EMAIL_APPLICATION_EDITED,
            NOTIFICATION_EMAIL_APPLICATION_CONVERTED
        );
        addIfActive(mailNotifications, personNotificationsDto.getApplicationAppliedAndChanges(), notificationEmailApplications);
        addIfActive(mailNotifications, personNotificationsDto.getApplicationUpcoming(), NOTIFICATION_EMAIL_APPLICATION_UPCOMING);
        addIfActive(mailNotifications, personNotificationsDto.getHolidayReplacement(), NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT);
        addIfActive(mailNotifications, personNotificationsDto.getHolidayReplacementUpcoming(), NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING);

        addIfActive(mailNotifications, personNotificationsDto.getOvertimeAppliedForManagement(), NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED);
        addIfActive(mailNotifications, personNotificationsDto.getOvertimeAppliedByManagement(), NOTIFICATION_EMAIL_OVERTIME_APPLIED_BY_MANAGEMENT);
        addIfActive(mailNotifications, personNotificationsDto.getOvertimeApplied(), NOTIFICATION_EMAIL_OVERTIME_APPLIED);

        final List<MailNotification> notificationEmailAbsenceColleagues = List.of(
            NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED,
            NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION,
            NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CREATED,
            NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CANCELLED
        );
        addIfActive(mailNotifications, personNotificationsDto.getAbsenceForColleagues(), notificationEmailAbsenceColleagues);

        final List<MailNotification> notificationEmailOwnSickNote = List.of(
            NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_USER,
            NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT,
            NOTIFICATION_EMAIL_SICK_NOTE_EDITED_BY_MANAGEMENT,
            NOTIFICATION_EMAIL_SICK_NOTE_CANCELLED_BY_MANAGEMENT
        );
        addIfActive(mailNotifications, personNotificationsDto.getOwnSickNoteSubmittedCreatedEditedCancelled(), notificationEmailOwnSickNote);
        addIfActive(mailNotifications, personNotificationsDto.getSickNoteCreatedByManagementForManagement(), NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT_TO_MANAGEMENT);
        addIfActive(mailNotifications, personNotificationsDto.getSickNoteSubmittedByUserForManagement(), NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_MANAGEMENT);
        addIfActive(mailNotifications, personNotificationsDto.getSickNoteAcceptedByManagementForManagement(), NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_MANAGEMENT);

        return mailNotifications;
    }

    static PersonNotificationsDto mapToPersonNotificationsDto(Person person, boolean userIsAllowedToSubmitSickNotes) {

        final List<MailNotification> activePersonMailNotifications = new ArrayList<>(person.getNotifications());
        final PersonNotificationsDto personNotificationsDto = new PersonNotificationsDto();

        final Map<MailNotification, Consumer<PersonNotificationDto>> setterByNotification = new EnumMap<>(MailNotification.class);

        // personal notifications
        setterByNotification.put(NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL, personNotificationsDto::setPersonNewManagementAll);
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
        setterByNotification.put(NOTIFICATION_EMAIL_OVERTIME_APPLIED, personNotificationsDto::setOvertimeApplied);
        setterByNotification.put(NOTIFICATION_EMAIL_OVERTIME_APPLIED_BY_MANAGEMENT, personNotificationsDto::setOvertimeAppliedByManagement);
        setterByNotification.put(NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_USER, personNotificationsDto::setOwnSickNoteSubmittedCreatedEditedCancelled);
        setterByNotification.put(NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT, personNotificationsDto::setOwnSickNoteSubmittedCreatedEditedCancelled);
        setterByNotification.put(NOTIFICATION_EMAIL_SICK_NOTE_EDITED_BY_MANAGEMENT, personNotificationsDto::setOwnSickNoteSubmittedCreatedEditedCancelled);
        setterByNotification.put(NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_USER, personNotificationsDto::setOwnSickNoteSubmittedCreatedEditedCancelled);
        setterByNotification.put(NOTIFICATION_EMAIL_SICK_NOTE_CANCELLED_BY_MANAGEMENT, personNotificationsDto::setOwnSickNoteSubmittedCreatedEditedCancelled);

        // department notifications
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
        setterByNotification.put(NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED, personNotificationsDto::setOvertimeAppliedForManagement);
        setterByNotification.put(NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED, personNotificationsDto::setAbsenceForColleagues);
        setterByNotification.put(NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION, personNotificationsDto::setAbsenceForColleagues);
        setterByNotification.put(NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT_TO_MANAGEMENT, personNotificationsDto::setSickNoteCreatedByManagementForManagement);
        setterByNotification.put(NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_MANAGEMENT, personNotificationsDto::setSickNoteSubmittedByUserForManagement);
        setterByNotification.put(NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_MANAGEMENT, personNotificationsDto::setSickNoteAcceptedByManagementForManagement);
        setterByNotification.put(NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CREATED, personNotificationsDto::setAbsenceForColleagues);
        setterByNotification.put(NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CANCELLED, personNotificationsDto::setAbsenceForColleagues);

        for (MailNotification mailNotificationToCheck : MailNotification.values()) {
            final boolean departmentRelated = mailNotificationToCheck.isDepartmentRelated();
            final boolean isVisible = mailNotificationToCheck.isValidWith(person.getPermissions()) && (!mailNotificationToCheck.isSickNoteSubmissionRelated() || userIsAllowedToSubmitSickNotes);
            final boolean isActive = activePersonMailNotifications.contains(mailNotificationToCheck);
            setterByNotification.get(mailNotificationToCheck).accept(new PersonNotificationDtoDepartmentAware(departmentRelated, isVisible, isActive));
        }

        final boolean isPersonalAll = isPersonalAllChecked(personNotificationsDto);
        personNotificationsDto.setAllPersonal(isPersonalAll);

        final boolean isDepartmentAll = isDepartmentAllChecked(personNotificationsDto);
        personNotificationsDto.setAllDepartment(isDepartmentAll);

        personNotificationsDto.setPersonId(person.getId());

        return personNotificationsDto;
    }

    private static boolean isPersonalAllChecked(PersonNotificationsDto personNotificationsDto) {

        final List<PersonNotificationDto> dtoPersonalNotifications = List.of(
            personNotificationsDto.getPersonNewManagementAll(),
            personNotificationsDto.getApplicationAppliedAndChanges(),
            personNotificationsDto.getApplicationUpcoming(),
            personNotificationsDto.getHolidayReplacement(),
            personNotificationsDto.getHolidayReplacementUpcoming(),
            personNotificationsDto.getOvertimeAppliedByManagement(),
            personNotificationsDto.getOvertimeApplied(),
            personNotificationsDto.getOwnSickNoteSubmittedCreatedEditedCancelled()
        );

        final List<PersonNotificationDtoDepartmentAware> visiblePersonal = dtoPersonalNotifications.stream()
            .map(PersonNotificationDtoDepartmentAware.class::cast)
            .filter(not(PersonNotificationDtoDepartmentAware::isDepartmentAware))
            .filter(PersonNotificationDto::isVisible)
            .toList();

        final long visiblePersonalCount = visiblePersonal.size();
        final long activePersonalCount = visiblePersonal.stream().filter(PersonNotificationDto::isActive).count();

        return visiblePersonalCount == activePersonalCount;
    }

    private static boolean isDepartmentAllChecked(PersonNotificationsDto personNotificationsDto) {

        final List<PersonNotificationDto> dtoDepartmentNotifications = List.of(
            personNotificationsDto.getApplicationAppliedForManagement(),
            personNotificationsDto.getApplicationAdaptedForManagement(),
            personNotificationsDto.getApplicationCancellationForManagement(),
            personNotificationsDto.getApplicationAllowedForManagement(),
            personNotificationsDto.getApplicationTemporaryAllowedForManagement(),
            personNotificationsDto.getApplicationWaitingReminderForManagement(),
            personNotificationsDto.getApplicationCancellationRequestedForManagement(),
            personNotificationsDto.getOvertimeAppliedForManagement(),
            personNotificationsDto.getAbsenceForColleagues(),
            personNotificationsDto.getSickNoteCreatedByManagementForManagement(),
            personNotificationsDto.getSickNoteSubmittedByUserForManagement(),
            personNotificationsDto.getSickNoteAcceptedByManagementForManagement()
        );

        final List<PersonNotificationDtoDepartmentAware> visibleDepartment = dtoDepartmentNotifications.stream()
            .map(PersonNotificationDtoDepartmentAware.class::cast)
            .filter(PersonNotificationDtoDepartmentAware::isDepartmentAware)
            .filter(PersonNotificationDto::isVisible)
            .toList();

        final long visibleDepartmentCount = visibleDepartment.size();
        final long activeDepartmentCount = visibleDepartment.stream().filter(PersonNotificationDto::isActive).count();

        return visibleDepartmentCount == activeDepartmentCount;
    }

    private static void addIfActive(List<MailNotification> activatedMailNotifications, PersonNotificationDto personNotificationDto, MailNotification mailNotification) {
        addIfActive(activatedMailNotifications, personNotificationDto, List.of(mailNotification));
    }

    private static void addIfActive(List<MailNotification> activatedMailNotifications, PersonNotificationDto personNotificationDto, List<MailNotification> mailNotifications) {
        if (personNotificationDto != null && personNotificationDto.isActive()) {
            activatedMailNotifications.addAll(mailNotifications);
        }
    }

    /**
     * Internal {@link PersonNotificationDto} to be able to calculate number of personal and department related notifications.
     */
    private static class PersonNotificationDtoDepartmentAware extends PersonNotificationDto {
        private final boolean departmentAware;

        private PersonNotificationDtoDepartmentAware(boolean departmentAware, boolean visible, boolean active) {
            super(visible, active);
            this.departmentAware = departmentAware;
        }

        public boolean isDepartmentAware() {
            return departmentAware;
        }
    }
}
