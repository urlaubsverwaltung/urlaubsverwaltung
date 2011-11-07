package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.dao.PersonDAO;
import org.synyx.urlaubsverwaltung.dao.UrlaubsanspruchDAO;
import org.synyx.urlaubsverwaltung.domain.Antrag;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Urlaubsanspruch;
import org.synyx.urlaubsverwaltung.domain.Urlaubskonto;

import java.util.ArrayList;
import java.util.List;


/**
 * implementation of the persondata-access-service. for now just passing functions, but this can change(maybe)
 *
 * @author  johannes
 */
@Transactional
public class PersonServiceImpl implements PersonService {

    private PersonDAO personDAO;
    private AntragService antragService;
    private KontoService kontoService;
    private UrlaubsanspruchDAO urlaubsanspruchDAO;

    // wird hier und im anderen service benötigt, weil wir ja
    // ständig irgendwelche mails schicken müssen... =)
    private MailService mailService;

    @Autowired
    public PersonServiceImpl(PersonDAO personDAO, AntragService antragService, UrlaubsanspruchDAO urlaubsanspruchDAO,
        MailService mailService, KontoService kontoService) {

        this.personDAO = personDAO;
        this.antragService = antragService;
        this.urlaubsanspruchDAO = urlaubsanspruchDAO;
        this.mailService = mailService;
        this.kontoService = kontoService;
    }

    /**
     * @see  PersonService#save(org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public void save(Person person) {

        personDAO.save(person);
    }


    /**
     * @see  PersonService#delete(org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public void delete(Person person) {

        personDAO.delete(person);
    }


    /**
     * @see  PersonService#getPersonByID(java.lang.Integer)
     */
    @Override
    public Person getPersonByID(Integer id) {

        return personDAO.findOne(id);
    }


    /**
     * @see  PersonService#getAllPersons()
     */
    @Override
    public List<Person> getAllPersons() {

        return personDAO.findAll();
    }


    /**
     * @see  PersonService#deleteResturlaub()
     */
    @Override
    public void deleteResturlaub() {

        List<Person> persons = getAllPersons();

        for (Person person : persons) {
            Urlaubskonto currentKonto = kontoService.getUrlaubskonto(DateMidnight.now().getYear(), person);
            currentKonto.setRestVacationDays(0);
        }
    }


    /**
     * @see  PersonService#getPersonsWithResturlaub()
     */
    @Override
    public List<Person> getPersonsWithResturlaub() {

        List<Person> restUrlaubPersons = new ArrayList<Person>();

        int year = DateMidnight.now().getYear();

        List<Person> persons = getAllPersons();

        for (Person person : persons) {
            if (kontoService.getUrlaubskonto(year, person).getRestVacationDays() != 0) {
                restUrlaubPersons.add(person);
            }
        }

        return restUrlaubPersons;
    }


    /**
     * @see  PersonService#updateVacationDays()
     */
    @Override
    public void updateVacationDays() {

        int year = DateMidnight.now().getYear();

        Urlaubskonto urlaubskonto;

        List<Person> persons = getAllPersons();

        for (Person person : persons) {
            Urlaubsanspruch urlaubsanspruch = kontoService.getUrlaubsanspruch(year, person);

            if (urlaubsanspruch == null) {
                // wenn der urlaubsanspruch für das neue jahr noch nicht besteht (z.b. durch änderung)
                // dann nimm den aus dem letzten jahr, der MUSS da sein (YO)
                urlaubsanspruch = kontoService.newUrlaubsanspruch(person, year,
                        kontoService.getUrlaubsanspruch(year - 1, person).getVacationDays());
            }

            // lies das urlaubskonto für das aktuelle jahr aus
            urlaubskonto = kontoService.getUrlaubskonto(year, person);

            if (urlaubskonto == null) {
                // wenn das konto noch nicht besteht, trage aktuellen urlaubsanspruch ein
                // und übertrage kontostand aus altem jahr als resturlaub ins neue

                Integer restDays = kontoService.getUrlaubskonto(year - 1, person).getVacationDays();

                // neues Konto anlegen und zurückgeben
                urlaubskonto = kontoService.newUrlaubskonto(person, urlaubsanspruch.getVacationDays(), restDays, year);
            } else {
                // wenn das konto schon besteht..
                Integer restDays = kontoService.getUrlaubskonto(year - 1, person).getVacationDays();

                if (urlaubskonto.getVacationDays() < urlaubsanspruch.getVacationDays()) {
                    // wenn vom urlaubskonto dieses jahres schon tage abgebucht wurden,
                    // muss der resturlaub erst dafür verwendet werden, um das urlaubskonto
                    // wieder aufzufüllen
                    Integer urlaubsTage;

                    // urlaubstage enthält alle tage, die die person in diesem jahr hat
                    urlaubsTage = urlaubskonto.getVacationDays() + restDays;
                    restDays = 0;

                    if (urlaubsTage > urlaubsanspruch.getVacationDays()) {
                        // falls das mehr sind, als er urlaub haben dürfte, so wird der überhang als
                        // resturlaub gewertet
                        restDays = urlaubsTage - urlaubsanspruch.getVacationDays();

                        // und das normale urlaubskonto ist wieder voll
                        urlaubsTage = urlaubsanspruch.getVacationDays();
                    }

                    // trage die neue anzahl urlaubstage ein
                    urlaubskonto.setVacationDays(urlaubsTage);
                }

                // trage die resturlaubstage ein
                urlaubskonto.setRestVacationDays(restDays);

                // schreib alles in die db
                kontoService.saveUrlaubskonto(urlaubskonto);
            }
        }
    }


    /**
     * @see  PersonService#getAllUrlauberForThisWeekAndPutItInAnEmail(org.joda.time.DateMidnight, org.joda.time.DateMidnight)
     */
    @Override
    public void getAllUrlauberForThisWeekAndPutItInAnEmail(DateMidnight startDate, DateMidnight endDate) {

        List<Antrag> requests = antragService.getAllRequestsForACertainTime(startDate, endDate);

        List<Person> urlauber = new ArrayList<Person>();

        for (Antrag antrag : requests) {
            urlauber.add(antrag.getPerson());
        }

        mailService.sendWeeklyVacationForecast(urlauber);
    }


    @Override
    public Urlaubsanspruch getUrlaubsanspruchByPersonAndYear(Person person, Integer year) {

        return urlaubsanspruchDAO.getUrlaubsanspruchByDate(year, person);
    }


    @Override
    public List<Urlaubsanspruch> getUrlaubsanspruchByPersonForAllYears(Person person) {

        return urlaubsanspruchDAO.getUrlaubsanspruchByPerson(person);
    }


    @Override
    public void setUrlaubsanspruchForPerson(Person person, Integer year, Integer days) {

        Urlaubsanspruch urlaubsanspruch = new Urlaubsanspruch();
        urlaubsanspruch.setPerson(person);
        urlaubsanspruch.setYear(year);
        urlaubsanspruch.setVacationDays(days);

        urlaubsanspruchDAO.save(urlaubsanspruch);
    }
}
