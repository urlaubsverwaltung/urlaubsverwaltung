package org.synyx.urlaubsverwaltung.companyvacation;

import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.settings.WorkingDurationForChristmasEveUpdatedEvent;
import org.synyx.urlaubsverwaltung.settings.WorkingDurationForNewYearsEveUpdatedEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class CompanyVacationService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    // In first place we do not have another source then public holiday settings
    // later with fully implemented company vacation management this might be used by a source id like source id of
    // application or sick note to determine origin
    private static final String SOURCE_ID_FOR_SETTINGS = "settings";

    private final ApplicationEventPublisher applicationEventPublisher;

    public CompanyVacationService(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener
    public void handleWorkingDurationForChristmasEveUpdatedEvent(WorkingDurationForChristmasEveUpdatedEvent event) {
        LOG.info("Received WorkingDurationForChristmasEveUpdatedEvent {}", event);

        DayLength companyVacationLength = event.workingDurationForChristmasEve().getInverse();
        final LocalDate christmasEve = LocalDate.of(LocalDate.now().getYear(), 12, 24);

        final CompanyVacationPublishedEvent companyVacationPublishedEvent = new CompanyVacationPublishedEvent(SOURCE_ID_FOR_SETTINGS, UUID.randomUUID(), Instant.now(), companyVacationLength, christmasEve, christmasEve);
        applicationEventPublisher.publishEvent(companyVacationPublishedEvent);

        LOG.info("Published CompanyVacationPublishedEvent {}", companyVacationPublishedEvent);
    }

    @EventListener
    public void handleWorkingDurationForNewYearsEveUpdatedEvent(WorkingDurationForNewYearsEveUpdatedEvent event) {
        LOG.info("Received WorkingDurationForNewYearsEveUpdatedEvent {}", event);

        DayLength companyVacationLength = event.workingDurationForNewYearsEve().getInverse();
        final LocalDate newYearsEve = LocalDate.of(LocalDate.now().getYear(), 12, 31);

        final CompanyVacationPublishedEvent companyVacationPublishedEvent = new CompanyVacationPublishedEvent(SOURCE_ID_FOR_SETTINGS, UUID.randomUUID(), Instant.now(), companyVacationLength, newYearsEve, newYearsEve);
        applicationEventPublisher.publishEvent(companyVacationPublishedEvent);

        LOG.info("Published CompanyVacationPublishedEvent {}", companyVacationPublishedEvent);
    }
}
