package org.synyx.urlaubsverwaltung.calendar;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.absence.AbsenceService;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.Absence;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;


@Service
class CompanyCalendarService {

    private final AbsenceService absenceService;
    private final CompanyCalendarRepository companyCalendarRepository;
    private final ICalService iCalService;
    private final PersonService personService;
    private final MessageSource messageSource;

    @Autowired
    CompanyCalendarService(AbsenceService absenceService, CompanyCalendarRepository companyCalendarRepository, ICalService iCalService, PersonService personService, MessageSource messageSource) {
        this.absenceService = absenceService;
        this.companyCalendarRepository = companyCalendarRepository;
        this.iCalService = iCalService;
        this.personService = personService;
        this.messageSource = messageSource;
    }

    CompanyCalendar createCalendarForPerson(int personId) {

        final Person person = getPersonOrThrow(personId);

        final CompanyCalendar maybeCompanyCalendar = companyCalendarRepository.findByPerson(person);
        final CompanyCalendar companyCalendar = maybeCompanyCalendar == null ? new CompanyCalendar() : maybeCompanyCalendar;
        companyCalendar.setPerson(person);
        companyCalendar.generateSecret();

        return companyCalendarRepository.save(companyCalendar);
    }

    Optional<CompanyCalendar> getCompanyCalendar(int personId) {

        final Person person = getPersonOrThrow(personId);

        return Optional.ofNullable(companyCalendarRepository.findByPerson(person));
    }

    String getCalendarForAll(Integer personId, String secret, Locale locale) {

        if (StringUtils.isBlank(secret)) {
            throw new IllegalArgumentException("secret must not be empty.");
        }

        final Person person = getPersonOrThrow(personId);
        final CompanyCalendar calendar = companyCalendarRepository.findBySecretAndPerson(secret, person);
        if (calendar == null) {
            throw new IllegalArgumentException("No calendar found for secret=" + secret);
        }

        final String title = messageSource.getMessage("calendar.company.title", new Object[]{}, locale);
        final List<Absence> absences = absenceService.getOpenAbsences();

        return iCalService.generateCalendar(title, absences);
    }

    @Transactional
    public void deleteCalendarForPerson(int personId) {

        final Person person = getPersonOrThrow(personId);

        companyCalendarRepository.deleteByPerson(person);
    }

    /**
     * Delete all {@link CompanyCalendar} for persons who don't have one of the given {@link Role}.
     *
     * @param roles
     */
    @Transactional
    public void deleteCalendarsForPersonsWithoutOneOfRole(Role... roles) {

        final List<Role> roleList = Arrays.asList(roles);

        for (final Person person : personService.getActivePersons()) {
            if (roleList.stream().noneMatch(person::hasRole)) {
                companyCalendarRepository.deleteByPerson(person);
            }
        }
    }

    private Person getPersonOrThrow(Integer personId) {

        final Optional<Person> maybePerson = personService.getPersonByID(personId);
        if (maybePerson.isEmpty()) {
            throw new IllegalArgumentException("could not find person for given personId=" + personId);
        }

        return maybePerson.get();
    }
}
