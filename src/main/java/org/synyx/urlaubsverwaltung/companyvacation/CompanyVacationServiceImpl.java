package org.synyx.urlaubsverwaltung.companyvacation;

import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosure;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureChangedEvent;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureService;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.util.UUID;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.Month.DECEMBER;
import static org.slf4j.LoggerFactory.getLogger;

@Service
class CompanyVacationServiceImpl implements CompanyVacationService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    // legacy source ids of the first two company closure days, the extension already knows them
    private static final String SOURCE_ID_FOR_SETTINGS_CHRISTMAS_EVE = "settings-christmas-eve";
    private static final String SOURCE_ID_FOR_SETTINGS_NEW_YEARS_EVE = "settings-new-years-eve";

    private static final MonthDay CHRISTMAS_EVE = MonthDay.of(DECEMBER, 24);
    private static final MonthDay NEW_YEARS_EVE = MonthDay.of(DECEMBER, 31);

    private final CompanyClosureService companyClosureService;
    private final Clock clock;
    private final ApplicationEventPublisher applicationEventPublisher;

    CompanyVacationServiceImpl(
        CompanyClosureService companyClosureService,
        Clock clock,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        this.companyClosureService = companyClosureService;
        this.clock = clock;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener
    void handleCompanyClosureChangedEvent(CompanyClosureChangedEvent event) {
        LOG.info("Received CompanyClosureChangedEvent {}", event);
        convertAndPublishCompanyEvent(event.monthDay());
    }

    @Override
    public void publishCompanyEvents() {
        LOG.info("Publishing all company vacation events based on company closures");
        companyClosureService.getCompanyClosureEntries().forEach(entry -> convertAndPublishCompanyEvent(entry.monthDay()));
        LOG.info("Published all company vacation events based on company closures");
    }

    private static String sourceId(MonthDay monthDay) {
        if (CHRISTMAS_EVE.equals(monthDay)) {
            return SOURCE_ID_FOR_SETTINGS_CHRISTMAS_EVE;
        } else if (NEW_YEARS_EVE.equals(monthDay)) {
            return SOURCE_ID_FOR_SETTINGS_NEW_YEARS_EVE;
        }
        return String.format("company-closure-%02d-%02d", monthDay.getMonthValue(), monthDay.getDayOfMonth());
    }

    private void convertAndPublishCompanyEvent(MonthDay monthDay) {
        final String sourceId = sourceId(monthDay);
        final LocalDate date = monthDay.atYear(Year.now(clock).getValue());
        final DayLength companyVacationLength = companyClosureService.getCompanyClosures(date, date).stream()
            .findFirst()
            .map(CompanyClosure::dayLength)
            .orElse(DayLength.ZERO);

        if (!companyVacationLength.isZero()) {
            final CompanyVacationPublishedEvent companyVacationPublishedEvent = new CompanyVacationPublishedEvent(sourceId, UUID.randomUUID(), Instant.now(), companyVacationLength, date, date);
            applicationEventPublisher.publishEvent(companyVacationPublishedEvent);
            LOG.info("Published CompanyVacationPublishedEvent {}", companyVacationPublishedEvent);
        } else {
            final CompanyVacationDeletedEvent companyVacationDeletedEvent = new CompanyVacationDeletedEvent(sourceId, UUID.randomUUID(), Instant.now());
            applicationEventPublisher.publishEvent(companyVacationDeletedEvent);
            LOG.info("Published CompanyVacationDeletedEvent {}", companyVacationDeletedEvent);
        }
    }
}
