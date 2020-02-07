package org.synyx.urlaubsverwaltung.calendar;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.AbsenceService;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.Absence;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;
import java.util.Optional;


@Service
class DepartmentCalendarService {

    private final AbsenceService absenceService;
    private final DepartmentService departmentService;
    private final PersonService personService;
    private final DepartmentCalendarRepository departmentCalendarRepository;
    private final ICalService iCalService;

    @Autowired
    public DepartmentCalendarService(AbsenceService absenceService, DepartmentService departmentService,
                                     PersonService personService, DepartmentCalendarRepository departmentCalendarRepository, ICalService iCalService) {

        this.absenceService = absenceService;
        this.departmentService = departmentService;
        this.personService = personService;
        this.departmentCalendarRepository = departmentCalendarRepository;
        this.iCalService = iCalService;
    }

    String getCalendarForDepartment(Integer departmentId, Integer personId, String secret) {

        if (StringUtils.isBlank(secret)) {
            throw new IllegalArgumentException("secret must not be empty.");
        }

        final Person person = getPersonOrThrow(personId);
        final DepartmentCalendar calendar = departmentCalendarRepository.findBySecretAndPerson(secret, person);
        if (calendar == null) {
            throw new IllegalArgumentException("No calendar found for secret=" + secret);
        }

        final Optional<Department> optionalDepartment = departmentService.getDepartmentById(departmentId);
        if (optionalDepartment.isEmpty()) {
            throw new IllegalArgumentException("No department found for ID=" + departmentId);
        }

        final Department department = optionalDepartment.get();

        if (!calendar.getDepartment().equals(department)) {
            throw new IllegalArgumentException(String.format("Secret=%s does not match the given departmentId=%s", secret, departmentId));
        }

        final String title = "Abwesenheitskalender der Abteilung " + department.getName();
        final List<Absence> absences = absenceService.getOpenAbsences(department.getMembers());

        return iCalService.generateCalendar(title, absences);
    }

    private Person getPersonOrThrow(Integer personId) {

        final Optional<Person> maybePerson = personService.getPersonByID(personId);
        if (maybePerson.isEmpty()) {
            throw new IllegalArgumentException("could not find person for given personId=" + personId);
        }

        return maybePerson.get();
    }
}
