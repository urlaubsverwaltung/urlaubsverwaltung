package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeDto;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeViewModelService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static java.time.Month.MARCH;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.ORANGE;

@ExtendWith(MockitoExtension.class)
class AccountViewControllerTest {

    private AccountViewController sut;

    private static final long UNKNOWN_PERSON_ID = 715;
    private static final long SOME_PERSON_ID = 5;

    @Mock
    private PersonService personService;
    @Mock
    private AccountService accountService;
    @Mock
    private AccountInteractionService accountInteractionService;
    @Mock
    private VacationTypeViewModelService vacationTypeViewModelService;
    @Mock
    private AccountFormValidator validator;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new AccountViewController(personService, accountService, accountInteractionService, vacationTypeViewModelService, validator, clock);
    }

    @Test
    void editAccountForUnknownIdThrowsUnknownPersonException() {
        assertThatThrownBy(() ->
            perform(get("/web/person/" + UNKNOWN_PERSON_ID + "/account"))
        ).hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    void editAccountProvidesCorrectModelAndView() throws Exception {

        final int currentYear = Year.now(clock).getValue();

        final Person person = new Person();
        person.setId(1L);

        final Account account = new Account();
        account.setValidFrom(Year.of(currentYear).atDay(1));
        account.setValidTo(Year.of(currentYear).atDay(1).with(lastDayOfYear()));

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));
        when(accountService.getHolidaysAccount(currentYear, person)).thenReturn(Optional.of(account));
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));

        perform(get("/web/person/" + SOME_PERSON_ID + "/account"))
            .andExpect(model().attribute("person", person))
            .andExpect(model().attribute("account", instanceOf(AccountForm.class)))
            .andExpect(model().attribute("currentYear", notNullValue()))
            .andExpect(model().attribute("selectedYear", notNullValue()))
            .andExpect(model().attribute("vacationTypeColors", equalTo(List.of(new VacationTypeDto(1L, ORANGE)))))
            .andExpect(view().name("account/account_form"));
    }

    @Test
    void editAccountUsesProvidedYear() throws Exception {

        final int providedYear = 1987;
        final int currentYear = Year.now(clock).getValue();

        final Person person = new Person();
        person.setId(1L);

        final Account account = new Account();
        account.setValidFrom(Year.of(providedYear).atDay(1));
        account.setValidTo(Year.of(providedYear).atDay(1).with(lastDayOfYear()));

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));
        when(accountService.getHolidaysAccount(providedYear, person)).thenReturn(Optional.of(account));

        perform(get("/web/person/" + SOME_PERSON_ID + "/account")
            .param("year", Integer.toString(providedYear)))
            .andExpect(model().attribute("currentYear", currentYear))
            .andExpect(model().attribute("selectedYear", providedYear));

        verifyNoMoreInteractions(accountService);
    }

    @Test
    void editAccountUsesProvidedYearWithNonExistentAccount() throws Exception {

        final Year currentYear = Year.now(clock);
        final Year providedYear = currentYear.plusYears(1);

        final Person person = new Person();
        person.setId(1L);

        final AccountDraft accountDraft = AccountDraft.builder()
            .person(person)
            .year(providedYear)
            .annualVacationDays(BigDecimal.valueOf(30))
            .doRemainingVacationDaysExpireLocally(null)
            .doRemainingVacationDaysExpireGlobally(false)
            .build();

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));
        when(accountService.getHolidaysAccount(providedYear.getValue(), person)).thenReturn(Optional.empty());
        when(accountService.createHolidaysAccountDraft(providedYear.getValue(), person)).thenReturn(accountDraft);

        perform(get("/web/person/" + SOME_PERSON_ID + "/account")
            .param("year", String.valueOf(providedYear.getValue())))
            .andExpect(model().attribute("currentYear", currentYear.getValue()))
            .andExpect(model().attribute("selectedYear", providedYear.getValue()))
            .andExpect(model().attribute("account", allOf(
                hasProperty("holidaysAccountYear", is(providedYear.getValue())),
                hasProperty("holidaysAccountValidFrom", is(providedYear.atDay(1))),
                hasProperty("holidaysAccountValidTo", is(providedYear.atDay(1).with(lastDayOfYear()))),
                hasProperty("expiryDateLocally", nullValue()),
                hasProperty("annualVacationDays", is(BigDecimal.valueOf(30)))
            )));
    }

    @Test
    void editAccountDefaultsToCurrentYear() throws Exception {

        final int currentYear = Year.now(clock).getValue();

        final Person person = new Person();
        person.setId(1L);

        final Account account = new Account();
        account.setValidFrom(Year.of(currentYear).atDay(1));
        account.setValidTo(Year.of(currentYear).atDay(1).with(lastDayOfYear()));

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));
        when(accountService.getHolidaysAccount(currentYear, person)).thenReturn(Optional.of(account));

        perform(get("/web/person/" + SOME_PERSON_ID + "/account"))
            .andExpect(model().attribute("currentYear", currentYear))
            .andExpect(model().attribute("selectedYear", currentYear));
    }

    @Test
    void updateAccountForUnknownIdThrowsUnknownPersonException() {
        assertThatThrownBy(() ->
            perform(post("/web/person/" + UNKNOWN_PERSON_ID + "/account"))
        ).hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    void updateAccountShowsFormIfValidationFailsOnFieldError() throws Exception {

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(somePerson()));
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("comment", "errors");
            return null;
        }).when(validator).validate(any(), any());

        perform(post("/web/person/" + SOME_PERSON_ID + "/account"))
            .andExpect(model().attributeHasErrors("account"))
            .andExpect(model().attribute("vacationTypeColors", equalTo(List.of(new VacationTypeDto(1L, ORANGE)))))
            .andExpect(view().name("account/account_form"));
    }

    @Test
    void updateAccountShowGlobalError() throws Exception {

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(somePerson()));

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.reject("errors");
            return null;
        }).when(validator).validate(any(), any());

        perform(post("/web/person/" + SOME_PERSON_ID + "/account"))
            .andExpect(view().name("account/account_form"))
            .andExpect(model().attributeHasErrors("account"));
    }

    @Test
    void updateAccountCallsEditForExistingAccountWithoutOverridingExpireVacationDays() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(signedInPerson));

        final LocalDate validFrom = LocalDate.now(clock).with(firstDayOfYear());
        final LocalDate validTo = validFrom.with(lastDayOfYear());

        final Account account = someAccount();
        account.setValidFrom(validFrom);
        account.setValidTo(validTo);
        account.setAnnualVacationDays(ZERO);
        account.setActualVacationDays(ZERO);
        account.setRemainingVacationDays(ZERO);

        when(accountService.getHolidaysAccount(Year.now(clock).getValue(), signedInPerson)).thenReturn(Optional.of(account));

        final LocalDate expiryDate = validFrom.withMonth(MARCH.getValue()).withDayOfMonth(1);

        final AccountForm accountForm = new AccountForm(account);
        accountForm.setExpiryDateLocally(expiryDate);
        accountForm.setRemainingVacationDaysNotExpiring(BigDecimal.valueOf(5L));
        accountForm.setOverrideVacationDaysExpire(false);
        accountForm.setDoRemainingVacationDaysExpireLocally(null);
        accountForm.setComment("comment");

        perform(post("/web/person/" + SOME_PERSON_ID + "/account")
            .flashAttr("account", accountForm));

        verify(accountInteractionService).editHolidaysAccount(account, validFrom, validTo, null, expiryDate, ZERO, ZERO, ZERO, BigDecimal.valueOf(5L), "comment");
    }

    @Test
    void updateAccountCallsEditForExistingAccountOverridingExpireVacationDays() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(signedInPerson));

        final LocalDate validFrom = LocalDate.now(clock).with(firstDayOfYear());
        final LocalDate validTo = validFrom.with(lastDayOfYear());

        final Account account = someAccount();
        account.setValidFrom(validFrom);
        account.setValidTo(validTo);
        account.setAnnualVacationDays(ZERO);
        account.setActualVacationDays(ZERO);
        account.setRemainingVacationDays(ZERO);

        when(accountService.getHolidaysAccount(Year.now(clock).getValue(), signedInPerson))
            .thenReturn(Optional.of(account));

        final LocalDate expiryDate = validFrom.withMonth(MARCH.getValue()).withDayOfMonth(1);

        final AccountForm accountForm = new AccountForm(account);
        accountForm.setExpiryDateLocally(expiryDate);
        accountForm.setRemainingVacationDaysNotExpiring(BigDecimal.valueOf(5L));
        accountForm.setOverrideVacationDaysExpire(true);
        accountForm.setDoRemainingVacationDaysExpireLocally(false);
        accountForm.setComment("comment");

        perform(post("/web/person/" + SOME_PERSON_ID + "/account")
            .flashAttr("account", accountForm));

        verify(accountInteractionService).editHolidaysAccount(account, validFrom, validTo, false, expiryDate, ZERO, ZERO, ZERO, BigDecimal.valueOf(5L), "comment");
    }

    @Test
    void updateAccountCallsUpdateOrCreateForNotExistingAccount() throws Exception {

        Person person = somePerson();
        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));

        when(accountService.getHolidaysAccount(Year.now(clock).getValue(), person)).thenReturn(Optional.empty());

        AccountForm mockedAccountForm = mock(AccountForm.class);
        when(mockedAccountForm.getHolidaysAccountValidFrom()).thenReturn(LocalDate.now(clock));

        perform(post("/web/person/" + SOME_PERSON_ID + "/account")
            .flashAttr("account", mockedAccountForm));

        verify(accountInteractionService).updateOrCreateHolidaysAccount(eq(person), any(), any(), isNull(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void updateAccountAddsFlashAttributeAndRedirectsToPerson() throws Exception {

        final Person accountPerson = new Person();
        accountPerson.setId(2L);

        when(personService.getPersonByID(2L)).thenReturn(Optional.of(accountPerson));
        when(accountService.getHolidaysAccount(Year.now(clock).getValue(), accountPerson)).thenReturn(Optional.of(someAccount()));

        AccountForm mockedAccountForm = mock(AccountForm.class);
        when(mockedAccountForm.getHolidaysAccountValidFrom()).thenReturn(LocalDate.now(clock));

        perform(post("/web/person/2/account")
            .flashAttr("account", mockedAccountForm))
            .andExpect(flash().attribute("updateSuccess", true))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/person/2"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"25.03.2022", "25.03.22", "25.3.2022", "25.3.22", "1.4.22"})
    void ensureUpdateAccountSucceedsWithValidFromDateFormat(String givenDate) throws Exception {

        when(personService.getPersonByID(5L)).thenReturn(Optional.of(somePerson()));

        perform(post("/web/person/5/account")
            .param("holidaysAccountValidFrom", givenDate))
            .andExpect(redirectedUrl("/web/person/5"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"25.03.2022", "25.03.22", "25.3.2022", "25.3.22", "1.4.22"})
    void ensureUpdateAccountSucceedsWithValidToDateFormat(String givenDate) throws Exception {

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(somePerson()));

        perform(
            post("/web/person/" + SOME_PERSON_ID + "/account")
                .param("holidaysAccountValidFrom", "01.01.2022")
                .param("holidaysAccountValidTo", givenDate)
        )
            .andExpect(redirectedUrl("/web/person/5"));
    }

    private Person somePerson() {
        return new Person();
    }

    private Account someAccount() {
        return new Account();
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
