package org.synyx.urlaubsverwaltung.dev;

import org.slf4j.Logger;
import org.synyx.urlaubsverwaltung.person.Person;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.WEEKS;
import static java.time.temporal.ChronoUnit.YEARS;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.SPECIALLEAVE;
import static org.synyx.urlaubsverwaltung.dev.TestUser.BOSS;
import static org.synyx.urlaubsverwaltung.dev.TestUser.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.dev.TestUser.OFFICE;
import static org.synyx.urlaubsverwaltung.dev.TestUser.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteCategory.SICK_NOTE_CHILD;


public class TestDataCreationService {

    private static final String NO_PASSWORD = "";

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonDataProvider personDataProvider;
    private final ApplicationForLeaveDataProvider applicationForLeaveDataProvider;
    private final SickNoteDataProvider sickNoteDataProvider;
    private final OvertimeRecordDataProvider overtimeRecordDataProvider;
    private final DepartmentDataProvider departmentDataProvider;
    private final TestDataProperties testDataProperties;
    private final Clock clock;

    public TestDataCreationService(PersonDataProvider personDataProvider, ApplicationForLeaveDataProvider applicationForLeaveDataProvider,
                                   SickNoteDataProvider sickNoteDataProvider, OvertimeRecordDataProvider overtimeRecordDataProvider,
                                   DepartmentDataProvider departmentDataProvider, TestDataProperties testDataProperties, Clock clock) {
        this.personDataProvider = personDataProvider;
        this.applicationForLeaveDataProvider = applicationForLeaveDataProvider;
        this.sickNoteDataProvider = sickNoteDataProvider;
        this.overtimeRecordDataProvider = overtimeRecordDataProvider;
        this.departmentDataProvider = departmentDataProvider;
        this.testDataProperties = testDataProperties;
        this.clock = clock;
    }

    @PostConstruct
    public void createTestData() {

        LOG.info(">> TestData Creation (uv.development.testdata.create={})", testDataProperties.isCreate());

        if (personDataProvider.isPersonAlreadyCreated(TestUser.USER.getUsername())) {
            LOG.info("-> Test data was already created. Abort.");
            return;
        }

        LOG.info("-> Starting test data creation...");
        // Users to be able to SIGN-IN with
        final Person user = personDataProvider.createTestPerson(TestUser.USER, "Klaus", "Müller", "user@firma.test");
        final Person departmentHead = personDataProvider.createTestPerson(DEPARTMENT_HEAD, "Thorsten", "Krüger", "departmentHead@firma.test");
        final Person boss = personDataProvider.createTestPerson(BOSS, "Max", "Mustermann", "boss@firma.test");
        final Person office = personDataProvider.createTestPerson(OFFICE, "Marlene", "Muster", "office@firma.test");
        final Person secondStageAuthority = personDataProvider.createTestPerson(SECOND_STAGE_AUTHORITY, "Peter", "Huber", "secondStageAuthority@firma.test");
        personDataProvider.createTestPerson(TestUser.ADMIN, "Senor", "Operation", "admin@firma.test");

        // Users
        final Person hans = personDataProvider.createTestPerson("hdampf", NO_PASSWORD, "Hans", "Dampf", "dampf@firma.test", USER);
        final Person guenther = personDataProvider.createTestPerson("gbaier", NO_PASSWORD, "Günther", "Baier", "baier@firma.test", USER);
        final Person elena = personDataProvider.createTestPerson("eschneider", NO_PASSWORD, "Elena", "Schneider", "schneider@firma.test", USER);
        final Person brigitte = personDataProvider.createTestPerson("bhaendel", NO_PASSWORD, "Brigitte", "Händel", "haendel@firma.test", USER);
        final Person niko = personDataProvider.createTestPerson("nschmidt", NO_PASSWORD, "Niko", "Schmidt", "schmidt@firma.test", USER);

        personDataProvider.createTestPerson("horst", NO_PASSWORD, "Horst", "Dieter", "hdieter@firma.test", INACTIVE);

        // Departments
        final List<Person> adminDepartmentUser = asList(hans, brigitte, departmentHead, secondStageAuthority);
        final List<Person> adminDepartmentHeads = singletonList(departmentHead);
        final List<Person> adminSecondStageAuthorities = singletonList(secondStageAuthority);
        departmentDataProvider.createTestDepartment("Admins", "Das sind die, die so Admin Sachen machen", adminDepartmentUser, adminDepartmentHeads, adminSecondStageAuthorities);

        final List<Person> developmentMembers = asList(user, niko, departmentHead);
        departmentDataProvider.createTestDepartment("Entwicklung", "Das sind die, die so entwickeln", developmentMembers, emptyList(), emptyList());

        final List<Person> marketingMembers = asList(guenther, elena);
        departmentDataProvider.createTestDepartment("Marketing", "Das sind die, die so Marketing Sachen machen", marketingMembers, emptyList(), emptyList());

        final List<Person> bossMembers = asList(boss, office);
        departmentDataProvider.createTestDepartment("Geschäftsführung", "Das sind die, die so Geschäftsführung Sachen machen", bossMembers, emptyList(), emptyList());

        // Applications for leave and sick notes
        createTestData(user, boss, office);
        createTestData(boss, boss, office);
        createTestData(office, boss, office);
        createTestData(hans, boss, office);
        createTestData(niko, boss, office);
        createTestData(secondStageAuthority, boss, office);

        LOG.info("-> Test data was created");
    }


    private void createTestData(Person person, Person boss, Person office) {

        createApplicationsForLeave(person, boss, office);
        createSickNotes(person, office);
        createOvertimeRecords(person);
    }


    private void createApplicationsForLeave(Person person, Person boss, Person office) {

        final Instant now = Instant.now(clock);

        // FUTURE APPLICATIONS FOR LEAVE
        applicationForLeaveDataProvider.createWaitingApplication(person, HOLIDAY, FULL, now.plus(10, DAYS), now.plus(16, DAYS));
        applicationForLeaveDataProvider.createWaitingApplication(person, OVERTIME, FULL, now.plus(1, DAYS), now.plus(1, DAYS));
        applicationForLeaveDataProvider.createWaitingApplication(person, SPECIALLEAVE, FULL, now.plus(4, DAYS), now.plus(6, DAYS));

        // PAST APPLICATIONS FOR LEAVE
        applicationForLeaveDataProvider.createAllowedApplication(person, boss, HOLIDAY, FULL, now.minus(20, DAYS), now.minus(13, DAYS));
        applicationForLeaveDataProvider.createAllowedApplication(person, boss, HOLIDAY, MORNING, now.minus(5, DAYS), now.minus(5, DAYS));
        applicationForLeaveDataProvider.createAllowedApplication(person, boss, SPECIALLEAVE, MORNING, now.minus(9, DAYS), now.minus(9, DAYS));

        applicationForLeaveDataProvider.createRejectedApplication(person, boss, HOLIDAY, FULL, now.minus(33, DAYS), now.minus(30, DAYS));
        applicationForLeaveDataProvider.createRejectedApplication(person, boss, HOLIDAY, MORNING, now.minus(32, DAYS), now.minus(32, DAYS));

        applicationForLeaveDataProvider.createCancelledApplication(person, office, HOLIDAY, FULL, now.minus(11, DAYS), now.minus(10, DAYS));
        applicationForLeaveDataProvider.createCancelledApplication(person, office, HOLIDAY, NOON, now.minus(12, DAYS), now.minus(12, DAYS));
    }


    private void createSickNotes(Person person, Person office) {

        final Instant now = Instant.now(clock);

        // SICK NOTES
        sickNoteDataProvider.createSickNote(person, office, NOON, now.minus(10, DAYS), now.minus(10, DAYS), SICK_NOTE, false);
        sickNoteDataProvider.createSickNote(person, office, FULL, now.minus(2, DAYS), now.minus(2, DAYS), SICK_NOTE, false);
        sickNoteDataProvider.createSickNote(person, office, FULL, now.minus(30, DAYS), now.minus(25, DAYS), SICK_NOTE, true);

        // CHILD SICK NOTES
        sickNoteDataProvider.createSickNote(person, office, FULL, now.minus(40, DAYS), now.minus(38, DAYS), SICK_NOTE_CHILD, false);
    }


    private void createOvertimeRecords(Person person) {

        final Instant now = Instant.now(clock);

        final Instant lastWeek = now.minus(1, WEEKS);
        final Instant weekBeforeLast = now.minus(2, WEEKS);
        final Instant lastYear = now.minus(1, YEARS);

        overtimeRecordDataProvider.createOvertimeRecord(person, lastWeek.with(MONDAY), lastWeek.with(FRIDAY), new BigDecimal("2.5"));
        overtimeRecordDataProvider.createOvertimeRecord(person, weekBeforeLast.with(MONDAY), weekBeforeLast.with(FRIDAY), new BigDecimal("3"));
        overtimeRecordDataProvider.createOvertimeRecord(person, lastYear.with(MONDAY), lastYear.with(FRIDAY), new BigDecimal("4"));
    }
}
