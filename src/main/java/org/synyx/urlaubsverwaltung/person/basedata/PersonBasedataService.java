package org.synyx.urlaubsverwaltung.person.basedata;

import java.util.Optional;

public interface PersonBasedataService {

    Optional<PersonBasedata> getBasedataByPersonId(int personId);

    void update(PersonBasedata personBasedata);
}
