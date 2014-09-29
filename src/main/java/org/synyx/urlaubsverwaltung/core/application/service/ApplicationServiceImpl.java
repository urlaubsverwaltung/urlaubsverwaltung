
package org.synyx.urlaubsverwaltung.core.application.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

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

    private ApplicationDAO applicationDAO;

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
     * @see  ApplicationService#getAllowedApplicationsForACertainPeriod(org.joda.time.DateMidnight, org.joda.time.DateMidnight)
     */
    @Override
    public List<Application> getAllowedApplicationsForACertainPeriod(DateMidnight startDate, DateMidnight endDate) {

        return applicationDAO.getApplicationsForACertainTimeAndState(startDate.toDate(), endDate.toDate(),
                ApplicationStatus.ALLOWED);
    }


    /**
     * @see  ApplicationService#getApplicationsForACertainPeriod(org.joda.time.DateMidnight, org.joda.time.DateMidnight)
     */
    @Override
    public List<Application> getApplicationsForACertainPeriod(DateMidnight startDate, DateMidnight endDate) {

        return applicationDAO.getApplicationsForACertainTime(startDate.toDate(), endDate.toDate());
    }


    @Override
    public List<Application> getAllAllowedApplicationsOfAPersonForAMonth(Person person, int month, int year) {

        return applicationDAO.getAllAllowedApplicationsOfAPersonForMonth(person, month, year);
    }


    /**
     * @see  ApplicationService#getApplicationsByStateAndYear(org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus,
     *       int)
     */
    @Override
    public List<Application> getApplicationsByStateAndYear(ApplicationStatus state, int year) {

        DateMidnight firstDayOfYear = new DateMidnight(year, DateTimeConstants.JANUARY, 1);
        DateMidnight lastDayOfYear = new DateMidnight(year, DateTimeConstants.DECEMBER, 31);

        if (state == ApplicationStatus.CANCELLED) {
            return applicationDAO.getCancelledApplicationsByYearThatHaveBeenAllowedFormerly(state,
                    firstDayOfYear.toDate(), lastDayOfYear.toDate());
        } else {
            return applicationDAO.getApplicationsByStateAndYear(state, firstDayOfYear.toDate(), lastDayOfYear.toDate());
        }
    }


    /**
     * @see  ApplicationService#getCancelledApplicationsByYearFormerlyAllowed(int)
     */
    @Override
    public List<Application> getCancelledApplicationsByYearFormerlyAllowed(int year) {

        DateMidnight firstDayOfYear = new DateMidnight(year, DateTimeConstants.JANUARY, 1);
        DateMidnight lastDayOfYear = new DateMidnight(year, DateTimeConstants.DECEMBER, 31);

        return applicationDAO.getCancelledApplicationsByYearThatHaveBeenAllowedFormerly(ApplicationStatus.CANCELLED,
                firstDayOfYear.toDate(), lastDayOfYear.toDate());
    }


    /**
     * @see  ApplicationService#getAllApplicationsByPersonAndYear(org.synyx.urlaubsverwaltung.core.person.Person, int)
     */
    @Override
    public List<Application> getAllApplicationsByPersonAndYear(Person person, int year) {

        DateMidnight firstDayOfYear = new DateMidnight(year, DateTimeConstants.JANUARY, 1);
        DateMidnight lastDayOfYear = new DateMidnight(year, DateTimeConstants.DECEMBER, 31);

        return applicationDAO.getAllApplicationsByPersonAndYear(person, firstDayOfYear.toDate(),
                lastDayOfYear.toDate());
    }


    @Override
    public List<Application> getAllApplicationsByPersonAndYearAndState(Person person, int year,
        ApplicationStatus state) {

        return applicationDAO.getApplicationsByPersonAndYearAndState(person, year, state);
    }
}
