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
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;

import java.time.LocalDate;

@Component
@ConditionalOnProperty(value = "uv.extensions.application.republish.enabled", havingValue = "true")
@ConditionalOnBean(ApplicationEventHandlerExtension.class)
public class ApplicationEventRepublisher {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationEventRepublisher.class);

    private final ApplicationService applicationService;
    private final ApplicationEventHandlerExtension applicationEventHandlerExtension;

    public ApplicationEventRepublisher(ApplicationService applicationService, ApplicationEventHandlerExtension applicationEventHandlerExtension) {
        this.applicationService = applicationService;
        this.applicationEventHandlerExtension = applicationEventHandlerExtension;
    }

    @Async
    @EventListener(ApplicationStartedEvent.class)
    void republishEvents() {

        LOG.info("Republishing all events with type=ApplicationAllowedEvent");

        LocalDate now = LocalDate.now();

        LocalDate startOfYear = now.withDayOfYear(1);
        LocalDate endOfYear = now.withDayOfYear(now.lengthOfYear());

        this.applicationService.getApplicationsForACertainPeriodAndState(startOfYear, endOfYear, ApplicationStatus.ALLOWED)
            .stream()
            .map(ApplicationAllowedEvent::of)
            .forEach(event -> {
                LOG.info("Publishing ApplicationAllowedEvent with id={} for personId={} with startDate={} and endDate={}", event.getApplication().getId(), event.getApplication().getPerson().getUsername(), event.getApplication().getStartDate(), event.getApplication().getEndDate());
                applicationEventHandlerExtension.on(event);
            });

        LOG.info("Republished all events with type=ApplicationAllowedEvent");
    }
}
