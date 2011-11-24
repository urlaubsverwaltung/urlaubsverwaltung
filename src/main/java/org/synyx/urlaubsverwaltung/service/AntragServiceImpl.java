package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.dao.AntragDAO;
import org.synyx.urlaubsverwaltung.domain.Antrag;
import org.synyx.urlaubsverwaltung.domain.Kommentar;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.State;
import org.synyx.urlaubsverwaltung.domain.Urlaubsanspruch;
import org.synyx.urlaubsverwaltung.domain.Urlaubskonto;

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
    private KontoService kontoService;
    private PGPService pgpService;
    private OwnCalendarService calendarService;
    private MailService mailService;

    @Autowired
    public AntragServiceImpl(AntragDAO antragDAO, KontoService kontoService, PGPService pgpService,
        OwnCalendarService calendarService, MailService mailService) {

        this.antragDAO = antragDAO;
        this.kontoService = kontoService;
        this.pgpService = pgpService;
        this.calendarService = calendarService;
        this.mailService = mailService;
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


    /**
     * @see  AntragService#wait(org.synyx.urlaubsverwaltung.domain.Antrag)
     */
    @Override
    public void wait(Antrag antrag) {

        antrag.setStatus(State.WARTEND);
        antragDAO.save(antrag);
    }


    /**
     * @see  AntragService#approve(org.synyx.urlaubsverwaltung.domain.Antrag)
     */
    @Override
    public void approve(Antrag antrag) {

        // status auf genehmigt setzen
        antrag.setStatus(State.GENEHMIGT);

        // verbrauchte netto urlaubstage eintragen
        antrag.setBeantragteTageNetto(calendarService.getVacationDays(antrag.getStartDate(), antrag.getEndDate()));

        antragDAO.save(antrag);

        mailService.sendApprovedNotification(antrag.getPerson(), antrag);
    }


    /**
     * @see  AntragService#save(org.synyx.urlaubsverwaltung.domain.Antrag)
     */
    @Override
    public void save(Antrag antrag) {

        // wird nach erfolgreichem Check eingesetzt

        Person person = antrag.getPerson();
        DateMidnight start = antrag.getStartDate();
        DateMidnight end = antrag.getEndDate();

        // liegen Datumsangaben im gleichen Jahr?
        // wenn sich der antrag nur in einem jahr befindet
        if (start.getYear() == end.getYear()) {
            // hole urlaubskonto
            Urlaubskonto konto = kontoService.getUrlaubskonto(start.getYear(), person);

            // wenn noch nicht angelegt
            if (konto == null) {
                // erzeuge konto
                konto = kontoService.newUrlaubskonto(person,
                    kontoService.getUrlaubsanspruch(start.getYear(), person).getVacationDays(), 0.0, start.getYear());
            }

            // beachte die Sonderfaelle, die April mit sich bringt
            kontoService.noticeApril(antrag, konto);

            // dann speichere
            kontoService.saveUrlaubskonto(konto);
        } else {
            // ueber zwei jahre, d.h. es gibt zwei urlaubskonten

            Urlaubskonto kontoCurrentYear = kontoService.getUrlaubskonto(start.getYear(), antrag.getPerson());
            Urlaubskonto kontoNextYear = kontoService.getUrlaubskonto(end.getYear(), antrag.getPerson());

            kontoService.noticeJanuary(antrag, kontoCurrentYear, kontoNextYear);

            kontoService.saveUrlaubskonto(kontoCurrentYear);
            kontoService.saveUrlaubskonto(kontoNextYear);
        }

        antragDAO.save(antrag);
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

        kontoService.rollbackUrlaub(antrag);

        antragDAO.save(antrag);

        mailService.sendDeclinedNotification(antrag);
    }


    /**
     * @see  AntragService#storno(org.synyx.urlaubsverwaltung.domain.Antrag)
     */
    @Override
    public void storno(Antrag antrag) {

        kontoService.rollbackUrlaub(antrag);

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
    }


    /**
     * @see  AntragService#krankheitBeachten(org.synyx.urlaubsverwaltung.domain.Antrag, java.lang.Integer)
     */
    @Override
    public void krankheitBeachten(Antrag antrag, Double krankheitsTage) {

        antrag.setKrankheitsTage(krankheitsTage);
        antrag.setBeantragteTageNetto(antrag.getBeantragteTageNetto() - krankheitsTage);

        Person person = antrag.getPerson();
        DateMidnight start = antrag.getStartDate();
        DateMidnight end = antrag.getEndDate();

        // wenn nicht im gleichen Jahr
        if (start.getYear() != end.getYear()) {
            Urlaubskonto konto = kontoService.getUrlaubskonto(end.getYear(), person);

            if (konto == null) {
                // erzeuge konto
                konto = kontoService.newUrlaubskonto(person,
                    kontoService.getUrlaubsanspruch(end.getYear(), person).getVacationDays(), 0.0, end.getYear());
            }

            konto.setVacationDays(konto.getVacationDays() + krankheitsTage);

            if (konto.getVacationDays() > kontoService.getUrlaubsanspruch(end.getYear(), person).getVacationDays()) {
                konto.setRestVacationDays(konto.getRestVacationDays()
                    + (konto.getVacationDays()
                        - kontoService.getUrlaubsanspruch(end.getYear(), person).getVacationDays()));
                konto.setVacationDays(kontoService.getUrlaubsanspruch(end.getYear(), person).getVacationDays());
            }
        } else {
            // im gleichen Jahr
            Urlaubskonto konto = kontoService.getUrlaubskonto(start.getYear(), person);
            Urlaubsanspruch anspruch = kontoService.getUrlaubsanspruch(start.getYear(), person);

            Double newVacDays = krankheitsTage + konto.getVacationDays();

            if (newVacDays > anspruch.getVacationDays()) {
                // wenn es vor April ist, wird Konto voll gemacht und Resturlaub auch gefüllt
                if (antrag.getEndDate().getMonthOfYear() < DateTimeConstants.APRIL) {
                    konto.setRestVacationDays(newVacDays - anspruch.getVacationDays());
                    newVacDays = anspruch.getVacationDays();
                }

                // wenn es nach April ist, hat der Mensch halt Pech gehabt
                // kriegt nur Konto bis zum Ende befüllt, aber Resturlaub gibbets nicht
                if (antrag.getEndDate().getMonthOfYear() >= DateTimeConstants.APRIL) {
                    newVacDays = anspruch.getVacationDays();
                }
            }

            konto.setVacationDays(newVacDays);
        }

        antragDAO.save(antrag);
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


    /**
     * @see  AntragService#checkAntrag(org.synyx.urlaubsverwaltung.domain.Antrag)
     */
    @Override
    public boolean checkAntrag(Antrag antrag) {

        DateMidnight start = antrag.getStartDate();
        DateMidnight end = antrag.getEndDate();

        // liegen Datumsangaben im gleichen Jahr?
        // wenn sich der antrag nur in einem jahr befindet
        if (start.getYear() == end.getYear()) {
            return checkAntragOneYear(antrag, start, end);
        } else {
            // Antrag laeuft ueber 2 jahre

            return checkAntragTwoYears(antrag, start, end);
        }
    }


    @Override
    public boolean checkAntragOneYear(Antrag antrag, DateMidnight start, DateMidnight end) {

        Double days;

        Person person = antrag.getPerson();

        // hole urlaubskonto
        Urlaubskonto konto = kontoService.getUrlaubskonto(start.getYear(), person);

        // wenn noch nicht angelegt
        if (konto == null) {
            // erzeuge konto
            konto = kontoService.newUrlaubskonto(person,
                kontoService.getUrlaubsanspruch(start.getYear(), person).getVacationDays(), 0.0, start.getYear());
        }

        // wenn antrag vor april ist
        if (antrag.getEndDate().getMonthOfYear() < DateTimeConstants.APRIL) {
            days = calendarService.getVacationDays(start, end);

            if (((konto.getRestVacationDays() + konto.getVacationDays()) - days) >= 0) {
                return true;
            }
        } else if (antrag.getStartDate().getMonthOfYear() >= DateTimeConstants.APRIL) {
            // wenn der antrag nach april ist, gibt's keinen resturlaub mehr

            days = calendarService.getVacationDays(start, end);

            if ((konto.getVacationDays() - days) >= 0) {
                return true;
            }
        } else {
            // wenn der antrag über april läuft
            Double beforeApril = calendarService.getVacationDays(antrag.getStartDate(),
                    new DateMidnight(start.getYear(), DateTimeConstants.MARCH, LAST_DAY));
            Double afterApril = calendarService.getVacationDays(new DateMidnight(end.getYear(), DateTimeConstants.APRIL,
                        FIRST_DAY), antrag.getEndDate());

            // erstmal beforeApril vom Resturlaub abziehen
            Double zwischenergebnis = konto.getRestVacationDays() - beforeApril;

            // wenn Resturlaub nicht ausreicht, um beforeApril wegzukriegen
            // muss beforeApril noch vom normalen Urlaub abgezogen werden
            // ansonsten eben nicht mehr
            if (zwischenergebnis > 0) {
                beforeApril = -1.0 * (konto.getRestVacationDays() - beforeApril);
            } else {
                beforeApril = 0.0;
            }

            if ((konto.getVacationDays() - (beforeApril + afterApril)) >= 0) {
                return true;
            }
        }

        return false;
    }


    @Override
    public boolean checkAntragTwoYears(Antrag antrag, DateMidnight start, DateMidnight end) {

        Person person = antrag.getPerson();

        // Antrag laeuft ueber 2 jahre

        // hole urlaubskonten
        Urlaubskonto kontoCurrentYear = kontoService.getUrlaubskonto(start.getYear(), person);
        Urlaubskonto kontoNextYear = kontoService.getUrlaubskonto(end.getYear(), person);

        // wenn noch nicht angelegt
        if (kontoCurrentYear == null) {
            // erzeuge konto
            kontoCurrentYear = kontoService.newUrlaubskonto(person,
                kontoService.getUrlaubsanspruch(start.getYear(), person).getVacationDays(), 0.0, start.getYear());
        }

        if (kontoNextYear == null) {
            // erzeuge konto
            kontoNextYear = kontoService.newUrlaubskonto(person,
                kontoService.getUrlaubsanspruch(end.getYear(), person).getVacationDays(), 0.0, end.getYear());
        }

        Double beforeJan = calendarService.getVacationDays(antrag.getStartDate(),
                new DateMidnight(start.getYear(), DateTimeConstants.DECEMBER, LAST_DAY));
        Double afterJan = calendarService.getVacationDays(new DateMidnight(end.getYear(), DateTimeConstants.JANUARY,
                    FIRST_DAY), antrag.getEndDate());

        if (((kontoCurrentYear.getVacationDays() - beforeJan) >= 0)
                && ((kontoNextYear.getVacationDays() + kontoNextYear.getRestVacationDays() - afterJan) >= 0)) {
            return true;
        }

        return false;
    }
}
