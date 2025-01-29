package org.synyx.urlaubsverwaltung.extension.sicknote;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCreatedEvent;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;

import java.time.LocalDate;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Component
@ConditionalOnProperty(value = "uv.extensions.sicknote.republish.enabled", havingValue = "true")
@ConditionalOnBean(SickNoteEventHandlerExtension.class)
public class SickNoteEventRepublisher {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final SickNoteService sickNoteService;
    private final SickNoteEventHandlerExtension sickNoteEventHandlerExtension;

    SickNoteEventRepublisher(SickNoteService sickNoteService, SickNoteEventHandlerExtension sickNoteEventHandlerExtension) {
        this.sickNoteService = sickNoteService;
        this.sickNoteEventHandlerExtension = sickNoteEventHandlerExtension;
    }

    public void republishEvents(LocalDate start, LocalDate end) {
        LOG.info("Republishing all events with type sickNoteCreatedEvent");
        sickNoteService.getAllActiveByPeriod(start, end)
            .stream()
            .map(SickNoteCreatedEvent::of)
            .forEach(event -> {
                LOG.info("Publishing sickNoteCreatedEvent with id={} for personId={} with startDate={} and endDate={}", event.sickNote().getId(), event.sickNote().getPerson().getId(), event.sickNote().getStartDate(), event.sickNote().getEndDate());
                sickNoteEventHandlerExtension.on(event);
            });
        LOG.info("Republished all events with type=SickNoteCreatedEvent");
    }
}
