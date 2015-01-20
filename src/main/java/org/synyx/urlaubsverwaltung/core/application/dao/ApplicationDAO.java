package org.synyx.urlaubsverwaltung.core.application.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.Date;
import java.util.List;


/**
 * Repository for {@link Application} entities.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public interface ApplicationDAO extends JpaRepository<Application, Integer> {

    @Query("select max(id) from Application x where x.person = ?1 and x.status = ?2")
    int getIdOfLatestApplication(Person person, ApplicationStatus status);


    @Query(
        "select x from Application x where (x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2) "
        + "order by x.startDate"
    )
    List<Application> getApplicationsForACertainTime(Date startDate, Date endDate);


    @Query(
        "select x from Application x "
        + "where x.status = ?3 and ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) "
        + "or (x.startDate < ?1 and x.endDate > ?2)) "
        + "order by x.startDate"
    )
    List<Application> getApplicationsForACertainTimeAndState(Date startDate, Date endDate, ApplicationStatus status);


    @Query(
        "select x from Application x "
        + "where x.person = ?3 and ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) "
        + "or (x.startDate < ?1 and x.endDate > ?2)) "
        + "order by x.startDate"
    )
    List<Application> getApplicationsForACertainTimeAndPerson(Date startDate, Date endDate, Person person);


    @Query(
        "select x from Application x "
        + "where x.person = ?3 and x.status = ?4 and ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) "
        + "or (x.startDate < ?1 and x.endDate > ?2)) "
        + "order by x.startDate"
    )
    List<Application> getApplicationsForACertainTimeAndPersonAndState(Date startDate, Date endDate, Person person,
        ApplicationStatus status);


    @Query(
        "select x from Application x where x.person = ?1 "
        + "and ((x.startDate between ?2 and ?3) and (x.endDate between ?2 and ?3)) "
        + "and x.vacationType = ?4 "
        + "and (x.status = ?5 or x.status = ?6) "
        + "order by x.startDate"
    )
    List<Application> getApplicationsBetweenTwoMilestones(Person person, Date firstMilestone, Date lastMilestone,
        VacationType type, ApplicationStatus waitingState, ApplicationStatus allowedState);


    @Query(
        "select x from Application x where x.person = ?1 "
        + "and ((x.startDate < ?2) and (x.endDate between ?2 and ?3)) "
        + "and x.vacationType = ?4 "
        + "and (x.status = ?5 or x.status = ?6) "
        + "order by x.startDate"
    )
    List<Application> getApplicationsBeforeFirstMilestone(Person person, Date firstMilestone, Date lastMilestone,
        VacationType type, ApplicationStatus cancelledState, ApplicationStatus allowedState);


    @Query(
        "select x from Application x where x.person = ?1 "
        + "and ((x.startDate between ?2 and ?3) and (x.endDate > ?3)) "
        + "and x.vacationType = ?4 "
        + "and (x.status = ?5 or x.status = ?6) "
        + "order by x.startDate"
    )
    List<Application> getApplicationsAfterLastMilestone(Person person, Date firstMilestone, Date lastMilestone,
        VacationType type, ApplicationStatus cancelledState, ApplicationStatus allowedState);


    @Query(
        "select x from Application x where ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2)) "
        + "and x.person = ?3 and (x.status = 'WAITING' or x.status = 'ALLOWED') and x.howLong = ?4 order by x.startDate"
    )
    List<Application> getRelevantActiveApplicationsByPeriodAndDayLength(Date startDate, Date endDate, Person person,
        DayLength length);


    @Query(
        "select x from Application x where ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2)) "
        + "and x.person = ?3 and (x.status = 'WAITING' or x.status = 'ALLOWED') order by x.startDate"
    )
    List<Application> getRelevantActiveApplicationsByPeriodForEveryDayLength(Date startDate, Date endDate,
        Person person);

}
