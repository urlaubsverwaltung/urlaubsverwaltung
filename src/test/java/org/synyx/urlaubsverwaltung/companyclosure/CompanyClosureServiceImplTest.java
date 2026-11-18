package org.synyx.urlaubsverwaltung.companyclosure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.MARCH;
import static java.time.Month.MAY;
import static java.time.Month.NOVEMBER;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static java.util.Locale.forLanguageTag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;

@ExtendWith(MockitoExtension.class)
class CompanyClosureServiceImplTest {

    private static final MonthDay CHRISTMAS_EVE = MonthDay.of(DECEMBER, 24);
    private static final Person MODIFIER = new Person("office", "Muster", "Marlene", "muster@example.org");

    private CompanyClosureServiceImpl sut;

    @Mock
    private CompanyClosureRepository companyClosureRepository;
    @Mock
    private CompanyClosureVersionRepository companyClosureVersionRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        final Clock clock = Clock.fixed(LocalDate.of(2026, NOVEMBER, 6).atStartOfDay(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));
        sut = new CompanyClosureServiceImpl(companyClosureRepository, companyClosureVersionRepository, clock, applicationEventPublisher, messageSource);
    }

    @Test
    void returnsClosureOfEveryYearInRange() {
        givenChristmasEve(version(NOON, null));

        assertThat(sut.getCompanyClosures(LocalDate.of(2025, JANUARY, 1), LocalDate.of(2026, DECEMBER, 31)))
            .extracting(CompanyClosure::date, CompanyClosure::dayLength)
            .containsExactly(
                tuple(LocalDate.of(2025, DECEMBER, 24), NOON),
                tuple(LocalDate.of(2026, DECEMBER, 24), NOON));
    }

    @Test
    void resolvesNameForTheRequestedLocale() {
        givenChristmasEve(version(NOON, null));
        when(messageSource.getMessage("companyclosure.name.christmasEve", new Object[]{}, null, GERMAN)).thenReturn("Heiligabend");
        when(messageSource.getMessage("companyclosure.name.christmasEve", new Object[]{}, null, ENGLISH)).thenReturn("Christmas Eve");

        final List<CompanyClosure> actual = sut.getCompanyClosures(LocalDate.of(2026, DECEMBER, 24), LocalDate.of(2026, DECEMBER, 24));

        assertThat(actual).hasSize(1);
        assertThat(actual.getFirst().name(GERMAN)).isEqualTo("Heiligabend");
        assertThat(actual.getFirst().name(ENGLISH)).isEqualTo("Christmas Eve");
    }

    @Test
    void fallsBackToGenericNameForUnknownMessageKey() {
        givenChristmasEve(version(NOON, null));
        when(messageSource.getMessage("companyclosure.name.christmasEve", new Object[]{}, null, ENGLISH)).thenReturn(null);
        when(messageSource.getMessage("absences.overview.company-closure", new Object[]{}, ENGLISH)).thenReturn("Company closure");

        final List<CompanyClosure> actual = sut.getCompanyClosures(LocalDate.of(2026, DECEMBER, 24), LocalDate.of(2026, DECEMBER, 24));

        assertThat(actual).hasSize(1);
        assertThat(actual.getFirst().name(ENGLISH)).isEqualTo("Company closure");
    }

    @Test
    void namesGivenByUserAreUsedPerLocale() {
        final CompanyClosureEntity christmasEve = christmasEveEntity();
        christmasEve.setNameByLocale(Map.of(GERMAN, "Weihnachtsruhe", ENGLISH, "Christmas break"));
        when(companyClosureRepository.findAll()).thenReturn(List.of(christmasEve));
        when(companyClosureVersionRepository.findAll()).thenReturn(List.of(version(NOON, null)));

        final List<CompanyClosure> actual = sut.getCompanyClosures(LocalDate.of(2026, DECEMBER, 24), LocalDate.of(2026, DECEMBER, 24));

        assertThat(actual).hasSize(1);
        assertThat(actual.getFirst().name(GERMAN)).isEqualTo("Weihnachtsruhe");
        assertThat(actual.getFirst().name(ENGLISH)).isEqualTo("Christmas break");
        verifyNoInteractions(messageSource);
    }

    @Test
    void nameGivenByUserFallsBackToLanguageOfLocale() {
        final CompanyClosureEntity christmasEve = christmasEveEntity();
        christmasEve.setNameByLocale(Map.of(GERMAN, "Weihnachtsruhe"));
        when(companyClosureRepository.findAll()).thenReturn(List.of(christmasEve));
        when(companyClosureVersionRepository.findAll()).thenReturn(List.of(version(NOON, null)));

        final List<CompanyClosure> actual = sut.getCompanyClosures(LocalDate.of(2026, DECEMBER, 24), LocalDate.of(2026, DECEMBER, 24));

        assertThat(actual).hasSize(1);
        assertThat(actual.getFirst().name(forLanguageTag("de-AT"))).isEqualTo("Weihnachtsruhe");
        verifyNoInteractions(messageSource);
    }

    @Test
    void languageWithoutNameGivenByUserShowsGenericName() {
        final CompanyClosureEntity christmasEve = christmasEveEntity();
        christmasEve.setNameByLocale(Map.of(GERMAN, "Weihnachtsruhe", ENGLISH, " "));
        when(companyClosureRepository.findAll()).thenReturn(List.of(christmasEve));
        when(companyClosureVersionRepository.findAll()).thenReturn(List.of(version(NOON, null)));
        when(messageSource.getMessage("absences.overview.company-closure", new Object[]{}, ENGLISH)).thenReturn("Company closure");

        final List<CompanyClosure> actual = sut.getCompanyClosures(LocalDate.of(2026, DECEMBER, 24), LocalDate.of(2026, DECEMBER, 24));

        assertThat(actual).hasSize(1);
        assertThat(actual.getFirst().name(ENGLISH)).isEqualTo("Company closure");
    }

    @Test
    void fallsBackToGenericNameWithoutNameAndMessageKey() {
        final CompanyClosureEntity christmasEve = christmasEveEntity();
        christmasEve.setMessageKey(null);
        when(companyClosureRepository.findAll()).thenReturn(List.of(christmasEve));
        when(companyClosureVersionRepository.findAll()).thenReturn(List.of(version(NOON, null)));
        when(messageSource.getMessage("absences.overview.company-closure", new Object[]{}, ENGLISH)).thenReturn("Company closure");

        final List<CompanyClosure> actual = sut.getCompanyClosures(LocalDate.of(2026, DECEMBER, 24), LocalDate.of(2026, DECEMBER, 24));

        assertThat(actual).hasSize(1);
        assertThat(actual.getFirst().name(ENGLISH)).isEqualTo("Company closure");
    }

    @Test
    void closureOnLeapDayExistsOnlyInLeapYears() {
        final CompanyClosureEntity leapDay = new CompanyClosureEntity();
        leapDay.setId(1L);
        leapDay.setMonth(2);
        leapDay.setDayOfMonth(29);
        leapDay.setMessageKey("companyclosure.name.leapDay");
        when(companyClosureRepository.findAll()).thenReturn(List.of(leapDay));
        when(companyClosureVersionRepository.findAll()).thenReturn(List.of(version(FULL, null)));

        assertThat(sut.getCompanyClosures(LocalDate.of(2027, JANUARY, 1), LocalDate.of(2029, DECEMBER, 31)))
            .extracting(CompanyClosure::date)
            .containsExactly(LocalDate.of(2028, FEBRUARY, 29));
    }

    @Test
    void olderYearsKeepTheVersionValidThen() {
        givenChristmasEve(version(NOON, null), version(FULL, LocalDate.of(2026, NOVEMBER, 6)));

        assertThat(sut.getCompanyClosures(LocalDate.of(2025, JANUARY, 1), LocalDate.of(2026, DECEMBER, 31)))
            .extracting(CompanyClosure::date, CompanyClosure::dayLength)
            .containsExactly(
                tuple(LocalDate.of(2025, DECEMBER, 24), NOON),
                tuple(LocalDate.of(2026, DECEMBER, 24), FULL));
    }

    @Test
    void skipsVersionQueryWhenNoClosureDayInRange() {
        when(companyClosureRepository.findAll()).thenReturn(List.of(christmasEveEntity()));

        assertThat(sut.getCompanyClosures(LocalDate.of(2026, MARCH, 1), LocalDate.of(2026, MARCH, 31))).isEmpty();

        verifyNoInteractions(companyClosureVersionRepository);
    }

    @Test
    void zeroVersionMeansNoClosure() {
        givenChristmasEve(version(NOON, null), version(ZERO, LocalDate.of(2026, NOVEMBER, 6)));

        assertThat(sut.getCompanyClosures(LocalDate.of(2026, JANUARY, 1), LocalDate.of(2026, DECEMBER, 31))).isEmpty();
    }

    @Test
    void rejectsTooLongRange() {
        final LocalDate from = LocalDate.of(1000, JANUARY, 1);
        final LocalDate to = LocalDate.of(2026, JANUARY, 1);
        assertThatIllegalArgumentException().isThrownBy(() -> sut.getCompanyClosures(from, to));
    }

    @Test
    void rejectsValidFromBeforeCurrentYear() {
        final LocalDate validFrom = LocalDate.of(2025, DECEMBER, 31);
        assertThatIllegalArgumentException().isThrownBy(() -> sut.changeCompanyClosure(CHRISTMAS_EVE, FULL, validFrom, MODIFIER));
        verifyNoInteractions(companyClosureVersionRepository, applicationEventPublisher);
    }

    @Test
    void allowsValidFromAtStartOfCurrentYear() {
        givenChristmasEve(version(NOON, null));

        sut.changeCompanyClosure(CHRISTMAS_EVE, FULL, LocalDate.of(2026, JANUARY, 1), MODIFIER);

        verify(companyClosureVersionRepository).save(any(CompanyClosureVersionEntity.class));
        verify(applicationEventPublisher).publishEvent(new CompanyClosureChangedEvent(CHRISTMAS_EVE));
    }

    @Test
    void replacesVersionWithSameValidFrom() {
        final LocalDate validFrom = LocalDate.of(2026, NOVEMBER, 6);
        final CompanyClosureVersionEntity existing = version(NOON, validFrom);
        givenChristmasEve(version(NOON, null), existing);

        sut.changeCompanyClosure(CHRISTMAS_EVE, FULL, validFrom, MODIFIER);

        assertThat(existing.getDayLength()).isEqualTo(FULL);
        assertThat(existing.getLastModifiedAt()).isEqualTo(Instant.parse("2026-11-06T00:00:00Z"));
        assertThat(existing.getLastModifiedBy()).isEqualTo(MODIFIER);
        verify(companyClosureVersionRepository).save(existing);
        verify(applicationEventPublisher).publishEvent(new CompanyClosureChangedEvent(CHRISTMAS_EVE));
    }

    @Test
    void unchangedValueSavesNothing() {
        givenChristmasEve(version(NOON, null));

        sut.changeCompanyClosure(CHRISTMAS_EVE, NOON, LocalDate.of(2026, NOVEMBER, 6), MODIFIER);

        verify(companyClosureVersionRepository, never()).save(any());
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void rejectsUnknownMonthDay() {
        when(companyClosureRepository.findAll()).thenReturn(List.of(christmasEveEntity()));
        final MonthDay labourDay = MonthDay.of(MAY, 1);
        final LocalDate validFrom = LocalDate.of(2026, NOVEMBER, 6);

        assertThatIllegalArgumentException().isThrownBy(() -> sut.changeCompanyClosure(labourDay, FULL, validFrom, MODIFIER));
        verify(companyClosureVersionRepository, never()).save(any());
    }

    @Test
    void newVersionRecordsTheModifier() {
        givenChristmasEve(version(NOON, null));

        sut.changeCompanyClosure(CHRISTMAS_EVE, FULL, LocalDate.of(2026, NOVEMBER, 6), MODIFIER);

        verify(companyClosureVersionRepository).save(argThat(version ->
            Instant.parse("2026-11-06T00:00:00Z").equals(version.getLastModifiedAt()) && MODIFIER.equals(version.getLastModifiedBy())));
    }

    @Test
    void forgetsDeletedModifierAndKeepsTheClosures() {
        final CompanyClosureEntity closure = christmasEveEntity();
        closure.setLastModifiedBy(MODIFIER);
        final CompanyClosureVersionEntity version = version(FULL, LocalDate.of(2026, NOVEMBER, 6));
        version.setLastModifiedBy(MODIFIER);
        when(companyClosureRepository.findByLastModifiedBy(MODIFIER)).thenReturn(List.of(closure));
        when(companyClosureVersionRepository.findByLastModifiedBy(MODIFIER)).thenReturn(List.of(version));

        sut.forgetDeletedModifier(new PersonDeletedEvent(MODIFIER));

        assertThat(closure.getLastModifiedBy()).isNull();
        assertThat(version.getLastModifiedBy()).isNull();
        verify(companyClosureRepository).saveAll(List.of(closure));
        verify(companyClosureVersionRepository).saveAll(List.of(version));
        verify(companyClosureRepository, never()).delete(any());
        verify(companyClosureVersionRepository, never()).delete(any());
    }

    @Test
    void createsDefaultsOnInitialSettings() {
        when(companyClosureRepository.findAll()).thenReturn(List.of());
        when(companyClosureRepository.save(any(CompanyClosureEntity.class))).thenAnswer(returnsFirstArg());

        sut.createDefaults();

        final ArgumentCaptor<CompanyClosureEntity> entityCaptor = ArgumentCaptor.forClass(CompanyClosureEntity.class);
        verify(companyClosureRepository, times(2)).save(entityCaptor.capture());
        assertThat(entityCaptor.getAllValues())
            .extracting(CompanyClosureEntity::getMonth, CompanyClosureEntity::getDayOfMonth, CompanyClosureEntity::getMessageKey)
            .containsExactlyInAnyOrder(
                tuple(12, 24, "companyclosure.name.christmasEve"),
                tuple(12, 31, "companyclosure.name.newYearsEve"));
        verify(companyClosureVersionRepository, times(2)).save(any(CompanyClosureVersionEntity.class));
        verify(applicationEventPublisher).publishEvent(new CompanyClosureChangedEvent(CHRISTMAS_EVE));
        verify(applicationEventPublisher).publishEvent(new CompanyClosureChangedEvent(MonthDay.of(DECEMBER, 31)));
    }

    @Test
    void createsNoDefaultsWithExistingEntries() {
        when(companyClosureRepository.findAll()).thenReturn(List.of(christmasEveEntity()));

        sut.createDefaults();

        verify(companyClosureRepository, never()).save(any());
    }

    @Test
    void createsEntryWithFirstVersion() {
        when(companyClosureRepository.findAll()).thenReturn(List.of(christmasEveEntity()));
        when(companyClosureRepository.save(any(CompanyClosureEntity.class))).thenAnswer(invocation -> {
            final CompanyClosureEntity entity = invocation.getArgument(0);
            entity.setId(2L);
            return entity;
        });
        final MonthDay bridgeDay = MonthDay.of(MAY, 15);
        final LocalDate validFrom = LocalDate.of(2026, NOVEMBER, 6);

        sut.createCompanyClosure(bridgeDay, Map.of(GERMAN, " Brückentag ", ENGLISH, ""), FULL, validFrom);

        final ArgumentCaptor<CompanyClosureEntity> entityCaptor = ArgumentCaptor.forClass(CompanyClosureEntity.class);
        verify(companyClosureRepository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue())
            .extracting(CompanyClosureEntity::getMonth, CompanyClosureEntity::getDayOfMonth, CompanyClosureEntity::getNameByLocale, CompanyClosureEntity::getMessageKey)
            .containsExactly(5, 15, Map.of(GERMAN, "Brückentag"), null);

        final ArgumentCaptor<CompanyClosureVersionEntity> versionCaptor = ArgumentCaptor.forClass(CompanyClosureVersionEntity.class);
        verify(companyClosureVersionRepository).save(versionCaptor.capture());
        assertThat(versionCaptor.getValue())
            .extracting(CompanyClosureVersionEntity::getCompanyClosureId, CompanyClosureVersionEntity::getDayLength, CompanyClosureVersionEntity::getValidFrom)
            .containsExactly(2L, FULL, validFrom);

        verify(applicationEventPublisher).publishEvent(new CompanyClosureChangedEvent(bridgeDay));
    }

    @Test
    void rejectsExistingDay() {
        when(companyClosureRepository.findAll()).thenReturn(List.of(christmasEveEntity()));
        final LocalDate validFrom = LocalDate.of(2026, NOVEMBER, 6);
        final Map<Locale, String> names = Map.of(GERMAN, "Heiligabend");

        assertThatIllegalArgumentException().isThrownBy(() -> sut.createCompanyClosure(CHRISTMAS_EVE, names, FULL, validFrom));
        verify(companyClosureRepository, never()).save(any());
        verifyNoInteractions(companyClosureVersionRepository, applicationEventPublisher);
    }

    @Test
    void rejectsLeapDay() {
        final MonthDay leapDay = MonthDay.of(FEBRUARY, 29);
        final LocalDate validFrom = LocalDate.of(2026, NOVEMBER, 6);
        final Map<Locale, String> names = Map.of(GERMAN, "Schalttag");

        assertThatIllegalArgumentException().isThrownBy(() -> sut.createCompanyClosure(leapDay, names, FULL, validFrom));
        verifyNoInteractions(companyClosureRepository, companyClosureVersionRepository, applicationEventPublisher);
    }

    @Test
    void rejectsCreationWithValidFromBeforeCurrentYear() {
        final MonthDay bridgeDay = MonthDay.of(MAY, 15);
        final LocalDate validFrom = LocalDate.of(2025, DECEMBER, 31);
        final Map<Locale, String> names = Map.of(GERMAN, "Brückentag");

        assertThatIllegalArgumentException().isThrownBy(() -> sut.createCompanyClosure(bridgeDay, names, FULL, validFrom));
        verifyNoInteractions(companyClosureRepository, companyClosureVersionRepository, applicationEventPublisher);
    }

    @Test
    void rejectsCreationWithoutName() {
        final MonthDay bridgeDay = MonthDay.of(MAY, 15);
        final LocalDate validFrom = LocalDate.of(2026, NOVEMBER, 6);
        final Map<Locale, String> names = Map.of(GERMAN, " ");

        assertThatIllegalArgumentException().isThrownBy(() -> sut.createCompanyClosure(bridgeDay, names, FULL, validFrom));
        verifyNoInteractions(companyClosureRepository, companyClosureVersionRepository, applicationEventPublisher);
    }

    @Test
    void renamesEntry() {
        final CompanyClosureEntity christmasEve = christmasEveEntity();
        when(companyClosureRepository.findAll()).thenReturn(List.of(christmasEve));

        sut.renameCompanyClosure(CHRISTMAS_EVE, Map.of(GERMAN, "Weihnachten", ENGLISH, "Christmas"));

        assertThat(christmasEve.getNameByLocale()).isEqualTo(Map.of(GERMAN, "Weihnachten", ENGLISH, "Christmas"));
        assertThat(christmasEve.getMessageKey()).isEqualTo("companyclosure.name.christmasEve");
        verify(companyClosureRepository).save(christmasEve);
    }

    @Test
    void renameRejectsUnknownMonthDay() {
        when(companyClosureRepository.findAll()).thenReturn(List.of(christmasEveEntity()));
        final MonthDay labourDay = MonthDay.of(MAY, 1);
        final Map<Locale, String> names = Map.of(GERMAN, "Tag der Arbeit");

        assertThatIllegalArgumentException().isThrownBy(() -> sut.renameCompanyClosure(labourDay, names));
        verify(companyClosureRepository, never()).save(any());
    }

    @Test
    void renameRejectsMissingName() {
        final Map<Locale, String> names = Map.of(GERMAN, "");

        assertThatIllegalArgumentException().isThrownBy(() -> sut.renameCompanyClosure(CHRISTMAS_EVE, names));
        verify(companyClosureRepository, never()).save(any());
    }

    @Test
    void returnsEntriesWithSortedVersions() {
        final CompanyClosureEntity newYearsEve = new CompanyClosureEntity();
        newYearsEve.setId(3L);
        newYearsEve.setMonth(12);
        newYearsEve.setDayOfMonth(31);
        newYearsEve.setNameByLocale(Map.of(GERMAN, "Silvester"));
        when(companyClosureRepository.findAll()).thenReturn(List.of(newYearsEve, christmasEveEntity()));

        final CompanyClosureVersionEntity newYearsEveVersion = version(NOON, null);
        newYearsEveVersion.setCompanyClosureId(3L);
        final List<CompanyClosureVersionEntity> versions = List.of(
            version(FULL, LocalDate.of(2026, NOVEMBER, 6)),
            newYearsEveVersion,
            version(ZERO, LocalDate.of(2026, JANUARY, 1)),
            version(NOON, null));
        when(companyClosureVersionRepository.findAll()).thenReturn(versions);

        when(messageSource.getMessage("companyclosure.name.christmasEve", new Object[]{}, null, GERMAN)).thenReturn("Heiligabend");

        final List<CompanyClosureVersion> christmasEveVersions = List.of(
            new CompanyClosureVersion(NOON, null),
            new CompanyClosureVersion(ZERO, LocalDate.of(2026, JANUARY, 1)),
            new CompanyClosureVersion(FULL, LocalDate.of(2026, NOVEMBER, 6)));
        final List<CompanyClosureVersion> newYearsEveVersions = List.of(new CompanyClosureVersion(NOON, null));
        assertThat(sut.getCompanyClosureEntries())
            .extracting(CompanyClosureEntry::monthDay, entry -> entry.name(GERMAN), CompanyClosureEntry::names, CompanyClosureEntry::versions)
            .containsExactly(
                tuple(CHRISTMAS_EVE, "Heiligabend", Map.of(), christmasEveVersions),
                tuple(MonthDay.of(DECEMBER, 31), "Silvester", Map.of(GERMAN, "Silvester"), newYearsEveVersions));
    }

    private void givenChristmasEve(CompanyClosureVersionEntity... versions) {
        when(companyClosureRepository.findAll()).thenReturn(List.of(christmasEveEntity()));
        when(companyClosureVersionRepository.findAll()).thenReturn(List.of(versions));
    }

    private static CompanyClosureEntity christmasEveEntity() {
        final CompanyClosureEntity entity = new CompanyClosureEntity();
        entity.setId(1L);
        entity.setMonth(12);
        entity.setDayOfMonth(24);
        entity.setMessageKey("companyclosure.name.christmasEve");
        return entity;
    }

    private static CompanyClosureVersionEntity version(DayLength dayLength, LocalDate validFrom) {
        final CompanyClosureVersionEntity version = new CompanyClosureVersionEntity();
        version.setCompanyClosureId(1L);
        version.setDayLength(dayLength);
        version.setValidFrom(validFrom);
        return version;
    }
}
