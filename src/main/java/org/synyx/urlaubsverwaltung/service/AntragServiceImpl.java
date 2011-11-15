package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.dao.AntragDAO;
import org.synyx.urlaubsverwaltung.dao.PersonDAO;
import org.synyx.urlaubsverwaltung.dao.UrlaubsanspruchDAO;
import org.synyx.urlaubsverwaltung.dao.UrlaubskontoDAO;
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

    private AntragDAO antragDAO;
    private PersonDAO personDAO;
    private UrlaubsanspruchDAO urlaubsanspruchDAO;
    private UrlaubskontoDAO urlaubskontoDAO;
    private KontoService kontoService;
    private DateService dateService;
    private PGPService pgpService;

    // wird hier und im anderen service benötigt, weil wir ja
    // ständig irgendwelche mails schicken müssen... =)
    private MailServiceImpl mailService;

    @Autowired
    public AntragServiceImpl(AntragDAO antragDAO, PersonDAO personDAO, UrlaubsanspruchDAO urlaubsanspruchDAO,
        UrlaubskontoDAO urlaubskontoDAO, KontoService kontoService, DateService dateService, PGPService pgpService,
        MailServiceImpl mailService) {

        this.antragDAO = antragDAO;
        this.personDAO = personDAO;
        this.urlaubsanspruchDAO = urlaubsanspruchDAO;
        this.urlaubskontoDAO = urlaubskontoDAO;
        this.kontoService = kontoService;
        this.dateService = dateService;
        this.pgpService = pgpService;
        this.mailService = mailService;
    }

    /**
     * @see  AntragService#save(org.synyx.urlaubsverwaltung.domain.Antrag)
     */
    @Override
    public void save(Antrag antrag) {

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

        Kommentar comment = new Kommentar();
        comment.setText(reasonToDecline);
        comment.setPerson(boss);
        comment.setDatum(DateMidnight.now()); // weiss nicht, ob das stimmt mit dem Datum

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


    // Beachte, dass Berechnung von wie viele Tage noch nicht stimmt
    // Dazu muss Google Kalender benutzt werden, da ja Feiertage dazwischen liegen können

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

        // ein Urlaubskonto und ein Urlaubsanspruch hat Mensch auf jeden Fall
        // bei unterschiedlichen Jahren (= Sonderfall) wird ein zweites Urlaubskonto und -anspruch geholt
        Urlaubskonto konto1 = urlaubskontoDAO.getUrlaubskontoForDateAndPerson(startDate.getYear(), person);
        Urlaubsanspruch anspruch1 = urlaubsanspruchDAO.getUrlaubsanspruchByDate(startDate.getYear(), person);

        // Liegen Start- und Enddatum nicht im gleichen Jahr, läuft der Urlaub über den 1.1.
        if (startDate.getYear() != endDate.getYear()) {
            Integer beforeJan = dateService.countDaysBetweenTwoDates(startDate,
                    new DateMidnight(startDate.getYear(), 12, 31));
            Integer afterJan = dateService.countDaysBetweenTwoDates(new DateMidnight(endDate.getYear(), 1, 1), endDate);

            // Urlaubskonto für Jahr 2 (vom endDate)
            Urlaubskonto konto2 = urlaubskontoDAO.getUrlaubskontoForDateAndPerson(endDate.getYear(), person);

            // Urlaubsanspruch für Jahr 2 (vom endDate)
            Urlaubsanspruch anspruch2 = urlaubsanspruchDAO.getUrlaubsanspruchByDate(endDate.getYear(), person);

            // beforeJan füllt Urlaubskonto von Jahr 1 auf
            konto1.setVacationDays(konto1.getVacationDays() + beforeJan);

            // wenn Urlaubskonto Anspruch überschreitet, muss stattdessen Resturlaub aufgefüllt werden
            if (konto1.getVacationDays() > anspruch1.getVacationDays()) {
                konto1.setRestVacationDays(konto1.getRestVacationDays()
                    + (konto1.getVacationDays() - anspruch1.getVacationDays()));
                konto1.setVacationDays(konto1.getVacationDays()
                    - (konto1.getVacationDays() - anspruch1.getVacationDays()));
            }

            // afterJan füllt Urlaubskonto von Jahr 2 auf
            konto2.setVacationDays(konto2.getVacationDays() + afterJan);

            // wenn Urlaubskonto Anspruch überschreitet, muss stattdessen Resturlaub aufgefüllt werden
            if (konto2.getVacationDays() > anspruch2.getVacationDays()) {
                konto1.setRestVacationDays(konto2.getRestVacationDays()
                    + (konto2.getVacationDays() - anspruch2.getVacationDays()));
                konto2.setVacationDays(konto2.getVacationDays()
                    - (konto2.getVacationDays() - anspruch2.getVacationDays()));
            }

            kontoService.saveUrlaubskonto(konto2);
        } else {
            // Urlaub nicht über 1.1., aber auch nicht über 1.4.
            // Urlaub nur nach dem 1.4., d.h. kein Resturlaub zu berechnen, da kein Resturlaub mehr vorhanden
            // Berechnung also easy
            if (startDate.getMonthOfYear() >= 4 && endDate.getMonthOfYear() <= 12) {
                Integer gut = antrag.getBeantragteTageNetto();
                konto1.setVacationDays(gut + konto1.getVacationDays());
            }

            // Urlaub vor dem 1.4. bzw. evtl. über 1.4.
            // es muss geprüft werden, ob der Urlaub über den 1.4. läuft

            // wenn er über den 1.4. läuft
            // wird es ärgerlich kompliziert....
            if (startDate.getMonthOfYear() <= 3 && endDate.getMonthOfYear() >= 4) {
                Integer beforeApr = dateService.countDaysBetweenTwoDates(startDate,
                        new DateMidnight(startDate.getYear(), 3, 31));
                Integer afterApr = dateService.countDaysBetweenTwoDates(new DateMidnight(startDate.getYear(), 4, 1),
                        endDate);

                // erstmal die nach April Tage (afterApr) aufs Urlaubskonto füllen
                konto1.setVacationDays(konto1.getVacationDays() + afterApr);

                // wenn so das Urlaubskonto gleich dem Anspruch ist,
                // setze die Tage vor dem April (beforeApr) ins Resturlaubkonto (das sowieso ab 1.4. verfällt)
                if (konto1.getVacationDays().equals(anspruch1.getVacationDays())) {
                    konto1.setRestVacationDays(konto1.getRestVacationDays() + beforeApr);
                } else if (konto1.getVacationDays() < anspruch1.getVacationDays()) {
                    // wenn das Auffüllen mit afterApr das Konto noch nicht zum Überlaufen gebracht hat
                    // d.h. urlaubskonto < anspruch
                    // addiere beforeApr dazu
                    konto1.setVacationDays(konto1.getVacationDays() + beforeApr);

                    // wenn so das urlaubskonto überquillt
                    // d.h. urlaubskonto > anspruch
                    if (konto1.getVacationDays() >= anspruch1.getVacationDays()) {
                        // schmeiss den überhängenden scheiss in den resturlaub
                        konto1.setRestVacationDays(konto1.getRestVacationDays()
                            + (konto1.getVacationDays() - anspruch1.getVacationDays()));
                        konto1.setVacationDays(anspruch1.getVacationDays());
                    }
                }
            } else if (endDate.getMonthOfYear() < 4) {
                // Urlaub ist nach dem 1.1., aber vor dem 1.4., d.h. keine Berührung mit dem 1.4. ganz normal Konto und
                // evtl. Resturlaub füllen

                Integer gut = antrag.getBeantragteTageNetto();
                konto1.setVacationDays(gut + konto1.getVacationDays());

                if (konto1.getVacationDays() > anspruch1.getVacationDays()) {
                    konto1.setRestVacationDays(konto1.getRestVacationDays()
                        + (konto1.getVacationDays() - anspruch1.getVacationDays()));
                    konto1.setVacationDays(anspruch1.getVacationDays());
                }
            }

            kontoService.saveUrlaubskonto(konto1);
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

        personDAO.save(person);
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

        antragDAO.getAllRequestsByState(state);

        return null;
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

        konto.setVacationDays(krankheitsTage + konto.getVacationDays());

        if (konto.getVacationDays() > anspruch.getVacationDays()) {
            // wenn es vor April ist, wird Konto voll gemacht und Resturlaub auch gefüllt
            if (antrag.getEndDate().getMonthOfYear() < 4) {
                konto.setRestVacationDays(konto.getVacationDays() - anspruch.getVacationDays());
                konto.setVacationDays(anspruch.getVacationDays());
            }

            // wenn es nach April ist, hat der Mensch halt Pech gehabt
            // kriegt nur Konto bis zum Ende befüllt, aber Resturlaub gibbets nicht
            if (antrag.getEndDate().getMonthOfYear() >= 4) {
                konto.setVacationDays(anspruch.getVacationDays());
            }
        }

        antragDAO.save(antrag);
        personDAO.save(person);
    }


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
