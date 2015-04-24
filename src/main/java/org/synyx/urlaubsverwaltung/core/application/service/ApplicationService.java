package org.synyx.urlaubsverwaltung.core.application.service;

import com.google.common.base.Optional;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.List;


/**
 * This service provides access to the {@link Application} entities. Except for saving, the access is read-only.
 * Business interactions are found in
 * {@link org.synyx.urlaubsverwaltung.core.application.service.ApplicationInteractionService}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public interface ApplicationService {

    /**
     * Gets an {@link Application} by its primary key.
     *
     * @param  id  to get the {@link Application} by.
     *
     * @return  optional {@link Application} for the given id
     */
    Optional<Application> getApplicationById(Integer id);


    /**
     * Saves a new {@link Application}.
     *
     * @param  application  to be saved
     */
    void save(Application application);


    /**
     * Gets all {@link Application}s with the given state.
     *
     * @param  state  of the {@link Application}s for leave to be fetched
     *
     * @return  all {@link Application}s for leave with the given state
     */
    List<Application> getApplicationsForACertainState(ApplicationStatus state);


    /**
     * Gets all {@link Application}s with vacation time between startDate x and endDate y for the given person.
     *
     * @param  startDate {@link DateMidnight}
     * @param  endDate {@link DateMidnight}
     * @param  person {@link Person}
     *
     * @return  all {@link Application}s of the given person with vacation time between startDate x and endDate y
     */
    List<Application> getApplicationsForACertainPeriodAndPerson(DateMidnight startDate, DateMidnight endDate,
        Person person);


    /**
     * Gets all {@link Application}s with vacation time between startDate x and endDate y for the given state.
     *
     * @param  startDate {@link DateMidnight}
     * @param  endDate {@link DateMidnight}
     * @param  status {@link org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus}
     *
     * @return  all {@link Application}s with the given state and vacation time between startDate x and endDate y
     */
    List<Application> getApplicationsForACertainPeriodAndState(DateMidnight startDate, DateMidnight endDate,
        ApplicationStatus status);


    /**
     * Gets all {@link Application}s with vacation time between startDate x and endDate y for the given person and
     * state.
     *
     * @param  startDate {@link DateMidnight}
     * @param  endDate {@link DateMidnight}
     * @param  person {@link Person}
     * @param  status {@link org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus}
     *
     * @return  all {@link Application}s of the given person with vacation time between startDate x and endDate y and
     *          with a certain state
     */
    List<Application> getApplicationsForACertainPeriodAndPersonAndState(DateMidnight startDate, DateMidnight endDate,
        Person person, ApplicationStatus status);
}
