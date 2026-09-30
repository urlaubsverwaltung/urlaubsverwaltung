package org.synyx.urlaubsverwaltung.vacationcertificate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.JUNE;
import static java.time.Month.MAY;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class VacationCertificateViewControllerTest {

    private static final long PERSON_ID = 1L;
    private static final String URL = "/web/person/" + PERSON_ID + "/vacation-certificate";

    private VacationCertificateViewController sut;

    @Mock
    private PersonService personService;
    @Mock
    private PersonBasedataService personBasedataService;
    @Mock
    private AccountService accountService;
    @Mock
    private VacationCertificateService vacationCertificateService;

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-15T10:00:00Z"), ZoneId.of("UTC"));

    @BeforeEach
    void setUp() {
        sut = new VacationCertificateViewController(personService, personBasedataService, accountService,
            vacationCertificateService, new VacationCertificateFormValidator(), clock);
    }

    @Test
    void ensureUnknownPersonThrows() {
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> perform(get(URL))).hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    void ensureDefaultsFromTheHolidayAccounts() throws Exception {
        final Person person = person();
        final Account account = account(person, LocalDate.of(2026, JANUARY, 1), LocalDate.of(2026, JUNE, 30));
        final Account firstAccount = account(person, LocalDate.of(2019, APRIL, 1), LocalDate.of(2019, DECEMBER, 31));
        when(accountService.getHolidaysAccount(2026, person)).thenReturn(Optional.of(account));
        when(accountService.getHolidaysAccountsByPerson(person)).thenReturn(List.of(account, firstAccount));
        final VacationCertificate certificate = anyCertificate();
        when(vacationCertificateService.getVacationCertificate(account, LocalDate.of(2026, JUNE, 30))).thenReturn(certificate);

        perform(get(URL))
            .andExpect(status().isOk())
            .andExpect(view().name("vacationcertificate/vacation_certificate"))
            .andExpect(model().attribute("person", person))
            .andExpect(model().attribute("selectedYear", 2026))
            .andExpect(model().attribute("currentYear", 2026))
            .andExpect(model().attribute("personnelNumber", nullValue()))
            .andExpect(model().attribute("certificateForm", allOf(
                hasProperty("year", is(2026)),
                hasProperty("employmentFrom", is(LocalDate.of(2019, APRIL, 1))),
                hasProperty("employmentTo", is(LocalDate.of(2026, JUNE, 30))),
                hasProperty("compensatedDays", comparesEqualTo(ZERO))
            )))
            .andExpect(model().attribute("certificate", certificate))
            .andExpect(model().attributeDoesNotExist("noAccount"));
    }

    @Test
    void ensureSubmittedValuesWinOverDefaults() throws Exception {
        final Person person = person();
        final Account account = account(person, LocalDate.of(2026, JANUARY, 1), LocalDate.of(2026, DECEMBER, 31));
        when(accountService.getHolidaysAccount(2026, person)).thenReturn(Optional.of(account));
        when(vacationCertificateService.getVacationCertificate(account, LocalDate.of(2026, MAY, 31))).thenReturn(anyCertificate());

        perform(get(URL)
            .param("year", "2026")
            .param("employmentFrom", "01.02.2020")
            .param("employmentTo", "31.05.2026")
            .param("compensatedDays", "2")
            .param("employer", "ACME"))
            .andExpect(model().attribute("certificateForm", allOf(
                hasProperty("employmentFrom", is(LocalDate.of(2020, 2, 1))),
                hasProperty("employmentTo", is(LocalDate.of(2026, MAY, 31))),
                hasProperty("compensatedDays", comparesEqualTo(new BigDecimal("2"))),
                hasProperty("employer", is("ACME"))
            )))
            .andExpect(model().attributeExists("certificate"));

        verify(accountService, never()).getHolidaysAccountsByPerson(any());
    }

    @Test
    void ensureDecimalCommaIsAcceptedForCompensatedDays() throws Exception {
        final Person person = person();
        final Account account = account(person, LocalDate.of(2026, JANUARY, 1), LocalDate.of(2026, DECEMBER, 31));
        when(accountService.getHolidaysAccount(2026, person)).thenReturn(Optional.of(account));
        when(accountService.getHolidaysAccountsByPerson(person)).thenReturn(List.of(account));
        when(vacationCertificateService.getVacationCertificate(account, LocalDate.of(2026, DECEMBER, 31))).thenReturn(anyCertificate());

        perform(get(URL).param("compensatedDays", "2,5"))
            .andExpect(model().attribute("certificateForm", hasProperty("compensatedDays", comparesEqualTo(new BigDecimal("2.5")))))
            .andExpect(model().attributeExists("certificate"));
    }

    @Test
    void ensureSelectedYear() throws Exception {
        final Person person = person();
        final Account account = account(person, LocalDate.of(2025, JANUARY, 1), LocalDate.of(2025, DECEMBER, 31));
        when(accountService.getHolidaysAccount(2025, person)).thenReturn(Optional.of(account));
        when(accountService.getHolidaysAccountsByPerson(person)).thenReturn(List.of(account));
        when(vacationCertificateService.getVacationCertificate(account, LocalDate.of(2025, DECEMBER, 31))).thenReturn(anyCertificate());

        perform(get(URL).param("year", "2025"))
            .andExpect(model().attribute("selectedYear", 2025))
            .andExpect(model().attribute("currentYear", 2026));
    }

    @Test
    void ensureNoCertificateWithoutAccount() throws Exception {
        final Person person = person();
        when(accountService.getHolidaysAccount(2026, person)).thenReturn(Optional.empty());

        perform(get(URL))
            .andExpect(status().isOk())
            .andExpect(view().name("vacationcertificate/vacation_certificate"))
            .andExpect(model().attribute("noAccount", true))
            .andExpect(model().attributeDoesNotExist("certificate"));

        verify(vacationCertificateService, never()).getVacationCertificate(any(), any());
    }

    @Test
    void ensureNoCertificateWithValidationErrors() throws Exception {
        final Person person = person();
        final Account account = account(person, LocalDate.of(2026, JANUARY, 1), LocalDate.of(2026, DECEMBER, 31));
        when(accountService.getHolidaysAccount(2026, person)).thenReturn(Optional.of(account));

        perform(get(URL)
            .param("employmentFrom", "30.06.2026")
            .param("employmentTo", "01.06.2026"))
            .andExpect(model().attributeHasFieldErrorCode("certificateForm", "employmentTo", "vacationcertificate.form.error.employmentTo.beforeFrom"))
            .andExpect(model().attributeDoesNotExist("certificate"));

        verify(vacationCertificateService, never()).getVacationCertificate(any(), any());
    }

    @Test
    void ensureUnparsableDateIsNotReplacedByTheDefault() throws Exception {
        final Person person = person();
        final Account account = account(person, LocalDate.of(2026, JANUARY, 1), LocalDate.of(2026, DECEMBER, 31));
        when(accountService.getHolidaysAccount(2026, person)).thenReturn(Optional.of(account));

        perform(get(URL)
            .param("employmentFrom", "01.01.2020")
            .param("employmentTo", "31.02.2026"))
            .andExpect(model().attributeHasFieldErrorCode("certificateForm", "employmentTo", "typeMismatch"))
            .andExpect(model().attribute("certificateForm", hasProperty("employmentTo", nullValue())))
            .andExpect(model().attributeDoesNotExist("certificate"));
    }

    @Test
    void ensurePersonnelNumber() throws Exception {
        final Person person = person();
        when(personBasedataService.getBasedataByPersonId(PERSON_ID))
            .thenReturn(Optional.of(new PersonBasedata(new PersonId(PERSON_ID), "4711", null)));
        when(accountService.getHolidaysAccount(2026, person)).thenReturn(Optional.empty());

        perform(get(URL)).andExpect(model().attribute("personnelNumber", "4711"));
    }

    private Person person() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(PERSON_ID);
        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(person));
        return person;
    }

    private static Account account(Person person, LocalDate validFrom, LocalDate validTo) {
        final Account account = new Account();
        account.setPerson(person);
        account.setValidFrom(validFrom);
        account.setValidTo(validTo);
        return account;
    }

    private static VacationCertificate anyCertificate() {
        return new VacationCertificate(Year.of(2026), BigDecimal.TEN, Optional.empty(), List.of(), ZERO, ZERO, false);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
