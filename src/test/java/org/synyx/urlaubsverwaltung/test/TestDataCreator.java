package org.synyx.urlaubsverwaltung.test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.velocity.util.StringUtils;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.util.ReflectionUtils;
import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.department.Department;
import org.synyx.urlaubsverwaltung.core.overtime.Overtime;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.MailNotification;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteCategory;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteType;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTime;

/**
 * Util class to create data for tests.
 *
 * @author Aljona Murygina - murygina@synyx.de
 */
public final class TestDataCreator {

    private TestDataCreator() {

        // Hide constructor for util class
    }

    // Person ----------------------------------------------------------------------------------------------------------

    // Person ----------------------------------------------------------------------------------------------------------
    public static Person createPerson(Integer id, String username) throws IllegalAccessException {

        Person person = TestDataCreator.createPerson(username);

        Field dateField = ReflectionUtils.findField(Person.class, "id");
        dateField.setAccessible(true);
        dateField.set(person, id);

        return person;
    }

    public static Person createPerson(String username) {

        String name = StringUtils.capitalizeFirstLetter(username);

        return TestDataCreator.createPerson(username, name, name, username + "@test.de");
    }

    public static Person createPerson(String username, Role... roles) {

        String name = StringUtils.capitalizeFirstLetter(username);

        Person person = TestDataCreator.createPerson(username, name, name, username + "@test.de");

        person.setPermissions(Arrays.asList(roles));

        return person;
    }

    public static Person createPerson() {

        return TestDataCreator.createPerson("muster", "Marlene", "Muster", "muster@test.de");
    }

    public static Person createPerson(String username, String firstName, String lastName, String email) {

        Person person = new Person(username, lastName, firstName, email);
        person.setPermissions(Collections.singletonList(Role.USER));
        person.setNotifications(Collections.singletonList(MailNotification.NOTIFICATION_USER));

        return person;
    }

    // Overtime record -------------------------------------------------------------------------------------------------

    public static Overtime createOvertimeRecord() {

        DateMidnight startDate = DateMidnight.now();
        DateMidnight endDate = startDate.plusDays(7);

        return new Overtime(createPerson(), startDate, endDate, BigDecimal.ONE);
    }

    public static Overtime createOvertimeRecord(Person person) {

        DateMidnight startDate = DateMidnight.now();
        DateMidnight endDate = startDate.plusDays(7);

        return new Overtime(person, startDate, endDate, BigDecimal.ONE);
    }

    // Application for leave -------------------------------------------------------------------------------------------

    public static Application createApplication(Person person, VacationType vacationType) {

        return createApplication(person, vacationType, DateMidnight.now(), DateMidnight.now().plusDays(3),
            DayLength.FULL);
    }

    public static Application createApplication(Person person, DateMidnight startDate, DateMidnight endDate,
        DayLength dayLength) {

        VacationType vacationType = TestDataCreator.createVacationType(VacationCategory.HOLIDAY, "Erholungsurlaub");

        Application application = new Application();
        application.setPerson(person);
        application.setStartDate(startDate);
        application.setEndDate(endDate);
        application.setDayLength(dayLength);
        application.setVacationType(vacationType);
        application.setStatus(ApplicationStatus.WAITING);

        return application;
    }

    public static Application createApplication(Person person, VacationType vacationType, DateMidnight startDate,
        DateMidnight endDate, DayLength dayLength) {

        Application application = new Application();
        application.setPerson(person);
        application.setStartDate(startDate);
        application.setEndDate(endDate);
        application.setDayLength(dayLength);
        application.setVacationType(vacationType);
        application.setStatus(ApplicationStatus.WAITING);

        return application;
    }

    // Sick note -------------------------------------------------------------------------------------------------------

    public static SickNote createSickNote(Person person) {

        return createSickNote(person, DateMidnight.now(), DateMidnight.now().plusDays(3), DayLength.FULL);
    }

    public static SickNote createSickNote(Person person, DateMidnight startDate, DateMidnight endDate,
        DayLength dayLength) {

        SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SickNoteCategory.SICK_NOTE);
        sickNoteType.setDisplayName("Krankmeldung");

        SickNote sickNote = new SickNote();
        sickNote.setPerson(person);
        sickNote.setStartDate(startDate);
        sickNote.setEndDate(endDate);
        sickNote.setDayLength(dayLength);
        sickNote.setSickNoteType(sickNoteType);
        sickNote.setStatus(SickNoteStatus.ACTIVE);

        return sickNote;
    }

    // Department ------------------------------------------------------------------------------------------------------

    public static Department createDepartment() {

        return createDepartment("Abteilung");
    }

    public static Department createDepartment(String name) {

        return createDepartment(name, "Dies ist eine Abteilung");
    }

    public static Department createDepartment(String name, String description) {

        Department department = new Department();
        department.setName(name);
        department.setDescription(description);

        return department;
    }

    // Holidays account ------------------------------------------------------------------------------------------------

    public static Account createHolidaysAccount(Person person) {

        return createHolidaysAccount(person, DateTime.now().getYear());
    }

    public static Account createHolidaysAccount(Person person, int year) {

        return createHolidaysAccount(person, year, new BigDecimal("30"), new BigDecimal("3"), BigDecimal.ZERO,
            "comment");
    }

    public static Account createHolidaysAccount(Person person, int year, BigDecimal annualVacationDays,
        BigDecimal remainingVacationDays, BigDecimal remainingVacationDaysNotExpiring, String comment) {

        DateMidnight firstDayOfYear = DateUtil.getFirstDayOfYear(year);
        DateMidnight lastDayOfYear = DateUtil.getLastDayOfYear(year);

        return new Account(person, firstDayOfYear.toDate(), lastDayOfYear.toDate(), annualVacationDays,
            remainingVacationDays, remainingVacationDaysNotExpiring, comment);
    }

    // Working time ----------------------------------------------------------------------------------------------------

    public static WorkingTime createWorkingTime() {

        WorkingTime workingTime = new WorkingTime();

        List<Integer> workingDays = Arrays.asList(DateTimeConstants.MONDAY, DateTimeConstants.TUESDAY,
            DateTimeConstants.WEDNESDAY, DateTimeConstants.THURSDAY, DateTimeConstants.FRIDAY);
        workingTime.setWorkingDays(workingDays, DayLength.FULL);

        return workingTime;
    }

    public static VacationType createVacationType(VacationCategory category) {

        return createVacationType(category, category.toString());
    }

    public static VacationType createVacationType(VacationCategory category, String displayName) {

        VacationType vacationType = new VacationType();

        vacationType.setCategory(category);
        vacationType.setDisplayName(displayName);

        return vacationType;
    }

    public static List<VacationType> createVacationTypes() {

        ArrayList<VacationType> vacationTypes = new ArrayList<>();

        VacationType vacationType1 = new VacationType();
        vacationType1.setId(1000);
        vacationType1.setCategory(VacationCategory.HOLIDAY);
        vacationType1.setDisplayName("Erholungsurlaub");
        vacationTypes.add(vacationType1);

        VacationType vacationType2 = new VacationType();
        vacationType2.setCategory(VacationCategory.SPECIALLEAVE);
        vacationType2.setDisplayName("Sonderurlaub");
        vacationType2.setId(2000);
        vacationTypes.add(vacationType2);

        VacationType vacationType3 = new VacationType();
        vacationType3.setCategory(VacationCategory.UNPAIDLEAVE);
        vacationType3.setDisplayName("Unbezahlter Urlaub");
        vacationType3.setId(3000);
        vacationTypes.add(vacationType3);

        VacationType vacationType4 = new VacationType();
        vacationType4.setCategory(VacationCategory.OVERTIME);
        vacationType4.setDisplayName("Überstundenabbau");
        vacationType4.setId(4000);
        vacationTypes.add(vacationType4);

        return vacationTypes;
    }
}
