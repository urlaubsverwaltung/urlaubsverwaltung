package org.synyx.urlaubsverwaltung.calendar;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.AbsenceService;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.Absence;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;
import java.util.Optional;


@Service
class CompanyCalendarService {

    private final AbsenceService absenceService;
    private final CompanyCalendarRepository companyCalendarRepository;
    private final ICalService iCalService;
    private final PersonService personService;

    @Autowired
    CompanyCalendarService(AbsenceService absenceService, CompanyCalendarRepository companyCalendarRepository, ICalService iCalService, PersonService personService) {
        this.absenceService = absenceService;
        this.companyCalendarRepository = companyCalendarRepository;
        this.iCalService = iCalService;
        this.personService = personService;
    }

    String getCalendarForAll(Integer personId, String secret) {

        if (StringUtils.isBlank(secret)) {
            throw new IllegalArgumentException("secret must not be empty.");
        }

        final Person person = getPersonOrThrow(personId);
        final CompanyCalendar calendar = companyCalendarRepository.findBySecretAndPerson(secret, person);
        if (calendar == null) {
            throw new IllegalArgumentException("No calendar found for secret=" + secret);
        }

        final String title = "Abwesenheitskalender der Firma";
        final List<Absence> absences = absenceService.getOpenAbsences();

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
