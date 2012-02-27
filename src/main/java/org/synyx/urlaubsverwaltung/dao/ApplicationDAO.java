package org.synyx.urlaubsverwaltung.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.DayLength;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.util.Date;
import java.util.List;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
public interface ApplicationDAO extends JpaRepository<Application, Integer> {

    // get List<Application> by certain state (e.g. waiting) and for a certain year
    @Query(
        "select x from Application x where x.status = ?1 and x.supplementaryApplication = false and ((x.startDate between ?2 and ?3) or (x.endDate between ?2 and ?3)) order by x.startDate"
    )
    List<Application> getApplicationsByStateAndYear(ApplicationStatus state, Date firstDayOfYear, Date lastDayOfYear);


    // get list of cancelled applications that have been allowed before cancelling
    @Query(
        "select x from Application x where x.status = ?1 and x.formerlyAllowed = true and x.supplementaryApplication = false and ((x.startDate between ?2 and ?3) or (x.endDate between ?2 and ?3)) order by x.startDate"
    )
    List<Application> getCancelledApplicationsByYear(ApplicationStatus state, Date firstDayOfYear, Date lastDayOfYear);


    // get List<Application> for a certain time (between startDate and endDate)
    @Query(
        "select x from Application x where (x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2) and x.supplementaryApplication = false order by x.startDate"
    )
    List<Application> getApplicationsForACertainTime(Date startDate, Date endDate);


    // get List<Application> (only the supplemental applications) by certain person for a certain year
    @Query(
        "select x from Application x where x.person = ?1 and x.supplementaryApplication = true and ((x.startDate between ?2 and ?3) or (x.endDate between ?2 and ?3)) order by x.startDate"
    )
    List<Application> getSupplementalApplicationsByPersonAndYear(Person person, Date firstDayOfYear,
        Date lastDayOfYear);


    // get List<Application> the supplemental applications for the given application resp. its id
    @Query("select x from Application x where x.idOfApplication = ?1 order by x.startDate")
    List<Application> getSupplementalApplicationsForApplication(Integer applicationId);


    // get List<Application> by certain person for a certain year, get only the not cancelled applications
    @Query(
        "select x from Application x where x.status != ?1 and x.person = ?2 and x.supplementaryApplication = false and ((x.startDate between ?3 and ?4) or (x.endDate between ?3 and ?4)) order by x.startDate"
    )
    List<Application> getNotCancelledApplicationsByPersonAndYear(ApplicationStatus state, Person person,
        Date firstDayOfYear, Date lastDayOfYear);


    // get List<Application> by certain person for a certain year, get only the applications before 1st April
    @Query(
        "select x from Application x where x.person = ?1 and x.supplementaryApplication = false and ((x.startDate between ?3 and ?4) or (x.endDate between ?3 and ?4)) order by x.startDate"
    )
    List<Application> getApplicationsBeforeAprilByPersonAndYear(Person person, Date firstJanuary, Date lastDayOfMarch);


    // get List<Application> for a certain time (between startDate and endDate)for the given person and the given day
    // length, get only the not cancelled applications!
    @Query(
        "select x from Application x where ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2)) "
        + "and x.person = ?3 and x.status != 3 and x.howLong = ?4 and x.supplementaryApplication = false order by x.startDate"
    )
    List<Application> getApplicationsByPeriodAndDayLength(Date startDate, Date endDate, Person person,
        DayLength length);


    // get List<Application> for a certain time (between startDate and endDate)for the given person and the given day
    // length, get only the not cancelled applications!
    @Query(
        "select x from Application x where ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2)) "
        + "and x.person = ?3 and x.status != 3 and x.supplementaryApplication = false order by x.startDate"
    )
    List<Application> getApplicationsByPeriodForEveryDayLength(Date startDate, Date endDate, Person person);
}
