package org.synyx.urlaubsverwaltung.application.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.person.Person;

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
        "select x from Application x where x.person = ?1 and x.supplementaryApplication = false and ((x.startDate between ?2 and ?3) or (x.endDate between ?2 and ?3)) order by x.startDate"
    )
    List<Application> getAllApplicationsByPersonAndYear(Person person, Date firstDayOfYear, Date lastDayOfYear);


    @Query(
        "select x from Application x "
        + "where (x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) "
        + "or (x.startDate < ?1 and x.endDate > ?2) "
        + "and x.supplementaryApplication = false "
        + "and x.status = ?3 order by x.startDate"
    )
    List<Application> getApplicationsForACertainTimeAndState(Date startDate, Date endDate,
        ApplicationStatus allowedState);


    @Query(
        "select x from Application x where (x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2) and x.supplementaryApplication = false order by x.startDate"
    )
    List<Application> getApplicationsForACertainTime(Date startDate, Date endDate);


    @Query(
        "select x from Application x where x.status = ?1 and x.supplementaryApplication = false and ((x.startDate between ?2 and ?3) or (x.endDate between ?2 and ?3)) order by x.startDate"
    )
    List<Application> getApplicationsByStateAndYear(ApplicationStatus state, Date firstDayOfYear, Date lastDayOfYear);


    @Query(
        "select x from Application x where x.status = ?1 and x.formerlyAllowed = true and x.supplementaryApplication = false and ((x.startDate between ?2 and ?3) or (x.endDate between ?2 and ?3)) order by x.startDate"
    )
    List<Application> getCancelledApplicationsByYearThatHaveBeenAllowedFormerly(ApplicationStatus state,
        Date firstDayOfYear, Date lastDayOfYear);


    @Query(
        "select x from Application x where x.person = ?1 "
        + "and x.supplementaryApplication = false "
        + "and ((x.startDate between ?2 and ?3) and (x.endDate between ?2 and ?3)) "
        + "and x.vacationType = ?4 "
        + "and (x.status = ?5 or x.status = ?6) "
        + "order by x.startDate"
    )
    List<Application> getApplicationsBetweenTwoMilestones(Person person, Date firstMilestone, Date lastMilestone,
        VacationType type, ApplicationStatus waitingState, ApplicationStatus allowedState);


    @Query(
        "select x from Application x where x.person = ?1 "
        + "and x.supplementaryApplication = false "
        + "and ((x.startDate < ?2) and (x.endDate between ?2 and ?3)) "
        + "and x.vacationType = ?4 "
        + "and (x.status = ?5 or x.status = ?6) "
        + "order by x.startDate"
    )
    List<Application> getApplicationsBeforeFirstMilestone(Person person, Date firstMilestone, Date lastMilestone,
        VacationType type, ApplicationStatus cancelledState, ApplicationStatus allowedState);


    @Query(
        "select x from Application x where x.person = ?1 "
        + "and x.supplementaryApplication = false "
        + "and ((x.startDate between ?2 and ?3) and (x.endDate > ?3)) "
        + "and x.vacationType = ?4 "
        + "and (x.status = ?5 or x.status = ?6) "
        + "order by x.startDate"
    )
    List<Application> getApplicationsAfterLastMilestone(Person person, Date firstMilestone, Date lastMilestone,
        VacationType type, ApplicationStatus cancelledState, ApplicationStatus allowedState);


    @Query("select x from Application x where x.idOfApplication = ?1 order by x.startDate")
    List<Application> getSupplementalApplicationsForApplication(Integer applicationId);


    @Query(
        "select x from Application x where ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2)) "
        + "and x.person = ?3 and (x.status = 0 or x.status = 1) and x.howLong = ?4 and x.supplementaryApplication = false order by x.startDate"
    )
    List<Application> getRelevantActiveApplicationsByPeriodAndDayLength(Date startDate, Date endDate, Person person,
        DayLength length);


    @Query(
        "select x from Application x where ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2)) "
        + "and x.person = ?3 and (x.status = 0 or x.status = 1) and x.supplementaryApplication = false order by x.startDate"
    )
    List<Application> getRelevantActiveApplicationsByPeriodForEveryDayLength(Date startDate, Date endDate,
        Person person);


    // ONLY FOR JMX
    @Query(
        "select count(x) from Application x where x.status = ?1 and x.supplementaryApplication = false and ((x.startDate between ?2 and ?3) or (x.endDate between ?2 and ?3)) order by x.startDate"
    )
    long countApplicationsInStateAndYear(ApplicationStatus state, Date firstDayOfYear, Date lastDayOfYear);
}
