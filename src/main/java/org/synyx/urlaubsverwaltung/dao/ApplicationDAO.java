package org.synyx.urlaubsverwaltung.dao;

import java.util.Date;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.synyx.urlaubsverwaltung.domain.Application;
import java.util.List;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.DayLength;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.VacationType;

/**
 *
 * @author Aljona Murygina - murygina@synyx.de
 */
public interface ApplicationDAO extends JpaRepository<Application, Integer> {

    @Query("select max(id) from Application x where x.person = ?1 and x.status = ?2")
    int getIdOfLatestApplication(Person person, ApplicationStatus status);

    @Query("select x from Application x where x.person = ?1 "
    + "and x.supplementaryApplication = false "
    + "and ((x.startDate between ?2 and ?3) and (x.endDate between ?2 and ?3)) "
    + "and x.vacationType = ?4 "
    + "and x.status != ?5 "
    + "order by x.startDate")
    List<Application> getApplicationsBetweenTwoMilestones(Person person,
            Date firstMilestone, Date lastMilestone, VacationType type, ApplicationStatus state);

    @Query("select x from Application x where x.person = ?1 "
    + "and x.supplementaryApplication = false "
    + "and ((x.startDate < ?2) and (x.endDate between ?2 and ?3)) "
    + "and x.vacationType = ?4 "
    + "and x.status != ?5 "
    + "order by x.startDate")
    List<Application> getApplicationsBeforeFirstMilestone(Person person,
            Date firstMilestone, Date lastMilestone, VacationType type, ApplicationStatus state);

    @Query("select x from Application x where x.person = ?1 "
    + "and x.supplementaryApplication = false "
    + "and ((x.startDate between ?2 and ?3) and (x.endDate > ?3)) "
    + "and x.vacationType = ?4 "
    + "and x.status != ?5 "
    + "order by x.startDate")
    List<Application> getApplicationsAfterLastMilestone(Person person,
            Date firstMilestone, Date lastMilestone, VacationType type, ApplicationStatus state);

    // get List<Application> the supplemental applications for the given application resp. its id
    @Query("select x from Application x where x.idOfApplication = ?1 order by x.startDate")
    List<Application> getSupplementalApplicationsForApplication(Integer applicationId);

    // get List<Application> for a certain time (between startDate and endDate)for the given person and the given day
    // length, get only the not cancelled applications!
    @Query("select x from Application x where ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2)) "
    + "and x.person = ?3 and (x.status = 0 or x.status = 1) and x.howLong = ?4 and x.supplementaryApplication = false order by x.startDate")
    List<Application> getRelevantActiveApplicationsByPeriodAndDayLength(Date startDate, Date endDate, Person person,
            DayLength length);

    // get List<Application> for a certain time (between startDate and endDate)for the given person and the given day
    // length, get only the not cancelled applications!
    @Query("select x from Application x where ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2)) "
    + "and x.person = ?3 and (x.status = 0 or x.status = 1) and x.supplementaryApplication = false order by x.startDate")
    List<Application> getRelevantActiveApplicationsByPeriodForEveryDayLength(Date startDate, Date endDate, Person person);
}
