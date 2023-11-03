package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;

record ApplicationMailRemindCronManagementDto(
    Long id,
    Person person,
    LocalDate startDate,
    LocalDate endDate,
    DayLength dayLength,
    String vacationTypeLabel
) {
}
