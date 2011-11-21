/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.dao.UrlaubsanspruchDAO;
import org.synyx.urlaubsverwaltung.dao.UrlaubskontoDAO;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Urlaubsanspruch;
import org.synyx.urlaubsverwaltung.domain.Urlaubskonto;

import java.util.List;


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


    @Override
    public List<Urlaubskonto> getUrlaubskontoForYear(Integer year) {

        return urlaubskontoDAO.getUrlaubskontoForYear(year);
    }
}
