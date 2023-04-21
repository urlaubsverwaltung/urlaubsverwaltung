package org.synyx.urlaubsverwaltung.dev;

import org.slf4j.Logger;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

import javax.annotation.PostConstruct;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
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
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;

public class DemoDataCreationService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonDataProvider personDataProvider;
    private final ApplicationForLeaveDataProvider applicationForLeaveDataProvider;
    private final SickNoteDataProvider sickNoteDataProvider;
    private final OvertimeRecordDataProvider overtimeRecordDataProvider;
    private final DepartmentDataProvider departmentDataProvider;
    private final DemoDataProperties demoDataProperties;
    private final Clock clock;

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

    @PostConstruct
    public void createDemoData() {

        LOG.info(">> Demo data creation (uv.development.demodata.create={})", demoDataProperties.isCreate());

        if (personDataProvider.isPersonAlreadyCreated("user@urlaubsverwaltung.cloud")) {
            LOG.info("-> Demo data was already created. Abort.");
            return;
        }

        LOG.info("-> Starting demo data creation...");

        final List<MailNotification> personNotifications = List.of(
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
            NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING
        );

        final List<MailNotification> managementNotifications = List.of(
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

        final List<MailNotification> notificationsWithManagementDepartment = Stream.concat(personNotifications.stream(), managementNotifications.stream()).collect(toList());

        final Person user = personDataProvider.createTestPerson("user", 1, "Klaus", "Müller", "user@urlaubsverwaltung.cloud", List.of(Role.USER), personNotifications);
        final Person departmentHead = personDataProvider.createTestPerson("departmentHead", 2, "Thorsten", "Krüger", "departmentHead@urlaubsverwaltung.cloud", List.of(Role.USER, Role.DEPARTMENT_HEAD), notificationsWithManagementDepartment);
        final Person secondStageAuthority = personDataProvider.createTestPerson("secondStageAuthority", 3, "Juliane", "Huber", "secondStageAuthority@urlaubsverwaltung.cloud", List.of(Role.USER, Role.SECOND_STAGE_AUTHORITY), notificationsWithManagementDepartment);
        final Person boss = personDataProvider.createTestPerson("boss", 4, "Theresa", "Scherer", "boss@urlaubsverwaltung.cloud", List.of(Role.USER, Role.BOSS), personNotifications);
        final Person office = personDataProvider.createTestPerson("office", 5, "Marlene", "Muster", "office@urlaubsverwaltung.cloud", List.of(Role.USER, Role.OFFICE), personNotifications);
        personDataProvider.createTestPerson("admin", 6, "Anne", "Roth", "admin@urlaubsverwaltung.cloud", List.of(Role.USER, Role.ADMIN), List.of());

        // Users
        int personnelNumber = 100;
        final Person hans = personDataProvider.createTestPerson("hdampf", personnelNumber++, "Hans", "Dampf", "dampf@urlaubsverwaltung.cloud", List.of(Role.USER), personNotifications);
        final Person franziska = personDataProvider.createTestPerson("fbaier", personnelNumber++, "Franziska", "Baier", "baier@urlaubsverwaltung.cloud", List.of(Role.USER), personNotifications);
        final Person elena = personDataProvider.createTestPerson("eschneider", personnelNumber++, "Elena", "Schneider", "schneider@urlaubsverwaltung.cloud", List.of(Role.USER), personNotifications);
        final Person brigitte = personDataProvider.createTestPerson("bhaendel", personnelNumber++, "Brigitte", "Händel", "haendel@urlaubsverwaltung.cloud", List.of(Role.USER), personNotifications);
        final Person niko = personDataProvider.createTestPerson("nschmidt", personnelNumber++, "Niko", "Schmidt", "schmidt@urlaubsverwaltung.cloud", List.of(Role.USER), personNotifications);
        personDataProvider.createTestPerson("heinz", personnelNumber, "Holger", "Dieter", "hdieter@urlaubsverwaltung.cloud", List.of(INACTIVE), List.of());

        IntStream.rangeClosed(0, demoDataProperties.getAdditionalActiveUser())
            .forEach(i -> personDataProvider.createTestPerson("horst-active-" + i, i + 42, "Horst", "Aktiv", "hdieter-active@urlaubsverwaltung.cloud", List.of(Role.USER), personNotifications));

        IntStream.rangeClosed(0, demoDataProperties.getAdditionalInactiveUser())
            .forEach(i -> personDataProvider.createTestPerson("horst-inactive-" + i, i + 21, "Horst", "Inaktiv", "hdieter-inactive@urlaubsverwaltung.cloud", List.of(INACTIVE), List.of()));

        // Departments
        final List<Person> adminDepartmentUser = asList(hans, brigitte, departmentHead, secondStageAuthority);
        final List<Person> adminDepartmentHeads = singletonList(departmentHead);
        final List<Person> adminSecondStageAuthorities = singletonList(secondStageAuthority);
        departmentDataProvider.createTestDepartment("Admins", "Das sind die, die so Admin Sachen machen", adminDepartmentUser, adminDepartmentHeads, adminSecondStageAuthorities);

        final List<Person> developmentMembers = asList(user, niko, departmentHead);
        departmentDataProvider.createTestDepartment("Entwicklung", "Das sind die, die so entwickeln", developmentMembers, emptyList(), emptyList());

        final List<Person> marketingMembers = asList(franziska, elena);
        departmentDataProvider.createTestDepartment("Marketing", "Das sind die, die so Marketing Sachen machen", marketingMembers, emptyList(), emptyList());

        final List<Person> bossMembers = asList(boss, office);
        departmentDataProvider.createTestDepartment("Geschäftsführung", "Das sind die, die so Geschäftsführung Sachen machen", bossMembers, emptyList(), emptyList());

        // Applications for leave and sick notes
        createDemoData(user, boss, office);
        createDemoData(boss, boss, office);
        createDemoData(office, boss, office);
        createDemoData(hans, boss, office);
        createDemoData(niko, boss, office);
        createDemoData(secondStageAuthority, boss, office);

        LOG.info("-> Demo data was created");
    }

    private void createDemoData(Person person, Person boss, Person office) {
        createApplicationsForLeave(person, boss, office);
        createSickNotes(person, office);
        createOvertimeRecords(person);
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
