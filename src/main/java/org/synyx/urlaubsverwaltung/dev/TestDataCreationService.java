package org.synyx.urlaubsverwaltung.dev;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.core.calendar.WorkDaysService;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.Day;
import org.synyx.urlaubsverwaltung.core.department.Department;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.overtime.Overtime;
import org.synyx.urlaubsverwaltung.core.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.MailNotification;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonInteractionService;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteInteractionService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteType;
import org.synyx.urlaubsverwaltung.core.util.CalcUtil;
import org.synyx.urlaubsverwaltung.security.CryptoUtil;
import org.synyx.urlaubsverwaltung.web.person.PersonForm;

import java.math.BigDecimal;

import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
    private PersonService personService;

    @Autowired
    private PersonInteractionService personInteractionService;

    @Autowired
    private ApplicationInteractionService applicationInteractionService;

    @Autowired
    private SickNoteInteractionService sickNoteInteractionService;

    @Autowired
    private WorkDaysService calendarService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private OvertimeService overtimeService;

    private Person boss;
    private Person office;

    @PostConstruct
    public void createTestData() throws NoSuchAlgorithmException {

        LOG.info("Test data will be created...");

        // Users to be able to sign in with
        Person user = createTestPerson(TestUser.USER.getLogin(), PASSWORD, "Klaus", "Müller", "mueller@muster.de",
                TestUser.USER.getRoles());
        Person departmentHead = createTestPerson(TestUser.DEPARTMENT_HEAD.getLogin(), PASSWORD, "Thorsten", "Krüger",
                "krueger@muster.de", TestUser.DEPARTMENT_HEAD.getRoles());
        boss = createTestPerson(TestUser.BOSS.getLogin(), PASSWORD, "Max", "Mustermann", "maxMuster@muster.de",
                TestUser.BOSS.getRoles());
        office = createTestPerson(TestUser.OFFICE.getLogin(), PASSWORD, "Marlene", "Muster", "mmuster@muster.de",
                TestUser.OFFICE.getRoles());

        // Users
        Person hans = createTestPerson("hdampf", NO_PASSWORD, "Hans", "Dampf", "dampf@muster.de", Role.USER);
        Person guenther = createTestPerson("gbaier", NO_PASSWORD, "Günther", "Baier", "baier@muster.de", Role.USER);
        Person elena = createTestPerson("eschneider", NO_PASSWORD, "Elena", "Schneider", "schneider@muster.de",
                Role.USER);
        Person brigitte = createTestPerson("bhaendel", NO_PASSWORD, "Brigitte", "Händel", "haendel@muster.de",
                Role.USER);
        Person niko = createTestPerson("nschmidt", NO_PASSWORD, "Niko", "Schmidt", "schmidt@muster.de", Role.USER);

        createTestPerson("horst", NO_PASSWORD, "Horst", "Dieter", "hdieter@muster.de", Role.INACTIVE);

        // Applications for leave and sick notes
        createTestData(user);
        createTestData(boss);
        createTestData(office);
        createTestData(hans);
        createTestData(niko);

        // Departments
        createTestDepartment("Admins", "Das sind die, die so Admin Sachen machen",
            Arrays.asList(hans, brigitte, departmentHead), Arrays.asList(departmentHead));
        createTestDepartment("Entwicklung", "Das sind die, die so entwickeln",
            Arrays.asList(user, niko, departmentHead), Collections.emptyList());
        createTestDepartment("Marketing", "Das sind die, die so Marketing Sachen machen",
            Arrays.asList(guenther, elena), Collections.emptyList());
        createTestDepartment("Geschäftsführung", "Das sind die, die so Geschäftsführung Sachen machen",
            Arrays.asList(boss, office), Collections.emptyList());
    }


    private void createTestDepartment(String name, String description, List<Person> members,
        List<Person> departmentHeads) {

        Department department = new Department();
        department.setName(name);
        department.setDescription(description);
        department.setLastModification(DateTime.now());
        department.getMembers().addAll(members);

        if (!departmentHeads.isEmpty()) {
            department.getDepartmentHeads().addAll(departmentHeads);
        }

        departmentService.create(department);
    }


    private Person createTestPerson(String login, String password, String firstName, String lastName, String email,
        Role... roles) throws NoSuchAlgorithmException {

        int currentYear = DateMidnight.now().getYear();

        PersonForm personForm = new PersonForm(DateMidnight.now().getYear());
        personForm.setLoginName(login);
        personForm.setLastName(lastName);
        personForm.setFirstName(firstName);
        personForm.setEmail(email);

        personForm.setAnnualVacationDays(new BigDecimal("28.5"));
        personForm.setRemainingVacationDays(new BigDecimal("5"));
        personForm.setRemainingVacationDaysNotExpiring(BigDecimal.ZERO);
        personForm.setValidFrom(new DateMidnight(currentYear - 1, 1, 1));

        personForm.setWorkingDays(Arrays.asList(Day.MONDAY.getDayOfWeek(), Day.TUESDAY.getDayOfWeek(),
                Day.WEDNESDAY.getDayOfWeek(), Day.THURSDAY.getDayOfWeek(), Day.FRIDAY.getDayOfWeek()));

        personForm.setPermissions(Arrays.asList(roles));

        List<MailNotification> notifications = new ArrayList<>();

        notifications.add(MailNotification.NOTIFICATION_USER);

        if (personForm.getPermissions().contains(Role.DEPARTMENT_HEAD)) {
            notifications.add(MailNotification.NOTIFICATION_DEPARTMENT_HEAD);
        }

        if (personForm.getPermissions().contains(Role.BOSS)) {
            notifications.add(MailNotification.NOTIFICATION_BOSS);
        }

        if (personForm.getPermissions().contains(Role.OFFICE)) {
            notifications.add(MailNotification.NOTIFICATION_OFFICE);
        }

        personForm.setNotifications(notifications);

        Person person = personInteractionService.create(personForm);

        // TODO: Solve this in a better way!
        // workaround for non generated password
        person.setPassword(CryptoUtil.encodePassword(password));
        personService.save(person);

        return person;
    }


    private void createTestData(Person person) {

        DateMidnight now = DateMidnight.now();

        // FUTURE APPLICATIONS FOR LEAVE
        createWaitingApplication(person, VacationType.HOLIDAY, DayLength.FULL, now.plusDays(10), now.plusDays(16)); // NOSONAR
        createWaitingApplication(person, VacationType.OVERTIME, DayLength.FULL, now.plusDays(1), now.plusDays(1)); // NOSONAR
        createWaitingApplication(person, VacationType.SPECIALLEAVE, DayLength.FULL, now.plusDays(4), now.plusDays(6)); // NOSONAR

        // PAST APPLICATIONS FOR LEAVE
        createAllowedApplication(person, VacationType.HOLIDAY, DayLength.FULL, now.minusDays(20), now.minusDays(13)); // NOSONAR
        createAllowedApplication(person, VacationType.HOLIDAY, DayLength.MORNING, now.minusDays(5), now.minusDays(5)); // NOSONAR
        createAllowedApplication(person, VacationType.SPECIALLEAVE, DayLength.MORNING, now.minusDays(9), // NOSONAR
            now.minusDays(9)); // NOSONAR

        createRejectedApplication(person, VacationType.HOLIDAY, DayLength.FULL, now.minusDays(33), now.minusDays(30)); // NOSONAR

        createCancelledApplication(person, VacationType.HOLIDAY, DayLength.FULL, now.minusDays(11), now.minusDays(10)); // NOSONAR

        // SICK NOTES
        createSickNote(person, DayLength.NOON, now.minusDays(10), now.minusDays(10), SickNoteType.SICK_NOTE, false); // NOSONAR
        createSickNote(person, DayLength.FULL, now.minusDays(2), now.minusDays(2), SickNoteType.SICK_NOTE, false); // NOSONAR
        createSickNote(person, DayLength.FULL, now.minusDays(30), now.minusDays(25), SickNoteType.SICK_NOTE, true); // NOSONAR
        createSickNote(person, DayLength.FULL, now.minusDays(60), now.minusDays(55), SickNoteType.SICK_NOTE_CHILD, // NOSONAR
            true); // NOSONAR
        createSickNote(person, DayLength.FULL, now.minusDays(44), now.minusDays(44), SickNoteType.SICK_NOTE_CHILD, // NOSONAR
            false); // NOSONAR

        // OVERTIME RECORDS

        DateMidnight lastWeek = now.minusWeeks(1);
        DateMidnight weekBeforeLast = now.minusWeeks(2);
        DateMidnight lastYear = now.minusYears(1);

        createOvertimeRecord(person, lastWeek.withDayOfWeek(DateTimeConstants.MONDAY),
            lastWeek.withDayOfWeek(DateTimeConstants.FRIDAY), new BigDecimal("2.5")); // NOSONAR

        createOvertimeRecord(person, weekBeforeLast.withDayOfWeek(DateTimeConstants.MONDAY),
            weekBeforeLast.withDayOfWeek(DateTimeConstants.FRIDAY), new BigDecimal("3")); // NOSONAR

        createOvertimeRecord(person, lastYear.withDayOfWeek(DateTimeConstants.MONDAY),
            lastYear.withDayOfWeek(DateTimeConstants.FRIDAY), new BigDecimal("4")); // NOSONAR
    }


    private Application createWaitingApplication(Person person, VacationType vacationType, DayLength dayLength,
        DateMidnight startDate, DateMidnight endDate) {

        Application application = null;

        if (startAndEndDatesAreInCurrentYear(startDate, endDate)
                && durationIsGreaterThanZero(startDate, endDate, person)) {
            application = new Application();
            application.setPerson(person);
            application.setStartDate(startDate);
            application.setEndDate(endDate);
            application.setVacationType(vacationType);
            application.setDayLength(dayLength);
            application.setReason(
                "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt"
                + "ut labore et dolore magna aliquyam erat, sed diam voluptua."
                + "At vero eos et accusam et justo duo dolores");

            if (vacationType.equals(VacationType.OVERTIME)) {
                switch (dayLength) {
                    case FULL:
                        application.setHours(new BigDecimal("8"));
                        break;

                    default:
                        application.setHours(new BigDecimal("4"));
                        break;
                }
            }

            applicationInteractionService.apply(application, person, Optional.of("Ich hätte gerne Urlaub"));
        }

        return application;
    }


    private boolean startAndEndDatesAreInCurrentYear(DateMidnight start, DateMidnight end) {

        int currentYear = DateMidnight.now().getYear();

        return start.getYear() == currentYear && end.getYear() == currentYear;
    }


    private boolean durationIsGreaterThanZero(DateMidnight start, DateMidnight end, Person person) {

        BigDecimal workDays = calendarService.getWorkDays(DayLength.FULL, start, end, person);

        return CalcUtil.isPositive(workDays);
    }


    private Application createAllowedApplication(Person person, VacationType vacationType, DayLength dayLength,
        DateMidnight startDate, DateMidnight endDate) {

        Application application = createWaitingApplication(person, vacationType, dayLength, startDate, endDate);

        if (application != null) {
            applicationInteractionService.allow(application, boss, Optional.of("Ist in Ordnung"));
        }

        return application;
    }


    private Application createRejectedApplication(Person person, VacationType vacationType, DayLength dayLength,
        DateMidnight startDate, DateMidnight endDate) {

        Application application = createWaitingApplication(person, vacationType, dayLength, startDate, endDate);

        if (application != null) {
            applicationInteractionService.reject(application, boss,
                Optional.of("Aus organisatorischen Gründen leider nicht möglich"));
        }

        return application;
    }


    private Application createCancelledApplication(Person person, VacationType vacationType, DayLength dayLength,
        DateMidnight startDate, DateMidnight endDate) {

        Application application = createAllowedApplication(person, vacationType, dayLength, startDate, endDate);

        if (application != null) {
            applicationInteractionService.cancel(application, office,
                Optional.of("Urlaub wurde nicht genommen, daher storniert"));
        }

        return application;
    }


    private SickNote createSickNote(Person person, DayLength dayLength, DateMidnight startDate, DateMidnight endDate,
        SickNoteType type, boolean withAUB) {

        SickNote sickNote = null;

        if (startAndEndDatesAreInCurrentYear(startDate, endDate)
                && durationIsGreaterThanZero(startDate, endDate, person)) {
            sickNote = new SickNote();
            sickNote.setPerson(person);
            sickNote.setStartDate(startDate);
            sickNote.setEndDate(endDate);
            sickNote.setStatus(SickNoteStatus.ACTIVE);
            sickNote.setType(type);
            sickNote.setDayLength(dayLength);

            if (withAUB) {
                sickNote.setAubStartDate(startDate);
                sickNote.setAubEndDate(endDate);
            }

            sickNoteInteractionService.create(sickNote, office);
        }

        return sickNote;
    }


    private Overtime createOvertimeRecord(Person person, DateMidnight startDate, DateMidnight endDate,
        BigDecimal hours) {

        Overtime overtime = new Overtime(person, startDate, endDate, hours);

        return overtimeService.record(overtime, Optional.of("Ich habe ganz viel gearbeitet"), person);
    }
}
