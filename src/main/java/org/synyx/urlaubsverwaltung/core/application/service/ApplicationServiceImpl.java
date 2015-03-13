
package org.synyx.urlaubsverwaltung.core.application.service;

import com.google.common.base.Optional;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.application.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.List;


/**
 * Implementation of interface {@link ApplicationService}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationDAO applicationDAO;

    @Autowired
    public ApplicationServiceImpl(ApplicationDAO applicationDAO) {

        this.applicationDAO = applicationDAO;
    }

    @Override
    public Optional<Application> getApplicationById(Integer id) {

        return Optional.fromNullable(applicationDAO.findOne(id));
    }


    @Override
    public void save(Application application) {

        applicationDAO.save(application);
    }


    @Override
    public List<Application> getApplicationsForACertainPeriod(DateMidnight startDate, DateMidnight endDate) {

        return applicationDAO.getApplicationsForACertainTime(startDate.toDate(), endDate.toDate());
    }


    @Override
    public List<Application> getApplicationsForACertainPeriodAndPerson(DateMidnight startDate, DateMidnight endDate,
        Person person) {

        return applicationDAO.getApplicationsForACertainTimeAndPerson(startDate.toDate(), endDate.toDate(), person);
    }


    @Override
    public List<Application> getApplicationsForACertainPeriodAndState(DateMidnight startDate, DateMidnight endDate,
        ApplicationStatus status) {

        return applicationDAO.getApplicationsForACertainTimeAndState(startDate.toDate(), endDate.toDate(), status);
    }


    @Override
    public List<Application> getApplicationsForACertainPeriodAndPersonAndState(DateMidnight startDate,
        DateMidnight endDate, Person person, ApplicationStatus status) {

        return applicationDAO.getApplicationsForACertainTimeAndPersonAndState(startDate.toDate(), endDate.toDate(),
                person, status);
    }
}
