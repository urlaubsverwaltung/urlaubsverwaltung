/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.domain;

/**
 * @author  aljona
 */
public enum Length {

    GANZTAGS("ganz"),
    MORGENS("morgens"),
    MITTAGS("mittags");

    private String dayLength;

    private Length(String dayLength) {

        this.dayLength = dayLength;
    }

    public String getDayLength() {

        return this.dayLength;
    }
}
