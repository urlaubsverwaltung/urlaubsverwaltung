/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.dao.UrlaubsanspruchDAO;
import org.synyx.urlaubsverwaltung.dao.UrlaubskontoDAO;
import org.synyx.urlaubsverwaltung.domain.Antrag;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Urlaubsanspruch;
import org.synyx.urlaubsverwaltung.domain.Urlaubskonto;

import java.util.List;


/**
 * @author  johannes
 */
@Transactional
public class KontoServiceImpl implements KontoService {

    private static final int LAST_DAY = 31;
    private static final int FIRST_DAY = 1;

    private UrlaubskontoDAO urlaubskontoDAO;
    private UrlaubsanspruchDAO urlaubsanspruchDAO;
    private OwnCalendarService calendarService;

    @Autowired
    public KontoServiceImpl(UrlaubskontoDAO urlaubskontoDAO, UrlaubsanspruchDAO urlaubsanspruchDAO,
        OwnCalendarService calendarService) {

        this.urlaubskontoDAO = urlaubskontoDAO;
        this.urlaubsanspruchDAO = urlaubsanspruchDAO;
        this.calendarService = calendarService;
    }

    /**
     * @see  KontoService#newUrlaubsanspruch(org.synyx.urlaubsverwaltung.domain.Person, java.lang.Integer,
     *       java.lang.Integer)
     */
    @Override
    public Urlaubsanspruch newUrlaubsanspruch(Person person, Integer year, Double anspruch) {

        Urlaubsanspruch urlaubsanspruch = new Urlaubsanspruch();
        urlaubsanspruch.setPerson(person);
        urlaubsanspruch.setVacationDays(anspruch);
        urlaubsanspruch.setYear(year);

        urlaubsanspruchDAO.save(urlaubsanspruch);

        return urlaubsanspruch;
    }


    /**
     * @see  KontoService#saveUrlaubsanspruch(org.synyx.urlaubsverwaltung.domain.Urlaubsanspruch)
     */
    @Override
    public void saveUrlaubsanspruch(Urlaubsanspruch urlaubsanspruch) {

        urlaubsanspruchDAO.save(urlaubsanspruch);
    }


    /**
     * @see  KontoService#saveUrlaubskonto(org.synyx.urlaubsverwaltung.domain.Urlaubskonto)
     */
    @Override
    public void saveUrlaubskonto(Urlaubskonto urlaubskonto) {

        urlaubskontoDAO.save(urlaubskonto);
    }


    /**
     * @see  KontoService#newUrlaubskonto(org.synyx.urlaubsverwaltung.domain.Person, java.lang.Integer,
     *       java.lang.Integer, java.lang.Integer)
     */
    @Override
    public Urlaubskonto newUrlaubskonto(Person person, Double vacDays, Double restVacDays, Integer year) {

        Urlaubskonto urlaubskonto = new Urlaubskonto();

        urlaubskonto.setPerson(person);
        urlaubskonto.setRestVacationDays(restVacDays);
        urlaubskonto.setVacationDays(vacDays);
        urlaubskonto.setYear(year);

        urlaubskontoDAO.save(urlaubskonto);

        return urlaubskonto;
    }


    /**
     * @see  KontoService#getUrlaubsanspruch(java.lang.Integer, org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public Urlaubsanspruch getUrlaubsanspruch(Integer year, Person person) {

        return urlaubsanspruchDAO.getUrlaubsanspruchByDate(year, person);
    }


    /**
     * @see  KontoService#getUrlaubskonto(java.lang.Integer, org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public Urlaubskonto getUrlaubskonto(Integer year, Person person) {

        return urlaubskontoDAO.getUrlaubskontoForDateAndPerson(year, person);
    }


    /**
     * @see  KontoService#getUrlaubskontoForYear(java.lang.Integer)
     */
    @Override
    public List<Urlaubskonto> getUrlaubskontoForYear(Integer year) {

        return urlaubskontoDAO.getUrlaubskontoForYear(year);
    }


    @Override
    public void rollbackUrlaub(Antrag antrag) {

        DateMidnight start = antrag.getStartDate();
        DateMidnight end = antrag.getEndDate();
        Person person = antrag.getPerson();

        // Liegen Start- und Enddatum nicht im gleichen Jahr, läuft der Urlaub über den 1.1.
        if (start.getYear() != end.getYear()) {
            Urlaubskonto kontoCurrentYear = getUrlaubskonto(start.getYear(), person);
            Double anspruchCurrentYear = getUrlaubsanspruch(start.getYear(), person).getVacationDays();

            Urlaubskonto kontoNextYear = getUrlaubskonto(end.getYear(), person);
            Double anspruchNextYear = getUrlaubsanspruch(end.getYear(), person).getVacationDays();

            rollbackNoticeJanuary(antrag, kontoCurrentYear, kontoNextYear, anspruchCurrentYear, anspruchNextYear);

            saveUrlaubskonto(kontoCurrentYear);
            saveUrlaubskonto(kontoNextYear);
        } else {
            // Es muss der 1.4. beachtet werden
            Urlaubskonto konto = getUrlaubskonto(start.getYear(), person);
            Double anspruch = getUrlaubsanspruch(start.getYear(), person).getVacationDays();

            rollbackNoticeApril(antrag, konto, anspruch);

            saveUrlaubskonto(konto);
        }
    }


    /**
     * Wird eingesetzt, um bei stornieren oder ablehnen die eingetragenen verbrauchten Tage des Urlaubskontos wieder zu
     * fuellen. Diese Methode beachtet den Sonderfall Januar, d.h. wenn ein Antrag ueber den 1.1., also ueber zwei Jahre
     * laeuft.
     *
     * @param  antrag
     * @param  start
     * @param  end
     */
    @Override
    public void rollbackNoticeJanuary(Antrag antrag, Urlaubskonto kontoCurrentYear, Urlaubskonto kontoNextYear,
        Double anspruchCurrentYear, Double anspruchNextYear) {

        DateMidnight start = antrag.getStartDate();
        DateMidnight end = antrag.getEndDate();

        Double beforeJan = calendarService.getVacationDays(start,
                new DateMidnight(start.getYear(), DateTimeConstants.DECEMBER, LAST_DAY), antrag.isGanztags());
        Double afterJan = calendarService.getVacationDays(new DateMidnight(end.getYear(), DateTimeConstants.JANUARY,
                    FIRST_DAY), end, antrag.isGanztags());

        Double oldVacationDays = kontoCurrentYear.getVacationDays() + beforeJan;

        // beforeJan füllt Urlaubskonto von Jahr 1 auf
        // konto1.setVacationDays(konto1.getVacationDays() + beforeJan);

        // wenn Urlaubskonto Anspruch überschreitet, muss stattdessen Resturlaub aufgefüllt werden
        if (oldVacationDays > anspruchCurrentYear) {
            kontoCurrentYear.setRestVacationDays(kontoCurrentYear.getRestVacationDays()
                + (oldVacationDays - anspruchCurrentYear));
            kontoCurrentYear.setVacationDays(oldVacationDays - (oldVacationDays - anspruchCurrentYear));
        } else {
            kontoCurrentYear.setVacationDays(oldVacationDays);
        }

        // afterJan füllt Urlaubskonto von Jahr 2 auf
        // konto2.setVacationDays(konto2.getVacationDays() + afterJan);

        Double newVacationDays = kontoNextYear.getVacationDays() + afterJan;

        // wenn Urlaubskonto Anspruch überschreitet, muss stattdessen Resturlaub aufgefüllt werden
        if (newVacationDays > anspruchNextYear) {
            kontoCurrentYear.setRestVacationDays(kontoNextYear.getRestVacationDays()
                + (newVacationDays - anspruchNextYear));
            kontoNextYear.setVacationDays(newVacationDays - (newVacationDays - anspruchNextYear));
        } else {
            kontoNextYear.setVacationDays(newVacationDays);
        }
    }


    /**
     * Wird eingesetzt, um bei stornieren oder ablehnen die eingetragenen verbrauchten Tage des Urlaubskontos wieder zu
     * fuellen. Diese Methode beachtet den Sonderfall April, d.h. wenn ein Antrag ueber den 1.4. laeuft, das evtl.
     * Verfallen des Resturlaubs also beachtet werden muss.
     *
     * @param  antrag
     * @param  start
     * @param  end
     */
    @Override
    public void rollbackNoticeApril(Antrag antrag, Urlaubskonto konto, Double anspruch) {

        DateMidnight start = antrag.getStartDate();
        DateMidnight end = antrag.getEndDate();

        // Urlaub nach dem 1.4., d.h. kein Resturlaub zu berechnen, da kein Resturlaub mehr vorhanden
        // keine Beruehrung mit der Problematik vom 1.4.
        if (start.getMonthOfYear() >= DateTimeConstants.APRIL) {
            Double gut = calendarService.getVacationDays(start, end, antrag.isGanztags());
            konto.setVacationDays(gut + konto.getVacationDays());
        } else if (end.getMonthOfYear() < DateTimeConstants.APRIL) {
            // Urlaub endet vor dem 1.4., d.h. keine Beruehrung mit der Problematik vom 1.4.
            Double gut = calendarService.getVacationDays(start, end, antrag.isGanztags()) + konto.getVacationDays();

            if (gut > anspruch) {
                konto.setRestVacationDays(konto.getRestVacationDays() + (gut - anspruch));
                konto.setVacationDays(anspruch);
            } else if (gut <= anspruch) {
                konto.setVacationDays(gut);
            }
        } else if (start.getMonthOfYear() <= DateTimeConstants.MARCH
                && end.getMonthOfYear() >= DateTimeConstants.APRIL) {
            rollbackOverApril(antrag, konto, anspruch, start, end);
        }
    }


    @Override
    public void rollbackOverApril(Antrag antrag, Urlaubskonto konto, Double anspruch, DateMidnight start,
        DateMidnight end) {

        // Urlaub laeuft ueber den 1.4.
        Double beforeApr = calendarService.getVacationDays(start,
                new DateMidnight(start.getYear(), DateTimeConstants.MARCH, LAST_DAY), antrag.isGanztags());
        Double afterApr = calendarService.getVacationDays(new DateMidnight(start.getYear(), DateTimeConstants.APRIL,
                    FIRST_DAY), end, antrag.isGanztags());

        // erstmal die nach April Tage (afterApr) aufs Urlaubskonto füllen
        // konto1.setVacationDays(konto1.getVacationDays() + afterApr);

        Double newVacationDays = konto.getVacationDays() + afterApr;

        // wenn so das Urlaubskonto gleich dem Anspruch ist,
        // setze die Tage vor dem April (beforeApr) ins Resturlaubkonto (das sowieso ab 1.4. verfällt)
        if (newVacationDays.equals(anspruch)) {
            konto.setRestVacationDays(konto.getRestVacationDays() + beforeApr);
            konto.setVacationDays(newVacationDays);
        } else if (konto.getVacationDays() < anspruch) {
            // wenn das Auffüllen mit afterApr das Konto noch nicht zum Überlaufen gebracht hat
            // d.h. urlaubskonto < anspruch
            // addiere beforeApr dazu
            // konto1.setVacationDays(konto1.getVacationDays() + beforeApr);
            newVacationDays = newVacationDays + beforeApr;

            // wenn so das urlaubskonto überquillt
            // d.h. urlaubskonto > anspruch
            if (newVacationDays >= anspruch) {
                // schmeiss den überhängenden scheiss in den resturlaub
                konto.setRestVacationDays(konto.getRestVacationDays() + (newVacationDays - anspruch));
                konto.setVacationDays(anspruch);
            } else {
                konto.setVacationDays(newVacationDays);
            }
        }
    }


    /**
     * Berechnung Netto-Urlaubstage von angegebenem Zeitraum, aktualisiert Urlaubskonto. Beachtung des Sonderfalls 1.1.:
     * zwei Urlaubskonten werden genutzt.
     *
     * @param  antrag
     * @param  start
     * @param  end
     * @param  mustBeSaved  wenn auf true gesetzt, wird in der methode das konto automatisch gespeichert, wenn nicht,
     *                      muss es von aussen gespeichert werden
     */
    @Override
    public void noticeJanuary(Antrag antrag, Urlaubskonto kontoCurrentYear, Urlaubskonto kontoNextYear) {

        DateMidnight start = antrag.getStartDate();
        DateMidnight end = antrag.getEndDate();

        // wenn der antrag über 2 Jahre läuft...
        Double beforeJan = calendarService.getVacationDays(antrag.getStartDate(),
                new DateMidnight(start.getYear(), DateTimeConstants.DECEMBER, LAST_DAY), antrag.isGanztags());
        Double afterJan = calendarService.getVacationDays(new DateMidnight(end.getYear(), DateTimeConstants.JANUARY,
                    FIRST_DAY), antrag.getEndDate(), antrag.isGanztags());

        // konto des alten jahres = einfach die tage vor januar abziehen
        kontoCurrentYear.setVacationDays(kontoCurrentYear.getVacationDays() - beforeJan);

        // resturlaub des neuen jahres = einfach die tage nach 1.1. abziehen
        Double newRestDays = kontoNextYear.getRestVacationDays() - afterJan;
        Double newVacDays = kontoNextYear.getVacationDays();

        if (newRestDays < 0.0) {
            // wenn resttage < 0, dann ziehe differenz von den normalen urlaubstagen ab..
            newVacDays = newVacDays + newRestDays;

            // und setze resturlaub auf 0 (yay)
            newRestDays = 0.0;
        }

        // alles schön abspeichern

        kontoNextYear.setRestVacationDays(newRestDays);
        kontoNextYear.setVacationDays(newVacDays);
    }


    /**
     * Berechnung Netto-Urlaubstage von angegebenem Zeitraum, aktualisiert Urlaubskonto. Beachtung des Sonderfalls 1.4.:
     * es muss beachtet werden, dass Resturlaub verfaellt
     *
     * @param  antrag
     * @param  konto
     * @param  start
     * @param  end
     */
    @Override
    public void noticeApril(Antrag antrag, Urlaubskonto konto) {

        DateMidnight start = antrag.getStartDate();
        DateMidnight end = antrag.getEndDate();

        // wenn antrag vor april ist
        if (antrag.getEndDate().getMonthOfYear() < DateTimeConstants.APRIL) {
            // errechne, wie viele resturlaubstage übrigbleiben würden
            Double newRestDays = konto.getRestVacationDays()
                - (calendarService.getVacationDays(start, end, antrag.isGanztags()));

            // wenn das weniger sind als 0
            if (newRestDays < 0) {
                // ziehe die differenz vom normalen konto ab
                konto.setVacationDays(konto.getVacationDays() + newRestDays);

                // und setze den resturlaub auf 0
                konto.setRestVacationDays(0.0);
            } else {
                // sonst speichere einfach neue resturlaubstage
                konto.setRestVacationDays(newRestDays);
            }
        } else if (antrag.getStartDate().getMonthOfYear() >= DateTimeConstants.APRIL) {
            // wenn der antrag nach april ist, gibt's keinen resturlaub mehr
            konto.setVacationDays(konto.getVacationDays()
                - (calendarService.getVacationDays(start, end, antrag.isGanztags())));
        } else {
            // wenn der antrag über april läuft
            Double beforeApril = calendarService.getVacationDays(antrag.getStartDate(),
                    new DateMidnight(start.getYear(), DateTimeConstants.MARCH, LAST_DAY), antrag.isGanztags());
            Double afterApril = calendarService.getVacationDays(new DateMidnight(end.getYear(), DateTimeConstants.APRIL,
                        FIRST_DAY), antrag.getEndDate(), antrag.isGanztags());

            // errechne, wie viele resturlaubstage übrigbleiben würden
            Double newRestDays = konto.getRestVacationDays() - beforeApril;
            Double newVacDays = konto.getVacationDays();

            // wenn es weniger wie 0 sind,
            if (newRestDays < 0.0) {
                // ziehe differenz von normalen urlaubstagen ab
                newVacDays = konto.getVacationDays() + newRestDays;

                // und setze resturlaub auf 0
                newRestDays = 0.0;
            }

            konto.setRestVacationDays(newRestDays);
            konto.setVacationDays(newVacDays - afterApril);
        }
    }
}
