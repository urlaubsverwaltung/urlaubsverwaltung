package org.synyx.urlaubsverwaltung.extension.application;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.application.application.ApplicationAllowedEvent;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;

import java.time.LocalDate;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;

@Component
@ConditionalOnProperty(value = "uv.extensions.application.republish.enabled", havingValue = "true")
@ConditionalOnBean(ApplicationEventHandlerExtension.class)
public class ApplicationEventRepublisher {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final ApplicationService applicationService;
    private final ApplicationEventHandlerExtension applicationEventHandlerExtension;

    ApplicationEventRepublisher(ApplicationService applicationService, ApplicationEventHandlerExtension applicationEventHandlerExtension) {
        this.applicationService = applicationService;
        this.applicationEventHandlerExtension = applicationEventHandlerExtension;
    }

    public void republishEvents(LocalDate start, LocalDate end) {
        LOG.info("Republishing all events with type=ApplicationAllowedEvent");
        applicationService.getApplicationsForACertainPeriodAndState(start, end, ALLOWED)
            .stream()
            .map(ApplicationAllowedEvent::of)
            .forEach(event -> {
                LOG.info("Publishing ApplicationAllowedEvent with id={} for personId={} with startDate={} and endDate={}", event.application().getId(), event.application().getPerson().getId(), event.application().getStartDate(), event.application().getEndDate());
                applicationEventHandlerExtension.on(event);
            });
        LOG.info("Republished all events with type=ApplicationAllowedEvent");
    }
}
