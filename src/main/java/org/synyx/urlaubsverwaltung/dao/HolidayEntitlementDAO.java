package org.synyx.urlaubsverwaltung.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.domain.HolidayEntitlement;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.util.List;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
public interface HolidayEntitlementDAO extends JpaRepository<HolidayEntitlement, Integer> {

    @Query("select x from HolidayEntitlement x where x.year = ?1 and x.person = ?2 and x.active = true")
    HolidayEntitlement getHolidayEntitlementByYearAndPerson(int year, Person person);


    @Query("select x from HolidayEntitlement x where x.person = ?1")
    List<HolidayEntitlement> getHolidayEntitlementByPerson(Person person);
}
