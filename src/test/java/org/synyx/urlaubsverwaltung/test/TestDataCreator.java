package org.synyx.urlaubsverwaltung.test;

import org.joda.time.DateMidnight;

import org.springframework.util.ReflectionUtils;

import org.synyx.urlaubsverwaltung.core.overtime.Overtime;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.lang.reflect.Field;

import java.math.BigDecimal;


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

        Person person = createPerson(username);

        Field dateField = ReflectionUtils.findField(Person.class, "id");
        dateField.setAccessible(true);
        dateField.set(person, id);

        return person;
    }


    public static Person createPerson(String username) {

        return new Person(username, "Muster", "Marlene", username + "@test.de");
    }


    public static Person createPerson() {

        return new Person("muster", "Muster", "Marlene", "muster@test.de");
    }


    public static Overtime createOvertimeRecord() {

        DateMidnight startDate = DateMidnight.now();
        DateMidnight endDate = startDate.plusDays(7);

        return new Overtime(createPerson(), startDate, endDate, BigDecimal.ONE);
    }
}
