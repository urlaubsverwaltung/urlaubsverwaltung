package org.synyx.urlaubsverwaltung.calendar;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.AbsenceService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.io.File;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;
import java.util.Optional;


@Service
class DepartmentCalendarService {

    private final AbsenceService absenceService;
    private final DepartmentService departmentService;
    private final PersonService personService;
    private final DepartmentCalendarRepository departmentCalendarRepository;
    private final ICalService iCalService;
    private final MessageSource messageSource;
    private final Clock clock;

    @Autowired
    public DepartmentCalendarService(AbsenceService absenceService, DepartmentService departmentService,
                                     PersonService personService, DepartmentCalendarRepository departmentCalendarRepository, ICalService iCalService, MessageSource messageSource, Clock clock) {

        this.absenceService = absenceService;
        this.departmentService = departmentService;
        this.personService = personService;
        this.departmentCalendarRepository = departmentCalendarRepository;
        this.iCalService = iCalService;
        this.messageSource = messageSource;
        this.clock = clock;
    }

    @Transactional
    public void deleteCalendarForDepartmentAndPerson(int departmentId, int personId) {

        final Person person = getPersonOrThrow(personId);
        final Department department = getDepartmentOrThrow(departmentId);

        departmentCalendarRepository.deleteByDepartmentAndPerson(department, person);
    }

    DepartmentCalendar createCalendarForDepartmentAndPerson(int departmentId, int personId, Period calendarPeriod) {

        final Person person = getPersonOrThrow(personId);
        final Department department = getDepartmentOrThrow(departmentId);

        final Optional<DepartmentCalendar> maybeDepartmentCalendar = departmentCalendarRepository.findByDepartmentAndPerson(department, person);
        final DepartmentCalendar departmentCalendar = maybeDepartmentCalendar.isEmpty() ? new DepartmentCalendar() : maybeDepartmentCalendar.get();
        departmentCalendar.setDepartment(department);
        departmentCalendar.setPerson(person);
        departmentCalendar.setCalendarPeriod(calendarPeriod);
        departmentCalendar.generateSecret();

        return departmentCalendarRepository.save(departmentCalendar);
    }

    Optional<DepartmentCalendar> getCalendarForDepartment(Integer departmentId, Integer personId) {

        final Person person = getPersonOrThrow(personId);
        final Department department = getDepartmentOrThrow(departmentId);

        return departmentCalendarRepository.findByDepartmentAndPerson(department, person);
    }

    File getCalendarForDepartment(Integer departmentId, Integer personId, String secret, Locale locale) {

        if (StringUtils.isBlank(secret)) {
            throw new IllegalArgumentException("secret must not be empty.");
        }

        final Person person = getPersonOrThrow(personId);
        final Optional<DepartmentCalendar> maybeDepartmentCalendar = departmentCalendarRepository.findBySecretAndPerson(secret, person);
        if (maybeDepartmentCalendar.isEmpty()) {
            throw new IllegalArgumentException("No calendar found for secret=" + secret);
        }

        final Department department = getDepartmentOrThrow(departmentId);
        final DepartmentCalendar departmentCalendar = maybeDepartmentCalendar.get();
        if (!departmentCalendar.getDepartment().equals(department)) {
            throw new IllegalArgumentException(String.format("Secret=%s does not match the given departmentId=%s", secret, departmentId));
        }

        final String title = messageSource.getMessage("calendar.department.title", List.of(department.getName()).toArray(), locale);

        final LocalDate sinceDate = LocalDate.now(clock).minus(departmentCalendar.getCalendarPeriod());
        final List<Absence> absences = absenceService.getOpenAbsencesSince(department.getMembers(), sinceDate);

        return iCalService.getCalendar(title, absences);
    }

    @Transactional
    public void deleteDepartmentsCalendarsForPerson(int personId) {

        final Person person = getPersonOrThrow(personId);

        departmentCalendarRepository.deleteByPerson(person);
    }

    private Department getDepartmentOrThrow(Integer departmentId) {

        final Optional<Department> maybeDepartment = departmentService.getDepartmentById(departmentId);
        if (maybeDepartment.isEmpty()) {
            throw new IllegalArgumentException("No department found for ID=" + departmentId);
        }

        return maybeDepartment.get();
    }

    private Person getPersonOrThrow(Integer personId) {

        final Optional<Person> maybePerson = personService.getPersonByID(personId);
        if (maybePerson.isEmpty()) {
            throw new IllegalArgumentException("could not find person for given personId=" + personId);
        }

        return maybePerson.get();
    }
}
