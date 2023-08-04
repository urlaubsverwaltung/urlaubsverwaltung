package org.synyx.urlaubsverwaltung.dev;

import org.slf4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonCreatedEvent;
import org.synyx.urlaubsverwaltung.person.Role;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;
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
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;

public class DemoDataCreationService {

    public static final List<MailNotification> PERSON_NOTIFICATIONS = List.of(NOTIFICATION_EMAIL_APPLICATION_APPLIED, NOTIFICATION_EMAIL_APPLICATION_ALLOWED, NOTIFICATION_EMAIL_APPLICATION_REVOKED, NOTIFICATION_EMAIL_APPLICATION_REJECTED, NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED, NOTIFICATION_EMAIL_APPLICATION_CANCELLATION, NOTIFICATION_EMAIL_APPLICATION_EDITED, NOTIFICATION_EMAIL_APPLICATION_CONVERTED, NOTIFICATION_EMAIL_APPLICATION_UPCOMING, NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT, NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING);
    public static final List<MailNotification> MANAGEMENT_NOTIFICATIONS = List.of(
            NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED,
            NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED,
            NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED,
            NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED,
            NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED,
            NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION,
            NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED,
            NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED,
            NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER
    );
    public static final List<MailNotification> NOTIFICATIONS_WITH_MANAGEMENT_DEPARTMENT = Stream.concat(PERSON_NOTIFICATIONS.stream(), MANAGEMENT_NOTIFICATIONS.stream()).collect(toList());
    private static final Logger LOG = getLogger(lookup().lookupClass());
    public static final String DEPARTMENT_ADMINS = "Admins";
    public static final String DEPARTMENT_ENTWICKLUNG = "Entwicklung";
    public static final String DEPARTMENT_MARKETING = "Marketing";
    public static final String DEPARTMENT_BOSS = "Geschäftsführung";

    private final PersonDataProvider personDataProvider;
    private final ApplicationForLeaveDataProvider applicationForLeaveDataProvider;
    private final SickNoteDataProvider sickNoteDataProvider;
    private final OvertimeRecordDataProvider overtimeRecordDataProvider;
    private final DepartmentDataProvider departmentDataProvider;
    private final DemoDataProperties demoDataProperties;
    private final Clock clock;

    private final Random random = new Random();

    public DemoDataCreationService(PersonDataProvider personDataProvider, ApplicationForLeaveDataProvider applicationForLeaveDataProvider,
                                   SickNoteDataProvider sickNoteDataProvider, OvertimeRecordDataProvider overtimeRecordDataProvider,
                                   DepartmentDataProvider departmentDataProvider, DemoDataProperties demoDataProperties, Clock clock) {
        this.personDataProvider = personDataProvider;
        this.applicationForLeaveDataProvider = applicationForLeaveDataProvider;
        this.sickNoteDataProvider = sickNoteDataProvider;
        this.overtimeRecordDataProvider = overtimeRecordDataProvider;
        this.departmentDataProvider = departmentDataProvider;
        this.demoDataProperties = demoDataProperties;
        this.clock = clock;
    }

    @Async
    @EventListener
    void on(PersonCreatedEvent event) {

        final String email = event.getEmail();
        if (email == null || email.isEmpty()) {
            return;
        }

        // Departments
        departmentDataProvider.createTestDepartment(DEPARTMENT_ADMINS, "Das sind die, die so Admin Sachen machen");
        departmentDataProvider.createTestDepartment(DEPARTMENT_ENTWICKLUNG, "Das sind die, die so entwickeln");
        departmentDataProvider.createTestDepartment(DEPARTMENT_MARKETING, "Das sind die, die so Marketing Sachen machen");
        departmentDataProvider.createTestDepartment(DEPARTMENT_BOSS, "Das sind die, die so Geschäftsführung Sachen machen");

        switch (email) {
            case "user@urlaubsverwaltung.cloud":
                final Person person = personDataProvider.updateTestPerson(1, event.getEmail(), List.of(Role.USER), PERSON_NOTIFICATIONS);
                departmentDataProvider.addDepartmentMember(DEPARTMENT_ENTWICKLUNG, person);
                createDemoApplicationsAndSickNotes("user@urlaubsverwaltung.cloud");
                break;
            case "departmentHead@urlaubsverwaltung.cloud":
                final Person departmentHead = personDataProvider.updateTestPerson(2, event.getEmail(), List.of(Role.USER, Role.DEPARTMENT_HEAD), NOTIFICATIONS_WITH_MANAGEMENT_DEPARTMENT);
                departmentDataProvider.addDepartmentMember(DEPARTMENT_ADMINS, departmentHead);
                departmentDataProvider.addDepartmentMember(DEPARTMENT_ENTWICKLUNG, departmentHead);
                departmentDataProvider.addDepartmentHead(DEPARTMENT_ADMINS, departmentHead);
                break;
            case "secondStageAuthority@urlaubsverwaltung.cloud":
                final Person secondStageAuthority = personDataProvider.updateTestPerson(3, event.getEmail(), List.of(Role.USER, Role.SECOND_STAGE_AUTHORITY), NOTIFICATIONS_WITH_MANAGEMENT_DEPARTMENT);
                departmentDataProvider.addDepartmentMember(DEPARTMENT_ADMINS, secondStageAuthority);
                departmentDataProvider.addDepartmentSecondStageAuthority(DEPARTMENT_ADMINS, secondStageAuthority);
                createDemoApplicationsAndSickNotes("secondStageAuthority@urlaubsverwaltung.cloud");
                break;
            case "boss@urlaubsverwaltung.cloud":
                final Person boss = personDataProvider.updateTestPerson(4, event.getEmail(), List.of(Role.USER, Role.BOSS), PERSON_NOTIFICATIONS);
                departmentDataProvider.addDepartmentMember(DEPARTMENT_BOSS, boss);
                createDemoApplicationsAndSickNotes("user@urlaubsverwaltung.cloud");
                createDemoApplicationsAndSickNotes("boss@urlaubsverwaltung.cloud");
                createDemoApplicationsAndSickNotes("office@urlaubsverwaltung.cloud");
                createDemoApplicationsAndSickNotes("dampf@urlaubsverwaltung.cloud");
                createDemoApplicationsAndSickNotes("schmidt@urlaubsverwaltung.cloud");
                createDemoApplicationsAndSickNotes("secondStageAuthority@urlaubsverwaltung.cloud");

                break;
            case "office@urlaubsverwaltung.cloud":
                final Person office = personDataProvider.updateTestPerson(5, event.getEmail(), List.of(Role.USER, Role.OFFICE), PERSON_NOTIFICATIONS);
                departmentDataProvider.addDepartmentMember(DEPARTMENT_BOSS, office);
                createDemoApplicationsAndSickNotes("user@urlaubsverwaltung.cloud");
                createDemoApplicationsAndSickNotes("boss@urlaubsverwaltung.cloud");
                createDemoApplicationsAndSickNotes("office@urlaubsverwaltung.cloud");
                createDemoApplicationsAndSickNotes("dampf@urlaubsverwaltung.cloud");
                createDemoApplicationsAndSickNotes("schmidt@urlaubsverwaltung.cloud");
                createDemoApplicationsAndSickNotes("secondStageAuthority@urlaubsverwaltung.cloud");
                break;
            case "admin@urlaubsverwaltung.cloud":
                personDataProvider.updateTestPerson(5, event.getEmail(), List.of(Role.USER, Role.ADMIN), List.of());
                break;
            case "dampf@urlaubsverwaltung.cloud":
                final Person dampf = personDataProvider.updateTestPerson(6, event.getEmail(), List.of(Role.USER, Role.USER), PERSON_NOTIFICATIONS);
                departmentDataProvider.addDepartmentMember(DEPARTMENT_ADMINS, dampf);
                createDemoApplicationsAndSickNotes("dampf@urlaubsverwaltung.cloud");
                break;
            case "baier@urlaubsverwaltung.cloud":
                final Person baier = personDataProvider.updateTestPerson(7, event.getEmail(), List.of(Role.USER, Role.USER), PERSON_NOTIFICATIONS);
                departmentDataProvider.addDepartmentMember(DEPARTMENT_MARKETING, baier);
                break;
            case "schneider@urlaubsverwaltung.cloud":
                final Person schneider = personDataProvider.updateTestPerson(8, event.getEmail(), List.of(Role.USER, Role.USER), PERSON_NOTIFICATIONS);
                departmentDataProvider.addDepartmentMember(DEPARTMENT_MARKETING, schneider);
                break;
            case "haendel@urlaubsverwaltung.cloud":
                final Person haendel = personDataProvider.updateTestPerson(9, event.getEmail(), List.of(Role.USER, Role.USER), PERSON_NOTIFICATIONS);
                departmentDataProvider.addDepartmentMember(DEPARTMENT_ADMINS, haendel);
                break;
            case "schmidt@urlaubsverwaltung.cloud":
                final Person schmidt = personDataProvider.updateTestPerson(10, event.getEmail(), List.of(Role.USER, Role.USER), PERSON_NOTIFICATIONS);
                departmentDataProvider.addDepartmentMember(DEPARTMENT_ENTWICKLUNG, schmidt);
                createDemoApplicationsAndSickNotes("schmidt@urlaubsverwaltung.cloud");
                break;
            case "hdieter@urlaubsverwaltung.cloud":
                personDataProvider.updateTestPerson(11, event.getEmail(), List.of(Role.USER, Role.USER), PERSON_NOTIFICATIONS);
                break;
            default:
                personDataProvider.updateTestPerson(randomPersonnelNumber(), event.getEmail(), List.of(Role.USER, Role.USER), PERSON_NOTIFICATIONS);
                break;
        }
    }

    private int randomPersonnelNumber() {
        return random.nextInt(1000 - 11 + 1) + 11;
    }


    private void createDemoApplicationsAndSickNotes(String personEmail) {

        Optional<Person> person = personDataProvider.getPersonByMailAddress(personEmail);
        Optional<Person> boss = personDataProvider.getPersonByMailAddress("boss@urlaubsverwaltung.cloud");
        Optional<Person> office = personDataProvider.getPersonByMailAddress("office@urlaubsverwaltung.cloud");

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

        applicationForLeaveDataProvider.createCancelledApplication(person, boss, office, HOLIDAY, FULL, now.minusDays(11), now.minusDays(10));
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
