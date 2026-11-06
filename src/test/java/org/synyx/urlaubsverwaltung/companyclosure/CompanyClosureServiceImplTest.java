package org.synyx.urlaubsverwaltung.companyclosure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.synyx.urlaubsverwaltung.period.DayLength;

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
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;

@ExtendWith(MockitoExtension.class)
class CompanyClosureServiceImplTest {

    private static final MonthDay CHRISTMAS_EVE = MonthDay.of(DECEMBER, 24);

    private CompanyClosureServiceImpl sut;

    @Mock
    private CompanyClosureRepository companyClosureRepository;
    @Mock
    private CompanyClosureVersionRepository companyClosureVersionRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setUp() {
        final Clock clock = Clock.fixed(LocalDate.of(2026, NOVEMBER, 6).atStartOfDay(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));
        sut = new CompanyClosureServiceImpl(companyClosureRepository, companyClosureVersionRepository, clock, applicationEventPublisher);
    }

    @Test
    void returnsClosureOfEveryYearInRange() {
        givenChristmasEve(version(NOON, null));

        assertThat(sut.getCompanyClosures(LocalDate.of(2025, JANUARY, 1), LocalDate.of(2026, DECEMBER, 31)))
            .containsExactly(
                new CompanyClosure(LocalDate.of(2025, DECEMBER, 24), NOON, "Heiligabend"),
                new CompanyClosure(LocalDate.of(2026, DECEMBER, 24), NOON, "Heiligabend"));
    }

    @Test
    void olderYearsKeepTheVersionValidThen() {
        givenChristmasEve(version(NOON, null), version(FULL, LocalDate.of(2026, NOVEMBER, 6)));

        assertThat(sut.getCompanyClosures(LocalDate.of(2025, JANUARY, 1), LocalDate.of(2026, DECEMBER, 31)))
            .containsExactly(
                new CompanyClosure(LocalDate.of(2025, DECEMBER, 24), NOON, "Heiligabend"),
                new CompanyClosure(LocalDate.of(2026, DECEMBER, 24), FULL, "Heiligabend"));
    }

    @Test
    void zeroVersionMeansNoClosure() {
        givenChristmasEve(version(NOON, null), version(ZERO, LocalDate.of(2026, NOVEMBER, 6)));

        assertThat(sut.getCompanyClosures(LocalDate.of(2026, JANUARY, 1), LocalDate.of(2026, DECEMBER, 31))).isEmpty();
    }

    @Test
    void rejectsTooLongRange() {
        final var from = LocalDate.of(1000, JANUARY, 1);
        final var to = LocalDate.of(2026, JANUARY, 1);
        assertThatIllegalArgumentException().isThrownBy(() -> sut.getCompanyClosures(from, to));
    }

    @Test
    void rejectsValidFromBeforeCurrentYear() {
        final var validFrom = LocalDate.of(2025, DECEMBER, 31);
        assertThatIllegalArgumentException().isThrownBy(() -> sut.changeCompanyClosure(CHRISTMAS_EVE, FULL, validFrom));
        verifyNoInteractions(companyClosureVersionRepository, applicationEventPublisher);
    }

    @Test
    void allowsValidFromAtStartOfCurrentYear() {
        givenChristmasEve(version(NOON, null));

        sut.changeCompanyClosure(CHRISTMAS_EVE, FULL, LocalDate.of(2026, JANUARY, 1));

        verify(companyClosureVersionRepository).save(any(CompanyClosureVersionEntity.class));
        verify(applicationEventPublisher).publishEvent(new CompanyClosureChangedEvent(CHRISTMAS_EVE));
    }

    @Test
    void replacesVersionWithSameValidFrom() {
        final var validFrom = LocalDate.of(2026, NOVEMBER, 6);
        final var existing = version(NOON, validFrom);
        givenChristmasEve(version(NOON, null), existing);

        sut.changeCompanyClosure(CHRISTMAS_EVE, FULL, validFrom);

        assertThat(existing.getDayLength()).isEqualTo(FULL);
        verify(companyClosureVersionRepository).save(existing);
        verify(applicationEventPublisher).publishEvent(new CompanyClosureChangedEvent(CHRISTMAS_EVE));
    }

    @Test
    void unchangedValueSavesNothing() {
        givenChristmasEve(version(NOON, null));

        sut.changeCompanyClosure(CHRISTMAS_EVE, NOON, LocalDate.of(2026, NOVEMBER, 6));

        verify(companyClosureVersionRepository, never()).save(any());
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void rejectsUnknownMonthDay() {
        when(companyClosureRepository.findAll()).thenReturn(List.of(christmasEveEntity()));
        final var labourDay = MonthDay.of(MAY, 1);
        final var validFrom = LocalDate.of(2026, NOVEMBER, 6);

        assertThatIllegalArgumentException().isThrownBy(() -> sut.changeCompanyClosure(labourDay, FULL, validFrom));
        verify(companyClosureVersionRepository, never()).save(any());
    }

    @Test
    void createsDefaultsOnInitialSettings() {
        when(companyClosureRepository.findAll()).thenReturn(List.of());
        when(companyClosureRepository.save(any(CompanyClosureEntity.class))).thenAnswer(returnsFirstArg());

        sut.createDefaults();

        verify(companyClosureRepository).save(entity(12, 24, "Heiligabend"));
        verify(companyClosureRepository).save(entity(12, 31, "Silvester"));
        verify(companyClosureVersionRepository, org.mockito.Mockito.times(2)).save(any(CompanyClosureVersionEntity.class));
        verify(applicationEventPublisher).publishEvent(new CompanyClosureChangedEvent(CHRISTMAS_EVE));
        verify(applicationEventPublisher).publishEvent(new CompanyClosureChangedEvent(MonthDay.of(DECEMBER, 31)));
    }

    @Test
    void createsNoDefaultsWithExistingEntries() {
        when(companyClosureRepository.findAll()).thenReturn(List.of(christmasEveEntity()));

        sut.createDefaults();

        verify(companyClosureRepository, never()).save(any());
    }

    private void givenChristmasEve(CompanyClosureVersionEntity... versions) {
        when(companyClosureRepository.findAll()).thenReturn(List.of(christmasEveEntity()));
        when(companyClosureVersionRepository.findAll()).thenReturn(List.of(versions));
    }

    private static CompanyClosureEntity christmasEveEntity() {
        final var entity = new CompanyClosureEntity();
        entity.setId(1L);
        entity.setMonth(12);
        entity.setDayOfMonth(24);
        entity.setName("Heiligabend");
        return entity;
    }

    private static CompanyClosureVersionEntity version(DayLength dayLength, LocalDate validFrom) {
        final var version = new CompanyClosureVersionEntity();
        version.setCompanyClosureId(1L);
        version.setDayLength(dayLength);
        version.setValidFrom(validFrom);
        return version;
    }

    private static CompanyClosureEntity entity(int month, int day, String name) {
        return org.mockito.ArgumentMatchers.argThat(e -> e.getMonth() == month && e.getDayOfMonth() == day && name.equals(e.getName()));
    }
}
