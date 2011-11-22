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
    private PersonService personService;
    private MailServiceImpl mailService;

    @Autowired
    public AntragServiceImpl(AntragDAO antragDAO, KontoService kontoService, PGPService pgpService,
        OwnCalendarService calendarService, PersonService personService, MailServiceImpl mailService) {

        this.antragDAO = antragDAO;
        this.kontoService = kontoService;
        this.pgpService = pgpService;
        this.calendarService = calendarService;
        this.personService = personService;
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
                kontoService.newUrlaubskonto(person,
                    kontoService.getUrlaubsanspruch(start.getYear(), person).getVacationDays(), 0.0, start.getYear());
            }

            // beachte die Sonderfaelle, die April mit sich bringt
            noticeApril(antrag, konto, start, end);

            // dann speichere
            kontoService.saveUrlaubskonto(konto);
        } else {
            noticeJanuary(antrag, start, end, true);
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

        // Urlaubskonten muessen aktualisiert werden
        // was ist der Zeitraum des Urlaubs
        DateMidnight start = antrag.getStartDate();
        DateMidnight end = antrag.getEndDate();

        // Liegen Start- und Enddatum nicht im gleichen Jahr, läuft der Urlaub über den 1.1.
        if (start.getYear() != end.getYear()) {
            rollbackNoticeJanuary(antrag, start, end);
        } else {
            // Es muss der 1.4. beachtet werden
            rollbackNoticeApril(antrag, start, end);
        }

        antragDAO.save(antrag);

        mailService.sendDeclinedNotification(antrag);
    }


    /**
     * @see  AntragService#storno(org.synyx.urlaubsverwaltung.domain.Antrag)
     */
    @Override
    public void storno(Antrag antrag) {

        // was ist der Zeitraum des Urlaubs
        DateMidnight start = antrag.getStartDate();
        DateMidnight end = antrag.getEndDate();

        // Liegen Start- und Enddatum nicht im gleichen Jahr, läuft der Urlaub über den 1.1.
        if (start.getYear() != end.getYear()) {
            rollbackNoticeJanuary(antrag, start, end);
        } else {
            // Es muss der 1.4. beachtet werden
            rollbackNoticeApril(antrag, start, end);
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
    public void rollbackNoticeJanuary(Antrag antrag, DateMidnight start, DateMidnight end) {

        Person person = antrag.getPerson();

        Urlaubskonto kontoCurrentYear = kontoService.getUrlaubskonto(start.getYear(), person);
        Urlaubsanspruch anspruchCurrentYear = kontoService.getUrlaubsanspruch(start.getYear(), person);

        Urlaubskonto kontoNextYear = kontoService.getUrlaubskonto(end.getYear(), person);
        Urlaubsanspruch anspruchNextYear = kontoService.getUrlaubsanspruch(end.getYear(), person);

        Double beforeJan = calendarService.getVacationDays(start,
                new DateMidnight(start.getYear(), DateTimeConstants.DECEMBER, LAST_DAY));
        Double afterJan = calendarService.getVacationDays(new DateMidnight(end.getYear(), DateTimeConstants.JANUARY,
                    FIRST_DAY), end);

        Double oldVacationDays = kontoCurrentYear.getVacationDays() + beforeJan;

        Double newVacationDays = 0.0;

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

        kontoService.saveUrlaubskonto(kontoCurrentYear);
        kontoService.saveUrlaubskonto(kontoNextYear);
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
    public void rollbackNoticeApril(Antrag antrag, DateMidnight start, DateMidnight end) {

        Person person = antrag.getPerson();

        Urlaubskonto kontoCurrentYear = kontoService.getUrlaubskonto(start.getYear(), person);
        Urlaubsanspruch anspruchCurrentYear = kontoService.getUrlaubsanspruch(start.getYear(), person);

        // Urlaub nach dem 1.4., d.h. kein Resturlaub zu berechnen, da kein Resturlaub mehr vorhanden
        // keine Beruehrung mit der Problematik vom 1.4.
        if (start.getMonthOfYear() >= DateTimeConstants.APRIL) {
            Double gut = calendarService.getVacationDays(start, end);
            kontoCurrentYear.setVacationDays(gut + kontoCurrentYear.getVacationDays());
        } else if (end.getMonthOfYear() < DateTimeConstants.APRIL) {
            // Urlaub endet vor dem 1.4., d.h. keine Beruehrung mit der Problematik vom 1.4.
            Double gut = calendarService.getVacationDays(start, end) + kontoCurrentYear.getVacationDays();

            if (gut > anspruchCurrentYear.getVacationDays()) {
                kontoCurrentYear.setRestVacationDays(kontoCurrentYear.getRestVacationDays()
                    + (gut - anspruchCurrentYear.getVacationDays()));
                kontoCurrentYear.setVacationDays(anspruchCurrentYear.getVacationDays());
            } else if (gut <= anspruchCurrentYear.getVacationDays()) {
                kontoCurrentYear.setVacationDays(gut);
            }
        } else if (start.getMonthOfYear() <= DateTimeConstants.MARCH
                && end.getMonthOfYear() >= DateTimeConstants.APRIL) {
            // Urlaub laeuft ueber den 1.4.
            Double beforeApr = calendarService.getVacationDays(start,
                    new DateMidnight(start.getYear(), DateTimeConstants.MARCH, LAST_DAY));
            Double afterApr = calendarService.getVacationDays(new DateMidnight(start.getYear(), DateTimeConstants.APRIL,
                        FIRST_DAY), end);

            // erstmal die nach April Tage (afterApr) aufs Urlaubskonto füllen
            // konto1.setVacationDays(konto1.getVacationDays() + afterApr);

            Double newVacationDays = kontoCurrentYear.getVacationDays() + afterApr;

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
        }

        kontoService.saveUrlaubskonto(kontoCurrentYear);
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
                kontoService.newUrlaubskonto(person,
                    kontoService.getUrlaubsanspruch(end.getYear(), person).getVacationDays(), 0.0, end.getYear());
                konto = kontoService.getUrlaubskonto(end.getYear(), person);
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


    /**
     * @see  AntragService#checkAntrag(org.synyx.urlaubsverwaltung.domain.Antrag)
     */
    @Override
    public boolean checkAntrag(Antrag antrag) {

        Double days;
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
                kontoService.newUrlaubskonto(person,
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
                Double afterApril = calendarService.getVacationDays(new DateMidnight(end.getYear(),
                            DateTimeConstants.APRIL, FIRST_DAY), antrag.getEndDate());

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
        } else {
            // Antrag laeuft ueber 2 jahre

            // hole urlaubskonten
            Urlaubskonto kontoCurrentYear = kontoService.getUrlaubskonto(start.getYear(), person);
            Urlaubskonto kontoNextYear = kontoService.getUrlaubskonto(end.getYear(), person);

            // wenn noch nicht angelegt
            if (kontoCurrentYear == null) {
                // erzeuge konto
                kontoService.newUrlaubskonto(person,
                    kontoService.getUrlaubsanspruch(start.getYear(), person).getVacationDays(), 0.0, start.getYear());
            }

            if (kontoNextYear == null) {
                // erzeuge konto
                kontoService.newUrlaubskonto(person,
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
        }

        return false;
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
    public void noticeJanuary(Antrag antrag, DateMidnight start, DateMidnight end, boolean mustBeSaved) {

        // wenn der antrag über 2 Jahre läuft...
        Double beforeJan = calendarService.getVacationDays(antrag.getStartDate(),
                new DateMidnight(start.getYear(), DateTimeConstants.DECEMBER, LAST_DAY));
        Double afterJan = calendarService.getVacationDays(new DateMidnight(end.getYear(), DateTimeConstants.JANUARY,
                    FIRST_DAY), antrag.getEndDate());
        Urlaubskonto kontoCurrentYear = kontoService.getUrlaubskonto(start.getYear(), antrag.getPerson());
        Urlaubskonto kontoNextYear = kontoService.getUrlaubskonto(end.getYear(), antrag.getPerson());

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

        if (mustBeSaved) {
            kontoService.saveUrlaubskonto(kontoCurrentYear);
            kontoService.saveUrlaubskonto(kontoNextYear);
        }
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
    public void noticeApril(Antrag antrag, Urlaubskonto konto, DateMidnight start, DateMidnight end) {

        // wenn antrag vor april ist
        if (antrag.getEndDate().getMonthOfYear() < DateTimeConstants.APRIL) {
            // errechne, wie viele resturlaubstage übrigbleiben würden
            Double newRestDays = konto.getRestVacationDays() - (calendarService.getVacationDays(start, end));

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
            konto.setVacationDays(konto.getVacationDays() - (calendarService.getVacationDays(start, end)));
        } else {
            // wenn der antrag über april läuft
            Double beforeApril = calendarService.getVacationDays(antrag.getStartDate(),
                    new DateMidnight(start.getYear(), DateTimeConstants.MARCH, LAST_DAY));
            Double afterApril = calendarService.getVacationDays(new DateMidnight(end.getYear(), DateTimeConstants.APRIL,
                        FIRST_DAY), antrag.getEndDate());

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
