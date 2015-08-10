package org.synyx.urlaubsverwaltung.core.sync;

import org.apache.log4j.Logger;

import org.springframework.context.annotation.Conditional;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;
import org.synyx.urlaubsverwaltung.core.sync.condition.NoCalendarCondition;

import java.util.Optional;


/**
 * Dummy service if calendar sync is not activated.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service("calendarProviderService")
@Conditional(NoCalendarCondition.class)
public class DummyCalendarProviderService implements CalendarProviderService {

    private static final Logger LOG = Logger.getLogger(DummyCalendarProviderService.class);

    @Override
    public Optional<String> addAbsence(Absence absence) {

        LOG.info(String.format("No calendar provider configured for adding of event: %s", absence));

        return Optional.empty();
    }


    @Override
    public void update(Absence absence, String eventId) {

        LOG.info(String.format("No calendar provider configured for updating of event: %s, eventId %s", absence,
                eventId));
    }


    @Override
    public void deleteAbsence(String eventId) {

        LOG.info(String.format("No calendar provider configured for deletion of event '%s'", eventId));
    }
}
