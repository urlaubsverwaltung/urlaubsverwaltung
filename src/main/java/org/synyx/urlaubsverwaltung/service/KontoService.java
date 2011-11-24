/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.domain.Antrag;
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
    Urlaubsanspruch newUrlaubsanspruch(Person person, Integer year, Double anspruch);


    /**
     * creates a new Urlaubskonto for a person with params vacation days, resturlaub and year
     *
     * @param  person
     * @param  vacDays
     * @param  restVacDays
     * @param  year
     */
    Urlaubskonto newUrlaubskonto(Person person, Double vacDays, Double restVacDays, Integer year);


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


    void rollbackUrlaub(Antrag antrag);


    /**
     * Wird eingesetzt, um bei stornieren oder ablehnen die eingetragenen verbrauchten Tage des Urlaubskontos wieder zu
     * fuellen. Diese Methode beachtet den Sonderfall Januar, d.h. wenn ein Antrag ueber den 1.1., also ueber zwei Jahre
     * laeuft.
     *
     * @param  antrag
     * @param  kontoCurrentYear
     * @param  kontoNextYear
     * @param  anspruchCurrentYear
     * @param  anspruchNextYear
     * @param  start
     * @param  end
     */
    void rollbackNoticeJanuary(Antrag antrag, Urlaubskonto kontoCurrentYear, Urlaubskonto kontoNextYear,
        Double anspruchCurrentYear, Double anspruchNextYear, DateMidnight start, DateMidnight end);


    /**
     * Wird eingesetzt, um bei stornieren oder ablehnen die eingetragenen verbrauchten Tage des Urlaubskontos wieder zu
     * fuellen. Diese Methode beachtet den Sonderfall April, d.h. wenn ein Antrag ueber den 1.4. laeuft, das evtl.
     * Verfallen des Resturlaubs also beachtet werden muss.
     *
     * @param  antrag
     * @param  konto
     * @param  anspruch
     * @param  start
     * @param  end
     */
    void rollbackNoticeApril(Antrag antrag, Urlaubskonto konto, Double anspruch, DateMidnight start, DateMidnight end);


    /**
     * Berechnung Netto-Urlaubstage von angegebenem Zeitraum, aktualisiert Urlaubskonto. Beachtung des Sonderfalls 1.1.:
     * zwei Urlaubskonten werden genutzt.
     *
     * @param  antrag
     * @param  kontoCurrentYear
     * @param  kontoNextYear
     * @param  start
     * @param  end
     * @param  mustBeSaved  wenn auf true gesetzt, wird in der methode das konto automatisch gespeichert, wenn nicht,
     *                      muss es von aussen gespeichert werden
     */
    void noticeJanuary(Antrag antrag, Urlaubskonto kontoCurrentYear, Urlaubskonto kontoNextYear, DateMidnight start,
        DateMidnight end);


    /**
     * Berechnung Netto-Urlaubstage von angegebenem Zeitraum, aktualisiert Urlaubskonto. Beachtung des Sonderfalls 1.4.:
     * es muss beachtet werden, dass Resturlaub verfaellt
     *
     * @param  antrag
     * @param  konto
     * @param  start
     * @param  end
     */
    void noticeApril(Antrag antrag, Urlaubskonto konto, DateMidnight start, DateMidnight end);
}
