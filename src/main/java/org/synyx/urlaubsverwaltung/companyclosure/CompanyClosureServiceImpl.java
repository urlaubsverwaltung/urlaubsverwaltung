package org.synyx.urlaubsverwaltung.companyclosure;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.period.DateLimits;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.settings.InitialDefaultSettingsSavedEvent;

import java.time.Clock;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.util.Comparator.nullsFirst;
import static java.util.stream.Collectors.groupingBy;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;

@Service
class CompanyClosureServiceImpl implements CompanyClosureService {

    private static final Comparator<CompanyClosureVersionEntity> BY_VALID_FROM =
        Comparator.comparing(CompanyClosureVersionEntity::getValidFrom, nullsFirst(Comparator.naturalOrder()));

    private static final MonthDay LEAP_DAY = MonthDay.of(FEBRUARY, 29);

    private final CompanyClosureRepository companyClosureRepository;
    private final CompanyClosureVersionRepository companyClosureVersionRepository;
    private final Clock clock;
    private final ApplicationEventPublisher applicationEventPublisher;

    CompanyClosureServiceImpl(CompanyClosureRepository companyClosureRepository,
                              CompanyClosureVersionRepository companyClosureVersionRepository,
                              Clock clock, ApplicationEventPublisher applicationEventPublisher) {
        this.companyClosureRepository = companyClosureRepository;
        this.companyClosureVersionRepository = companyClosureVersionRepository;
        this.clock = clock;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyClosure> getCompanyClosures(LocalDate from, LocalDate to) {
        DateLimits.requireCalculableDuration(new DateRange(from, to));

        // only closure days within the range need their versions, most ranges contain none
        final var candidates = new ArrayList<Map.Entry<CompanyClosureEntity, LocalDate>>();
        for (CompanyClosureEntity entity : companyClosureRepository.findAll()) {
            final var monthDay = monthDayOf(entity);
            for (int year = from.getYear(); year <= to.getYear(); year++) {
                if (!monthDay.isValidYear(year)) {
                    continue;
                }
                final LocalDate date = monthDay.atYear(year);
                if (!date.isBefore(from) && !date.isAfter(to)) {
                    candidates.add(Map.entry(entity, date));
                }
            }
        }
        if (candidates.isEmpty()) {
            return List.of();
        }

        final var versions = companyClosureVersionRepository.findAll();
        final var closures = new ArrayList<CompanyClosure>();
        for (var candidate : candidates) {
            final CompanyClosureEntity entity = candidate.getKey();
            final LocalDate date = candidate.getValue();
            versionValidAt(versions, entity.getId(), date)
                .map(CompanyClosureVersionEntity::getDayLength)
                .filter(dayLength -> dayLength != ZERO)
                .ifPresent(dayLength -> closures.add(new CompanyClosure(date, dayLength, entity.getName())));
        }
        closures.sort(Comparator.comparing(CompanyClosure::date));
        return closures;
    }

    @Override
    @Transactional
    public void changeCompanyClosure(MonthDay monthDay, DayLength dayLength, LocalDate validFrom) {
        requireNotBeforeCurrentYear(validFrom);

        final CompanyClosureEntity entity = findEntity(monthDay)
            .orElseThrow(() -> new IllegalArgumentException("No company closure for " + monthDay));

        final var versions = companyClosureVersionRepository.findAll();
        final Optional<CompanyClosureVersionEntity> sameValidFrom = versions.stream()
            .filter(version -> version.getCompanyClosureId().equals(entity.getId()) && validFrom.equals(version.getValidFrom()))
            .findFirst();

        final CompanyClosureVersionEntity version;
        if (sameValidFrom.isPresent()) {
            version = sameValidFrom.get();
        } else {
            if (versionValidAt(versions, entity.getId(), validFrom).map(CompanyClosureVersionEntity::getDayLength).filter(dayLength::equals).isPresent()) {
                return;
            }
            version = new CompanyClosureVersionEntity();
            version.setCompanyClosureId(entity.getId());
            version.setValidFrom(validFrom);
        }
        if (dayLength.equals(version.getDayLength())) {
            return;
        }
        version.setDayLength(dayLength);
        companyClosureVersionRepository.save(version);
        applicationEventPublisher.publishEvent(new CompanyClosureChangedEvent(monthDay));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyClosureEntry> getCompanyClosureEntries() {
        final Map<Long, List<CompanyClosureVersionEntity>> versionsByClosureId = companyClosureVersionRepository.findAll().stream()
            .collect(groupingBy(CompanyClosureVersionEntity::getCompanyClosureId));
        return companyClosureRepository.findAll().stream()
            .map(entity -> new CompanyClosureEntry(monthDayOf(entity), entity.getName(),
                versionsByClosureId.getOrDefault(entity.getId(), List.of()).stream()
                    .sorted(BY_VALID_FROM)
                    .map(version -> new CompanyClosureVersion(version.getDayLength(), version.getValidFrom()))
                    .toList()))
            .sorted(Comparator.comparing(CompanyClosureEntry::monthDay))
            .toList();
    }

    @Override
    @Transactional
    public void createCompanyClosure(MonthDay monthDay, String name, DayLength dayLength, LocalDate validFrom) {
        requireNotBeforeCurrentYear(validFrom);
        if (LEAP_DAY.equals(monthDay)) {
            throw new IllegalArgumentException("A company closure on " + monthDay + " does not exist every year.");
        }
        if (findEntity(monthDay).isPresent()) {
            throw new IllegalArgumentException("Company closure for " + monthDay + " exists already.");
        }

        final var entity = new CompanyClosureEntity();
        entity.setMonth(monthDay.getMonthValue());
        entity.setDayOfMonth(monthDay.getDayOfMonth());
        entity.setName(name);
        final var saved = companyClosureRepository.save(entity);

        final var version = new CompanyClosureVersionEntity();
        version.setCompanyClosureId(saved.getId());
        version.setDayLength(dayLength);
        version.setValidFrom(validFrom);
        companyClosureVersionRepository.save(version);

        applicationEventPublisher.publishEvent(new CompanyClosureChangedEvent(monthDay));
    }

    @Override
    @Transactional
    public void renameCompanyClosure(MonthDay monthDay, String name) {
        final CompanyClosureEntity entity = findEntity(monthDay)
            .orElseThrow(() -> new IllegalArgumentException("No company closure for " + monthDay));
        entity.setName(name);
        companyClosureRepository.save(entity);
    }

    @EventListener(InitialDefaultSettingsSavedEvent.class)
    @Transactional
    void createDefaults() {
        if (!companyClosureRepository.findAll().isEmpty()) {
            return;
        }
        createDefault(MonthDay.of(DECEMBER, 24), "Heiligabend");
        createDefault(MonthDay.of(DECEMBER, 31), "Silvester");
    }

    private void createDefault(MonthDay monthDay, String name) {
        final var entity = new CompanyClosureEntity();
        entity.setMonth(monthDay.getMonthValue());
        entity.setDayOfMonth(monthDay.getDayOfMonth());
        entity.setName(name);
        final var saved = companyClosureRepository.save(entity);

        final var version = new CompanyClosureVersionEntity();
        version.setCompanyClosureId(saved.getId());
        version.setDayLength(NOON);
        companyClosureVersionRepository.save(version);

        applicationEventPublisher.publishEvent(new CompanyClosureChangedEvent(monthDay));
    }

    private void requireNotBeforeCurrentYear(LocalDate validFrom) {
        if (validFrom.isBefore(Year.now(clock).atDay(1))) {
            throw new IllegalArgumentException("validFrom " + validFrom + " must not be before the current year.");
        }
    }

    private Optional<CompanyClosureEntity> findEntity(MonthDay monthDay) {
        return companyClosureRepository.findAll().stream()
            .filter(closure -> monthDayOf(closure).equals(monthDay))
            .findFirst();
    }

    private static MonthDay monthDayOf(CompanyClosureEntity entity) {
        return MonthDay.of(entity.getMonth(), entity.getDayOfMonth());
    }

    private static Optional<CompanyClosureVersionEntity> versionValidAt(List<CompanyClosureVersionEntity> versions, Long closureId, LocalDate date) {
        return versions.stream()
            .filter(version -> version.getCompanyClosureId().equals(closureId))
            .filter(version -> version.getValidFrom() == null || !version.getValidFrom().isAfter(date))
            .max(BY_VALID_FROM);
    }
}
