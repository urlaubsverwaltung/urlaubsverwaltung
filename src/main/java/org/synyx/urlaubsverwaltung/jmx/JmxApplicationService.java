/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.jmx;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.springframework.beans.factory.annotation.Autowired;

import org.synyx.urlaubsverwaltung.core.application.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;

import java.util.List;


/**
 * Relevant service methods for JmxDemo.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class JmxApplicationService {

    private ApplicationDAO applicationDAO;

    @Autowired
    public JmxApplicationService(ApplicationDAO applicationDAO) {

        this.applicationDAO = applicationDAO;
    }

    public List<Application> getWaitingApplications() {

        int year = DateMidnight.now().getYear();

        DateMidnight firstDayOfYear = new DateMidnight(year, DateTimeConstants.JANUARY, 1);
        DateMidnight lastDayOfYear = new DateMidnight(year, DateTimeConstants.DECEMBER, 31);

        return applicationDAO.getApplicationsByStateAndYear(ApplicationStatus.WAITING, firstDayOfYear.toDate(),
                lastDayOfYear.toDate());
    }


    public long countWaitingApplications() {

        int year = DateMidnight.now().getYear();

        DateMidnight firstDayOfYear = new DateMidnight(year, DateTimeConstants.JANUARY, 1);
        DateMidnight lastDayOfYear = new DateMidnight(year, DateTimeConstants.DECEMBER, 31);

        return applicationDAO.countApplicationsInStateAndYear(ApplicationStatus.WAITING, firstDayOfYear.toDate(),
                lastDayOfYear.toDate());
    }


    public long countApplicationsInStatus(ApplicationStatus status) {

        int year = DateMidnight.now().getYear();

        DateMidnight firstDayOfYear = new DateMidnight(year, DateTimeConstants.JANUARY, 1);
        DateMidnight lastDayOfYear = new DateMidnight(year, DateTimeConstants.DECEMBER, 31);

        return applicationDAO.countApplicationsInStateAndYear(status, firstDayOfYear.toDate(), lastDayOfYear.toDate());
    }
}
