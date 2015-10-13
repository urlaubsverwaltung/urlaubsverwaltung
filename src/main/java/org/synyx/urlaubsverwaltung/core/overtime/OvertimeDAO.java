package org.synyx.urlaubsverwaltung.core.overtime;

import org.springframework.data.jpa.repository.JpaRepository;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.List;


/**
 * Allows access to overtime records.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 * @since  2.11.0
 */
public interface OvertimeDAO extends JpaRepository<Overtime, Integer> {

    List<Overtime> findByPerson(Person person);
}
