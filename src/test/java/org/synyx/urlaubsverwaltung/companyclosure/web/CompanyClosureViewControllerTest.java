package org.synyx.urlaubsverwaltung.companyclosure.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.FlashMap;
import org.synyx.urlaubsverwaltung.blackoutperiod.BlackoutPeriodService;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureEntry;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureService;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureVersion;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Clock;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.MAY;
import static java.time.Month.NOVEMBER;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static java.util.Locale.forLanguageTag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;

@ExtendWith(MockitoExtension.class)
class CompanyClosureViewControllerTest {

    private static final MonthDay CHRISTMAS_EVE = MonthDay.of(DECEMBER, 24);
    private static final LocalDate TODAY = LocalDate.of(2026, NOVEMBER, 6);
    private static final Locale GERMAN_AUSTRIA = forLanguageTag("de-AT");
    private static final Locale GREEK = forLanguageTag("el");

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
            defaultPersonSuggestionUrlStrategy, personSearchUiFragmentSupplier, messageSource(), clock);
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
        final List<CompanyClosureEntry> entries = List.of(
            new CompanyClosureEntry(CHRISTMAS_EVE, locale -> "Heiligabend", Map.of(), christmasEveVersions),
            new CompanyClosureEntry(MonthDay.of(DECEMBER, 31), locale -> "Silvester", Map.of(), newYearsEveVersions));
        when(companyClosureService.getCompanyClosureEntries()).thenReturn(entries);

        final MvcResult result = perform(get("/web/company-closure"))
            .andExpect(view().name("companyclosure/company_closure_list"))
            .andReturn();

        final List<CompanyClosureVersion> christmasEveVersionsLatestFirst = List.of(
            new CompanyClosureVersion(ZERO, LocalDate.of(2027, JANUARY, 1)),
            new CompanyClosureVersion(FULL, LocalDate.of(2026, JANUARY, 1)),
            new CompanyClosureVersion(NOON, null));
        final List<CompanyClosureListDto> expectedCompanyClosures = List.of(
            new CompanyClosureListDto("12-24", CHRISTMAS_EVE, "Heiligabend", FULL, christmasEveVersionsLatestFirst),
            new CompanyClosureListDto("12-31", MonthDay.of(DECEMBER, 31), "Silvester", NOON, newYearsEveVersions));
        assertThat(result.getModelAndView().getModel()).containsEntry("companyClosures", expectedCompanyClosures);
    }

    @Test
    void newFormPrefillsFullDayAndToday() throws Exception {
        final MvcResult result = perform(get("/web/company-closure/new").locale(GERMAN))
            .andExpect(view().name("companyclosure/company_closure_form"))
            .andExpect(model().attribute("minValidFrom", LocalDate.of(2026, JANUARY, 1)))
            .andExpect(model().attribute("dayLengths", List.of(MORNING, NOON, FULL, ZERO)))
            .andReturn();

        final CompanyClosureFormDto form = (CompanyClosureFormDto) result.getModelAndView().getModel().get("companyClosure");
        assertThat(form.getNames()).containsExactly(
            new CompanyClosureNameFormDto(GERMAN, ""),
            new CompanyClosureNameFormDto(GERMAN_AUSTRIA, ""),
            new CompanyClosureNameFormDto(ENGLISH, ""),
            new CompanyClosureNameFormDto(GREEK, ""));
        final Map<Locale, String> expectedNamePlaceholders = Map.of(
            GERMAN, "Leer: „Betriebsruhe“",
            GERMAN_AUSTRIA, "Leer: „Betriebsruhe“",
            ENGLISH, "Leer: „Company closure“",
            GREEK, "Leer: „Αργία επιχείρησης“");
        assertThat(result.getModelAndView().getModel()).containsEntry("namePlaceholders", expectedNamePlaceholders);
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
            .andExpect(model().attribute("companyClosureName", "Christmas Eve"))
            .andExpect(model().attribute("minValidFrom", LocalDate.of(2026, JANUARY, 1)))
            .andExpect(model().attribute("dayLengths", List.of(MORNING, NOON, FULL, ZERO)))
            .andReturn();

        final CompanyClosureFormDto form = (CompanyClosureFormDto) result.getModelAndView().getModel().get("companyClosure");
        assertThat(form.getNames()).containsExactly(
            new CompanyClosureNameFormDto(GERMAN, "Heiligabend"),
            new CompanyClosureNameFormDto(GERMAN_AUSTRIA, "Heiligabend"),
            new CompanyClosureNameFormDto(ENGLISH, "Christmas Eve"),
            new CompanyClosureNameFormDto(GREEK, "Παραμονή Χριστουγέννων"));
        assertThat(form.getDayLength()).isEqualTo(FULL);
        assertThat(form.getValidFrom()).isEqualTo(TODAY);
    }

    @Test
    void editPrefillsNamesGivenByUserAndLeavesOtherLanguagesEmpty() throws Exception {
        final Map<Locale, String> names = Map.of(GERMAN, "Weihnachtsruhe", ENGLISH, "Christmas break");
        final List<CompanyClosureVersion> versions = List.of(new CompanyClosureVersion(NOON, null));
        final CompanyClosureEntry christmasEve = new CompanyClosureEntry(CHRISTMAS_EVE, locale -> "Weihnachtsruhe", names, versions);
        when(companyClosureService.getCompanyClosureEntries()).thenReturn(List.of(christmasEve));

        final MvcResult result = perform(get("/web/company-closure/12-24/edit")).andReturn();

        final CompanyClosureFormDto form = (CompanyClosureFormDto) result.getModelAndView().getModel().get("companyClosure");
        assertThat(form.getNames()).containsExactly(
            new CompanyClosureNameFormDto(GERMAN, "Weihnachtsruhe"),
            new CompanyClosureNameFormDto(GERMAN_AUSTRIA, ""),
            new CompanyClosureNameFormDto(ENGLISH, "Christmas break"),
            new CompanyClosureNameFormDto(GREEK, ""));
    }

    @Test
    void savesWithChosenValidFrom() throws Exception {
        givenChristmasEve();

        perform(withNames(post("/web/company-closure/12-24/edit"), "Weihnachten", "", "Christmas", "")
            .param("dayLength", "FULL")
            .param("validFrom", "01.01.2026"))
            .andExpect(redirectedUrl("/web/company-closure"))
            .andExpect(flash().attribute("updatedCompanyClosureName", "Christmas"));

        verify(companyClosureService).changeCompanyClosure(CHRISTMAS_EVE, FULL, LocalDate.of(2026, JANUARY, 1));
        verify(companyClosureService).renameCompanyClosure(CHRISTMAS_EVE, Map.of(GERMAN, "Weihnachten", ENGLISH, "Christmas"));
    }

    @Test
    void savesWithoutRenamingWhenNameIsUnchanged() throws Exception {
        givenChristmasEve();

        perform(withNames(post("/web/company-closure/12-24/edit"), "Heiligabend", "Heiligabend", "Christmas Eve", "Παραμονή Χριστουγέννων")
            .param("dayLength", "ZERO")
            .param("validFrom", "2026-11-06"))
            .andExpect(redirectedUrl("/web/company-closure"))
            .andExpect(flash().attribute("updatedCompanyClosureName", "Christmas Eve"));

        verify(companyClosureService).changeCompanyClosure(CHRISTMAS_EVE, ZERO, TODAY);
        verify(companyClosureService, never()).renameCompanyClosure(any(), any());
    }

    @Test
    void trimsNameBeforeSaving() throws Exception {
        givenChristmasEve();

        perform(withNames(post("/web/company-closure/12-24/edit"), "  Weihnachten ", " ", "", "")
            .param("dayLength", "FULL")
            .param("validFrom", "2026-11-06"))
            .andExpect(redirectedUrl("/web/company-closure"))
            .andExpect(flash().attribute("updatedCompanyClosureName", "Weihnachten"));

        verify(companyClosureService).renameCompanyClosure(CHRISTMAS_EVE, Map.of(GERMAN, "Weihnachten"));
    }

    @Test
    void createsNewDay() throws Exception {
        givenChristmasEve();

        perform(withNames(post("/web/company-closure/new"), "Brückentag", "Fenstertag", "Bridge day", "")
            .param("day", "15")
            .param("month", "5")
            .param("dayLength", "FULL")
            .param("validFrom", "06.11.2026"))
            .andExpect(redirectedUrl("/web/company-closure"))
            .andExpect(flash().attribute("createdCompanyClosureName", "Bridge day"));

        verify(companyClosureService).createCompanyClosure(MonthDay.of(MAY, 15),
            Map.of(GERMAN, "Brückentag", GERMAN_AUSTRIA, "Fenstertag", ENGLISH, "Bridge day"), FULL, TODAY);
    }

    @Test
    void showsFormAgainOnErrors() throws Exception {
        givenChristmasEve();

        final MvcResult rejected = perform(withNames(post("/web/company-closure/12-24/edit"), "", " ", "", "")
            .param("dayLength", "NOON")
            .param("validFrom", "31.12.2025"))
            .andExpect(redirectedUrl("/web/company-closure/12-24/edit"))
            .andReturn();

        final MvcResult result = perform(get("/web/company-closure/12-24/edit").flashAttrs(storedInSession(rejected.getFlashMap())))
            .andExpect(view().name("companyclosure/company_closure_form"))
            .andExpect(model().attributeHasFieldErrors("companyClosure", "names", "validFrom"))
            .andExpect(model().attribute("monthDay", CHRISTMAS_EVE))
            .andExpect(model().attribute("companyClosureName", "Christmas Eve"))
            .andExpect(model().attribute("minValidFrom", LocalDate.of(2026, JANUARY, 1)))
            .andExpect(model().attributeExists("namePlaceholders", "dayLengths"))
            .andReturn();

        // the form keeps the rejected input instead of the values of the closure day
        final CompanyClosureFormDto form = (CompanyClosureFormDto) result.getModelAndView().getModel().get("companyClosure");
        assertThat(form.getDayLength()).isEqualTo(NOON);
        assertThat(form.getValidFrom()).isEqualTo(LocalDate.of(2025, DECEMBER, 31));

        verify(companyClosureService, times(2)).getCompanyClosureEntries();
        verifyNoMoreInteractions(companyClosureService);
    }

    @Test
    void showsCreateFormAgainOnErrors() throws Exception {
        givenChristmasEve();

        final MvcResult rejected = perform(withNames(post("/web/company-closure/new"), "Heiligabend", "", "", "")
            .param("day", "24")
            .param("month", "12")
            .param("dayLength", "NOON")
            .param("validFrom", "06.11.2026"))
            .andExpect(redirectedUrl("/web/company-closure/new"))
            .andReturn();

        final MvcResult result = perform(get("/web/company-closure/new").flashAttrs(storedInSession(rejected.getFlashMap())))
            .andExpect(view().name("companyclosure/company_closure_form"))
            .andExpect(model().attributeHasFieldErrors("companyClosure", "monthDay"))
            .andExpect(model().attributeDoesNotExist("monthDay"))
            .andExpect(model().attributeExists("minValidFrom", "namePlaceholders", "dayLengths"))
            .andReturn();

        // the form keeps the rejected input instead of the defaults of a new closure day
        final CompanyClosureFormDto form = (CompanyClosureFormDto) result.getModelAndView().getModel().get("companyClosure");
        assertThat(form.getDay()).isEqualTo(24);
        assertThat(form.getMonth()).isEqualTo(12);
        assertThat(form.getDayLength()).isEqualTo(NOON);
        assertThat(form.nameFor(GERMAN)).isEqualTo("Heiligabend");

        verify(companyClosureService, never()).createCompanyClosure(any(), any(), any(), any());
    }

    @Test
    void showsCreateFormAgainWhenDayAndMonthAreNotNumbers() throws Exception {

        final MvcResult rejected = perform(withNames(post("/web/company-closure/new"), "Brückentag", "", "", "")
            .param("day", "x")
            .param("month", "Mai")
            .param("dayLength", "FULL")
            .param("validFrom", "06.11.2026"))
            .andExpect(redirectedUrl("/web/company-closure/new"))
            .andReturn();

        perform(get("/web/company-closure/new").flashAttrs(storedInSession(rejected.getFlashMap())))
            .andExpect(view().name("companyclosure/company_closure_form"))
            .andExpect(model().attributeHasFieldErrorCode("companyClosure", "day", "typeMismatch"))
            .andExpect(model().attributeHasFieldErrorCode("companyClosure", "month", "typeMismatch"))
            .andExpect(model().attributeErrorCount("companyClosure", 2));

        verify(companyClosureService, never()).createCompanyClosure(any(), any(), any(), any());
    }

    @Test
    void unknownMonthDayIs404() throws Exception {
        givenChristmasEve();

        perform(get("/web/company-closure/05-15/edit")).andExpect(status().isNotFound());
        perform(get("/web/company-closure/13-45/edit")).andExpect(status().isNotFound());
        perform(withNames(post("/web/company-closure/05-15/edit"), "Brückentag", "", "", "")
            .param("dayLength", "FULL")
            .param("validFrom", "06.11.2026"))
            .andExpect(status().isNotFound());

        verify(companyClosureService, never()).changeCompanyClosure(any(), any(), any());
    }

    /**
     * The session is stored in the database, so the flash attributes of a redirect are serialized.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> storedInSession(FlashMap flashMap) throws IOException, ClassNotFoundException {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(flashMap);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (Map<String, Object>) in.readObject();
        }
    }

    private void givenChristmasEve() {
        final List<CompanyClosureVersion> versions = List.of(
            new CompanyClosureVersion(NOON, null),
            new CompanyClosureVersion(FULL, LocalDate.of(2026, JANUARY, 1)));
        final CompanyClosureEntry christmasEve = new CompanyClosureEntry(CHRISTMAS_EVE, CompanyClosureViewControllerTest::christmasEveName, Map.of(), versions);
        when(companyClosureService.getCompanyClosureEntries()).thenReturn(List.of(christmasEve));
    }

    private static StaticMessageSource messageSource() {
        final StaticMessageSource messageSource = new StaticMessageSource();
        for (Locale locale : List.of(GERMAN, GERMAN_AUSTRIA)) {
            messageSource.addMessage("absences.overview.company-closure", locale, "Betriebsruhe");
            messageSource.addMessage("company-closure.data.names.placeholder", locale, "Leer: „{0}“");
        }
        messageSource.addMessage("absences.overview.company-closure", ENGLISH, "Company closure");
        messageSource.addMessage("company-closure.data.names.placeholder", ENGLISH, "Empty: “{0}”");
        messageSource.addMessage("absences.overview.company-closure", GREEK, "Αργία επιχείρησης");
        messageSource.addMessage("company-closure.data.names.placeholder", GREEK, "Κενό: «{0}»");
        return messageSource;
    }

    private static String christmasEveName(Locale locale) {
        return switch (locale.getLanguage()) {
            case "en" -> "Christmas Eve";
            case "el" -> "Παραμονή Χριστουγέννων";
            default -> "Heiligabend";
        };
    }

    /**
     * adds the names in the order of the form: german, german (austria), english and greek
     */
    private static MockHttpServletRequestBuilder withNames(MockHttpServletRequestBuilder builder, String german,
                                                           String germanAustria, String english, String greek) {
        final List<Locale> locales = List.of(GERMAN, GERMAN_AUSTRIA, ENGLISH, GREEK);
        final List<String> names = List.of(german, germanAustria, english, greek);
        for (int index = 0; index < locales.size(); index++) {
            builder.param("names[%d].locale".formatted(index), locales.get(index).toLanguageTag())
                .param("names[%d].name".formatted(index), names.get(index));
        }
        return builder;
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
