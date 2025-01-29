package org.synyx.urlaubsverwaltung.extension.sicknote;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.ConditionalOnSingleTenantMode;

import java.time.Clock;
import java.time.LocalDate;

import static java.time.temporal.TemporalAdjusters.lastDayOfYear;

@Component
@ConditionalOnProperty(value = "uv.extensions.sicknote.republish.enabled", havingValue = "true")
@ConditionalOnBean(SickNoteEventHandlerExtension.class)
@ConditionalOnSingleTenantMode
class SickNoteEventRepublisherSingleTenant {

    private final SickNoteEventRepublisher sickNoteEventRepublisher;
    private final Clock clock;

    SickNoteEventRepublisherSingleTenant(SickNoteEventRepublisher sickNoteEventRepublisher, Clock clock) {
        this.sickNoteEventRepublisher = sickNoteEventRepublisher;
        this.clock = clock;
    }

    @Async
    @EventListener(ApplicationStartedEvent.class)
    public void republishEvents() {
        final LocalDate now = LocalDate.now(clock);
        final LocalDate startOfYear = now.withDayOfYear(1);
        final LocalDate endOfYear = startOfYear.with(lastDayOfYear());

        sickNoteEventRepublisher.republishEvents(startOfYear, endOfYear);
    }
}
