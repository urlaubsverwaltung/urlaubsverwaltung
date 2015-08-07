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
@Service("calendarSyncService")
@Conditional(NoCalendarCondition.class)
public class DummyCalendarSyncService implements CalendarSyncService {

    private static final Logger LOG = Logger.getLogger(DummyCalendarSyncService.class);

    @Override
    public Optional<String> addAbsence(Absence absence) {

        LOG.info(String.format("No calendar provider configured for syncing: %s", absence));

        return Optional.empty();
    }


    @Override
    public void deleteAbsence(String eventId) {

        LOG.info(String.format("No calendar provider configured for deletion of event '%s'", eventId));
    }
}
