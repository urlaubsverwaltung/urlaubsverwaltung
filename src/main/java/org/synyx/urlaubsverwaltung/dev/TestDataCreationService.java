package org.synyx.urlaubsverwaltung.dev;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.core.calendar.WorkDaysService;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.core.overtime.Overtime;
import org.synyx.urlaubsverwaltung.core.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteInteractionService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteType;
import org.synyx.urlaubsverwaltung.core.util.CalcUtil;

import java.math.BigDecimal;

import java.security.NoSuchAlgorithmException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

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
    private OvertimeRecordDataProvider overtimeRecordDataProvider;

    @Autowired
    private DepartmentDataProvider departmentDataProvider;

    private Person boss;
    private Person office;

    @PostConstruct
    public void createTestData() throws NoSuchAlgorithmException {

        LOG.info("Test data will be created...");

        // Users to be able to sign in with
        Person user = personDataProvider.createTestPerson(TestUser.USER.getLogin(), PASSWORD, "Klaus", "Müller",
                "mueller@muster.de", TestUser.USER.getRoles());
        Person departmentHead = personDataProvider.createTestPerson(TestUser.DEPARTMENT_HEAD.getLogin(), PASSWORD,
                "Thorsten", "Krüger", "krueger@muster.de", TestUser.DEPARTMENT_HEAD.getRoles());
        boss = personDataProvider.createTestPerson(TestUser.BOSS.getLogin(), PASSWORD, "Max", "Mustermann",
                "maxMuster@muster.de", TestUser.BOSS.getRoles());
        office = personDataProvider.createTestPerson(TestUser.OFFICE.getLogin(), PASSWORD, "Marlene", "Muster",
                "mmuster@muster.de", TestUser.OFFICE.getRoles());

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

        // Applications for leave and sick notes
        createTestData(user);
        createTestData(boss);
        createTestData(office);
        createTestData(hans);
        createTestData(niko);

        // Departments
        departmentDataProvider.createTestDepartment("Admins", "Das sind die, die so Admin Sachen machen",
            Arrays.asList(hans, brigitte, departmentHead), Collections.singletonList(departmentHead));
        departmentDataProvider.createTestDepartment("Entwicklung", "Das sind die, die so entwickeln",
            Arrays.asList(user, niko, departmentHead), Collections.emptyList());
        departmentDataProvider.createTestDepartment("Marketing", "Das sind die, die so Marketing Sachen machen",
            Arrays.asList(guenther, elena), Collections.emptyList());
        departmentDataProvider.createTestDepartment("Geschäftsführung",
            "Das sind die, die so Geschäftsführung Sachen machen", Arrays.asList(boss, office),
            Collections.emptyList());
    }


    private void createTestData(Person person) {

        createApplicationsForLeave(person);
        createSickNotes(person);
        createOvertimeRecords(person);
    }


    private void createApplicationsForLeave(Person person) {

        DateMidnight now = DateMidnight.now();

        // FUTURE APPLICATIONS FOR LEAVE
        applicationForLeaveDataProvider.createWaitingApplication(person, VacationType.HOLIDAY, DayLength.FULL,
            now.plusDays(10), now.plusDays(16)); // NOSONAR
        applicationForLeaveDataProvider.createWaitingApplication(person, VacationType.OVERTIME, DayLength.FULL,
            now.plusDays(1), now.plusDays(1)); // NOSONAR
        applicationForLeaveDataProvider.createWaitingApplication(person, VacationType.SPECIALLEAVE, DayLength.FULL,
            now.plusDays(4), now.plusDays(6)); // NOSONAR

        // PAST APPLICATIONS FOR LEAVE
        applicationForLeaveDataProvider.createAllowedApplication(person, boss, VacationType.HOLIDAY, DayLength.FULL,
            now.minusDays(20), now.minusDays(13)); // NOSONAR
        applicationForLeaveDataProvider.createAllowedApplication(person, boss, VacationType.HOLIDAY, DayLength.MORNING,
            now.minusDays(5), now.minusDays(5)); // NOSONAR
        applicationForLeaveDataProvider.createAllowedApplication(person, boss, VacationType.SPECIALLEAVE,
            DayLength.MORNING, now.minusDays(9), // NOSONAR
            now.minusDays(9)); // NOSONAR

        applicationForLeaveDataProvider.createRejectedApplication(person, boss, VacationType.HOLIDAY, DayLength.FULL,
            now.minusDays(33), now.minusDays(30)); // NOSONAR

        applicationForLeaveDataProvider.createCancelledApplication(person, office, VacationType.HOLIDAY, DayLength.FULL,
            now.minusDays(11), now.minusDays(10)); // NOSONAR
    }


    private void createSickNotes(Person person) {

        DateMidnight now = DateMidnight.now();

        // SICK NOTES
        sickNoteDataProvider.createSickNote(person, office, DayLength.NOON, now.minusDays(10), now.minusDays(10), // NOSONAR
            SickNoteType.SICK_NOTE, false);
        sickNoteDataProvider.createSickNote(person, office, DayLength.FULL, now.minusDays(2), now.minusDays(2), // NOSONAR
            SickNoteType.SICK_NOTE, false);
        sickNoteDataProvider.createSickNote(person, office, DayLength.FULL, now.minusDays(30), now.minusDays(25), // NOSONAR
            SickNoteType.SICK_NOTE, true);

        // CHILD SICK NOTES
        sickNoteDataProvider.createSickNote(person, office, DayLength.FULL, now.minusDays(60), now.minusDays(55), // NOSONAR
            SickNoteType.SICK_NOTE_CHILD, true);
        sickNoteDataProvider.createSickNote(person, office, DayLength.FULL, now.minusDays(44), now.minusDays(44), // NOSONAR
            SickNoteType.SICK_NOTE_CHILD, false);
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
