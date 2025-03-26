package org.synyx.urlaubsverwaltung.companyvacation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDate.of;
import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;

@ExtendWith(MockitoExtension.class)
class CompanyVacationServiceImplTest {

    private CompanyVacationService sut;

    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        sut = new CompanyVacationServiceImpl(settingsService);
    }

    @Test
    void ensureCorrectWorkingDurationForWorkDay() {

        when(settingsService.getSettings()).thenReturn(new Settings());
        final LocalDate localDate = of(2013, Month.NOVEMBER, 27);

        final Optional<CompanyVacation> maybeCompanyVacation = sut.getCompanyVacation(localDate);
        assertThat(maybeCompanyVacation).isEmpty();
    }

    @Test
    void ensureCorrectWorkingDurationForCompanyVacation() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<CompanyVacation> maybeCompanyVacation = sut.getCompanyVacation(of(2013, DECEMBER, 25));
        assertThat(maybeCompanyVacation).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"FULL, 1", "MORNING, 0.5", "NOON, 0.5", "ZERO, 0"})
    void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOf(DayLength dayLength, BigDecimal workingDuration) {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForChristmasEve(dayLength);
        when(settingsService.getSettings()).thenReturn(settings);

        final Optional<CompanyVacation> maybeCompanyVacation = sut.getCompanyVacation(of(2013, DECEMBER, 24));
        assertThat(maybeCompanyVacation).hasValueSatisfying(companyVacation -> assertThat(companyVacation.workingDuration()).isEqualByComparingTo(workingDuration));
    }

    @ParameterizedTest
    @CsvSource({"FULL, 1", "MORNING, 0.5", "NOON, 0.5", "ZERO, 0"})
    void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOf(DayLength dayLength, BigDecimal workingDuration) {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForNewYearsEve(dayLength);
        when(settingsService.getSettings()).thenReturn(settings);

        final Optional<CompanyVacation> maybeCompanyVacation = sut.getCompanyVacation(of(2013, DECEMBER, 31));
        assertThat(maybeCompanyVacation).hasValueSatisfying(companyVacation -> assertThat(companyVacation.workingDuration()).isEqualByComparingTo(workingDuration));
    }


    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBerlin() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<CompanyVacation> maybeCompanyVacation = sut.getCompanyVacation(of(2015, AUGUST, 15));
        assertThat(maybeCompanyVacation).isEmpty();
    }

    @Test
    void ensureGetDayLengthReturnsForChristmasEve() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<CompanyVacation> maybeCompanyVacation = sut.getCompanyVacation(of(2019, DECEMBER, 24));
        assertThat(maybeCompanyVacation).hasValueSatisfying(companyVacation -> assertThat(companyVacation.dayLength()).isEqualTo(NOON));
    }

    @Test
    void ensureGetDayLengthReturnsForNewYearsEve() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<CompanyVacation> maybeCompanyVacation = sut.getCompanyVacation(of(2019, DECEMBER, 31));
        assertThat(maybeCompanyVacation).hasValueSatisfying(companyVacation -> assertThat(companyVacation.dayLength()).isEqualTo(NOON));
    }

    @Test
    void ensureGetCompanyVacationsReturnsForNewYearsEve() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final List<CompanyVacation> companyVacations = sut.getCompanyVacations(of(2019, DECEMBER, 30), of(2019, DECEMBER, 31));
        assertThat(companyVacations)
            .hasSize(1)
            .extracting(CompanyVacation::dayLength)
            .containsExactly(NOON);
    }

    @Test
    void ensureGetCompanyVacationsReturnsChristmasForMultipleYears() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final List<CompanyVacation> companyVacations = sut.getCompanyVacations(of(2020, JANUARY, 1), of(2023, DECEMBER, 31));

        assertThat(companyVacations).contains(
            new CompanyVacation(LocalDate.of(2020, DECEMBER, 24), NOON, "CHRISTMAS_EVE"),
            new CompanyVacation(LocalDate.of(2021, DECEMBER, 24), NOON, "CHRISTMAS_EVE"),
            new CompanyVacation(LocalDate.of(2023, DECEMBER, 24), NOON, "CHRISTMAS_EVE"));
    }

    @Test
    void ensureGetCompanyVacationsReturnsNewYearsEveForMultipleYears() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final List<CompanyVacation> companyVacations = sut.getCompanyVacations(of(2020, JANUARY, 1), of(2023, DECEMBER, 31));

        assertThat(companyVacations).contains(
            new CompanyVacation(LocalDate.of(2020, DECEMBER, 31), NOON, "NEW_YEARS_EVE"),
            new CompanyVacation(LocalDate.of(2021, DECEMBER, 31), NOON, "NEW_YEARS_EVE"),
            new CompanyVacation(LocalDate.of(2023, DECEMBER, 31), NOON, "NEW_YEARS_EVE"));
    }
}
