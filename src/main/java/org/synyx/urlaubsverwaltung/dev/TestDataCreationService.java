package org.synyx.urlaubsverwaltung.dev;

import org.slf4j.Logger;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteType;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteTypeService;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.ZoneOffset.UTC;
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
    private final SickNoteTypeService sickNoteTypeService;
    private final VacationTypeService vacationTypeService;
    private final OvertimeRecordDataProvider overtimeRecordDataProvider;
    private final DepartmentDataProvider departmentDataProvider;

    public TestDataCreationService(PersonDataProvider personDataProvider, ApplicationForLeaveDataProvider applicationForLeaveDataProvider,
                                   SickNoteDataProvider sickNoteDataProvider, SickNoteTypeService sickNoteTypeService,
                                   VacationTypeService vacationTypeService, OvertimeRecordDataProvider overtimeRecordDataProvider,
                                   DepartmentDataProvider departmentDataProvider) {
        this.personDataProvider = personDataProvider;
        this.applicationForLeaveDataProvider = applicationForLeaveDataProvider;
        this.sickNoteDataProvider = sickNoteDataProvider;
        this.sickNoteTypeService = sickNoteTypeService;
        this.vacationTypeService = vacationTypeService;
        this.overtimeRecordDataProvider = overtimeRecordDataProvider;
        this.departmentDataProvider = departmentDataProvider;
    }

    @PostConstruct
    public void createTestData() {

        LOG.info("STARTING CREATION OF TEST DATA --------------------------------------------------------------------");

        // Users to be able to SIGN-IN with
        final Person user = personDataProvider.createTestPerson(TestUser.USER, "Klaus", "Müller", "user@firma.test");
        final Person departmentHead = personDataProvider.createTestPerson(DEPARTMENT_HEAD, "Thorsten", "Krüger", "departmentHead@firma.test");
        final Person boss = personDataProvider.createTestPerson(BOSS, "Max", "Mustermann", "boss@firma.test");
        final Person office = personDataProvider.createTestPerson(OFFICE, "Marlene", "Muster", "office@firma.test");
        final Person secondStageAuthority = personDataProvider.createTestPerson(SECOND_STAGE_AUTHORITY, "Peter", "Huber", "secondStageAuthority@firma.test");
        personDataProvider.createTestPerson(TestUser.ADMIN,"Senor", "Operation", "admin@firma.test");

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

        LOG.info("DONE CREATION OF TEST DATA ------------------------------------------------------------------------");
    }


    private void createTestData(Person person, Person boss, Person office) {

        createApplicationsForLeave(person, boss, office);
        createSickNotes(person, office);
        createOvertimeRecords(person);
    }


    private void createApplicationsForLeave(Person person, Person boss, Person office) {

        final LocalDate now = LocalDate.now(UTC);

        VacationType holiday = null;
        VacationType overtime = null;
        VacationType specialLeave = null;

        List<VacationType> vacationTypes = vacationTypeService.getVacationTypes();

        for (VacationType vacationType : vacationTypes) {
            if (vacationType.isOfCategory(HOLIDAY)) {
                holiday = vacationType;
            }

            if (vacationType.isOfCategory(OVERTIME)) {
                overtime = vacationType;
            }

            if (vacationType.isOfCategory(SPECIALLEAVE)) {
                specialLeave = vacationType;
            }
        }

        // FUTURE APPLICATIONS FOR LEAVE
        applicationForLeaveDataProvider.createWaitingApplication(person, holiday, FULL, now.plusDays(10), now.plusDays(16));
        applicationForLeaveDataProvider.createWaitingApplication(person, overtime, FULL, now.plusDays(1), now.plusDays(1));
        applicationForLeaveDataProvider.createWaitingApplication(person, specialLeave, FULL, now.plusDays(4), now.plusDays(6));

        // PAST APPLICATIONS FOR LEAVE
        applicationForLeaveDataProvider.createAllowedApplication(person, boss, holiday, FULL, now.minusDays(20), now.minusDays(13));
        applicationForLeaveDataProvider.createAllowedApplication(person, boss, holiday, MORNING, now.minusDays(5), now.minusDays(5));
        applicationForLeaveDataProvider.createAllowedApplication(person, boss, specialLeave, MORNING, now.minusDays(9), now.minusDays(9));

        applicationForLeaveDataProvider.createRejectedApplication(person, boss, holiday, FULL, now.minusDays(33), now.minusDays(30));

        applicationForLeaveDataProvider.createCancelledApplication(person, office, holiday, FULL, now.minusDays(11), now.minusDays(10));
    }


    private void createSickNotes(Person person, Person office) {

        final LocalDate now = LocalDate.now(UTC);

        SickNoteType sickNoteTypeStandard = null;
        SickNoteType sickNoteTypeChild = null;
        final List<SickNoteType> sickNoteTypes = sickNoteTypeService.getSickNoteTypes();

        for (SickNoteType sickNoteType : sickNoteTypes) {
            if (sickNoteType.isOfCategory(SICK_NOTE)) {
                sickNoteTypeStandard = sickNoteType;
            }

            if (sickNoteType.isOfCategory(SICK_NOTE_CHILD)) {
                sickNoteTypeChild = sickNoteType;
            }
        }

        // SICK NOTES
        sickNoteDataProvider.createSickNote(person, office, NOON, now.minusDays(10), now.minusDays(10), sickNoteTypeStandard, false);
        sickNoteDataProvider.createSickNote(person, office, FULL, now.minusDays(2), now.minusDays(2), sickNoteTypeStandard, false);
        sickNoteDataProvider.createSickNote(person, office, FULL, now.minusDays(30), now.minusDays(25), sickNoteTypeStandard, true);

        // CHILD SICK NOTES
        sickNoteDataProvider.createSickNote(person, office, FULL, now.minusDays(40), now.minusDays(38), sickNoteTypeChild, false);
    }


    private void createOvertimeRecords(Person person) {

        final LocalDate now = LocalDate.now(UTC);

        final LocalDate lastWeek = now.minusWeeks(1);
        final LocalDate weekBeforeLast = now.minusWeeks(2);
        final LocalDate lastYear = now.minusYears(1);

        overtimeRecordDataProvider.createOvertimeRecord(person, lastWeek.with(MONDAY), lastWeek.with(FRIDAY), new BigDecimal("2.5"));
        overtimeRecordDataProvider.createOvertimeRecord(person, weekBeforeLast.with(MONDAY), weekBeforeLast.with(FRIDAY), new BigDecimal("3"));
        overtimeRecordDataProvider.createOvertimeRecord(person, lastYear.with(MONDAY), lastYear.with(FRIDAY), new BigDecimal("4"));
    }
}
