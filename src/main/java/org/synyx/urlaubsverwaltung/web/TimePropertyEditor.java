
package org.synyx.urlaubsverwaltung.web;

import java.beans.PropertyEditorSupport;

import java.sql.Time;


/**
 * Converts a {@link String} to {@link Time} and vice versa.
 *
 * @author  Marc Sommer - sommer@synyx.de
 */
public class TimePropertyEditor extends PropertyEditorSupport {

    private static final String SECONDS = ":00";
    private static final String TIME_SEPARATOR = ":";

    // Time to String
    @Override
    public String getAsText() {

        if (this.getValue() == null) {
            return "";
        }

        String text = this.getValue().toString();

        String[] timeParts = text.split(TIME_SEPARATOR);

        if (timeParts.length == 3) {
            return timeParts[0] + TIME_SEPARATOR + timeParts[1];
        }

        return text;
    }


    // String to Time
    @Override
    public void setAsText(String text) {

        if (text == null || text.length() < 1) {
            this.setValue(null);
        } else {
            if (text.split(TIME_SEPARATOR).length == 2) {
                text = text.concat(SECONDS);
            }

            Time time = Time.valueOf(text);
            this.setValue(time);
        }
    }
}
