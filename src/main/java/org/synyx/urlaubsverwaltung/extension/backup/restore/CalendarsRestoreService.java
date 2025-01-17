package org.synyx.urlaubsverwaltung.extension.backup.restore;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.calendar.CalendarAccessibleImportService;
import org.synyx.urlaubsverwaltung.calendar.CompanyCalendarImportService;
import org.synyx.urlaubsverwaltung.calendar.DepartmentCalendarImportService;
import org.synyx.urlaubsverwaltung.calendar.PersonCalendarImportService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.extension.backup.model.CalendarBackupDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.DepartmentDTO;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@ConditionalOnBackupRestoreEnabled
class CalendarsRestoreService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonCalendarImportService personCalendarImportService;
    private final CompanyCalendarImportService companyCalendarImportService;
    private final DepartmentCalendarImportService departmentCalendarImportService;
    private final CalendarAccessibleImportService calendarAccessibleImportService;
    private final DepartmentService departmentService;
    private final PersonService personService;

    CalendarsRestoreService(PersonCalendarImportService personCalendarImportService, CompanyCalendarImportService companyCalendarImportService, DepartmentCalendarImportService departmentCalendarImportService, CalendarAccessibleImportService calendarAccessibleImportService, DepartmentService departmentService, PersonService personService) {
        this.personCalendarImportService = personCalendarImportService;
        this.companyCalendarImportService = companyCalendarImportService;
        this.departmentCalendarImportService = departmentCalendarImportService;
        this.calendarAccessibleImportService = calendarAccessibleImportService;
        this.departmentService = departmentService;
        this.personService = personService;
    }

    void restore(CalendarBackupDTO calendars, List<DepartmentDTO> departments) {
        LOG.info("Restoring calendars ...");
        importCompanyCalendars(calendars);
        importDepartmentCalendars(calendars, departments);
        importPersonCalendars(calendars);
        LOG.info("Finished restoring calendars");
    }

    private void importPersonCalendars(CalendarBackupDTO calendars) {
        LOG.info("Restoring person calendars ...");
        calendars.personCalendars().forEach(personCalendarDTO -> {
            final Person owner = getPerson(personCalendarDTO.externalId());
            personCalendarImportService.importPersonCalendar(personCalendarDTO.toPersonCalendarEntity(owner));
        });
    }

    private void importDepartmentCalendars(CalendarBackupDTO calendars, List<DepartmentDTO> departments) {
        LOG.info("Restoring department calendars ...");
        calendars.departmentCalendars().forEach(departmentCalendarDTO -> {
            final Department department = getDepartment(departments, departmentCalendarDTO.departmentId());
            final Person owner = getPerson(departmentCalendarDTO.externalId());
            departmentCalendarImportService.importDepartmentCalendar(departmentCalendarDTO.toDepartmentCalendarEntity(department.getId(), owner));
        });
    }

    private void importCompanyCalendars(CalendarBackupDTO calendars) {
        LOG.info("Restoring company calendar accessible ...");
        calendarAccessibleImportService.importCompanyCalendarAccessible(calendars.toCompanyCalendarAccessibleEntity());

        LOG.info("Restoring company calendars ...");
        calendars.companyCalendars().forEach(companyCalendar -> {
            final Person owner = getPerson(companyCalendar.externalId());
            companyCalendarImportService.importCompanyCalendar(companyCalendar.toCompanyCalendarEntity(owner));
        });
    }

    private Department getDepartment(List<DepartmentDTO> departments, Long originalDepartmentId) {
        return departments.stream()
            .filter(departmentDTO -> departmentDTO.id().equals(originalDepartmentId))
            .findFirst()
            .flatMap(departmentDTO -> departmentService.getDepartmentByName(departmentDTO.name()))
            .orElseThrow(() -> new IllegalArgumentException("Could not restore department calendar because the department with originalId=%s does not exist - import that department first!".formatted(originalDepartmentId)));
    }

    private Person getPerson(String externalId) {
        return personService.getPersonByUsername(externalId)
            .orElseThrow(() -> new IllegalArgumentException("Person with username=%s does not exist".formatted(externalId)));
    }
}
