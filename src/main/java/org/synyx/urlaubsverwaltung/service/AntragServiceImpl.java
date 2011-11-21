package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.dao.AntragDAO;
import org.synyx.urlaubsverwaltung.dao.PersonDAO;
import org.synyx.urlaubsverwaltung.domain.Antrag;
import org.synyx.urlaubsverwaltung.domain.Kommentar;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.State;
import org.synyx.urlaubsverwaltung.domain.Urlaubsanspruch;
import org.synyx.urlaubsverwaltung.domain.Urlaubskonto;
import org.synyx.urlaubsverwaltung.util.DateService;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;

import java.util.List;


/**
 * implementation of the requestdata-access-service.
 *
 * @author  johannes
 */
@Transactional
public class AntragServiceImpl implements AntragService {

    private static final int LAST_DAY = 31;
    private static final int FIRST_DAY = 1;

    private AntragDAO antragDAO;
    private PersonDAO personDAO;
    private KontoService kontoService;
    private DateService dateService;
    private PGPService pgpService;
    private OwnCalendarService calendarService;
    private PersonService personService;
    private MailServiceImpl mailService;

    @Autowired
    public AntragServiceImpl(AntragDAO antragDAO, PersonDAO personDAO, KontoService kontoService,
        DateService dateService, PGPService pgpService, OwnCalendarService calendarService, PersonService personService,
        MailServiceImpl mailService) {

        this.antragDAO = antragDAO;
        this.personDAO = personDAO;
        this.kontoService = kontoService;
        this.dateService = dateService;
        this.pgpService = pgpService;
        this.calendarService = calendarService;
        this.personService = personService;
        this.mailService = mailService;
    }

    /**
     * @see  AntragService#save(org.synyx.urlaubsverwaltung.domain.Antrag)
     */
    @Override
    public void save(Antrag antrag) {

        // Tage zwischen zwei datumsangaben werden nicht mit dateservice gezaehlt, sondern mit kalendarservice

        Person person = antrag.getPerson();

        // liegen Datumsangaben im gleichen Jahr?
        Integer yearStartDate = antrag.getStartDate().getYear();
        Integer yearEndDate = antrag.getEndDate().getYear();

        // wenn sich der antrag nur in einem jahr befindet
        if (yearStartDate == yearEndDate) {
            // hole urlaubskonto
            Urlaubskonto konto = kontoService.getUrlaubskonto(yearStartDate, person);

            // wenn noch nicht angelegt
            if (konto == null) {
                // erzeuge konto
                kontoService.newUrlaubskonto(person,
                    kontoService.getUrlaubsanspruch(yearStartDate, person).getVacationDays(), 0, yearStartDate);
            }

            // wenn antrag vor april ist
            if (antrag.getEndDate().getMonthOfYear() < DateTimeConstants.APRIL) {
                // errechne, wie viele resturlaubstage übrigbleiben würden
                Integer newRestDays = konto.getRestVacationDays() - antrag.getBeantragteTageNetto();

                // wenn das weniger sind als 0
                if (newRestDays < 0) {
                    // ziehe die differenz vom normalen konto ab
                    konto.setVacationDays(konto.getVacationDays() + newRestDays);

                    // und setze den resturlaub auf 0
                    konto.setRestVacationDays(0);
                } else {
                    // sonst speichere einfach neue resturlaubstage
                    konto.setRestVacationDays(newRestDays);
                }
            } else if (antrag.getStartDate().getMonthOfYear() >= DateTimeConstants.APRIL) {
                // wenn der antrag nach april ist, gibt's keinen resturlaub mehr
                konto.setVacationDays(konto.getVacationDays() - antrag.getBeantragteTageNetto());
            } else {
                // wenn der antrag über april läuft
                Integer beforeApril = calendarService.getVacationDays(antrag.getStartDate(),
                        new DateMidnight(yearStartDate, DateTimeConstants.MARCH, LAST_DAY));
                Integer afterApril = calendarService.getVacationDays(new DateMidnight(yearEndDate,
                            DateTimeConstants.APRIL, FIRST_DAY), antrag.getEndDate());

                // errechne, wie viele resturlaubstage übrigbleiben würden
                Integer newRestDays = konto.getRestVacationDays() - beforeApril;
                Integer newVacDays = konto.getVacationDays();

                // wenn es weniger wie 0 sind,
                if (newRestDays < 0) {
                    // ziehe differenz von normalen urlaubstagen ab
                    newVacDays = konto.getVacationDays() + newRestDays;

                    // und setze resturlaub auf 0
                    newRestDays = 0;
                }

                konto.setRestVacationDays(newRestDays);
                konto.setVacationDays(newVacDays - afterApril);

                kontoService.saveUrlaubskonto(konto);
            }
        } else {
            // wenn der antrag über 2 Jahre läuft...
            Integer beforeJan = calendarService.getVacationDays(antrag.getStartDate(),
                    new DateMidnight(yearStartDate, DateTimeConstants.DECEMBER, LAST_DAY));
            Integer afterJan = calendarService.getVacationDays(new DateMidnight(yearEndDate, DateTimeConstants.JANUARY,
                        FIRST_DAY), antrag.getEndDate());
            Urlaubskonto kontoCurrentYear = kontoService.getUrlaubskonto(yearStartDate, person);
            Urlaubskonto kontoNextYear = kontoService.getUrlaubskonto(yearEndDate, person);

            // konto des alten jahres = einfach die tage vor januar abziehen
            kontoCurrentYear.setVacationDays(kontoCurrentYear.getVacationDays() - beforeJan);

            // resturlaub des neuen jahres = einfach die tage nach 1.1. abziehen
            Integer newRestDays = kontoNextYear.getRestVacationDays() - afterJan;
            Integer newVacDays = kontoNextYear.getVacationDays();

            if (newRestDays < 0) {
                // wenn resttage<0, dann ziehe differenz von den normalen urlaubstagen ab..
                newVacDays = newVacDays + newRestDays;

                // und setze resturlaub auf 0 (yay)
                newRestDays = 0;
            }

            // alles schön abspeichern

            kontoNextYear.setRestVacationDays(newRestDays);
            kontoNextYear.setVacationDays(newVacDays);

            kontoService.saveUrlaubskonto(kontoCurrentYear);
            kontoService.saveUrlaubskonto(kontoNextYear);
        }

        antragDAO.save(antrag);
    }


    /**
     * @see  AntragService#approve(org.synyx.urlaubsverwaltung.domain.Antrag)
     */
    @Override
    public void approve(Antrag antrag) {

        antrag.setStatus(State.GENEHMIGT);
        antragDAO.save(antrag);

        mailService.sendApprovedNotification(antrag.getPerson(), antrag);
    }


    /**
     * @see  AntragService#decline(org.synyx.urlaubsverwaltung.domain.Antrag, java.lang.String)
     */
    @Override
    public void decline(Antrag antrag, Person boss, String reasonToDecline) {

        antrag.setStatus(State.ABGELEHNT);

        antrag.setBoss(boss);

        Kommentar comment = new Kommentar();
        comment.setText(reasonToDecline);
        comment.setPerson(boss);
        comment.setDatum(new DateMidnight(DateMidnight.now().getYear(), DateMidnight.now().getMonthOfYear(),
                DateMidnight.now().getDayOfMonth()));

        antrag.setReasonToDecline(comment);

        antragDAO.save(antrag);

        mailService.sendDeclinedNotification(antrag);
    }


    /**
     * @see  AntragService#wait(org.synyx.urlaubsverwaltung.domain.Antrag)
     */
    @Override
    public void wait(Antrag antrag) {

        antrag.setStatus(State.WARTEND);
        antragDAO.save(antrag);
    }


    /**
     * @see  AntragService#storno(org.synyx.urlaubsverwaltung.domain.Antrag)
     */
    @Override
    public void storno(Antrag antrag) {

        // wer ist der Antragsteller
        Person person = antrag.getPerson();

        // was ist der Zeitraum des Urlaubs
        DateMidnight startDate = antrag.getStartDate();
        DateMidnight endDate = antrag.getEndDate();
        Integer yearStartDate = startDate.getYear();
        Integer yearEndDate = endDate.getYear();

        // ein Urlaubskonto und ein Urlaubsanspruch hat Mensch auf jeden Fall
        // bei unterschiedlichen Jahren (= Sonderfall) wird ein zweites Urlaubskonto und -anspruch geholt
        Urlaubskonto kontoCurrentYear = kontoService.getUrlaubskonto(yearStartDate, person);
        Urlaubsanspruch anspruchCurrentYear = kontoService.getUrlaubsanspruch(yearStartDate, person);

        // Liegen Start- und Enddatum nicht im gleichen Jahr, läuft der Urlaub über den 1.1.
        if (yearStartDate != yearEndDate) {
            Integer beforeJan = calendarService.getVacationDays(startDate,
                    new DateMidnight(startDate.getYear(), DateTimeConstants.DECEMBER, LAST_DAY));
            Integer afterJan = calendarService.getVacationDays(new DateMidnight(yearEndDate, DateTimeConstants.JANUARY,
                        FIRST_DAY), endDate);

            // Urlaubskonto für Jahr 2 (vom endDate)
            Urlaubskonto kontoNextYear = kontoService.getUrlaubskonto(yearEndDate, person);

            // Urlaubsanspruch für Jahr 2 (vom endDate)
            Urlaubsanspruch anspruchNextYear = kontoService.getUrlaubsanspruch(yearEndDate, person);

            Integer oldVacationDays = kontoCurrentYear.getVacationDays() + beforeJan;

            Integer newVacationDays = 0;

            // beforeJan füllt Urlaubskonto von Jahr 1 auf
            // konto1.setVacationDays(konto1.getVacationDays() + beforeJan);

            // wenn Urlaubskonto Anspruch überschreitet, muss stattdessen Resturlaub aufgefüllt werden
            if (oldVacationDays > anspruchCurrentYear.getVacationDays()) {
                kontoCurrentYear.setRestVacationDays(kontoCurrentYear.getRestVacationDays()
                    + (oldVacationDays - anspruchCurrentYear.getVacationDays()));
                kontoCurrentYear.setVacationDays(oldVacationDays
                    - (oldVacationDays - anspruchCurrentYear.getVacationDays()));
            } else {
                kontoCurrentYear.setVacationDays(oldVacationDays);
            }

            // afterJan füllt Urlaubskonto von Jahr 2 auf
            // konto2.setVacationDays(konto2.getVacationDays() + afterJan);

            newVacationDays = kontoNextYear.getVacationDays() + afterJan;

            // wenn Urlaubskonto Anspruch überschreitet, muss stattdessen Resturlaub aufgefüllt werden
            if (newVacationDays > anspruchNextYear.getVacationDays()) {
                kontoCurrentYear.setRestVacationDays(kontoNextYear.getRestVacationDays()
                    + (newVacationDays - anspruchNextYear.getVacationDays()));
                kontoNextYear.setVacationDays(newVacationDays - (newVacationDays - anspruchNextYear.getVacationDays()));
            } else {
                kontoNextYear.setVacationDays(newVacationDays);
            }

            kontoService.saveUrlaubskonto(kontoNextYear);
        } else {
            // Urlaub nicht über 1.1., aber auch nicht über 1.4.
            // Urlaub nur nach dem 1.4., d.h. kein Resturlaub zu berechnen, da kein Resturlaub mehr vorhanden
            // Berechnung also easy
            if (startDate.getMonthOfYear() >= DateTimeConstants.APRIL
                    && endDate.getMonthOfYear() <= DateTimeConstants.DECEMBER) {
                Integer gut = antrag.getBeantragteTageNetto();
                kontoCurrentYear.setVacationDays(gut + kontoCurrentYear.getVacationDays());
            }

            // Urlaub vor dem 1.4. bzw. evtl. über 1.4.
            // es muss geprüft werden, ob der Urlaub über den 1.4. läuft

            // wenn er über den 1.4. läuft
            // wird es ärgerlich kompliziert....
            if (startDate.getMonthOfYear() <= DateTimeConstants.MARCH
                    && endDate.getMonthOfYear() >= DateTimeConstants.APRIL) {
                Integer beforeApr = dateService.countDaysBetweenTwoDates(startDate,
                        new DateMidnight(startDate.getYear(), DateTimeConstants.MARCH, LAST_DAY));
                Integer afterApr = dateService.countDaysBetweenTwoDates(new DateMidnight(startDate.getYear(),
                            DateTimeConstants.APRIL, FIRST_DAY), endDate);

                // erstmal die nach April Tage (afterApr) aufs Urlaubskonto füllen
                // konto1.setVacationDays(konto1.getVacationDays() + afterApr);

                Integer newVacationDays = kontoCurrentYear.getVacationDays() + afterApr;

                // wenn so das Urlaubskonto gleich dem Anspruch ist,
                // setze die Tage vor dem April (beforeApr) ins Resturlaubkonto (das sowieso ab 1.4. verfällt)
                if (newVacationDays.equals(anspruchCurrentYear.getVacationDays())) {
                    kontoCurrentYear.setRestVacationDays(kontoCurrentYear.getRestVacationDays() + beforeApr);
                    kontoCurrentYear.setVacationDays(newVacationDays);
                } else if (kontoCurrentYear.getVacationDays() < anspruchCurrentYear.getVacationDays()) {
                    // wenn das Auffüllen mit afterApr das Konto noch nicht zum Überlaufen gebracht hat
                    // d.h. urlaubskonto < anspruch
                    // addiere beforeApr dazu
                    // konto1.setVacationDays(konto1.getVacationDays() + beforeApr);
                    newVacationDays = newVacationDays + beforeApr;

                    // wenn so das urlaubskonto überquillt
                    // d.h. urlaubskonto > anspruch
                    if (newVacationDays >= anspruchCurrentYear.getVacationDays()) {
                        // schmeiss den überhängenden scheiss in den resturlaub
                        kontoCurrentYear.setRestVacationDays(kontoCurrentYear.getRestVacationDays()
                            + (newVacationDays - anspruchCurrentYear.getVacationDays()));
                        kontoCurrentYear.setVacationDays(anspruchCurrentYear.getVacationDays());
                    } else {
                        kontoCurrentYear.setVacationDays(newVacationDays);
                    }
                }
            } else if (endDate.getMonthOfYear() < DateTimeConstants.APRIL) {
                // Urlaub ist nach dem 1.1., aber vor dem 1.4., d.h. keine Berührung mit dem 1.4. ganz normal Konto und
                // evtl. Resturlaub füllen

                Integer gut = antrag.getBeantragteTageNetto() + kontoCurrentYear.getVacationDays();
                kontoCurrentYear.setVacationDays(gut + kontoCurrentYear.getVacationDays());

                if (gut > anspruchCurrentYear.getVacationDays()) {
                    kontoCurrentYear.setRestVacationDays(kontoCurrentYear.getRestVacationDays()
                        + (gut - anspruchCurrentYear.getVacationDays()));
                    gut = anspruchCurrentYear.getVacationDays();
                }

                kontoCurrentYear.setVacationDays(gut);
            }

            kontoService.saveUrlaubskonto(kontoCurrentYear);
        }

        if (antrag.getStatus() == State.WARTEND) {
            antrag.setStatus(State.STORNIERT);
            antragDAO.save(antrag);

            // wenn Antrag wartend war, bekommen Chefs die Email
            mailService.sendCanceledNotification(antrag, EmailAdr.CHEFS.getEmail());
        } else if (antrag.getStatus() == State.GENEHMIGT) {
            antrag.setStatus(State.STORNIERT);
            antragDAO.save(antrag);

            // wenn Antrag genehmigt war, bekommt Office die Email
            mailService.sendCanceledNotification(antrag, EmailAdr.OFFICE.getEmail());
        }

        personService.save(person);
    }


    /**
     * @see  AntragService#getRequestById(java.lang.Integer)
     */
    @Override
    public Antrag getRequestById(Integer id) {

        return antragDAO.findOne(id);
    }


    /**
     * @see  AntragService#getAllRequestsForPerson(org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public List<Antrag> getAllRequestsForPerson(Person person) {

        return antragDAO.getAllRequestsForPerson(person);
    }


    /**
     * @see  AntragService#getAllRequests()
     */
    @Override
    public List<Antrag> getAllRequests() {

        return antragDAO.findAll();
    }


    /**
     * @see  AntragService#getAllRequestsByState(org.synyx.urlaubsverwaltung.domain.State)
     */
    @Override
    public List<Antrag> getAllRequestsByState(State state) {

        return antragDAO.getAllRequestsByState(state);
    }


    /**
     * @see  AntragService#getAllRequestsForACertainTime(org.joda.time.DateMidnight, org.joda.time.DateMidnight)
     */
    @Override
    public List<Antrag> getAllRequestsForACertainTime(DateMidnight startDate, DateMidnight endDate) {

        return antragDAO.getAllRequestsForACertainTime(startDate, endDate);
    }


    // Hab mal eine einfache Form davon implementiert
    // soll heissen Konto wird immer aufgefüllt, bei Bedarf auch Resturlaub
    // bloss nach dem 1.4. füllt man den Resturlaub nicht auf, da's ja keinen gibt
    /**
     * @see  AntragService#krankheitBeachten(org.synyx.urlaubsverwaltung.domain.Antrag, java.lang.Integer)
     */
    @Override
    public void krankheitBeachten(Antrag antrag, Integer krankheitsTage) {

        antrag.setKrankheitsTage(krankheitsTage);
        antrag.setBeantragteTageNetto(antrag.getBeantragteTageNetto() - krankheitsTage);

        Person person = antrag.getPerson();

        Urlaubskonto konto = kontoService.getUrlaubskonto(antrag.getStartDate().getYear(), person);
        Urlaubsanspruch anspruch = kontoService.getUrlaubsanspruch(antrag.getStartDate().getYear(), person);

        // konto.setVacationDays(krankheitsTage + konto.getVacationDays());
        Integer newVacDays = krankheitsTage + konto.getVacationDays();

        if (newVacDays > anspruch.getVacationDays()) {
            // wenn es vor April ist, wird Konto voll gemacht und Resturlaub auch gefüllt
            if (antrag.getEndDate().getMonthOfYear() < DateTimeConstants.APRIL) {
                konto.setRestVacationDays(newVacDays - anspruch.getVacationDays());

                // konto.setVacationDays(anspruch.getVacationDays());
                newVacDays = anspruch.getVacationDays();
            }

            // wenn es nach April ist, hat der Mensch halt Pech gehabt
            // kriegt nur Konto bis zum Ende befüllt, aber Resturlaub gibbets nicht
            if (antrag.getEndDate().getMonthOfYear() >= DateTimeConstants.APRIL) {
                konto.setVacationDays(anspruch.getVacationDays());
            }
        }

        konto.setVacationDays(newVacDays);

        antragDAO.save(antrag);
        personService.save(person);
    }


    /**
     * @see  AntragService#signAntrag(org.synyx.urlaubsverwaltung.domain.Antrag,org.synyx.urlaubsverwaltung.domain.Person,
     *       boolean)
     */
    @Override
    public void signAntrag(Antrag antrag, Person signierendePerson, boolean isBoss) throws NoSuchAlgorithmException,
        InvalidKeySpecException {

        PrivateKey privKey = pgpService.getPrivateKeyByBytes(signierendePerson.getPrivateKey());

        StringBuffer buf = new StringBuffer();

        buf.append(antrag.getPerson().getLastName());
        buf.append(antrag.getAntragsDate().toString());
        buf.append(antrag.getVacationType().toString());

        byte[] data = buf.toString().getBytes();

        data = pgpService.sign(privKey, data);

        if (isBoss) {
            antrag.setSignedDataBoss(data);
        } else {
            antrag.setSignedDataPerson(data);
        }

        antragDAO.save(antrag);
    }
}
