package org.synyx.urlaubsverwaltung.companyvacation;

import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.settings.WorkingDurationForChristmasEveUpdatedEvent;
import org.synyx.urlaubsverwaltung.settings.WorkingDurationForNewYearsEveUpdatedEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
class CompanyVacationService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    // In first place we do not have another source then public holiday settings
    // later with fully implemented company vacation management this might be used by a source id like source id of
    // application or sick note to determine origin
    private static final String SOURCE_ID_FOR_SETTINGS_CHRISTMAS_EVE = "settings-christmas-eve";
    private static final String SOURCE_ID_FOR_SETTINGS_NEW_YEARS_EVE = "settings-new-years-eve";

    private final ApplicationEventPublisher applicationEventPublisher;

    CompanyVacationService(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener
    void handleWorkingDurationForChristmasEveUpdatedEvent(WorkingDurationForChristmasEveUpdatedEvent event) {
        LOG.info("Received WorkingDurationForChristmasEveUpdatedEvent {}", event);

        final DayLength companyVacationLength = event.workingDurationForChristmasEve().getInverse();
        final LocalDate christmasEve = LocalDate.of(LocalDate.now().getYear(), 12, 24);

        publish(companyVacationLength, christmasEve, SOURCE_ID_FOR_SETTINGS_CHRISTMAS_EVE);
    }

    @EventListener
    void handleWorkingDurationForNewYearsEveUpdatedEvent(WorkingDurationForNewYearsEveUpdatedEvent event) {
        LOG.info("Received WorkingDurationForNewYearsEveUpdatedEvent {}", event);

        final DayLength companyVacationLength = event.workingDurationForNewYearsEve().getInverse();
        final LocalDate newYearsEve = LocalDate.of(LocalDate.now().getYear(), 12, 31);

        publish(companyVacationLength, newYearsEve, SOURCE_ID_FOR_SETTINGS_NEW_YEARS_EVE);
    }

    private void publish(DayLength companyVacationLength, LocalDate date, String sourceId) {
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
