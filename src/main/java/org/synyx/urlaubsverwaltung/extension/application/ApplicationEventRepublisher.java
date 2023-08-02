package org.synyx.urlaubsverwaltung.extension.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.application.application.ApplicationAllowedEvent;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;

import java.time.Clock;
import java.time.LocalDate;

import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;

@Component
@ConditionalOnProperty(value = "uv.extensions.application.republish.enabled", havingValue = "true")
@ConditionalOnBean(ApplicationEventHandlerExtension.class)
public class ApplicationEventRepublisher {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationEventRepublisher.class);

    private final ApplicationService applicationService;
    private final ApplicationEventHandlerExtension applicationEventHandlerExtension;
    private final Clock clock;

    ApplicationEventRepublisher(ApplicationService applicationService, ApplicationEventHandlerExtension applicationEventHandlerExtension, Clock clock) {
        this.applicationService = applicationService;
        this.applicationEventHandlerExtension = applicationEventHandlerExtension;
        this.clock = clock;
    }

    @Async
    @EventListener(ApplicationStartedEvent.class)
    void republishEvents() {

        LOG.info("Republishing all events with type=ApplicationAllowedEvent");

        final LocalDate now = LocalDate.now(clock);

        final LocalDate startOfYear = now.withDayOfYear(1);
        final LocalDate endOfYear = startOfYear.with(lastDayOfYear());

        applicationService.getApplicationsForACertainPeriodAndState(startOfYear, endOfYear, ALLOWED)
            .stream()
            .map(ApplicationAllowedEvent::of)
            .forEach(event -> {
                LOG.info("Publishing ApplicationAllowedEvent with id={} for personId={} with startDate={} and endDate={}", event.getApplication().getId(), event.getApplication().getPerson().getId(), event.getApplication().getStartDate(), event.getApplication().getEndDate());
                applicationEventHandlerExtension.on(event);
            });

        LOG.info("Republished all events with type=ApplicationAllowedEvent");
    }
}
