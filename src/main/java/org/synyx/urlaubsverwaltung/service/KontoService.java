/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Urlaubsanspruch;
import org.synyx.urlaubsverwaltung.domain.Urlaubskonto;


/**
 * @author  aljona
 */
public interface KontoService {

    Urlaubsanspruch getUrlaubsanspruch(Integer year, Person person);


    Urlaubskonto getUrlaubskonto(Integer year, Person person);


    void newUrlaubsanspruch(Person person, Integer year, Integer anspruch);


    void newUrlaubskonto(Person person, Integer vacDays, Integer restVacDays, Integer year);


    void saveUrlaubsanspruch(Urlaubsanspruch urlaubsanspruch);


    void saveUrlaubskonto(Urlaubskonto urlaubskonto);
}
