package org.synyx.urlaubsverwaltung.core.sicknote;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.List;


/**
 * Service for handling {@link SickNote}s.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public interface SickNoteService {

    /**
     * Persists the given sick note.
     *
     * @param  sickNote  to be persisted
     */
    void save(SickNote sickNote);


    /**
     * Gets the sick note with the given id.
     *
     * @param  id  to search the sick note by
     *
     * @return  the sick note matching the given id
     */
    SickNote getById(Integer id);


    /**
     * Get all the sick notes of the given person that are in the given period.
     *
     * @param  person  defines the owner of the sick notes
     * @param  from  defines the start of the period
     * @param  to  defines the end of the period
     *
     * @return  all the sick notes matching the given parameters
     */
    List<SickNote> getByPersonAndPeriod(Person person, DateMidnight from, DateMidnight to);


    /**
     * Get all the sick notes that are in the given period.
     *
     * @param  from  defines the start of the period
     * @param  to  defines the end of the period
     *
     * @return  all the sick notes matching the given parameters
     */
    List<SickNote> getByPeriod(DateMidnight from, DateMidnight to);


    /**
     * Get all the sick notes that are reaching the end of sick pay.
     *
     * @return  sick notes that are reaching the end of sick pay
     */
    List<SickNote> getSickNotesReachingEndOfSickPay();
}
