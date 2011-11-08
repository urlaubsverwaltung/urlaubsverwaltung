/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.util;

import org.apache.log4j.Level;


/**
 * @author  aljona
 */
public class CustomLevel extends Level {

    public static final int VORGANG_INT = Level.INFO_INT + 1;
    private static final String VORGANG_MSG = "VORGANG";
    public static final Level VORGANG = new CustomLevel(VORGANG_INT, VORGANG_MSG, 7);

    /**
     * Default constructor
     *
     * @param  VORGANG_INT
     * @param  string
     * @param  i
     */
    protected CustomLevel(int VORGANG_INT, String string, int i) {

        super(VORGANG_INT, string, i);
    }

    public static Level toLevel(String sArg) {

        if (sArg != null && sArg.toUpperCase().equals(VORGANG_MSG)) {
            return VORGANG;
        }

        return null;
    }


    public static Level toLevel(int val) {

        if (val == VORGANG_INT) {
            return VORGANG;
        }

        return null;
    }
}
