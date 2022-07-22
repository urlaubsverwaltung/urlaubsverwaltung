package org.synyx.urlaubsverwaltung.person.basedata;

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
    Optional<PersonBasedata> getBasedataByPersonId(int personId);

    /**
     * Update a person base data object
     *
     * @param personBasedata to update
     */
    void update(PersonBasedata personBasedata);

    Map<Integer, PersonBasedata> getBasedataByPersonIds(List<Integer> personIds);
}
