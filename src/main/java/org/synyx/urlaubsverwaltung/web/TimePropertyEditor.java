/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.web;

import java.beans.PropertyEditorSupport;

import java.sql.Time;


public class TimePropertyEditor extends PropertyEditorSupport {

    public TimePropertyEditor() {
    }

    // Time to String
    @Override
    public String getAsText() {

        if (this.getValue() == null) {
            return "";
        }

        return this.getValue().toString();
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
