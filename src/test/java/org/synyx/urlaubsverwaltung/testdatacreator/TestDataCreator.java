package org.synyx.urlaubsverwaltung.testdatacreator;

import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.synyx.urlaubsverwaltung.absence.AbsenceMapping;
import org.synyx.urlaubsverwaltung.absence.AbsenceType;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteCategory;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteType;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.time.ZoneOffset.UTC;

/**
 * Util class to create data for tests.
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

        String name = StringUtils.capitalize(username);

        return TestDataCreator.createPerson(username, name, name, username + "@test.de");
    }

    public static Person createPerson(String username, Role... roles) {

        String name = StringUtils.capitalize(username);

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

        LocalDate startDate = LocalDate.now(UTC);
        LocalDate endDate = startDate.plusDays(7);

        return new Overtime(createPerson(), startDate, endDate, BigDecimal.ONE);
    }

    public static Overtime createOvertimeRecord(Person person) {

        LocalDate startDate = LocalDate.now(UTC);
        LocalDate endDate = startDate.plusDays(7);

        Overtime overtime = new Overtime(person, startDate, endDate, BigDecimal.ONE);
        overtime.setId(1234);
        return overtime;
    }

    // Application for leave -------------------------------------------------------------------------------------------

    public static Application createApplication(Person person, VacationType vacationType) {

        LocalDate now = LocalDate.now(UTC);
        return createApplication(person, vacationType, now, now.plusDays(3), DayLength.FULL);
    }

    public static Application createApplication(Person person, LocalDate startDate, LocalDate endDate, DayLength dayLength) {

        VacationType vacationType = TestDataCreator.createVacationType(VacationCategory.HOLIDAY, "application.data.vacationType.holiday");

        Application application = new Application();
        application.setPerson(person);
        application.setStartDate(startDate);
        application.setEndDate(endDate);
        application.setDayLength(dayLength);
        application.setVacationType(vacationType);
        application.setStatus(ApplicationStatus.WAITING);

        return application;
    }

    public static Application createApplication(Person person, VacationType vacationType, LocalDate startDate,
                                                LocalDate endDate, DayLength dayLength) {

        Application application = new Application();
        application.setPerson(person);
        application.setStartDate(startDate);
        application.setEndDate(endDate);
        application.setDayLength(dayLength);
        application.setVacationType(vacationType);
        application.setStatus(ApplicationStatus.WAITING);

        return application;
    }

    public static Application anyFullDayApplication(Person person) {
        Application application = anyApplication();
        application.setPerson(person);
        return application;
    }

    public static Application anyApplication() {
        Application application = new Application();
        application.setPerson(createPerson());
        application.setDayLength(DayLength.FULL);
        return application;
    }

    // Sick note -------------------------------------------------------------------------------------------------------

    public static SickNote anySickNote() {
        return createSickNote(createPerson());
    }

    public static SickNote createSickNote(Person person) {

        return createSickNote(person, LocalDate.now(UTC), ZonedDateTime.now(UTC).plusDays(3).toLocalDate(), DayLength.FULL);
    }

    public static SickNote createSickNote(Person person, LocalDate startDate, LocalDate endDate,
                                          DayLength dayLength) {

        SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SickNoteCategory.SICK_NOTE);
        sickNoteType.setMessageKey("Krankmeldung");

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

    public static Account createHolidaysAccount(Person person, int year) {

        return createHolidaysAccount(person, year, new BigDecimal("30"), new BigDecimal("3"), BigDecimal.ZERO,
            "comment");
    }

    public static Account createHolidaysAccount(Person person, int year, BigDecimal annualVacationDays,
                                                BigDecimal remainingVacationDays, BigDecimal remainingVacationDaysNotExpiring, String comment) {

        LocalDate firstDayOfYear = DateUtil.getFirstDayOfYear(year);
        LocalDate lastDayOfYear = DateUtil.getLastDayOfYear(year);

        return new Account(person, firstDayOfYear, lastDayOfYear, annualVacationDays,
            remainingVacationDays, remainingVacationDaysNotExpiring, comment);
    }

    // Working time ----------------------------------------------------------------------------------------------------

    public static WorkingTime createWorkingTime() {

        WorkingTime workingTime = new WorkingTime();

        List<Integer> workingDays = Arrays.asList(MONDAY.getValue(), TUESDAY.getValue(),
            WEDNESDAY.getValue(), THURSDAY.getValue(), FRIDAY.getValue());
        workingTime.setWorkingDays(workingDays, DayLength.FULL);

        return workingTime;
    }

    public static VacationType createVacationType(VacationCategory category) {

        return createVacationType(category, category.getMessageKey());
    }

    public static VacationType createVacationType(VacationCategory category, String messageKey) {

        VacationType vacationType = new VacationType();

        vacationType.setCategory(category);
        vacationType.setMessageKey(messageKey);

        return vacationType;
    }

    public static List<VacationType> createVacationTypes() {

        ArrayList<VacationType> vacationTypes = new ArrayList<>();

        VacationType vacationType1 = new VacationType();
        vacationType1.setId(1000);
        vacationType1.setCategory(VacationCategory.HOLIDAY);
        vacationType1.setMessageKey("application.data.vacationType.holiday");
        vacationTypes.add(vacationType1);

        VacationType vacationType2 = new VacationType();
        vacationType2.setCategory(VacationCategory.SPECIALLEAVE);
        vacationType2.setMessageKey("application.data.vacationType.specialleave");
        vacationType2.setId(2000);
        vacationTypes.add(vacationType2);

        VacationType vacationType3 = new VacationType();
        vacationType3.setCategory(VacationCategory.UNPAIDLEAVE);
        vacationType3.setMessageKey("application.data.vacationType.unpaidleave");
        vacationType3.setId(3000);
        vacationTypes.add(vacationType3);

        VacationType vacationType4 = new VacationType();
        vacationType4.setCategory(VacationCategory.OVERTIME);
        vacationType4.setMessageKey("application.data.vacationType.overtime");
        vacationType4.setId(4000);
        vacationTypes.add(vacationType4);

        return vacationTypes;
    }

    public static AbsenceMapping anyAbsenceMapping() {
        AbsenceMapping absenceMapping = new AbsenceMapping();
        absenceMapping.setEventId("eventId");
        absenceMapping.setAbsenceType(AbsenceType.VACATION);
        return absenceMapping;
    }
}
