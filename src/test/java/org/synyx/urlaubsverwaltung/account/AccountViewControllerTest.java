package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class AccountViewControllerTest {

    private AccountViewController sut;

    private static final int UNKNOWN_PERSON_ID = 715;
    private static final int SOME_PERSON_ID = 5;

    @Mock
    private PersonService personService;
    @Mock
    private AccountService accountService;
    @Mock
    private AccountInteractionService accountInteractionService;
    @Mock
    private AccountFormValidator validator;
    @Mock
    private AccountForm accountForm;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new AccountViewController(personService, accountService, accountInteractionService, validator, clock);
    }

    @Test
    void editAccountForUnknownIdThrowsUnknownPersonException() {
        assertThatThrownBy(() ->
            perform(get("/web/person/" + UNKNOWN_PERSON_ID + "/account"))
        ).hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    void editAccountProvidesCorrectModelAndView() throws Exception {

        final Person person = somePerson();
        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));

        perform(get("/web/person/" + SOME_PERSON_ID + "/account"))
            .andExpect(model().attribute("person", person))
            .andExpect(model().attribute("account", instanceOf(AccountForm.class)))
            .andExpect(model().attribute("year", notNullValue()))
            .andExpect(view().name("account/account_form"));
    }

    @Test
    void editAccountUsesProvidedYear() throws Exception {

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(somePerson()));

        final int providedYear = 1987;

        perform(get("/web/person/" + SOME_PERSON_ID + "/account")
            .param("year", Integer.toString(providedYear)))
            .andExpect(model().attribute("year", providedYear));
    }

    @Test
    void editAccountDefaultsToCurrentYear() throws Exception {

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(somePerson()));

        final int currentYear = Year.now(clock).getValue();

        perform(get("/web/person/" + SOME_PERSON_ID + "/account"))
            .andExpect(model().attribute("year", currentYear));
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

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("comment", "errors");
            return null;
        }).when(validator).validate(any(), any());

        perform(post("/web/person/" + SOME_PERSON_ID + "/account"))
            .andExpect(view().name("account/account_form"))
            .andExpect(model().attributeHasErrors("account"));
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
    void updateAccountCallsEditForExistingAccount() throws Exception {

        when(accountForm.getHolidaysAccountValidFrom()).thenReturn(LocalDate.now(clock));

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(somePerson()));

        Account account = someAccount();
        when(accountService.getHolidaysAccount(anyInt(), any())).thenReturn(Optional.of(account));

        perform(post("/web/person/" + SOME_PERSON_ID + "/account")
            .flashAttr("account", accountForm));

        verify(accountInteractionService).editHolidaysAccount(eq(account), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void updateAccountCallsUpdateOrCreateForNotExistingAccount() throws Exception {

        Person person = somePerson();
        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(person));

        when(accountService.getHolidaysAccount(anyInt(), any())).thenReturn(Optional.empty());

        AccountForm mockedAccountForm = mock(AccountForm.class);
        when(mockedAccountForm.getHolidaysAccountValidFrom()).thenReturn(LocalDate.now(clock));

        perform(post("/web/person/" + SOME_PERSON_ID + "/account")
            .flashAttr("account", mockedAccountForm));

        verify(accountInteractionService).updateOrCreateHolidaysAccount(eq(person), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void updateAccountAddsFlashAttributeAndRedirectsToPerson() throws Exception {

        when(personService.getPersonByID(SOME_PERSON_ID)).thenReturn(Optional.of(somePerson()));
        when(accountService.getHolidaysAccount(anyInt(), any())).thenReturn(Optional.of(someAccount()));

        AccountForm mockedAccountForm = mock(AccountForm.class);
        when(mockedAccountForm.getHolidaysAccountValidFrom()).thenReturn(LocalDate.now(clock));

        perform(post("/web/person/" + SOME_PERSON_ID + "/account")
            .flashAttr("account", mockedAccountForm))
            .andExpect(flash().attribute("updateSuccess", true))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/person/" + SOME_PERSON_ID));
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
