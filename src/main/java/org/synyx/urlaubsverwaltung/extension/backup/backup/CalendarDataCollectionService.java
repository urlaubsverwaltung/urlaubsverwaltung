package org.synyx.urlaubsverwaltung.extension.backup.backup;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.calendar.CalendarAccessibleService;
import org.synyx.urlaubsverwaltung.calendar.CompanyCalendarService;
import org.synyx.urlaubsverwaltung.calendar.DepartmentCalendarService;
import org.synyx.urlaubsverwaltung.calendar.PersonCalendarService;
import org.synyx.urlaubsverwaltung.extension.backup.model.CalendarBackupDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.CompanyCalendarDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.DepartmentCalendarDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.PersonCalendarDTO;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@ConditionalOnBackupCreateEnabled
class CalendarDataCollectionService {

    private final PersonCalendarService personCalendarService;
    private final CompanyCalendarService companyCalendarService;
    private final DepartmentCalendarService departmentCalendarService;
    private final CalendarAccessibleService calendarAccessibleService;

    CalendarDataCollectionService(PersonCalendarService personCalendarService, CompanyCalendarService companyCalendarService, DepartmentCalendarService departmentCalendarService, CalendarAccessibleService calendarAccessibleService) {
        this.personCalendarService = personCalendarService;
        this.companyCalendarService = companyCalendarService;
        this.departmentCalendarService = departmentCalendarService;
        this.calendarAccessibleService = calendarAccessibleService;
    }

    CalendarBackupDTO collectCalendars(List<Person> allPersons) {
        final List<PersonCalendarDTO> personCalendars = allPersons.stream().map(person -> personCalendarService.getPersonCalendar(person.getId())).filter(Optional::isPresent).map(Optional::get).map(personCalendar -> new PersonCalendarDTO(personCalendar.getId(), personCalendar.getPerson().getUsername(), personCalendar.getCalendarPeriod(), personCalendar.getSecret())).toList();
        final List<CompanyCalendarDTO> companyCalendars = allPersons.stream().map(person -> companyCalendarService.getCompanyCalendar(person.getId())).filter(Optional::isPresent).map(Optional::get).map(companyCalendar -> new CompanyCalendarDTO(companyCalendar.getId(), companyCalendar.getPerson().getUsername(), companyCalendar.getCalendarPeriod(), companyCalendar.getSecret())).toList();
        final List<DepartmentCalendarDTO> departmentCalendars = allPersons.stream().map(person -> departmentCalendarService.getCalendarsForPerson(person.getId()).stream().map(departmentCalendar -> new DepartmentCalendarDTO(departmentCalendar.getId(), person.getUsername(), departmentCalendar.getDepartmentId(), departmentCalendar.getCalendarPeriod(), departmentCalendar.getSecret())).toList()).toList().stream().flatMap(Collection::stream).toList();
        return new CalendarBackupDTO(personCalendars, companyCalendars, departmentCalendars, calendarAccessibleService.isCompanyCalendarAccessible());
    }


}
