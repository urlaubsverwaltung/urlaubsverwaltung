package org.synyx.urlaubsverwaltung.extension.backup.backup;

import org.synyx.urlaubsverwaltung.person.Person;

class PersonHelper {

    private PersonHelper() {
    }

    static String optionalExternalUserId(Person person) {
        return person != null ? person.getUsername() : null;
    }
}
