/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Urlaubsanspruch;
import org.synyx.urlaubsverwaltung.domain.Urlaubskonto;

import java.util.List;


/**
 * @author  aljona
 */
public interface KontoService {

    /**
     * get Urlaubanspruch for certain year and person
     *
     * @param  year
     * @param  person
     *
     * @return
     */
    Urlaubsanspruch getUrlaubsanspruch(Integer year, Person person);


    /**
     * get Urlaubskonto for certain year and person
     *
     * @param  year
     * @param  person
     *
     * @return
     */
    Urlaubskonto getUrlaubskonto(Integer year, Person person);


    /**
     * get a list of Urlaubskonto for a certain year
     *
     * @param  year
     *
     * @return
     */
    List<Urlaubskonto> getUrlaubskontoForYear(Integer year);


    /**
     * creates a new Urlaubsanspruch for a person with params year and anspruch
     *
     * @param  person
     * @param  year
     * @param  anspruch
     */
    void newUrlaubsanspruch(Person person, Integer year, Integer anspruch);


    /**
     * creates a new Urlaubskonto for a person with params vacation days, resturlaub and year
     *
     * @param  person
     * @param  vacDays
     * @param  restVacDays
     * @param  year
     */
    void newUrlaubskonto(Person person, Integer vacDays, Integer restVacDays, Integer year);


    /**
     * saves Urlaubsanspruch
     *
     * @param  urlaubsanspruch
     */
    void saveUrlaubsanspruch(Urlaubsanspruch urlaubsanspruch);


    /**
     * saves Urlaubskonto
     *
     * @param  urlaubskonto
     */
    void saveUrlaubskonto(Urlaubskonto urlaubskonto);
}
