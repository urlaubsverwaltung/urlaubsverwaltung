package org.synyx.urlaubsverwaltung.calendar;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;
import java.util.Optional;


@Service
public class DepartmentCalendarService {

    private final CalendarAbsenceService absenceService;
    private final DepartmentService departmentService;
    private final PersonService personService;
    private final DepartmentCalendarRepository departmentCalendarRepository;
    private final ICalService iCalService;
    private final MessageSource messageSource;
    private final Clock clock;

    @Autowired
    DepartmentCalendarService(
        CalendarAbsenceService absenceService, DepartmentService departmentService,
        PersonService personService, DepartmentCalendarRepository departmentCalendarRepository,
        ICalService iCalService, MessageSource messageSource, Clock clock
    ) {
        this.absenceService = absenceService;
        this.departmentService = departmentService;
        this.personService = personService;
        this.departmentCalendarRepository = departmentCalendarRepository;
        this.iCalService = iCalService;
        this.messageSource = messageSource;
        this.clock = clock;
    }

    @Transactional
    public void deleteCalendarForDepartmentAndPerson(long departmentId, long personId) {

        final Person person = getPersonOrThrow(personId);

        departmentCalendarRepository.deleteByDepartmentIdAndPerson(departmentId, person);
    }

    DepartmentCalendar createCalendarForDepartmentAndPerson(long departmentId, long personId, Period calendarPeriod) {

        final Person person = getPersonOrThrow(personId);

        if (!departmentService.departmentExists(departmentId)) {
            throw new IllegalStateException("department with id does not exist.");
        }

        final Optional<DepartmentCalendar> maybeDepartmentCalendar = departmentCalendarRepository.findByDepartmentIdAndPerson(departmentId, person);
        final DepartmentCalendar departmentCalendar = maybeDepartmentCalendar.orElseGet(DepartmentCalendar::new);
        departmentCalendar.setDepartmentId(departmentId);
        departmentCalendar.setPerson(person);
        departmentCalendar.setCalendarPeriod(calendarPeriod);
        departmentCalendar.generateSecret();

        return departmentCalendarRepository.save(departmentCalendar);
    }

    Optional<DepartmentCalendar> getCalendarForDepartment(Long departmentId, Long personId) {

        final Person person = getPersonOrThrow(personId);

        return departmentCalendarRepository.findByDepartmentIdAndPerson(departmentId, person);
    }

    public List<DepartmentCalendar> getCalendarsForPerson(Long personId) {
        return departmentCalendarRepository.findByPersonId(personId);
    }

    ByteArrayResource getCalendarForDepartment(Long departmentId, Long personId, String secret, Locale locale) {

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
        if (!departmentCalendar.getDepartmentId().equals(departmentId)) {
            throw new IllegalArgumentException(String.format("Secret=%s does not match the given departmentId=%s", secret, departmentId));
        }

        final String title = messageSource.getMessage("calendar.department.title", List.of(department.getName()).toArray(), locale);

        final LocalDate chosenCalendarPeriodSinceDate = LocalDate.now(clock).minus(departmentCalendar.getCalendarPeriod());
        final LocalDate departmentExistsSinceDate = department.getCreatedAt();
        final LocalDate sinceDate = departmentExistsSinceDate.isAfter(chosenCalendarPeriodSinceDate) ? departmentExistsSinceDate : chosenCalendarPeriodSinceDate;

        final List<CalendarAbsence> absences = absenceService.getOpenAbsencesSince(department.getMembers(), sinceDate);

        return iCalService.getCalendar(title, absences, person);
    }

    @Transactional
    public void deleteDepartmentsCalendarsForPerson(long personId) {

        final Person person = getPersonOrThrow(personId);

        departmentCalendarRepository.deleteByPerson(person);
    }

    @EventListener
    void deleteCalendarForPerson(PersonDeletedEvent event) {

        departmentCalendarRepository.deleteByPerson(event.person());
    }

    private Department getDepartmentOrThrow(Long departmentId) {

        final Optional<Department> maybeDepartment = departmentService.getDepartmentById(departmentId);
        if (maybeDepartment.isEmpty()) {
            throw new IllegalArgumentException("No department found for ID=" + departmentId);
        }

        return maybeDepartment.get();
    }

    private Person getPersonOrThrow(Long personId) {

        final Optional<Person> maybePerson = personService.getPersonByID(personId);
        if (maybePerson.isEmpty()) {
            throw new IllegalArgumentException("could not find person for given personId=" + personId);
        }

        return maybePerson.get();
    }
}
