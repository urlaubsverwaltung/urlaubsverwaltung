/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.dao.UrlaubsanspruchDAO;
import org.synyx.urlaubsverwaltung.dao.UrlaubskontoDAO;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Urlaubsanspruch;
import org.synyx.urlaubsverwaltung.domain.Urlaubskonto;


/**
 * @author  johannes
 */
@Transactional
public class KontoServiceImpl implements KontoService {

    private UrlaubskontoDAO urlaubskontoDAO;

    private UrlaubsanspruchDAO urlaubsanspruchDAO;

    @Autowired
    public KontoServiceImpl(UrlaubskontoDAO urlaubskontoDAO, UrlaubsanspruchDAO urlaubsanspruchDAO) {

        this.urlaubskontoDAO = urlaubskontoDAO;
        this.urlaubsanspruchDAO = urlaubsanspruchDAO;
    }

    @Override
    public Urlaubsanspruch newUrlaubsanspruch(Person person, Integer year, Integer anspruch) {

        Urlaubsanspruch urlaubsanspruch = new Urlaubsanspruch();
        urlaubsanspruch.setPerson(person);
        urlaubsanspruch.setVacationDays(anspruch);
        urlaubsanspruch.setYear(year);

        urlaubsanspruchDAO.save(urlaubsanspruch);

        return urlaubsanspruch;
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
    public Urlaubskonto newUrlaubskonto(Person person, Integer vacDays, Integer restVacDays, Integer year) {

        Urlaubskonto urlaubskonto = new Urlaubskonto();

        urlaubskonto.setPerson(person);
        urlaubskonto.setRestVacationDays(restVacDays);
        urlaubskonto.setVacationDays(vacDays);
        urlaubskonto.setYear(year);

        urlaubskontoDAO.save(urlaubskonto);

        return urlaubskonto;
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

}
