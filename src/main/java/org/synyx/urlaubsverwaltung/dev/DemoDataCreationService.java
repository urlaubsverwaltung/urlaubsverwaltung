package org.synyx.urlaubsverwaltung.dev;

import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.SPECIALLEAVE;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
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
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_USER;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_CANCELLED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CANCELLED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CREATED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT_TO_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_EDITED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_USER;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;

public class DemoDataCreationService {

    private static final List<MailNotification> PERSON_NOTIFICATIONS = List.of(
        NOTIFICATION_EMAIL_APPLICATION_APPLIED,
        NOTIFICATION_EMAIL_APPLICATION_ALLOWED,
        NOTIFICATION_EMAIL_APPLICATION_REVOKED,
        NOTIFICATION_EMAIL_APPLICATION_REJECTED,
        NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED,
        NOTIFICATION_EMAIL_APPLICATION_CANCELLATION,
        NOTIFICATION_EMAIL_APPLICATION_EDITED,
        NOTIFICATION_EMAIL_APPLICATION_CONVERTED,
        NOTIFICATION_EMAIL_APPLICATION_UPCOMING,
        NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT,
        NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING,
        NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_USER,
        NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT,
        NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_USER,
        NOTIFICATION_EMAIL_SICK_NOTE_EDITED_BY_MANAGEMENT,
        NOTIFICATION_EMAIL_SICK_NOTE_CANCELLED_BY_MANAGEMENT,
        NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CREATED,
        NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CANCELLED
    );
    private static final List<MailNotification> MANAGEMENT_NOTIFICATIONS = List.of(
        NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED,
        NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED,
        NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED,
        NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED,
        NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED,
        NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION,
        NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED,
        NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED,
        NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER,
        NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT_TO_MANAGEMENT,
        NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER,
        NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_MANAGEMENT,
        NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_MANAGEMENT
    );
    private static final List<MailNotification> NOTIFICATIONS_WITH_MANAGEMENT_DEPARTMENT = Stream.concat(PERSON_NOTIFICATIONS.stream(), MANAGEMENT_NOTIFICATIONS.stream()).toList();

    // departments
    private static final String DEPARTMENT_ADMINS = "Admins";
    private static final String DEPARTMENT_ENTWICKLUNG = "Entwicklung";
    private static final String DEPARTMENT_MARKETING = "Marketing";
    private static final String DEPARTMENT_BOSS = "Geschäftsführung";

    // email addresses
    private static final String EMAIL_USER = "user@urlaubsverwaltung.cloud";
    private static final String EMAIL_DEPARTMENT_HEAD = "departmentHead@urlaubsverwaltung.cloud";
    private static final String EMAIL_SECOND_STAGE_AUTHORITY = "secondStageAuthority@urlaubsverwaltung.cloud";
    private static final String EMAIL_BOSS = "boss@urlaubsverwaltung.cloud";
    private static final String EMAIL_OFFICE = "office@urlaubsverwaltung.cloud";
    private static final String EMAIL_DAMPF = "dampf@urlaubsverwaltung.cloud";
    private static final String EMAIL_SCHMIDT = "schmidt@urlaubsverwaltung.cloud";

    private final PersonDataProvider personDataProvider;
    private final ApplicationForLeaveDataProvider applicationForLeaveDataProvider;
    private final SickNoteDataProvider sickNoteDataProvider;
    private final OvertimeRecordDataProvider overtimeRecordDataProvider;
    private final DepartmentDataProvider departmentDataProvider;
    private final Clock clock;

    private final SecureRandom random = new SecureRandom();

    DemoDataCreationService(PersonDataProvider personDataProvider, ApplicationForLeaveDataProvider applicationForLeaveDataProvider,
                            SickNoteDataProvider sickNoteDataProvider, OvertimeRecordDataProvider overtimeRecordDataProvider,
                            DepartmentDataProvider departmentDataProvider, Clock clock) {
        this.personDataProvider = personDataProvider;
        this.applicationForLeaveDataProvider = applicationForLeaveDataProvider;
        this.sickNoteDataProvider = sickNoteDataProvider;
        this.overtimeRecordDataProvider = overtimeRecordDataProvider;
        this.departmentDataProvider = departmentDataProvider;
        this.clock = clock;
    }

    void createDemoData(String email) {

        // Departments
        departmentDataProvider.createTestDepartment(DEPARTMENT_ADMINS, "Das sind die, die so Admin Sachen machen");
        departmentDataProvider.createTestDepartment(DEPARTMENT_ENTWICKLUNG, "Das sind die, die so entwickeln");
        departmentDataProvider.createTestDepartment(DEPARTMENT_MARKETING, "Das sind die, die so Marketing Sachen machen");
        departmentDataProvider.createTestDepartment(DEPARTMENT_BOSS, "Das sind die, die so Geschäftsführung Sachen machen");

        switch (email) {
            case EMAIL_USER:
                final Person person = personDataProvider.updateTestPerson(1, email, List.of(Role.USER), PERSON_NOTIFICATIONS);
                departmentDataProvider.addDepartmentMember(DEPARTMENT_ENTWICKLUNG, person);
                createDemoApplicationsAndSickNotes(EMAIL_USER);
                break;
            case EMAIL_DEPARTMENT_HEAD:
                final Person departmentHead = personDataProvider.updateTestPerson(2, email, List.of(Role.USER, Role.DEPARTMENT_HEAD), NOTIFICATIONS_WITH_MANAGEMENT_DEPARTMENT);
                departmentDataProvider.addDepartmentMember(DEPARTMENT_ADMINS, departmentHead);
                departmentDataProvider.addDepartmentMember(DEPARTMENT_ENTWICKLUNG, departmentHead);
                departmentDataProvider.addDepartmentHead(DEPARTMENT_ADMINS, departmentHead);
                break;
            case EMAIL_SECOND_STAGE_AUTHORITY:
                final Person secondStageAuthority = personDataProvider.updateTestPerson(3, email, List.of(Role.USER, Role.SECOND_STAGE_AUTHORITY), NOTIFICATIONS_WITH_MANAGEMENT_DEPARTMENT);
                departmentDataProvider.addDepartmentMember(DEPARTMENT_ADMINS, secondStageAuthority);
                departmentDataProvider.addDepartmentSecondStageAuthority(DEPARTMENT_ADMINS, secondStageAuthority);
                createDemoApplicationsAndSickNotes(EMAIL_SECOND_STAGE_AUTHORITY);
                break;
            case EMAIL_BOSS:
                final Person boss = personDataProvider.updateTestPerson(4, email, List.of(Role.USER, Role.BOSS), PERSON_NOTIFICATIONS);
                departmentDataProvider.addDepartmentMember(DEPARTMENT_BOSS, boss);
                createDemoApplicationsAndSickNotes(EMAIL_USER);
                createDemoApplicationsAndSickNotes(EMAIL_BOSS);
                createDemoApplicationsAndSickNotes(EMAIL_OFFICE);
                createDemoApplicationsAndSickNotes(EMAIL_DAMPF);
                createDemoApplicationsAndSickNotes(EMAIL_SCHMIDT);
                createDemoApplicationsAndSickNotes(EMAIL_SECOND_STAGE_AUTHORITY);
                break;
            case EMAIL_OFFICE:
                final Person office = personDataProvider.updateTestPerson(5, email, List.of(Role.USER, Role.OFFICE), PERSON_NOTIFICATIONS);
                departmentDataProvider.addDepartmentMember(DEPARTMENT_BOSS, office);
                createDemoApplicationsAndSickNotes(EMAIL_USER);
                createDemoApplicationsAndSickNotes(EMAIL_BOSS);
                createDemoApplicationsAndSickNotes(EMAIL_OFFICE);
                createDemoApplicationsAndSickNotes(EMAIL_DAMPF);
                createDemoApplicationsAndSickNotes(EMAIL_SCHMIDT);
                createDemoApplicationsAndSickNotes(EMAIL_SECOND_STAGE_AUTHORITY);
                break;
            case "admin@urlaubsverwaltung.cloud":
                personDataProvider.updateTestPerson(5, email, List.of(Role.USER), List.of());
                break;
            case EMAIL_DAMPF:
                final Person dampf = personDataProvider.updateTestPerson(6, email, List.of(Role.USER), PERSON_NOTIFICATIONS);
                departmentDataProvider.addDepartmentMember(DEPARTMENT_ADMINS, dampf);
                createDemoApplicationsAndSickNotes(EMAIL_DAMPF);
                break;
            case "baier@urlaubsverwaltung.cloud":
                final Person baier = personDataProvider.updateTestPerson(7, email, List.of(Role.USER), PERSON_NOTIFICATIONS);
                departmentDataProvider.addDepartmentMember(DEPARTMENT_MARKETING, baier);
                break;
            case "schneider@urlaubsverwaltung.cloud":
                final Person schneider = personDataProvider.updateTestPerson(8, email, List.of(Role.USER), PERSON_NOTIFICATIONS);
                departmentDataProvider.addDepartmentMember(DEPARTMENT_MARKETING, schneider);
                break;
            case "haendel@urlaubsverwaltung.cloud":
                final Person haendel = personDataProvider.updateTestPerson(9, email, List.of(Role.USER), PERSON_NOTIFICATIONS);
                departmentDataProvider.addDepartmentMember(DEPARTMENT_ADMINS, haendel);
                break;
            case EMAIL_SCHMIDT:
                final Person schmidt = personDataProvider.updateTestPerson(10, email, List.of(Role.USER), PERSON_NOTIFICATIONS);
                departmentDataProvider.addDepartmentMember(DEPARTMENT_ENTWICKLUNG, schmidt);
                createDemoApplicationsAndSickNotes(EMAIL_SCHMIDT);
                break;
            case "hdieter@urlaubsverwaltung.cloud":
                personDataProvider.updateTestPerson(11, email, List.of(Role.USER), PERSON_NOTIFICATIONS);
                break;
            default:
                personDataProvider.updateTestPerson(randomPersonnelNumber(), email, List.of(Role.USER), PERSON_NOTIFICATIONS);
                break;
        }
    }

    private int randomPersonnelNumber() {
        return random.nextInt(1000 - 11 + 1) + 11;
    }


    private void createDemoApplicationsAndSickNotes(String personEmail) {

        Optional<Person> person = personDataProvider.getPersonByMailAddress(personEmail);
        Optional<Person> boss = personDataProvider.getPersonByMailAddress(EMAIL_BOSS);
        Optional<Person> office = personDataProvider.getPersonByMailAddress(EMAIL_OFFICE);

        if (person.isPresent() && boss.isPresent() && office.isPresent()) {
            createApplicationsForLeave(person.get(), boss.get(), office.get());
            createSickNotes(person.get(), office.get());
            createOvertimeRecords(person.get());
        }
    }

    private void createApplicationsForLeave(Person person, Person boss, Person office) {

        final LocalDate now = LocalDate.now(clock);

        // FUTURE APPLICATIONS FOR LEAVE
        applicationForLeaveDataProvider.createWaitingApplication(person, HOLIDAY, FULL, now.plusDays(10), now.plusDays(16));
        applicationForLeaveDataProvider.createWaitingApplication(person, OVERTIME, FULL, now.plusDays(1), now.plusDays(1));
        applicationForLeaveDataProvider.createWaitingApplication(person, SPECIALLEAVE, FULL, now.plusDays(4), now.plusDays(6));

        // PAST APPLICATIONS FOR LEAVE
        applicationForLeaveDataProvider.createAllowedApplication(person, boss, HOLIDAY, FULL, now.minusDays(20), now.minusDays(13));
        applicationForLeaveDataProvider.createAllowedApplication(person, boss, HOLIDAY, MORNING, now.minusDays(5), now.minusDays(5));
        applicationForLeaveDataProvider.createAllowedApplication(person, boss, SPECIALLEAVE, MORNING, now.minusDays(9), now.minusDays(9));

        applicationForLeaveDataProvider.createRejectedApplication(person, boss, HOLIDAY, FULL, now.minusDays(33), now.minusDays(30));
        applicationForLeaveDataProvider.createRejectedApplication(person, boss, HOLIDAY, MORNING, now.minusDays(32), now.minusDays(32));

        applicationForLeaveDataProvider.createCancelledApplication(person, boss, office, HOLIDAY, FULL, now.minusDays(22), now.minusDays(21));
        applicationForLeaveDataProvider.createCancelledApplication(person, boss, office, HOLIDAY, NOON, now.minusDays(12), now.minusDays(12));
    }

    private void createSickNotes(Person person, Person office) {

        final LocalDate now = LocalDate.now(clock);

        // SICK NOTES
        sickNoteDataProvider.createSickNote(person, office, NOON, now.minusDays(10), now.minusDays(10), SICK_NOTE, false);
        sickNoteDataProvider.createSickNote(person, office, FULL, now.minusDays(2), now.minusDays(2), SICK_NOTE, false);
        sickNoteDataProvider.createSickNote(person, office, FULL, now.minusDays(30), now.minusDays(25), SICK_NOTE, true);

        // CHILD SICK NOTES
        sickNoteDataProvider.createSickNote(person, office, FULL, now.minusDays(40), now.minusDays(38), SICK_NOTE_CHILD, false);
    }

    private void createOvertimeRecords(Person person) {

        final LocalDate now = LocalDate.now(clock);

        final LocalDate lastWeek = now.minusWeeks(1);
        final LocalDate weekBeforeLast = now.minusWeeks(2);
        final LocalDate lastYear = now.minusYears(1);

        overtimeRecordDataProvider.activateOvertime();
        overtimeRecordDataProvider.createOvertimeRecord(person, lastWeek.with(MONDAY), lastWeek.with(FRIDAY), Duration.ofMinutes(150L));
        overtimeRecordDataProvider.createOvertimeRecord(person, weekBeforeLast.with(MONDAY), weekBeforeLast.with(FRIDAY), Duration.ofHours(3L));
        overtimeRecordDataProvider.createOvertimeRecord(person, lastYear.with(MONDAY), lastYear.with(FRIDAY), Duration.ofHours(4L));
    }
}
