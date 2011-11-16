/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.dao.UrlaubsanspruchDAO;
import org.synyx.urlaubsverwaltung.dao.UrlaubskontoDAO;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Urlaubsanspruch;
import org.synyx.urlaubsverwaltung.domain.Urlaubskonto;

import java.io.IOException;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author  johannes
 */
@Transactional
public class KontoServiceImpl implements KontoService {

    private UrlaubskontoDAO urlaubskontoDAO;
    private UrlaubsanspruchDAO urlaubsanspruchDAO;
    private OwnCalendarService ownCalendarService;

    @Autowired
    public KontoServiceImpl(UrlaubskontoDAO urlaubskontoDAO, UrlaubsanspruchDAO urlaubsanspruchDAO,
        OwnCalendarService ownCalendarService) {

        this.urlaubskontoDAO = urlaubskontoDAO;
        this.urlaubsanspruchDAO = urlaubsanspruchDAO;
        this.ownCalendarService = ownCalendarService;
    }

    @Override
    public void newUrlaubsanspruch(Person person, Integer year, Integer anspruch) {

        Urlaubsanspruch urlaubsanspruch = new Urlaubsanspruch();
        urlaubsanspruch.setPerson(person);
        urlaubsanspruch.setVacationDays(anspruch);
        urlaubsanspruch.setYear(year);

        urlaubsanspruchDAO.save(urlaubsanspruch);
    }


    @Override
    public void saveUrlaubsanspruch(Urlaubsanspruch urlaubsanspruch) {

        urlaubsanspruchDAO.save(urlaubsanspruch);
    }


    @Override
    public void saveUrlaubskonto(Urlaubskonto urlaubskonto) {

        urlaubskontoDAO.save(urlaubskonto);
    }


    @Override
    public void newUrlaubskonto(Person person, Integer vacDays, Integer restVacDays, Integer year) {

        Urlaubskonto urlaubskonto = new Urlaubskonto();

        urlaubskonto.setPerson(person);
        urlaubskonto.setRestVacationDays(restVacDays);
        urlaubskonto.setVacationDays(vacDays);
        urlaubskonto.setYear(year);

        urlaubskontoDAO.save(urlaubskonto);
    }


    @Override
    public Urlaubsanspruch getUrlaubsanspruch(Integer year, Person person) {

        return urlaubsanspruchDAO.getUrlaubsanspruchByDate(year, person);
    }


    @Override
    public Urlaubskonto getUrlaubskonto(Integer year, Person person) {

        return urlaubskontoDAO.getUrlaubskontoForDateAndPerson(year, person);
    }

// !!!! denke, die folgenden methoden sind nicht nötig!!!


// // meine (aljona) ansicht dazu:
// // fuer was braucht man das eigtl?
// public void updateUrlaubskonto(Urlaubskonto urlaubskonto, Integer vacDays, Integer restVacDays, Integer year) {
//
// urlaubskonto.setVacationDays(vacDays);
// urlaubskonto.setRestVacationDays(restVacDays);
// urlaubskonto.setYear(year);
// urlaubskontoDAO.save(urlaubskonto);
//
// // vorher: (jo)
//// urlaubskontoDAO.delete(urlaubskonto);
//// urlaubskontoDAO.save(urlaubskonto);
//    }
//
//    // hab ich (aljona) auch mal geaendert
//    // fuer was braucht man das eigtl?
//    public void updateUrlaubsanspruch(Urlaubsanspruch urlaubsanspruch, Integer vacDays, Integer year) {
//
//        urlaubsanspruch.setVacationDays(vacDays);
//        urlaubsanspruch.setYear(year);
//        urlaubsanspruchDAO.save(urlaubsanspruch);
//    }

    @Override
    public List<Urlaubskonto> getUrlaubskontoForYear(Integer year) {

        return urlaubskontoDAO.getUrlaubskontoForYear(year);
    }


    // NOCH NICHT FERTIG!!!!
    @Override
    public void calculateUrlaubskonto(Person person, DateMidnight startDate, DateMidnight endDate) {

        // liegen Datumsangaben im gleichen Jahr?
        Integer yearStartDate = startDate.getYear();
        Integer yearEndDate = endDate.getYear();

        // liegen nicht im gleichen Jahr
        if (yearStartDate != yearEndDate) {
            Urlaubskonto kontoCurrentYear = urlaubskontoDAO.getUrlaubskontoForDateAndPerson(yearStartDate, person);
            Urlaubskonto kontoNextYear;

            if (urlaubskontoDAO.getUrlaubskontoForDateAndPerson(yearEndDate, person) == null) {
                newUrlaubskonto(person,
                    (urlaubsanspruchDAO.getUrlaubsanspruchByDate(yearStartDate, person).getVacationDays()), 0,
                    yearEndDate);
            }

            Integer beforeJan = 0;
            Integer afterJan = 0;

            try {
                // Urlaub laeuft ueber den 1. Jan.
                beforeJan = ownCalendarService.getVacationDays(startDate, new DateMidnight(yearStartDate, 12, 31));
                afterJan = ownCalendarService.getVacationDays(new DateMidnight(yearEndDate, 1, 1), endDate);
            } catch (AuthenticationException ex) {
                Logger.getLogger(KontoServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(KontoServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ServiceException ex) {
                Logger.getLogger(KontoServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

            kontoCurrentYear.setVacationDays(kontoCurrentYear.getVacationDays() - beforeJan);
            kontoCurrentYear.setUsedVacationDays(kontoCurrentYear.getUsedVacationDays() + beforeJan);

            Integer putInResturlaub = kontoCurrentYear.getVacationDays();
        }
        // liegen im gleichen Jahr
        else {
            Urlaubskonto konto = urlaubskontoDAO.getUrlaubskontoForDateAndPerson(yearStartDate, person);
            Integer vacDays = null;

            try {
                vacDays = ownCalendarService.getVacationDays(startDate, endDate);
            } catch (AuthenticationException ex) {
                Logger.getLogger(KontoServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(KontoServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ServiceException ex) {
                Logger.getLogger(KontoServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

            // teste, ob Urlaub ueber den 1. Apr. geht
            if ((startDate.getMonthOfYear() <= DateTimeConstants.MARCH)
                    && (endDate.getMonthOfYear() >= DateTimeConstants.APRIL)) {
                try {
                    // ein Teil vom Resturlaub abgezogen
                    // ein Teil nicht
                    Integer beforeApr = ownCalendarService.getVacationDays(startDate,
                            new DateMidnight(yearStartDate, 3, 31));
                    Integer afterApr = ownCalendarService.getVacationDays(new DateMidnight(yearEndDate, 4, 1), endDate);

                    if ((konto.getRestVacationDays() - beforeApr) < 0) {
                        konto.setVacationDays(konto.getVacationDays() - (beforeApr - konto.getRestVacationDays()));
                        konto.setUsedRestVacationDays(konto.getUsedRestVacationDays()
                            + (beforeApr - konto.getRestVacationDays()));
                        konto.setRestVacationDays(0);
                    } else {
                        konto.setRestVacationDays(konto.getRestVacationDays() - beforeApr);
                        konto.setUsedRestVacationDays(konto.getUsedRestVacationDays() + beforeApr);
                    }
                } catch (AuthenticationException ex) {
                    Logger.getLogger(KontoServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(KontoServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ServiceException ex) {
                    Logger.getLogger(KontoServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (startDate.getMonthOfYear() >= DateTimeConstants.APRIL) {
                // es gibt keinen Resturlaub mehr, nur normaler Urlaub genutzt
                konto.setUsedVacationDays(konto.getUsedVacationDays() + vacDays);
                konto.setVacationDays(konto.getVacationDays() - vacDays);
            } else if (endDate.getMonthOfYear() <= DateTimeConstants.APRIL) {
                // es wird zuerst der gesamte Resturlaub abgezogen, danach der normale
            }
        }
    }
}
