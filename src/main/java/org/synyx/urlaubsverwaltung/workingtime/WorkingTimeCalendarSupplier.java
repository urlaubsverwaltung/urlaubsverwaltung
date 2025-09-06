package org.synyx.urlaubsverwaltung.workingtime;

import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;

@FunctionalInterface
public interface WorkingTimeCalendarSupplier {

    WorkingTimeCalendar getWorkingTimeCalendar(PersonId personId, DateRange dateRange);

    default WorkingTimeCalendar getWorkingTimeCalendar(Person person, DateRange dateRange) {
        return getWorkingTimeCalendar(person.getIdAsPersonId(), dateRange);
    }
}
