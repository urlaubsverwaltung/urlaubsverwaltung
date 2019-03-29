package org.synyx.urlaubsverwaltung.application.web;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.account.service.AccountService;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.settings.WorkingTimeSettings;
import org.synyx.urlaubsverwaltung.security.SessionService;

import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.OVERTIME;

@RunWith(MockitoJUnitRunner.class)
public class ApplyForLeaveControllerTest {

    private ApplyForLeaveController sut;

    @Mock
    private SessionService sessionService;
    @Mock
    private PersonService personService;
    @Mock
    private AccountService accountService;
    @Mock
    private VacationTypeService vacationTypeService;
    @Mock
    private ApplicationInteractionService applicationInteractionService;
    @Mock
    private ApplicationValidator applicationValidator;
    @Mock
    private SettingsService settingsService;

    private Person person;

    @Before
    public void setUp() {
        sut = new ApplyForLeaveController(sessionService, personService, accountService, vacationTypeService,
            applicationInteractionService, applicationValidator, settingsService);

        person = new Person();
        when(sessionService.getSignedInUser()).thenReturn(person);
    }

    @Test
    public void overtimeIsActivated() throws Exception {

        when(accountService.getHolidaysAccount(2019,person)).thenReturn(Optional.of(new Account()));

        final VacationType vacationType = new VacationType();
        when(vacationTypeService.getVacationTypes()).thenReturn(singletonList(vacationType));

        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setOvertimeActive(true);

        final Settings settings = new Settings();
        settings.setWorkingTimeSettings(workingTimeSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        final MockHttpServletRequestBuilder builder = get("/web/application/new");

        final ResultActions resultActions = perform(builder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("overtimeActive", is(true)));
        resultActions.andExpect(model().attribute("vacationTypes", hasItems(vacationType)));
    }

    @Test
    public void overtimeIsDeactivated() throws Exception {

        when(accountService.getHolidaysAccount(2019,person)).thenReturn(Optional.of(new Account()));

        final VacationType vacationType = new VacationType();
        when(vacationTypeService.getVacationTypesFilteredBy(OVERTIME)).thenReturn(singletonList(vacationType));

        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setOvertimeActive(false);

        final Settings settings = new Settings();
        settings.setWorkingTimeSettings(workingTimeSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        MockHttpServletRequestBuilder builder = get("/web/application/new");

        final ResultActions resultActions = perform(builder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("overtimeActive", is(false)));
        resultActions.andExpect(model().attribute("vacationTypes", hasItems(vacationType)));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {

        return standaloneSetup(sut).build().perform(builder);
    }

}
