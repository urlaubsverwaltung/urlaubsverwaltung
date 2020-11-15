package org.synyx.urlaubsverwaltung.dev;

import org.slf4j.Logger;
import org.synyx.urlaubsverwaltung.person.Person;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.SPECIALLEAVE;
import static org.synyx.urlaubsverwaltung.dev.DemoUser.BOSS;
import static org.synyx.urlaubsverwaltung.dev.DemoUser.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.dev.DemoUser.OFFICE;
import static org.synyx.urlaubsverwaltung.dev.DemoUser.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteCategory.SICK_NOTE_CHILD;


public class DemoDataCreationService {

    private static final String NO_PASSWORD_HASH = "{pbkdf2}1705db0e2b2e0f81fa9a0500ee21f16fe3f0b2f8370ba0fbba77bd07c072d292f3d251985686ec49";

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

        if (personDataProvider.isPersonAlreadyCreated(DemoUser.USER.getUsername())) {
            LOG.info("-> Demo data was already created. Abort.");
            return;
        }

        LOG.info("-> Starting demo data creation...");
        // Users to be able to SIGN-IN with
        final Person user = personDataProvider.createTestPerson(DemoUser.USER, "Klaus", "Müller", "user@example.org");
        final Person departmentHead = personDataProvider.createTestPerson(DEPARTMENT_HEAD, "Thorsten", "Krüger", "departmentHead@example.org");
        final Person boss = personDataProvider.createTestPerson(BOSS, "Max", "Mustermann", "boss@example.org");
        final Person office = personDataProvider.createTestPerson(OFFICE, "Marlene", "Muster", "office@example.org");
        final Person secondStageAuthority = personDataProvider.createTestPerson(SECOND_STAGE_AUTHORITY, "Peter", "Huber", "secondStageAuthority@example.org");
        personDataProvider.createTestPerson(DemoUser.ADMIN, "Senor", "Operation", "admin@example.org");

        // Users
        final Person hans = personDataProvider.createTestPerson("hdampf", NO_PASSWORD_HASH, "Hans", "Dampf", "dampf@example.org", USER);
        final Person guenther = personDataProvider.createTestPerson("gbaier", NO_PASSWORD_HASH, "Günther", "Baier", "baier@example.org", USER);
        final Person elena = personDataProvider.createTestPerson("eschneider", NO_PASSWORD_HASH, "Elena", "Schneider", "schneider@example.org", USER);
        final Person brigitte = personDataProvider.createTestPerson("bhaendel", NO_PASSWORD_HASH, "Brigitte", "Händel", "haendel@example.org", USER);
        final Person niko = personDataProvider.createTestPerson("nschmidt", NO_PASSWORD_HASH, "Niko", "Schmidt", "schmidt@example.org", USER);

        personDataProvider.createTestPerson("horst", NO_PASSWORD_HASH, "Horst", "Dieter", "hdieter@example.org", INACTIVE);

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

        applicationForLeaveDataProvider.createCancelledApplication(person, office, HOLIDAY, FULL, now.minusDays(11), now.minusDays(10));
        applicationForLeaveDataProvider.createCancelledApplication(person, office, HOLIDAY, NOON, now.minusDays(12), now.minusDays(12));
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
        overtimeRecordDataProvider.createOvertimeRecord(person, lastWeek.with(MONDAY), lastWeek.with(FRIDAY), new BigDecimal("2.5"));
        overtimeRecordDataProvider.createOvertimeRecord(person, weekBeforeLast.with(MONDAY), weekBeforeLast.with(FRIDAY), new BigDecimal("3"));
        overtimeRecordDataProvider.createOvertimeRecord(person, lastYear.with(MONDAY), lastYear.with(FRIDAY), new BigDecimal("4"));
    }
}
