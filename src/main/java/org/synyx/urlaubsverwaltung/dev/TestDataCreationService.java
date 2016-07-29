package org.synyx.urlaubsverwaltung.dev;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

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

import java.math.BigDecimal;

import java.security.NoSuchAlgorithmException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
@ConditionalOnProperty("testdata.create")
public class TestDataCreationService {

    private static final String PASSWORD = "secret";
    private static final String NO_PASSWORD = "";

    private static final Logger LOG = Logger.getLogger(TestDataCreationService.class);

    @Autowired
    private PersonDataProvider personDataProvider;

    @Autowired
    private ApplicationForLeaveDataProvider applicationForLeaveDataProvider;

    @Autowired
    private SickNoteDataProvider sickNoteDataProvider;

    @Autowired
    private SickNoteTypeService sickNoteTypeService;

    @Autowired
    private VacationTypeService vacationTypeService;

    @Autowired
    private OvertimeRecordDataProvider overtimeRecordDataProvider;

    @Autowired
    private DepartmentDataProvider departmentDataProvider;

    private Person boss;
    private Person office;

    @PostConstruct
    public void createTestData() throws NoSuchAlgorithmException {

        LOG.info("STARTING CREATION OF TEST DATA --------------------------------------------------------------------");

        // Users to be able to sign in with
        Person user = personDataProvider.createTestPerson(TestUser.USER.getLogin(), PASSWORD, "Klaus", "Müller",
                "user@muster.de", TestUser.USER.getRoles());
        Person departmentHead = personDataProvider.createTestPerson(TestUser.DEPARTMENT_HEAD.getLogin(), PASSWORD,
                "Thorsten", "Krüger", "departmentHead@muster.de", TestUser.DEPARTMENT_HEAD.getRoles());
        boss = personDataProvider.createTestPerson(TestUser.BOSS.getLogin(), PASSWORD, "Max", "Mustermann",
                "boss@muster.de", TestUser.BOSS.getRoles());
        office = personDataProvider.createTestPerson(TestUser.OFFICE.getLogin(), PASSWORD, "Marlene", "Muster",
                "office@muster.de", TestUser.OFFICE.getRoles());

        Person manager = personDataProvider.createTestPerson(TestUser.SECOND_STAGE_AUTHORITY.getLogin(), PASSWORD,
                "Peter", "Huber", "secondStageAuthority@muster.de", TestUser.SECOND_STAGE_AUTHORITY.getRoles());

        // Users
        Person hans = personDataProvider.createTestPerson("hdampf", NO_PASSWORD, "Hans", "Dampf", "dampf@muster.de",
                Role.USER);
        Person guenther = personDataProvider.createTestPerson("gbaier", NO_PASSWORD, "Günther", "Baier",
                "baier@muster.de", Role.USER);
        Person elena = personDataProvider.createTestPerson("eschneider", NO_PASSWORD, "Elena", "Schneider",
                "schneider@muster.de", Role.USER);
        Person brigitte = personDataProvider.createTestPerson("bhaendel", NO_PASSWORD, "Brigitte", "Händel",
                "haendel@muster.de", Role.USER);
        Person niko = personDataProvider.createTestPerson("nschmidt", NO_PASSWORD, "Niko", "Schmidt",
                "schmidt@muster.de", Role.USER);

        personDataProvider.createTestPerson("horst", NO_PASSWORD, "Horst", "Dieter", "hdieter@muster.de",
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
        sickNoteDataProvider.createSickNote(person, office, DayLength.FULL, now.minusDays(15), now.minusDays(13), // NOSONAR
            sickNoteTypeChild, true);
        sickNoteDataProvider.createSickNote(person, office, DayLength.FULL, now.minusDays(20), now.minusDays(19), // NOSONAR
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
