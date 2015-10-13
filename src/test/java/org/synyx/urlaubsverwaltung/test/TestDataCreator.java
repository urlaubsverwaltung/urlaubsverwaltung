package org.synyx.urlaubsverwaltung.test;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.core.overtime.Overtime;
import org.synyx.urlaubsverwaltung.core.person.Person;

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

    public static Person createPerson() {

        return new Person("muster", "Muster", "Marlene", "muster@test.de");
    }


    public static Overtime createOvertimeRecord() {

        DateMidnight startDate = DateMidnight.now();
        DateMidnight endDate = startDate.plusDays(7);

        return new Overtime(createPerson(), startDate, endDate, BigDecimal.ONE);
    }
}
