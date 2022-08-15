package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;

interface PersonOvertimeReduction {

    Person getPerson();

    BigDecimal getOvertimeReduction();
}
