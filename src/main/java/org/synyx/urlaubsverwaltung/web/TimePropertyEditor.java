package org.synyx.urlaubsverwaltung.web;

import java.beans.PropertyEditorSupport;
import java.sql.Time;


/**
 * Converts a {@link String} to {@link Time} and vice versa.
 */
public class TimePropertyEditor extends PropertyEditorSupport {

    private static final String SECONDS = ":00"; // NOSONAR - dear Sonar, this is really not an IP address
    private static final String TIME_SEPARATOR = ":";
    private static final int TWO_DIGIT = 2;
    private static final int THREE_DIGIT = 3;

    // Time to String
    @Override
    public String getAsText() {

        if (this.getValue() == null) {
            return "";
        }

        String text = this.getValue().toString();

        String[] timeParts = text.split(TIME_SEPARATOR);

        if (timeParts.length == THREE_DIGIT) {
            return timeParts[0] + TIME_SEPARATOR + timeParts[1];
        }

        return text;
    }


    // String to Time
    @Override
    public void setAsText(String text) {

        if (text == null || text.isEmpty()) {
            this.setValue(null);
        } else {
            String timeAsString = text;
            String[] timeParts = text.split(TIME_SEPARATOR);

            if (timeParts.length == TWO_DIGIT) {
                timeAsString = timeAsString.concat(SECONDS);
            }

            Time time = Time.valueOf(timeAsString);
            this.setValue(time);
        }
    }
}
