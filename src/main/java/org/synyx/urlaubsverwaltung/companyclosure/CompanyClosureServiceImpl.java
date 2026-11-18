package org.synyx.urlaubsverwaltung.companyclosure;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.period.DateLimits;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;
import org.synyx.urlaubsverwaltung.settings.InitialDefaultSettingsSavedEvent;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Locale.forLanguageTag;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;

@Service
class CompanyClosureServiceImpl implements CompanyClosureService {

    private static final String NAME_FALLBACK_MESSAGE_KEY = "absences.overview.company-closure";

    private static final Comparator<CompanyClosureVersionEntity> BY_VALID_FROM =
        comparing(CompanyClosureVersionEntity::getValidFrom, nullsFirst(naturalOrder()));

    private static final MonthDay LEAP_DAY = MonthDay.of(FEBRUARY, 29);

    private final CompanyClosureRepository companyClosureRepository;
    private final CompanyClosureVersionRepository companyClosureVersionRepository;
    private final Clock clock;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MessageSource messageSource;

    CompanyClosureServiceImpl(CompanyClosureRepository companyClosureRepository,
                              CompanyClosureVersionRepository companyClosureVersionRepository,
                              Clock clock, ApplicationEventPublisher applicationEventPublisher,
                              MessageSource messageSource) {
        this.companyClosureRepository = companyClosureRepository;
        this.companyClosureVersionRepository = companyClosureVersionRepository;
        this.clock = clock;
        this.applicationEventPublisher = applicationEventPublisher;
        this.messageSource = messageSource;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyClosure> getCompanyClosures(LocalDate from, LocalDate to) {
        DateLimits.requireCalculableDuration(new DateRange(from, to));

        // only closure days within the range need their versions, most ranges contain none
        final List<Map.Entry<CompanyClosureEntity, LocalDate>> candidates = new ArrayList<>();
        for (CompanyClosureEntity entity : companyClosureRepository.findAll()) {
            final MonthDay monthDay = monthDayOf(entity);
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

        final List<CompanyClosureVersionEntity> versions = companyClosureVersionRepository.findAll();
        final List<CompanyClosure> closures = new ArrayList<>();
        for (Map.Entry<CompanyClosureEntity, LocalDate> candidate : candidates) {
            final CompanyClosureEntity entity = candidate.getKey();
            final LocalDate date = candidate.getValue();
            final Function<Locale, String> nameByLocale = nameByLocale(entity);
            versionValidAt(versions, entity.getId(), date)
                .map(CompanyClosureVersionEntity::getDayLength)
                .filter(dayLength -> dayLength != ZERO)
                .ifPresent(dayLength -> closures.add(new CompanyClosure(date, dayLength, nameByLocale)));
        }
        closures.sort(comparing(CompanyClosure::date));
        return closures;
    }

    @Override
    @Transactional
    public void changeCompanyClosure(MonthDay monthDay, DayLength dayLength, LocalDate validFrom, Person modifier) {
        requireNotBeforeCurrentYear(validFrom);

        final CompanyClosureEntity entity = findEntity(monthDay)
            .orElseThrow(() -> new IllegalArgumentException("No company closure for " + monthDay));

        final List<CompanyClosureVersionEntity> versions = companyClosureVersionRepository.findAll();
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
        version.setLastModifiedAt(Instant.now(clock));
        version.setLastModifiedBy(modifier);
        companyClosureVersionRepository.save(version);
        applicationEventPublisher.publishEvent(new CompanyClosureChangedEvent(monthDay));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyClosureEntry> getCompanyClosureEntries() {
        final Map<Long, List<CompanyClosureVersionEntity>> versionsByClosureId = companyClosureVersionRepository.findAll().stream()
            .collect(groupingBy(CompanyClosureVersionEntity::getCompanyClosureId));
        return companyClosureRepository.findAll().stream()
            .map(entity -> new CompanyClosureEntry(monthDayOf(entity), nameByLocale(entity), namesOf(entity),
                versionsByClosureId.getOrDefault(entity.getId(), List.of()).stream()
                    .sorted(BY_VALID_FROM)
                    .map(version -> new CompanyClosureVersion(version.getDayLength(), version.getValidFrom()))
                    .toList()))
            .sorted(comparing(CompanyClosureEntry::monthDay))
            .toList();
    }

    @Override
    @Transactional
    public void createCompanyClosure(MonthDay monthDay, Map<Locale, String> names, DayLength dayLength, LocalDate validFrom) {
        requireNotBeforeCurrentYear(validFrom);
        final Map<Locale, String> nameByLocale = requireName(names);
        if (LEAP_DAY.equals(monthDay)) {
            throw new IllegalArgumentException("A company closure on " + monthDay + " does not exist every year.");
        }
        if (findEntity(monthDay).isPresent()) {
            throw new IllegalArgumentException("Company closure for " + monthDay + " exists already.");
        }

        final CompanyClosureEntity entity = new CompanyClosureEntity();
        entity.setMonth(monthDay.getMonthValue());
        entity.setDayOfMonth(monthDay.getDayOfMonth());
        entity.setNameByLocale(nameByLocale);
        final CompanyClosureEntity saved = companyClosureRepository.save(entity);

        final CompanyClosureVersionEntity version = new CompanyClosureVersionEntity();
        version.setCompanyClosureId(saved.getId());
        version.setDayLength(dayLength);
        version.setValidFrom(validFrom);
        companyClosureVersionRepository.save(version);

        applicationEventPublisher.publishEvent(new CompanyClosureChangedEvent(monthDay));
    }

    @Override
    @Transactional
    public void renameCompanyClosure(MonthDay monthDay, Map<Locale, String> names) {
        final Map<Locale, String> nameByLocale = requireName(names);
        final CompanyClosureEntity entity = findEntity(monthDay)
            .orElseThrow(() -> new IllegalArgumentException("No company closure for " + monthDay));
        entity.setNameByLocale(nameByLocale);
        companyClosureRepository.save(entity);
    }

    @EventListener(InitialDefaultSettingsSavedEvent.class)
    @Transactional
    void createDefaults() {
        if (companyClosureRepository.findAll().isEmpty()) {
            createDefault(MonthDay.of(DECEMBER, 24), "companyclosure.name.christmasEve");
            createDefault(MonthDay.of(DECEMBER, 31), "companyclosure.name.newYearsEve");
        }
    }

    /**
     * Keeps the closures of a deleted person and forgets only who changed them last.
     */
    @EventListener
    void forgetDeletedModifier(PersonDeletedEvent event) {
        final Person deletedPerson = event.person();

        final List<CompanyClosureEntity> closures = companyClosureRepository.findByLastModifiedBy(deletedPerson);
        closures.forEach(closure -> closure.setLastModifiedBy(null));
        companyClosureRepository.saveAll(closures);

        final List<CompanyClosureVersionEntity> versions = companyClosureVersionRepository.findByLastModifiedBy(deletedPerson);
        versions.forEach(version -> version.setLastModifiedBy(null));
        companyClosureVersionRepository.saveAll(versions);
    }

    private void createDefault(MonthDay monthDay, String messageKey) {
        final CompanyClosureEntity entity = new CompanyClosureEntity();
        entity.setMonth(monthDay.getMonthValue());
        entity.setDayOfMonth(monthDay.getDayOfMonth());
        entity.setMessageKey(messageKey);
        final CompanyClosureEntity saved = companyClosureRepository.save(entity);

        final CompanyClosureVersionEntity version = new CompanyClosureVersionEntity();
        version.setCompanyClosureId(saved.getId());
        version.setDayLength(NOON);
        companyClosureVersionRepository.save(version);

        applicationEventPublisher.publishEvent(new CompanyClosureChangedEvent(monthDay));
    }

    /**
     * The name given by a user for the locale or its language (e.g. "de" for "de_DE"), the translation of the
     * message key if the closure has no names given by a user, a generic name otherwise.
     */
    private Function<Locale, String> nameByLocale(CompanyClosureEntity entity) {
        final Map<Locale, String> names = namesOf(entity);
        if (!names.isEmpty()) {
            return locale -> ofNullable(names.get(locale))
                .or(() -> ofNullable(names.get(forLanguageTag(locale.getLanguage()))))
                .orElseGet(() -> messageSource.getMessage(NAME_FALLBACK_MESSAGE_KEY, new Object[]{}, locale));
        }

        final String messageKey = entity.getMessageKey();
        return locale -> {
            // an unknown message key, e.g. from a restored backup, must not break every page showing the closure
            final String message = messageKey == null ? null : messageSource.getMessage(messageKey, new Object[]{}, null, locale);
            return message != null ? message : messageSource.getMessage(NAME_FALLBACK_MESSAGE_KEY, new Object[]{}, locale);
        };
    }

    /**
     * @return the names given by a user without blank ones, e.g. from a restored backup
     */
    private static Map<Locale, String> namesOf(CompanyClosureEntity entity) {
        final Map<Locale, String> nameByLocale = entity.getNameByLocale();
        return nameByLocale == null ? Map.of() : withoutBlankNames(nameByLocale);
    }

    private static Map<Locale, String> requireName(Map<Locale, String> names) {
        final Map<Locale, String> nameByLocale = withoutBlankNames(names);
        if (nameByLocale.isEmpty()) {
            throw new IllegalArgumentException("At least one name must not be blank.");
        }
        return nameByLocale;
    }

    private static Map<Locale, String> withoutBlankNames(Map<Locale, String> names) {
        final Map<Locale, String> nonBlankNames = new HashMap<>();
        names.forEach((locale, name) -> {
            if (name != null && !name.isBlank()) {
                nonBlankNames.put(locale, name.strip());
            }
        });
        return Map.copyOf(nonBlankNames);
    }

    private void requireNotBeforeCurrentYear(LocalDate validFrom) {
        if (validFrom == null || validFrom.isBefore(Year.now(clock).atDay(1))) {
            throw new IllegalArgumentException("validFrom " + validFrom + " must be given and must not be before the current year.");
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
