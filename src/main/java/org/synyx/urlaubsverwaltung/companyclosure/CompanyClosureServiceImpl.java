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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.time.Month.DECEMBER;
import static java.util.Comparator.nullsFirst;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;

@Service
class CompanyClosureServiceImpl implements CompanyClosureService {

    private static final String NAME_FALLBACK_MESSAGE_KEY = "absences.overview.company-closure";

    private static final Comparator<CompanyClosureVersionEntity> BY_VALID_FROM =
        Comparator.comparing(CompanyClosureVersionEntity::getValidFrom, nullsFirst(Comparator.naturalOrder()));

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
            final MonthDay monthDay = MonthDay.of(entity.getMonth(), entity.getDayOfMonth());
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
            final Function<Locale, String> nameByLocale = nameByLocale(entity.getMessageKey());
            versionValidAt(versions, entity.getId(), date)
                .map(CompanyClosureVersionEntity::getDayLength)
                .filter(dayLength -> dayLength != ZERO)
                .ifPresent(dayLength -> closures.add(new CompanyClosure(date, dayLength, nameByLocale)));
        }
        closures.sort(Comparator.comparing(CompanyClosure::date));
        return closures;
    }

    @Override
    @Transactional
    public void changeCompanyClosure(MonthDay monthDay, DayLength dayLength, LocalDate validFrom, Person modifier) {
        if (validFrom.isBefore(Year.now(clock).atDay(1))) {
            throw new IllegalArgumentException("validFrom " + validFrom + " must not be before the current year.");
        }

        final CompanyClosureEntity entity = companyClosureRepository.findAll().stream()
            .filter(closure -> closure.getMonth() == monthDay.getMonthValue() && closure.getDayOfMonth() == monthDay.getDayOfMonth())
            .findFirst()
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

    private Function<Locale, String> nameByLocale(String messageKey) {
        return locale -> {
            // an unknown message key, e.g. from a restored backup, must not break every page showing the closure
            final String name = messageSource.getMessage(messageKey, new Object[]{}, null, locale);
            return name != null ? name : messageSource.getMessage(NAME_FALLBACK_MESSAGE_KEY, new Object[]{}, locale);
        };
    }

    private static Optional<CompanyClosureVersionEntity> versionValidAt(List<CompanyClosureVersionEntity> versions, Long closureId, LocalDate date) {
        return versions.stream()
            .filter(version -> version.getCompanyClosureId().equals(closureId))
            .filter(version -> version.getValidFrom() == null || !version.getValidFrom().isAfter(date))
            .max(BY_VALID_FROM);
    }
}
