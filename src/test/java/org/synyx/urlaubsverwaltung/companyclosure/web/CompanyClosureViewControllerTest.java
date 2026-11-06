package org.synyx.urlaubsverwaltung.companyclosure.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.blackoutperiod.BlackoutPeriodService;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureEntry;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureService;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureVersion;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.util.List;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.MAY;
import static java.time.Month.NOVEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
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
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;

@ExtendWith(MockitoExtension.class)
class CompanyClosureViewControllerTest {

    private static final MonthDay CHRISTMAS_EVE = MonthDay.of(DECEMBER, 24);
    private static final LocalDate TODAY = LocalDate.of(2026, NOVEMBER, 6);

    private CompanyClosureViewController sut;

    @Mock
    private CompanyClosureService companyClosureService;
    @Mock
    private BlackoutPeriodService blackoutPeriodService;
    @Mock
    private PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy;
    @Mock
    private PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier;

    @BeforeEach
    void setUp() {
        final Clock clock = Clock.fixed(TODAY.atStartOfDay(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));
        sut = new CompanyClosureViewController(companyClosureService, new CompanyClosureFormValidator(companyClosureService, blackoutPeriodService, clock),
            defaultPersonSuggestionUrlStrategy, personSearchUiFragmentSupplier, clock);
    }

    @Test
    void personSearch() {
        assertThat(sut.personSuggestionUrlStrategy()).isSameAs(defaultPersonSuggestionUrlStrategy);
        assertThat(sut.personSearchUiFragmentSupplier()).isSameAs(personSearchUiFragmentSupplier);
    }

    @Test
    void listShowsCurrentValueAndHistory() throws Exception {
        final List<CompanyClosureVersion> christmasEveVersions = List.of(
            new CompanyClosureVersion(NOON, null),
            new CompanyClosureVersion(FULL, LocalDate.of(2026, JANUARY, 1)),
            new CompanyClosureVersion(ZERO, LocalDate.of(2027, JANUARY, 1)));
        final List<CompanyClosureVersion> newYearsEveVersions = List.of(new CompanyClosureVersion(NOON, null));
        when(companyClosureService.getCompanyClosureEntries()).thenReturn(List.of(
            new CompanyClosureEntry(CHRISTMAS_EVE, "Heiligabend", christmasEveVersions),
            new CompanyClosureEntry(MonthDay.of(DECEMBER, 31), "Silvester", newYearsEveVersions)));

        final MvcResult result = perform(get("/web/company-closure"))
            .andExpect(view().name("companyclosure/company_closure_list"))
            .andReturn();

        assertThat(result.getModelAndView().getModel().get("companyClosures")).isEqualTo(List.of(
            new CompanyClosureListDto("12-24", CHRISTMAS_EVE, "Heiligabend", FULL, christmasEveVersions),
            new CompanyClosureListDto("12-31", MonthDay.of(DECEMBER, 31), "Silvester", NOON, newYearsEveVersions)));
    }

    @Test
    void newFormPrefillsFullDayAndToday() throws Exception {
        final MvcResult result = perform(get("/web/company-closure/new"))
            .andExpect(view().name("companyclosure/company_closure_form"))
            .andExpect(model().attribute("minValidFrom", LocalDate.of(2026, JANUARY, 1)))
            .andReturn();

        final CompanyClosureForm form = (CompanyClosureForm) result.getModelAndView().getModel().get("companyClosure");
        assertThat(form.getDayLength()).isEqualTo(FULL);
        assertThat(form.getValidFrom()).isEqualTo(TODAY);
        assertThat(result.getModelAndView().getModel().get("monthDay")).isNull();
    }

    @Test
    void editPrefillsToday() throws Exception {
        givenChristmasEve();

        final MvcResult result = perform(get("/web/company-closure/12-24/edit"))
            .andExpect(view().name("companyclosure/company_closure_form"))
            .andExpect(model().attribute("monthDay", CHRISTMAS_EVE))
            .andExpect(model().attribute("monthDayPath", "12-24"))
            .andExpect(model().attribute("companyClosureName", "Heiligabend"))
            .andExpect(model().attribute("minValidFrom", LocalDate.of(2026, JANUARY, 1)))
            .andReturn();

        final CompanyClosureForm form = (CompanyClosureForm) result.getModelAndView().getModel().get("companyClosure");
        assertThat(form.getName()).isEqualTo("Heiligabend");
        assertThat(form.getDayLength()).isEqualTo(FULL);
        assertThat(form.getValidFrom()).isEqualTo(TODAY);
    }

    @Test
    void savesWithChosenValidFrom() throws Exception {
        givenChristmasEve();

        perform(post("/web/company-closure/12-24")
            .param("name", "Weihnachten")
            .param("dayLength", "FULL")
            .param("validFrom", "01.01.2026"))
            .andExpect(redirectedUrl("/web/company-closure"))
            .andExpect(flash().attribute("updatedCompanyClosureName", "Weihnachten"));

        verify(companyClosureService).changeCompanyClosure(CHRISTMAS_EVE, FULL, LocalDate.of(2026, JANUARY, 1));
        verify(companyClosureService).renameCompanyClosure(CHRISTMAS_EVE, "Weihnachten");
    }

    @Test
    void savesWithoutRenamingWhenNameIsUnchanged() throws Exception {
        givenChristmasEve();

        perform(post("/web/company-closure/12-24")
            .param("name", "Heiligabend")
            .param("dayLength", "ZERO")
            .param("validFrom", "2026-11-06"))
            .andExpect(redirectedUrl("/web/company-closure"));

        verify(companyClosureService).changeCompanyClosure(CHRISTMAS_EVE, ZERO, TODAY);
        verify(companyClosureService, never()).renameCompanyClosure(any(), anyString());
    }

    @Test
    void trimsNameBeforeSaving() throws Exception {
        givenChristmasEve();

        perform(post("/web/company-closure/12-24")
            .param("name", "  Weihnachten ")
            .param("dayLength", "FULL")
            .param("validFrom", "2026-11-06"))
            .andExpect(redirectedUrl("/web/company-closure"));

        verify(companyClosureService).renameCompanyClosure(CHRISTMAS_EVE, "Weihnachten");
    }

    @Test
    void createsNewDay() throws Exception {
        givenChristmasEve();

        perform(post("/web/company-closure")
            .param("day", "15")
            .param("month", "5")
            .param("name", "Brückentag")
            .param("dayLength", "FULL")
            .param("validFrom", "06.11.2026"))
            .andExpect(redirectedUrl("/web/company-closure"))
            .andExpect(flash().attribute("createdCompanyClosureName", "Brückentag"));

        verify(companyClosureService).createCompanyClosure(MonthDay.of(MAY, 15), "Brückentag", FULL, TODAY);
    }

    @Test
    void showsFormAgainOnErrors() throws Exception {
        givenChristmasEve();

        perform(post("/web/company-closure/12-24")
            .param("name", "")
            .param("dayLength", "FULL")
            .param("validFrom", "31.12.2025"))
            .andExpect(view().name("companyclosure/company_closure_form"))
            .andExpect(model().attributeHasFieldErrors("companyClosure", "name", "validFrom"))
            .andExpect(model().attribute("monthDay", CHRISTMAS_EVE))
            .andExpect(model().attribute("companyClosureName", "Heiligabend"))
            .andExpect(model().attribute("minValidFrom", LocalDate.of(2026, JANUARY, 1)));

        verify(companyClosureService).getCompanyClosureEntries();
        verifyNoMoreInteractions(companyClosureService);
    }

    @Test
    void showsCreateFormAgainOnErrors() throws Exception {
        givenChristmasEve();

        perform(post("/web/company-closure")
            .param("day", "24")
            .param("month", "12")
            .param("name", "Heiligabend")
            .param("dayLength", "FULL")
            .param("validFrom", "06.11.2026"))
            .andExpect(view().name("companyclosure/company_closure_form"))
            .andExpect(model().attributeHasFieldErrors("companyClosure", "day"))
            .andExpect(model().attributeDoesNotExist("monthDay"));

        verify(companyClosureService, never()).createCompanyClosure(any(), any(), any(), any());
    }

    @Test
    void unknownMonthDayIs404() throws Exception {
        givenChristmasEve();

        perform(get("/web/company-closure/05-15/edit")).andExpect(status().isNotFound());
        perform(get("/web/company-closure/13-45/edit")).andExpect(status().isNotFound());
        perform(post("/web/company-closure/05-15")
            .param("name", "Brückentag")
            .param("dayLength", "FULL")
            .param("validFrom", "06.11.2026"))
            .andExpect(status().isNotFound());

        verify(companyClosureService, never()).changeCompanyClosure(any(), any(), any());
    }

    private void givenChristmasEve() {
        when(companyClosureService.getCompanyClosureEntries()).thenReturn(List.of(
            new CompanyClosureEntry(CHRISTMAS_EVE, "Heiligabend", List.of(
                new CompanyClosureVersion(NOON, null),
                new CompanyClosureVersion(FULL, LocalDate.of(2026, JANUARY, 1))))));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
