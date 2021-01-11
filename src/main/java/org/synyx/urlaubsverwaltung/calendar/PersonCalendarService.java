package org.synyx.urlaubsverwaltung.calendar;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.AbsenceService;
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
class PersonCalendarService {

    private final AbsenceService absenceService;
    private final PersonService personService;
    private final PersonCalendarRepository personCalendarRepository;
    private final ICalService iCalService;
    private final MessageSource messageSource;
    private final Clock clock;

    @Autowired
    PersonCalendarService(AbsenceService absenceService, PersonService personService,
                          PersonCalendarRepository personCalendarRepository, ICalService iCalService, MessageSource messageSource, Clock clock) {

        this.absenceService = absenceService;
        this.personService = personService;
        this.personCalendarRepository = personCalendarRepository;
        this.iCalService = iCalService;
        this.messageSource = messageSource;
        this.clock = clock;
    }

    PersonCalendar createCalendarForPerson(Integer personId, Period calendarPeriod) {

        final Person person = getPersonOrThrow(personId);

        final Optional<PersonCalendar> maybePersonCalendar = personCalendarRepository.findByPerson(person);
        final PersonCalendar personCalendar = maybePersonCalendar.isEmpty() ? new PersonCalendar() : maybePersonCalendar.get();
        personCalendar.setPerson(person);
        personCalendar.setCalendarPeriod(calendarPeriod);
        personCalendar.generateSecret();

        return personCalendarRepository.save(personCalendar);
    }

    Optional<PersonCalendar> getPersonCalendar(Integer personId) {

        final Person person = getPersonOrThrow(personId);

        return personCalendarRepository.findByPerson(person);
    }

    File getCalendarForPerson(Integer personId, String secret, Locale locale) {

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
        final List<Absence> absences = absenceService.getOpenAbsencesSince(List.of(person), sinceDate);

        return iCalService.getCalendar(title, absences);
    }

    @Transactional
    public void deletePersonalCalendarForPerson(int personId) {

        final Person person = getPersonOrThrow(personId);

        personCalendarRepository.deleteByPerson(person);
    }

    private Person getPersonOrThrow(Integer personId) {

        final Optional<Person> maybePerson = personService.getPersonByID(personId);
        if (maybePerson.isEmpty()) {
            throw new IllegalArgumentException("could not find person for given personId=" + personId);
        }

        return maybePerson.get();
    }
}
