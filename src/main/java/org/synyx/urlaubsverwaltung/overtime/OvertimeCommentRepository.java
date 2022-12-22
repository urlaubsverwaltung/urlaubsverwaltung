package org.synyx.urlaubsverwaltung.overtime;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;

/**
 * Allows access to overtime comments.
 *
 * @since 2.11.0
 */
interface OvertimeCommentRepository extends CrudRepository<OvertimeComment, Long> {

    List<OvertimeComment> findByOvertime(Overtime overtime);

    List<OvertimeComment> findByPerson(Person person);

    @Modifying
    void deleteByOvertimePerson(Person person);
}
