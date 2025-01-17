package org.synyx.urlaubsverwaltung.extension.backup.backup;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.calendar.CalendarAccessibleService;
import org.synyx.urlaubsverwaltung.calendar.CompanyCalendar;
import org.synyx.urlaubsverwaltung.calendar.CompanyCalendarService;
import org.synyx.urlaubsverwaltung.calendar.DepartmentCalendar;
import org.synyx.urlaubsverwaltung.calendar.DepartmentCalendarService;
import org.synyx.urlaubsverwaltung.calendar.PersonCalendar;
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
        final List<PersonCalendarDTO> personCalendars = allPersons.stream()
            .map(person -> personCalendarService.getPersonCalendar(person.getId()))
            .<PersonCalendar>mapMulti(Optional::ifPresent)
            .map(CalendarDataCollectionService::createPersonCalendarDTO)
            .toList();

        final List<CompanyCalendarDTO> companyCalendars = allPersons.stream()
            .map(person -> companyCalendarService.getCompanyCalendar(person.getId()))
            .<CompanyCalendar>mapMulti(Optional::ifPresent)
            .map(CalendarDataCollectionService::createCompanyCalendarDTO)
            .toList();

        final List<DepartmentCalendarDTO> departmentCalendars = allPersons.stream()
            .map(this::getDepartmentCalendarDTOS)
            .flatMap(Collection::stream)
            .toList();

        return new CalendarBackupDTO(personCalendars, companyCalendars, departmentCalendars, calendarAccessibleService.isCompanyCalendarAccessible());
    }

    private List<DepartmentCalendarDTO> getDepartmentCalendarDTOS(Person person) {
        return departmentCalendarService.getCalendarsForPerson(person.getId()).stream()
            .map(CalendarDataCollectionService::createDepartmentCalendarDTO)
            .toList();
    }

    private static DepartmentCalendarDTO createDepartmentCalendarDTO(DepartmentCalendar departmentCalendar) {
        return new DepartmentCalendarDTO(
            departmentCalendar.getId(),
            departmentCalendar.getPerson().getUsername(),
            departmentCalendar.getDepartmentId(),
            departmentCalendar.getCalendarPeriod(),
            departmentCalendar.getSecret()
        );
    }

    private static CompanyCalendarDTO createCompanyCalendarDTO(CompanyCalendar companyCalendar) {
        return new CompanyCalendarDTO(
            companyCalendar.getId(),
            companyCalendar.getPerson().getUsername(),
            companyCalendar.getCalendarPeriod(),
            companyCalendar.getSecret()
        );
    }

    private static PersonCalendarDTO createPersonCalendarDTO(PersonCalendar personCalendar) {
        return new PersonCalendarDTO(
            personCalendar.getId(),
            personCalendar.getPerson().getUsername(),
            personCalendar.getCalendarPeriod(),
            personCalendar.getSecret()
        );
    }
}
