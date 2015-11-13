package org.synyx.urlaubsverwaltung.test;

import org.joda.time.DateMidnight;

import org.springframework.util.ReflectionUtils;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.department.Department;
import org.synyx.urlaubsverwaltung.core.overtime.Overtime;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.Role;

import java.lang.reflect.Field;

import java.math.BigDecimal;

import java.util.Collections;


/**
 * Util class to create data for tests.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public final class TestDataCreator {

    private TestDataCreator() {

        // Hide constructor for util class
    }

    public static Person createPerson(Integer id, String username) throws IllegalAccessException {

        Person person = TestDataCreator.createPerson(username);

        Field dateField = ReflectionUtils.findField(Person.class, "id");
        dateField.setAccessible(true);
        dateField.set(person, id);

        return person;
    }


    public static Person createPerson(String username) {

        return TestDataCreator.createPerson(username, "Marlene", "Muster", username + "@test.de");
    }


    public static Person createPerson() {

        return TestDataCreator.createPerson("muster", "Marlene", "Muster", "muster@test.de");
    }


    public static Person createPerson(String username, String firstName, String lastName, String email) {

        Person person = new Person(username, lastName, firstName, email);
        person.setPermissions(Collections.singletonList(Role.USER));

        return person;
    }


    public static Overtime createOvertimeRecord() {

        DateMidnight startDate = DateMidnight.now();
        DateMidnight endDate = startDate.plusDays(7);

        return new Overtime(createPerson(), startDate, endDate, BigDecimal.ONE);
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
}
