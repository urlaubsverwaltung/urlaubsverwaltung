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
 *
 * @author johannes
 */
@Transactional
public class KontoService {
    
    private UrlaubskontoDAO urlaubskontoDAO;
    
    private UrlaubsanspruchDAO urlaubsanspruchDAO;
    
    @Autowired
    public KontoService(UrlaubskontoDAO urlaubskontoDAO, UrlaubsanspruchDAO urlaubsanspruchDAO) {
        this.urlaubskontoDAO = urlaubskontoDAO;
        this.urlaubsanspruchDAO = urlaubsanspruchDAO;
    }
    
    public void saveUrlaubsanspruch(Person person, Integer year, Integer anspruch) {    
        Urlaubsanspruch urlaubsanspruch = new Urlaubsanspruch();
        urlaubsanspruch.setPerson(person);
        urlaubsanspruch.setVacationDays(anspruch);
        urlaubsanspruch.setYear(year);
        
        urlaubsanspruchDAO.save(urlaubsanspruch);
    }
    
    public void saveUrlaubskonto(Person person, Integer year, Integer stand, Integer restdays) {    
        
        Urlaubskonto urlaubskonto = new Urlaubskonto();
        
        urlaubskonto.setPerson(person);
        urlaubskonto.setRestVacationDays(restdays);
        urlaubskonto.setVacationDays(stand);
        urlaubskonto.setYear(year);
        
        urlaubskontoDAO.save(urlaubskonto);
    }
    
    //geht wohl irgendwie auch besser. bitte mal gucken
    public void updateUrlaubskonto(Urlaubskonto urlaubskonto) {
        urlaubskontoDAO.delete(urlaubskonto);
        urlaubskontoDAO.save(urlaubskonto);
    }
    
    //geht wohl irgendwie auch besser. bitte mal gucken
    public void updateUrlaubsanspruch(Urlaubsanspruch urlaubsanspruch) {
        urlaubsanspruchDAO.delete(urlaubsanspruch);
        urlaubsanspruchDAO.save(urlaubsanspruch);
        
    }
    
    public Urlaubsanspruch getUrlaubsanspruch(Integer year, Person person) {
        return urlaubsanspruchDAO.getUrlaubsanspruchByDate(year, person);
    }
    
    public Urlaubskonto getUrlaubskonto(Integer year, Person person) {
        return urlaubskontoDAO.getUrlaubskontoForDateAndPerson(year, person);
    }
    
}
