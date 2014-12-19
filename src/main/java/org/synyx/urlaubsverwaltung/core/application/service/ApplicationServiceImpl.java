
package org.synyx.urlaubsverwaltung.core.application.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.springframework.util.Assert;

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

    /**
     * @see  ApplicationService#getIdOfLatestApplication(org.synyx.urlaubsverwaltung.core.person.Person, org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus)
     */
    @Override
    public int getIdOfLatestApplication(Person person, ApplicationStatus status) {

        return applicationDAO.getIdOfLatestApplication(person, status);
    }


    /**
     * @see  ApplicationService#getApplicationById(Integer)
     */
    @Override
    public Application getApplicationById(Integer id) {

        return applicationDAO.findOne(id);
    }


    /**
     * @see  ApplicationService#save(org.synyx.urlaubsverwaltung.core.application.domain.Application)
     */
    @Override
    public void save(Application application) {

        applicationDAO.save(application);
    }


    /**
     * @see  ApplicationService#getApplicationsForACertainPeriod(org.joda.time.DateMidnight, org.joda.time.DateMidnight)
     */
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

        List<Application> applications = applicationDAO.getApplicationsForACertainTimeAndPersonAndState(
                startDate.toDate(), endDate.toDate(), person, status);

        for (Application application : applications) {
            Assert.isTrue(status.equals(application.getStatus()), "WHAT THE FUCK!!!!");
        }

        return applications;
    }
}
