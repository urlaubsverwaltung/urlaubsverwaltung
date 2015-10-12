package org.synyx.urlaubsverwaltung.test;

import org.synyx.urlaubsverwaltung.core.person.Person;


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
}
