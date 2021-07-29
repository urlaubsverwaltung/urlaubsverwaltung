package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.AbsenceService;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.absence.settings.TimeSettingsEntity;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.io.File;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;


@ExtendWith(MockitoExtension.class)
class CompanyCalendarServiceTest {

    private CompanyCalendarService sut;

    @Mock
    private AbsenceService absenceService;
    @Mock
    private CompanyCalendarRepository companyCalendarRepository;
    @Mock
    private ICalService iCalService;
    @Mock
    private PersonService personService;
    @Mock
    private MessageSource messageSource;

    private static LocalDate toDateTime(String input) {
        return LocalDate.parse(input, ofPattern("yyyy-MM-dd"));
    }

    @BeforeEach
    void setUp() {

        sut = new CompanyCalendarService(absenceService, companyCalendarRepository, iCalService, personService, messageSource, Clock.systemUTC());
    }

    @Test
    void getCalendarForAllForOneFullDay() {

        final List<Absence> absences = List.of(absence(new Person("muster", "Muster", "Marlene", "muster@example.org"), toDateTime("2019-03-26"), toDateTime("2019-03-26"), FULL));
        when(absenceService.getOpenAbsencesSince(any(LocalDate.class))).thenReturn(absences);

        final Person person = new Person();
        person.setId(10);
        when(personService.getPersonByID(10)).thenReturn(Optional.of(person));

        CompanyCalendar companyCalendar = new CompanyCalendar(person);
        companyCalendar.setId(1L);
        companyCalendar.setCalendarPeriod(java.time.Period.parse("P1Y"));
        when(companyCalendarRepository.findBySecretAndPerson("secret", person)).thenReturn(Optional.of(companyCalendar));

        when(messageSource.getMessage(eq("calendar.company.title"), any(), eq(GERMAN))).thenReturn("Abwesenheitskalender der Firma");
        final File iCal = new File("calendar.ics");
        iCal.deleteOnExit();
        when(iCalService.getCalendar("Abwesenheitskalender der Firma", absences)).thenReturn(iCal);

        final File calendar = sut.getCalendarForAll(10, "secret", GERMAN);
        assertThat(calendar).hasName("calendar.ics");
    }

    @Test
    void getCalendarForAllButNoCompanyCalendarWithSecretFound() {

        final Person person = new Person();
        person.setId(10);
        when(personService.getPersonByID(10)).thenReturn(Optional.of(person));

        when(companyCalendarRepository.findBySecretAndPerson("secret", person)).thenReturn(Optional.empty());

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForAll(10, "secret", GERMAN));
    }

    @Test
    void getCalendarForAllSecretIsNull() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForAll(1, null, GERMAN));
    }

    @Test
    void getCalendarForAllSecretIsEmpty() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForAll(1, "", GERMAN));
    }

    @Test
    void getCalendarForAllSecretIsEmptyWithWhitespace() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForAll(1, "  ", GERMAN));
    }

    @Test
    void getCalendarForAllCorrectSecretButPersonIsWrong() {
        when(personService.getPersonByID(1)).thenReturn(Optional.empty());

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCalendarForAll(1, "secret", GERMAN));
    }

    @Test
    void deleteCalendarForPerson() {

        final Person person = new Person();
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        sut.deleteCalendarForPerson(1);

        verify(companyCalendarRepository).deleteByPerson(person);
    }

    @Test
    void deleteCalendarsForPersonsWithoutOneOfRole() {

        final Person office = createPerson("office", OFFICE);
        final Person user = createPerson("user", USER);

        when(personService.getActivePersons()).thenReturn(List.of(office, user));

        sut.deleteCalendarsForPersonsWithoutOneOfRole(OFFICE);

        verify(companyCalendarRepository).deleteByPerson(user);
        verify(companyCalendarRepository, never()).deleteByPerson(office);
    }

    @Test
    void ensureGetCompanyCalendarThrowsWhenGivenPersonDoesNotExist() {
        when(personService.getPersonByID(1)).thenReturn(Optional.empty());

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.getCompanyCalendar(1));
    }

    @Test
    void ensureGetCompanyCalendarReturnsCalendar() {

        final CompanyCalendar expectedCalendar = new CompanyCalendar();

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(companyCalendarRepository.findByPerson(person)).thenReturn(Optional.of(expectedCalendar));

        final Optional<CompanyCalendar> actualCompanyCalendar = sut.getCompanyCalendar(1);

        assertThat(actualCompanyCalendar).hasValue(expectedCalendar);
    }

    @Test
    void ensureGetCompanyCalendarReturnsEmptyOptional() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(companyCalendarRepository.findByPerson(person)).thenReturn(Optional.empty());

        final Optional<CompanyCalendar> actualCompanyCalendar = sut.getCompanyCalendar(1);

        assertThat(actualCompanyCalendar).isEmpty();
    }

    @Test
    void ensureCreateCalendarForPersonThrowsWhenGivenPersonDoesNotExist() {
        when(personService.getPersonByID(1)).thenReturn(Optional.empty());

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.createCalendarForPerson(1, java.time.Period.parse("P12M")));
    }

    @Test
    void ensureCreateCalendarForPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(companyCalendarRepository.findByPerson(person)).thenReturn(Optional.empty());
        when(companyCalendarRepository.save(any(CompanyCalendar.class))).thenAnswer(returnsFirstArg());

        final CompanyCalendar actualCalendarForPerson = sut.createCalendarForPerson(1, java.time.Period.parse("P12M"));

        assertThat(actualCalendarForPerson.getPerson()).isEqualTo(person);
        assertThat(actualCalendarForPerson.getSecret()).isNotBlank();
    }

    @Test
    void ensureCreateCalendarForPersonUpdatesExistingCalendar() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final CompanyCalendar expectedCompanyCalendar = new CompanyCalendar();
        final String secretBeforeUpdate = expectedCompanyCalendar.getSecret();

        when(companyCalendarRepository.findByPerson(person)).thenReturn(Optional.of(expectedCompanyCalendar));
        when(companyCalendarRepository.save(any(CompanyCalendar.class))).thenAnswer(returnsFirstArg());

        final CompanyCalendar actualCalendarForPerson = sut.createCalendarForPerson(1, java.time.Period.parse("P12M"));

        assertThat(actualCalendarForPerson.getPerson()).isEqualTo(person);
        assertThat(actualCalendarForPerson.getSecret()).isNotBlank();
        assertThat(actualCalendarForPerson.getSecret()).isNotEqualTo(secretBeforeUpdate);
    }

    private Absence absence(Person person, LocalDate start, LocalDate end, DayLength length) {
        final Period period = new Period(start, end, length);
        final AbsenceTimeConfiguration timeConfig = new AbsenceTimeConfiguration(new TimeSettingsEntity());

        return new Absence(person, period, timeConfig);
    }
}
