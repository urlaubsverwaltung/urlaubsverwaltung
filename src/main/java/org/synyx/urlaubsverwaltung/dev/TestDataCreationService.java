package org.synyx.urlaubsverwaltung.dev;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteCategory;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteType;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteTypeService;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
@ConditionalOnProperty("testdata.create")
public class TestDataCreationService {

    private static final String PASSWORD = "secret";
    private static final String NO_PASSWORD = "";

    private static final Logger LOG = LoggerFactory.getLogger(TestDataCreationService.class);

    private final PersonDataProvider personDataProvider;
    private final ApplicationForLeaveDataProvider applicationForLeaveDataProvider;
    private final SickNoteDataProvider sickNoteDataProvider;
    private final SickNoteTypeService sickNoteTypeService;
    private final VacationTypeService vacationTypeService;
    private final OvertimeRecordDataProvider overtimeRecordDataProvider;
    private final DepartmentDataProvider departmentDataProvider;

    private Person boss;
    private Person office;
    private Person admin;

    @Autowired
    public TestDataCreationService(PersonDataProvider personDataProvider, ApplicationForLeaveDataProvider applicationForLeaveDataProvider, SickNoteDataProvider sickNoteDataProvider, SickNoteTypeService sickNoteTypeService, VacationTypeService vacationTypeService, OvertimeRecordDataProvider overtimeRecordDataProvider, DepartmentDataProvider departmentDataProvider) {
        this.personDataProvider = personDataProvider;
        this.applicationForLeaveDataProvider = applicationForLeaveDataProvider;
        this.sickNoteDataProvider = sickNoteDataProvider;
        this.sickNoteTypeService = sickNoteTypeService;
        this.vacationTypeService = vacationTypeService;
        this.overtimeRecordDataProvider = overtimeRecordDataProvider;
        this.departmentDataProvider = departmentDataProvider;
    }

    @PostConstruct
    public void createTestData() throws NoSuchAlgorithmException {

        LOG.info("STARTING CREATION OF TEST DATA --------------------------------------------------------------------");

        // Users to be able to sign in with
        Person user = personDataProvider.createTestPerson(TestUser.USER.getLogin(), PASSWORD, "Klaus", "Müller",
                "user@firma.test", TestUser.USER.getRoles());
        Person departmentHead = personDataProvider.createTestPerson(TestUser.DEPARTMENT_HEAD.getLogin(), PASSWORD,
                "Thorsten", "Krüger", "departmentHead@firma.test", TestUser.DEPARTMENT_HEAD.getRoles());
        boss = personDataProvider.createTestPerson(TestUser.BOSS.getLogin(), PASSWORD, "Max", "Mustermann",
                "boss@firma.test", TestUser.BOSS.getRoles());
        office = personDataProvider.createTestPerson(TestUser.OFFICE.getLogin(), PASSWORD, "Marlene", "Muster",
                "office@firma.test", TestUser.OFFICE.getRoles());

        admin = personDataProvider.createTestPerson("admin", PASSWORD, "Senor", "Operation", "admin@firma.test", TestUser.ADMIN.getRoles());

        Person manager = personDataProvider.createTestPerson(TestUser.SECOND_STAGE_AUTHORITY.getLogin(), PASSWORD,
                "Peter", "Huber", "secondStageAuthority@firma.test", TestUser.SECOND_STAGE_AUTHORITY.getRoles());

        // Users
        Person hans = personDataProvider.createTestPerson("hdampf", NO_PASSWORD, "Hans", "Dampf", "dampf@firma.test",
                Role.USER);
        Person guenther = personDataProvider.createTestPerson("gbaier", NO_PASSWORD, "Günther", "Baier",
                "baier@firma.test", Role.USER);
        Person elena = personDataProvider.createTestPerson("eschneider", NO_PASSWORD, "Elena", "Schneider",
                "schneider@firma.test", Role.USER);
        Person brigitte = personDataProvider.createTestPerson("bhaendel", NO_PASSWORD, "Brigitte", "Händel",
                "haendel@firma.test", Role.USER);
        Person niko = personDataProvider.createTestPerson("nschmidt", NO_PASSWORD, "Niko", "Schmidt",
                "schmidt@firma.test", Role.USER);

        personDataProvider.createTestPerson("horst", NO_PASSWORD, "Horst", "Dieter", "hdieter@firma.test",
            Role.INACTIVE);

        // Departments
        departmentDataProvider.createTestDepartment("Admins", "Das sind die, die so Admin Sachen machen",
            Arrays.asList(hans, brigitte, departmentHead, manager), Collections.singletonList(departmentHead),
            Collections.singletonList(manager));
        departmentDataProvider.createTestDepartment("Entwicklung", "Das sind die, die so entwickeln",
            Arrays.asList(user, niko, departmentHead), Collections.emptyList(), Collections.emptyList());
        departmentDataProvider.createTestDepartment("Marketing", "Das sind die, die so Marketing Sachen machen",
            Arrays.asList(guenther, elena), Collections.emptyList(), Collections.emptyList());
        departmentDataProvider.createTestDepartment("Geschäftsführung",
            "Das sind die, die so Geschäftsführung Sachen machen", Arrays.asList(boss, office), Collections.emptyList(),
            Collections.emptyList());

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

        DateMidnight now = DateMidnight.now();

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
        applicationForLeaveDataProvider.createWaitingApplication(person, holiday, DayLength.FULL, now.plusDays(10), // NOSONAR
            now.plusDays(16)); // NOSONAR
        applicationForLeaveDataProvider.createWaitingApplication(person, overtime, DayLength.FULL, now.plusDays(1), // NOSONAR
            now.plusDays(1)); // NOSONAR
        applicationForLeaveDataProvider.createWaitingApplication(person, specialLeave, DayLength.FULL, now.plusDays(4), // NOSONAR
            now.plusDays(6)); // NOSONAR

        // PAST APPLICATIONS FOR LEAVE
        applicationForLeaveDataProvider.createAllowedApplication(person, boss, holiday, DayLength.FULL,
            now.minusDays(20), now.minusDays(13)); // NOSONAR
        applicationForLeaveDataProvider.createAllowedApplication(person, boss, holiday, DayLength.MORNING,
            now.minusDays(5), now.minusDays(5)); // NOSONAR
        applicationForLeaveDataProvider.createAllowedApplication(person, boss, specialLeave, DayLength.MORNING,
            now.minusDays(9), // NOSONAR
            now.minusDays(9)); // NOSONAR

        applicationForLeaveDataProvider.createRejectedApplication(person, boss, holiday, DayLength.FULL,
            now.minusDays(33), now.minusDays(30)); // NOSONAR

        applicationForLeaveDataProvider.createCancelledApplication(person, office, holiday, DayLength.FULL,
            now.minusDays(11), now.minusDays(10)); // NOSONAR

        if ("hdampf".equals(person.getLoginName()) && headOf != null) {
            applicationForLeaveDataProvider.createPremilinaryAllowedApplication(person, headOf, holiday, DayLength.FULL,
                now.plusDays(5), now.plusDays(8)); // NOSONAR
        }
    }


    private void createSickNotes(Person person) {

        DateMidnight now = DateMidnight.now();

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
        sickNoteDataProvider.createSickNote(person, office, DayLength.FULL, now.minusDays(2), now.minusDays(2), // NOSONAR
            sickNoteTypeStandard, false);
        sickNoteDataProvider.createSickNote(person, office, DayLength.FULL, now.minusDays(30), now.minusDays(25), // NOSONAR
            sickNoteTypeStandard, true);

        // CHILD SICK NOTES
        sickNoteDataProvider.createSickNote(person, office, DayLength.FULL, now.minusDays(40), now.minusDays(38), // NOSONAR
            sickNoteTypeChild, false);
    }


    private void createOvertimeRecords(Person person) {

        DateMidnight now = DateMidnight.now();

        DateMidnight lastWeek = now.minusWeeks(1);
        DateMidnight weekBeforeLast = now.minusWeeks(2);
        DateMidnight lastYear = now.minusYears(1);

        overtimeRecordDataProvider.createOvertimeRecord(person, lastWeek.withDayOfWeek(DateTimeConstants.MONDAY),
            lastWeek.withDayOfWeek(DateTimeConstants.FRIDAY), new BigDecimal("2.5")); // NOSONAR

        overtimeRecordDataProvider.createOvertimeRecord(person, weekBeforeLast.withDayOfWeek(DateTimeConstants.MONDAY),
            weekBeforeLast.withDayOfWeek(DateTimeConstants.FRIDAY), new BigDecimal("3")); // NOSONAR

        overtimeRecordDataProvider.createOvertimeRecord(person, lastYear.withDayOfWeek(DateTimeConstants.MONDAY),
            lastYear.withDayOfWeek(DateTimeConstants.FRIDAY), new BigDecimal("4")); // NOSONAR
    }
}
