package org.synyx.urlaubsverwaltung;

import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeEntity;
import org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMapping;
import org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingType;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteCategory;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteType;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.time.ZoneOffset.UTC;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.SPECIALLEAVE;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.UNPAIDLEAVE;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.BADEN_WUERTTEMBERG;

public final class TestDataCreator {

    private TestDataCreator() {
        // Hide constructor for util class
    }

    public static Person createPerson(String username, Role... roles) {
        final Person person = new Person(username, username, username, username + "@example.org");
        person.setPermissions(List.of(roles));
        return person;
    }

    // Overtime record -------------------------------------------------------------------------------------------------
    public static Overtime createOvertimeRecord() {
        final LocalDate startDate = LocalDate.now(UTC);
        final LocalDate endDate = startDate.plusDays(7);
        return new Overtime(new Person("muster", "Muster", "Marlene", "muster@example.org"), startDate, endDate, Duration.ofHours(1));
    }

    public static Overtime createOvertimeRecord(Person person) {

        LocalDate startDate = LocalDate.now(UTC);
        LocalDate endDate = startDate.plusDays(7);

        Overtime overtime = new Overtime(person, startDate, endDate, Duration.ofHours(1));
        overtime.setId(1234);
        return overtime;
    }

    // Application for leave -------------------------------------------------------------------------------------------
    public static Application createApplication(Person person, VacationTypeEntity vacationType) {

        LocalDate now = LocalDate.now(UTC);
        return createApplication(person, vacationType, now, now.plusDays(3), FULL);
    }

    public static Application createApplication(Person person, LocalDate startDate, LocalDate endDate, DayLength dayLength) {

        final VacationTypeEntity vacationType = createVacationTypeEntity(HOLIDAY, "application.data.vacationType.holiday");

        final Application application = new Application();
        application.setPerson(person);
        application.setStartDate(startDate);
        application.setEndDate(endDate);
        application.setDayLength(dayLength);
        application.setVacationType(vacationType);
        application.setStatus(ApplicationStatus.WAITING);

        return application;
    }

    public static Application createApplication(Person person, VacationTypeEntity vacationType, LocalDate startDate,
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

    public static Application anyApplication() {
        Application application = new Application();
        application.setPerson(new Person("muster", "Muster", "Marlene", "muster@example.org"));
        application.setDayLength(FULL);
        return application;
    }

    // Sick note -------------------------------------------------------------------------------------------------------
    public static SickNote anySickNote() {
        return createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"));
    }

    public static SickNote createSickNote(Person person) {
        return createSickNote(person, LocalDate.now(UTC), ZonedDateTime.now(UTC).plusDays(3).toLocalDate(), FULL);
    }

    public static SickNote createSickNote(Person person, LocalDate startDate, LocalDate endDate, DayLength dayLength) {

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
        return createHolidaysAccount(person, year, new BigDecimal("30"), new BigDecimal("3"), ZERO, "comment");
    }

    public static Account createHolidaysAccount(Person person, int year, BigDecimal annualVacationDays,
                                                BigDecimal remainingVacationDays, BigDecimal remainingVacationDaysNotExpiring, String comment) {

        LocalDate firstDayOfYear = DateUtil.getFirstDayOfYear(year);
        LocalDate lastDayOfYear = DateUtil.getLastDayOfYear(year);

        return new Account(person, firstDayOfYear, lastDayOfYear, annualVacationDays, remainingVacationDays,
            remainingVacationDaysNotExpiring, comment);
    }

    // Working time ----------------------------------------------------------------------------------------------------

    public static WorkingTime createWorkingTime() {

        final Person person = new Person();
        person.setId(1);

        final WorkingTime workingTime = new WorkingTime(person, LocalDate.MIN, BADEN_WUERTTEMBERG);

        List<DayOfWeek> workingDays = List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        workingTime.setWorkingDays(workingDays, FULL);

        return workingTime;
    }

    public static VacationTypeEntity createVacationTypeEntity(VacationCategory category) {
        return createVacationTypeEntity(category, category.getMessageKey());
    }

    public static VacationTypeEntity createVacationTypeEntity(VacationCategory category, String messageKey) {

        VacationTypeEntity vacationType = new VacationTypeEntity();
        vacationType.setCategory(category);
        vacationType.setMessageKey(messageKey);

        return vacationType;
    }

    public static List<VacationType> createVacationTypes() {

        final List<VacationType> vacationTypes = new ArrayList<>();

        final VacationType holidayType = new VacationType(1000, true, HOLIDAY, "application.data.vacationType.holiday", true);
        vacationTypes.add(holidayType);

        final VacationType specialLeaveType = new VacationType(2000, true, SPECIALLEAVE, "application.data.vacationType.specialleave", true);
        vacationTypes.add(specialLeaveType);

        final VacationType vacationType3 = new VacationType(3000, true, UNPAIDLEAVE, "application.data.vacationType.unpaidleave", true);
        vacationTypes.add(vacationType3);

        final VacationType vacationType4 = new VacationType(4000, true, OVERTIME, "application.data.vacationType.overtime", true);
        vacationTypes.add(vacationType4);

        return vacationTypes;
    }

    public static List<VacationTypeEntity> createVacationTypesEntities() {

        ArrayList<VacationTypeEntity> vacationTypes = new ArrayList<>();

        VacationTypeEntity vacationType1 = new VacationTypeEntity();
        vacationType1.setId(1000);
        vacationType1.setCategory(HOLIDAY);
        vacationType1.setMessageKey("application.data.vacationType.holiday");
        vacationTypes.add(vacationType1);

        VacationTypeEntity vacationType2 = new VacationTypeEntity();
        vacationType2.setCategory(VacationCategory.SPECIALLEAVE);
        vacationType2.setMessageKey("application.data.vacationType.specialleave");
        vacationType2.setId(2000);
        vacationTypes.add(vacationType2);

        VacationTypeEntity vacationType3 = new VacationTypeEntity();
        vacationType3.setCategory(VacationCategory.UNPAIDLEAVE);
        vacationType3.setMessageKey("application.data.vacationType.unpaidleave");
        vacationType3.setId(3000);
        vacationTypes.add(vacationType3);

        VacationTypeEntity vacationType4 = new VacationTypeEntity();
        vacationType4.setCategory(VacationCategory.OVERTIME);
        vacationType4.setMessageKey("application.data.vacationType.overtime");
        vacationType4.setId(4000);
        vacationTypes.add(vacationType4);

        return vacationTypes;
    }

    public static AbsenceMapping anyAbsenceMapping() {
        return new AbsenceMapping(null, AbsenceMappingType.VACATION, "eventId");
    }
}
