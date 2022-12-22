package org.synyx.urlaubsverwaltung.person.basedata;

import org.synyx.urlaubsverwaltung.person.PersonId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PersonBasedataService {

    /**
     * Returns an optional person base data object if there is
     * one saved in the database with the given `personId`
     *
     * @param personId to find base data of the person
     * @return base data of the given person
     */
    Optional<PersonBasedata> getBasedataByPersonId(long personId);

    /**
     * Returns an optional person base data object if there is
     * one saved in the database with the given `personId`
     *
     * @param personIds to find base data of the given persons
     * @return base data map of the given persons grouped by the id of the person
     */
    Map<PersonId, PersonBasedata> getBasedataByPersonId(List<Long> personIds);

    /**
     * Update a person base data object
     *
     * @param personBasedata to update
     */
    void update(PersonBasedata personBasedata);
}
