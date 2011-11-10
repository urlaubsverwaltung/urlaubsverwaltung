package org.synyx.urlaubsverwaltung.calendar;

/**
 * dies ist ein vorläufiger versuch den google calendar einzubinden die methoden wurden dem googlecalendarserviceimpl
 * des ressourcenplanungstools von Otto Allmendinger - allmendinger@synyx.de entnommen für das urlaubsverwaltungstool
 * nicht nötige methoden und attribute wurden auskommentiert
 */

public class GoogleCalendarSynchronisationException extends RuntimeException {

    private static final long serialVersionUID = -4372833923622616270L;

    public GoogleCalendarSynchronisationException(Exception e) {

        super("could not synchronize event entries", e);
    }


    public GoogleCalendarSynchronisationException(String message) {

        super(message);
    }
}
