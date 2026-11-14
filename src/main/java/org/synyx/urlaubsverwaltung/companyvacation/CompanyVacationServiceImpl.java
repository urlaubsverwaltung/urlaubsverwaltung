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

    // In first place we do not have another source then public holiday settings
    // later with fully implemented company vacation management this might be used by a source id like source id of
    // application or sick note to determine origin
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
        // shortcut: only 24.12./31.12. have source ids, other month days matter with the company closure menu
        final MonthDay monthDay = event.monthDay();
        if (CHRISTMAS_EVE.equals(monthDay)) {
            convertAndPublishCompanyEvent(CHRISTMAS_EVE, SOURCE_ID_FOR_SETTINGS_CHRISTMAS_EVE);
        } else if (NEW_YEARS_EVE.equals(monthDay)) {
            convertAndPublishCompanyEvent(NEW_YEARS_EVE, SOURCE_ID_FOR_SETTINGS_NEW_YEARS_EVE);
        }
    }

    @Override
    public void publishCompanyEvents() {
        LOG.info("Publishing all company vacation events based on company closures (Christmas and new years eve)");
        convertAndPublishCompanyEvent(CHRISTMAS_EVE, SOURCE_ID_FOR_SETTINGS_CHRISTMAS_EVE);
        convertAndPublishCompanyEvent(NEW_YEARS_EVE, SOURCE_ID_FOR_SETTINGS_NEW_YEARS_EVE);
        LOG.info("Published all company vacation events based on company closures (Christmas and new years eve)");
    }

    private void convertAndPublishCompanyEvent(MonthDay monthDay, String sourceId) {

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
