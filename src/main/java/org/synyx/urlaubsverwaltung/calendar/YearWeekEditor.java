package org.synyx.urlaubsverwaltung.calendar;

import java.beans.PropertyEditorSupport;


/**
 * dies ist ein vorläufiger versuch den google calendar einzubinden die methoden wurden dem googlecalendarserviceimpl
 * des ressourcenplanungstools von Otto Allmendinger - allmendinger@synyx.de entnommen für das urlaubsverwaltungstool
 * nicht nötige methoden und attribute wurden auskommentiert
 */

public class YearWeekEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) {

        YearWeek yearWeek = new YearWeek(text);
        setValue(yearWeek);
    }
}
