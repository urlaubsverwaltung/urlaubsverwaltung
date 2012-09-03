package org.synyx.urlaubsverwaltung.dao;

import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.DayLength;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.util.Date;
import java.util.List;
import org.synyx.urlaubsverwaltung.domain.VacationType;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
public interface ApplicationDAO extends JpaRepository<Application, Integer> {

    @Query("select max(id) from Application x where x.person = ?1 and x.status = ?2")
    int getIdOfLatestApplication(Person person, ApplicationStatus status);
    
    // get List<Application> by certain state (e.g. waiting) and for a certain year
    @Query(
        "select x from Application x where x.status = ?1 and x.supplementaryApplication = false and ((x.startDate between ?2 and ?3) or (x.endDate between ?2 and ?3)) order by x.startDate"
    )
    List<Application> getApplicationsByStateAndYear(ApplicationStatus state, Date firstDayOfYear, Date lastDayOfYear);
    
    @Query(
        "select count(x) from Application x where x.status = ?1 and x.supplementaryApplication = false and ((x.startDate between ?2 and ?3) or (x.endDate between ?2 and ?3)) order by x.startDate"
    )
    long countApplicationsInStateAndYear(ApplicationStatus state, Date firstDayOfYear, Date lastDayOfYear);

    
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
        "select x from Application x where x.status != ?1 and x.person = ?2 and x.supplementaryApplication = false and ((x.startDate between ?3 and ?4) or (x.endDate between ?3 and ?4)) and x.vacationType = ?5 order by x.startDate"
    )
    List<Application> getNotCancelledApplicationsByPersonAndYear(ApplicationStatus state, Person person,
        Date firstDayOfYear, Date lastDayOfYear, VacationType type);
    
        // get List<Application> by certain person for a certain year, get only all applications not dependent on status
    @Query(
        "select x from Application x where x.person = ?1 and x.supplementaryApplication = false and ((x.startDate between ?2 and ?3) or (x.endDate between ?2 and ?3)) order by x.startDate"
    )
    List<Application> getAllApplicationsByPersonAndYear(Person person,
        Date firstDayOfYear, Date lastDayOfYear);


    // get List<Application> by certain person for a certain year, get only the applications before 1st April
    @Query(
        "select x from Application x where x.person = ?1 and x.supplementaryApplication = false and ((x.startDate between ?2 and ?3) or (x.endDate between ?2 and ?3)) and x.vacationType = ?4 order by x.startDate"
    )
    List<Application> getApplicationsBeforeAprilByPersonAndYear(Person person, Date firstJanuary, Date lastDayOfMarch, VacationType type);


    // get List<Application> for a certain time (between startDate and endDate)for the given person and the given day
    // length, get only the not cancelled applications!
    @Query(
        "select x from Application x where ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2)) "
        + "and x.person = ?3 and (x.status = 0 or x.status = 1) and x.howLong = ?4 and x.supplementaryApplication = false order by x.startDate"
    )
    List<Application> getRelevantActiveApplicationsByPeriodAndDayLength(Date startDate, Date endDate, Person person,
        DayLength length);


    // get List<Application> for a certain time (between startDate and endDate)for the given person and the given day
    // length, get only the not cancelled applications!
    @Query(
        "select x from Application x where ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2)) "
        + "and x.person = ?3 and (x.status = 0 or x.status = 1) and x.supplementaryApplication = false order by x.startDate"
    )
    List<Application> getRelevantActiveApplicationsByPeriodForEveryDayLength(Date startDate, Date endDate, Person person);
    
    
    // TODO: check if querys are valid! (enums!)
    
    @Query("select count(daysBeforeApril) from Application x where x.person = ?1 and ((x.startDate between ?2 and ?3) or (x.endDate between ?2 and ?3)) and x.vacationType = 1 and x.status = 0 or x.status = 1")
    BigDecimal countDaysBeforeApril(Person person, Date firstDayOfYear, Date lastDayOfYear);
    
    @Query("select count(daysAfterApril) from Application x where x.person = ?1 and ((x.startDate between ?2 and ?3) or (x.endDate between ?2 and ?3)) and x.vacationType = 1 and x.status = 0 or x.status = 1")
    BigDecimal countDaysAfterApril(Person person, Date firstDayOfYear, Date lastDayOfYear);
}
