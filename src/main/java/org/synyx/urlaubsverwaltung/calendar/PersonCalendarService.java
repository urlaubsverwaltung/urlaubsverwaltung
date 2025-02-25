package org.synyx.urlaubsverwaltung.calendar;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
public class PersonCalendarService {

    private final CalendarAbsenceService absenceService;
    private final PersonService personService;
    private final PersonCalendarRepository personCalendarRepository;
    private final ICalService iCalService;
    private final MessageSource messageSource;
    private final Clock clock;

    @Autowired
    PersonCalendarService(
        CalendarAbsenceService absenceService, PersonService personService,
        PersonCalendarRepository personCalendarRepository, ICalService iCalService,
        MessageSource messageSource, Clock clock
    ) {
        this.absenceService = absenceService;
        this.personService = personService;
        this.personCalendarRepository = personCalendarRepository;
        this.iCalService = iCalService;
        this.messageSource = messageSource;
        this.clock = clock;
    }

    PersonCalendar createCalendarForPerson(Long personId, Period calendarPeriod) {

        final Person person = getPersonOrThrow(personId);

        final Optional<PersonCalendar> maybePersonCalendar = personCalendarRepository.findByPerson(person);
        final PersonCalendar personCalendar = maybePersonCalendar.orElseGet(PersonCalendar::new);
        personCalendar.setPerson(person);
        personCalendar.setCalendarPeriod(calendarPeriod);
        personCalendar.generateSecret();

        return personCalendarRepository.save(personCalendar);
    }

    public Optional<PersonCalendar> getPersonCalendar(Long personId) {

        final Person person = getPersonOrThrow(personId);

        return personCalendarRepository.findByPerson(person);
    }

    ByteArrayResource getCalendarForPerson(Long personId, String secret, Locale locale) {

        if (StringUtils.isBlank(secret)) {
            throw new IllegalArgumentException("secret must not be empty.");
        }

        final Optional<PersonCalendar> maybePersonCalendar = personCalendarRepository.findBySecret(secret);
        if (maybePersonCalendar.isEmpty()) {
            throw new IllegalArgumentException("No calendar found for secret=" + secret);
        }

        final Person person = getPersonOrThrow(personId);
        final PersonCalendar personCalendar = maybePersonCalendar.get();
        if (!personCalendar.getPerson().equals(person)) {
            throw new IllegalArgumentException(String.format("Secret=%s does not match the given personId=%s", secret, personId));
        }

        final String title = messageSource.getMessage("calendar.person.title", List.of(person.getNiceName()).toArray(), locale);

        final LocalDate sinceDate = LocalDate.now(clock).minus(personCalendar.getCalendarPeriod());
        final List<CalendarAbsence> absences = absenceService.getOpenAbsencesSince(List.of(person), sinceDate);

        return iCalService.getCalendar(title, absences, person);
    }

    @Transactional
    public void deletePersonalCalendarForPerson(long personId) {

        final Person person = getPersonOrThrow(personId);

        personCalendarRepository.deleteByPerson(person);
    }

    @EventListener
    void deletePersonalCalendar(PersonDeletedEvent event) {

        personCalendarRepository.deleteByPerson(event.person());
    }

    private Person getPersonOrThrow(Long personId) {

        final Optional<Person> maybePerson = personService.getPersonByID(personId);
        if (maybePerson.isEmpty()) {
            throw new IllegalArgumentException("could not find person for given personId=" + personId);
        }

        return maybePerson.get();
    }
}
