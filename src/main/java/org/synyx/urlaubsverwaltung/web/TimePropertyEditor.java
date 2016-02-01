
package org.synyx.urlaubsverwaltung.web;

import java.beans.PropertyEditorSupport;

import java.sql.Time;


/**
 * Converts a {@link String} to {@link Time} and vice versa.
 *
 * @author  Marc Sommer - sommer@synyx.de
 */
public class TimePropertyEditor extends PropertyEditorSupport {

    // Time to String
    @Override
    public String getAsText() {

        if (this.getValue() == null) {
            return "";
        }

        String text = this.getValue().toString();

        String[] timeParts = text.split(":");

        if (timeParts.length == 3) {
            text = timeParts[0] + ':' + timeParts[1];
        }

        return text;
    }


    // String to Time
    @Override
    public void setAsText(String text) {

        if (text == null || text.length() < 1) {
            this.setValue(null);
        } else {
            if (text.split(":").length == 2) {
                text = text.concat(":00");
            }

            Time time = Time.valueOf(text);
            this.setValue(time);
        }
    }
}
