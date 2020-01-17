package org.synyx.urlaubsverwaltung.calendar;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.XProperty;
import net.fortuna.ical4j.validate.ValidationException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.AbsenceService;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.Absence;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Date.from;
import static java.util.stream.Collectors.toList;
import static net.fortuna.ical4j.model.property.Version.VERSION_2_0;


@Service
class ICalService {

    private final AbsenceService absenceService;
    private final PersonService personService;
    private final DepartmentService departmentService;
    private final PersonCalendarRepository personCalendarRepository;
    private final DepartmentCalendarRepository departmentCalendarRepository;
    private final CompanyCalendarRepository companyCalendarRepository;

    @Autowired
    ICalService(AbsenceService absenceService, PersonService personService, DepartmentService departmentService,
                PersonCalendarRepository personCalendarRepository, DepartmentCalendarRepository departmentCalendarRepository,
                CompanyCalendarRepository companyCalendarRepository) {

        this.absenceService = absenceService;
        this.personService = personService;
        this.departmentService = departmentService;
        this.personCalendarRepository = personCalendarRepository;
        this.departmentCalendarRepository = departmentCalendarRepository;
        this.companyCalendarRepository = companyCalendarRepository;
    }

    String getCalendarForPerson(Integer personId, String secret) {

        if (Strings.isBlank(secret)) {
            throw new IllegalArgumentException("secret must not be empty.");
        }

        final PersonCalendar calendar = personCalendarRepository.findBySecret(secret);
        if (calendar == null) {
            throw new IllegalArgumentException("No calendar found for secret=" + secret);
        }

        final Optional<Person> optionalPerson = personService.getPersonByID(personId);
        if (optionalPerson.isEmpty()) {
            throw new IllegalArgumentException("No person found for ID=" + personId);
        }

        final Person person = optionalPerson.get();

        if (!calendar.getPerson().equals(person)) {
            throw new IllegalArgumentException(String.format("Secret=%s does not match the given personId=%s", secret, personId));
        }

        final String title = "Abwesenheitskalender von " + person.getNiceName();
        final List<Absence> absences = absenceService.getOpenAbsences(List.of(person));

        return this.generateCalendar(title, absences).toString();
    }

    String getCalendarForDepartment(Integer departmentId, String secret) {

        if (Strings.isBlank(secret)) {
            throw new IllegalArgumentException("secret must not be empty.");
        }

        final DepartmentCalendar calendar = departmentCalendarRepository.findBySecret(secret);
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

        return this.generateCalendar(title, absences).toString();
    }

    String getCalendarForAll(String secret) {

        if (Strings.isBlank(secret)) {
            throw new IllegalArgumentException("secret must not be empty.");
        }

        final CompanyCalendar calendar = companyCalendarRepository.findBySecret(secret);
        if (calendar == null) {
            throw new IllegalArgumentException("No calendar found for secret=" + secret);
        }

        final String title = "Abwesenheitskalender der Firma";
        final List<Absence> absences = absenceService.getOpenAbsences();

        return this.generateCalendar(title, absences).toString();
    }


    private Calendar generateCalendar(String title, List<Absence> absences) {

        final Calendar calendar = new Calendar();
        calendar.getProperties().add(VERSION_2_0);
        calendar.getProperties().add(new ProdId("-//Urlaubsverwaltung//iCal4j 1.0//DE"));
        calendar.getProperties().add(CalScale.GREGORIAN);
        calendar.getProperties().add(new XProperty("X-WR-CALNAME", title));

        final List<VEvent> absencesVEvents = absences.stream().map(this::toVEvent).collect(toList());
        calendar.getComponents().addAll(absencesVEvents);

        try {
            calendar.validate();
        } catch (ValidationException e) {
            throw new CalendarException("Validation does not pass", e);
        }

        return calendar;
    }


    private VEvent toVEvent(Absence absence) {

        final VTimeZone utc = TimeZoneRegistryFactory.getInstance().createRegistry()
            .getTimeZone("Etc/UTC").getVTimeZone();

        final ZonedDateTime startDateTime = absence.getStartDate();
        final ZonedDateTime endDateTime = absence.getEndDate();

        final DateTime start = new DateTime(from(startDateTime.toInstant()), new TimeZone(utc));

        final VEvent event;
        if (absence.isAllDay() && isSameDay(startDateTime, endDateTime)) {
            event = new VEvent(new Date(start.getTime()), absence.getEventSubject());
        } else if (absence.isAllDay() && !isSameDay(startDateTime, endDateTime)) {
            final DateTime end = new DateTime(from(endDateTime.minusDays(1).toInstant()), new TimeZone(utc));
            event = new VEvent(start, end, absence.getEventSubject());
        } else {
            final DateTime end = new DateTime(from(endDateTime.toInstant()), new TimeZone(utc));
            event = new VEvent(start, end, absence.getEventSubject());
        }

        event.getProperties().add(new Uid(generateUid(absence)));

        return event;
    }

    private boolean isSameDay(ZonedDateTime startDateTime, ZonedDateTime endDate) {
        return startDateTime.toLocalDate().isEqual(endDate.toLocalDate().minusDays(1));
    }

    private String generateUid(Absence absence) {
        final String data = absence.getStartDate() + "" + absence.getEndDate() + "" + absence.getPerson();
        return DigestUtils.md5Hex(data).toUpperCase();
    }
}
