package org.synyx.urlaubsverwaltung.dao;

import java.util.Date;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.synyx.urlaubsverwaltung.domain.Application;
import java.util.List;
import org.synyx.urlaubsverwaltung.domain.DayLength;
import org.synyx.urlaubsverwaltung.domain.Person;

/**
 *
 * @author Aljona Murygina - murygina@synyx.de
 */
public interface ApplicationDAO extends JpaRepository<Application, Integer> {

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
