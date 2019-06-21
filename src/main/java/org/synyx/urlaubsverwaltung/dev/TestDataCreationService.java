package org.synyx.urlaubsverwaltung.dev;

import org.slf4j.Logger;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteCategory;
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
import static org.synyx.urlaubsverwaltung.dev.TestUser.BOSS;
import static org.synyx.urlaubsverwaltung.dev.TestUser.OFFICE;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;


public class TestDataCreationService {

    private static final String PASSWORD = "secret";
    private static final String NO_PASSWORD = "";

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonDataProvider personDataProvider;
    private final ApplicationForLeaveDataProvider applicationForLeaveDataProvider;
    private final SickNoteDataProvider sickNoteDataProvider;
    private final SickNoteTypeService sickNoteTypeService;
    private final VacationTypeService vacationTypeService;
    private final OvertimeRecordDataProvider overtimeRecordDataProvider;
    private final DepartmentDataProvider departmentDataProvider;

    private Person boss;
    private Person office;

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

        // Users to be able to sign in with
        Person user = personDataProvider.createTestPerson(TestUser.USER.getLogin(), PASSWORD, "Klaus", "Müller", "user@firma.test", TestUser.USER.getRoles());
        Person departmentHead = personDataProvider.createTestPerson(TestUser.DEPARTMENT_HEAD.getLogin(), PASSWORD, "Thorsten", "Krüger", "departmentHead@firma.test", TestUser.DEPARTMENT_HEAD.getRoles());
        boss = personDataProvider.createTestPerson(BOSS.getLogin(), PASSWORD, "Max", "Mustermann", "boss@firma.test", BOSS.getRoles());
        office = personDataProvider.createTestPerson(OFFICE.getLogin(), PASSWORD, "Marlene", "Muster", "office@firma.test", OFFICE.getRoles());

        personDataProvider.createTestPerson("admin", PASSWORD, "Senor", "Operation", "admin@firma.test", TestUser.ADMIN.getRoles());

        Person manager = personDataProvider.createTestPerson(TestUser.SECOND_STAGE_AUTHORITY.getLogin(), PASSWORD,
            "Peter", "Huber", "secondStageAuthority@firma.test", TestUser.SECOND_STAGE_AUTHORITY.getRoles());

        // Users
        Person hans = personDataProvider.createTestPerson("hdampf", NO_PASSWORD, "Hans", "Dampf", "dampf@firma.test", USER);
        Person guenther = personDataProvider.createTestPerson("gbaier", NO_PASSWORD, "Günther", "Baier", "baier@firma.test", USER);
        Person elena = personDataProvider.createTestPerson("eschneider", NO_PASSWORD, "Elena", "Schneider", "schneider@firma.test", USER);
        Person brigitte = personDataProvider.createTestPerson("bhaendel", NO_PASSWORD, "Brigitte", "Händel", "haendel@firma.test", USER);
        Person niko = personDataProvider.createTestPerson("nschmidt", NO_PASSWORD, "Niko", "Schmidt", "schmidt@firma.test", USER);

        personDataProvider.createTestPerson("horst", NO_PASSWORD, "Horst", "Dieter", "hdieter@firma.test", INACTIVE);

        // Departments
        departmentDataProvider.createTestDepartment("Admins", "Das sind die, die so Admin Sachen machen",
            asList(hans, brigitte, departmentHead, manager), singletonList(departmentHead), singletonList(manager));
        departmentDataProvider.createTestDepartment("Entwicklung", "Das sind die, die so entwickeln",
            asList(user, niko, departmentHead), emptyList(), emptyList());
        departmentDataProvider.createTestDepartment("Marketing", "Das sind die, die so Marketing Sachen machen",
            asList(guenther, elena), emptyList(), emptyList());
        departmentDataProvider.createTestDepartment("Geschäftsführung",
            "Das sind die, die so Geschäftsführung Sachen machen", asList(boss, office), emptyList(),
            emptyList());

        // Applications for leave and sick notes
        createTestData(user);
        createTestData(boss);
        createTestData(office);
        createTestData(hans);
        createTestData(niko);
        createTestData(manager);

        LOG.info("DONE CREATION OF TEST DATA ------------------------------------------------------------------------");
    }


    private void createTestData(Person person) {

        createApplicationsForLeave(person, null);
        createSickNotes(person);
        createOvertimeRecords(person);
    }


    private void createApplicationsForLeave(Person person, Person headOf) {

        LocalDate now = LocalDate.now(UTC);

        VacationType holiday = null;
        VacationType overtime = null;
        VacationType specialLeave = null;

        List<VacationType> vacationTypes = vacationTypeService.getVacationTypes();

        for (VacationType vacationType : vacationTypes) {
            if (vacationType.isOfCategory(VacationCategory.HOLIDAY)) {
                holiday = vacationType;
            }

            if (vacationType.isOfCategory(VacationCategory.OVERTIME)) {
                overtime = vacationType;
            }

            if (vacationType.isOfCategory(VacationCategory.SPECIALLEAVE)) {
                specialLeave = vacationType;
            }
        }

        // FUTURE APPLICATIONS FOR LEAVE
        applicationForLeaveDataProvider.createWaitingApplication(person, holiday, FULL, now.plusDays(10), // NOSONAR
            now.plusDays(16)); // NOSONAR
        applicationForLeaveDataProvider.createWaitingApplication(person, overtime, FULL, now.plusDays(1), // NOSONAR
            now.plusDays(1)); // NOSONAR
        applicationForLeaveDataProvider.createWaitingApplication(person, specialLeave, FULL, now.plusDays(4), // NOSONAR
            now.plusDays(6)); // NOSONAR

        // PAST APPLICATIONS FOR LEAVE
        applicationForLeaveDataProvider.createAllowedApplication(person, boss, holiday, FULL,
            now.minusDays(20), now.minusDays(13)); // NOSONAR
        applicationForLeaveDataProvider.createAllowedApplication(person, boss, holiday, MORNING,
            now.minusDays(5), now.minusDays(5)); // NOSONAR
        applicationForLeaveDataProvider.createAllowedApplication(person, boss, specialLeave, MORNING,
            now.minusDays(9), now.minusDays(9)); // NOSONAR

        applicationForLeaveDataProvider.createRejectedApplication(person, boss, holiday, FULL, now.minusDays(33), now.minusDays(30)); // NOSONAR

        applicationForLeaveDataProvider.createCancelledApplication(person, office, holiday, FULL,
            now.minusDays(11), now.minusDays(10)); // NOSONAR

        if ("hdampf".equals(person.getLoginName()) && headOf != null) {
            applicationForLeaveDataProvider.createPremilinaryAllowedApplication(person, headOf, holiday, FULL,
                now.plusDays(5), now.plusDays(8)); // NOSONAR
        }
    }


    private void createSickNotes(Person person) {

        LocalDate now = LocalDate.now(UTC);

        SickNoteType sickNoteTypeStandard = null;
        SickNoteType sickNoteTypeChild = null;
        List<SickNoteType> sickNoteTypes = sickNoteTypeService.getSickNoteTypes();

        for (SickNoteType sickNoteType : sickNoteTypes) {
            if (sickNoteType.isOfCategory(SickNoteCategory.SICK_NOTE)) {
                sickNoteTypeStandard = sickNoteType;
            }

            if (sickNoteType.isOfCategory(SickNoteCategory.SICK_NOTE_CHILD)) {
                sickNoteTypeChild = sickNoteType;
            }
        }

        // SICK NOTES
        sickNoteDataProvider.createSickNote(person, office, DayLength.NOON, now.minusDays(10), now.minusDays(10), // NOSONAR
            sickNoteTypeStandard, false);
        sickNoteDataProvider.createSickNote(person, office, FULL, now.minusDays(2), now.minusDays(2), // NOSONAR
            sickNoteTypeStandard, false);
        sickNoteDataProvider.createSickNote(person, office, FULL, now.minusDays(30), now.minusDays(25), // NOSONAR
            sickNoteTypeStandard, true);

        // CHILD SICK NOTES
        sickNoteDataProvider.createSickNote(person, office, FULL, now.minusDays(40), now.minusDays(38), // NOSONAR
            sickNoteTypeChild, false);
    }


    private void createOvertimeRecords(Person person) {

        LocalDate now = LocalDate.now(UTC);

        LocalDate lastWeek = now.minusWeeks(1);
        LocalDate weekBeforeLast = now.minusWeeks(2);
        LocalDate lastYear = now.minusYears(1);

        overtimeRecordDataProvider.createOvertimeRecord(person, lastWeek.with(MONDAY),
            lastWeek.with(FRIDAY), new BigDecimal("2.5")); // NOSONAR

        overtimeRecordDataProvider.createOvertimeRecord(person, weekBeforeLast.with(MONDAY),
            weekBeforeLast.with(FRIDAY), new BigDecimal("3")); // NOSONAR

        overtimeRecordDataProvider.createOvertimeRecord(person, lastYear.with(MONDAY),
            lastYear.with(FRIDAY), new BigDecimal("4")); // NOSONAR
    }
}
