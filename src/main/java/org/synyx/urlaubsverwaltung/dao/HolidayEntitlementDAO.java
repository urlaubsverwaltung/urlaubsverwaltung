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

    @Query("select from HolidayEntitlement where year = ?1 and person = ?2")
    HolidayEntitlement getHolidayEntitlementByYearAndPerson(Integer year, Person person);


    @Query("select from HolidayEntitlement where person = ?1")
    List<HolidayEntitlement> getHolidayEntitlementByPerson(Person person);
}
