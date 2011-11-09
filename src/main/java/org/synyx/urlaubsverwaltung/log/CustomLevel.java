/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.log;

import org.apache.log4j.Level;


/**
 * @author  aljona
 */
public class CustomLevel extends Level {

    public static final int VORGANG_INT = (FATAL_INT + 1);
    private static final String VORGANG_MSG = "VORGANG";
    public static final Level VORGANG = new CustomLevel(VORGANG_INT, VORGANG_MSG, 7);

    protected CustomLevel(int arg0, String arg1, int arg2) {

        super(arg0, arg1, arg2);
    }

    public static Level toLevel(String sArg) {

        if (sArg != null && sArg.toUpperCase().equals(VORGANG_MSG)) {
            return VORGANG;
        }

        return (Level) toLevel(sArg, Level.DEBUG);
    }


    public static Level toLevel(int val) {

        if (val == VORGANG_INT) {
            return VORGANG;
        }

        return (Level) toLevel(val, Level.DEBUG);
    }


    public static Level toLevel(int val, Level defaultLevel) {

        if (val == VORGANG_INT) {
            return VORGANG;
        }

        return Level.toLevel(val, defaultLevel);
    }


    public static Level toLevel(String sArg, Level defaultLevel) {

        if (sArg != null && sArg.toUpperCase().equals(VORGANG_MSG)) {
            return VORGANG;
        }

        return Level.toLevel(sArg, defaultLevel);
    }
}
