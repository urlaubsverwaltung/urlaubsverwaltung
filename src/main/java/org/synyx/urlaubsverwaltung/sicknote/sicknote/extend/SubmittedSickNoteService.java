package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SubmittedSickNote;

import java.util.List;

public interface SubmittedSickNoteService {

    /**
     * Find all {@linkplain SubmittedSickNote}s for the given persons.
     *
     * @param persons persons to consider
     * @return unsorted list of {@linkplain SubmittedSickNote}s
     */
    List<SubmittedSickNote> findSubmittedSickNotes(List<Person> persons);
}
