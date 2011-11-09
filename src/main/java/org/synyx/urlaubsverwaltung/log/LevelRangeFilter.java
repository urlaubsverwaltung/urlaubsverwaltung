/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.log;

import org.apache.log4j.Level;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;


/**
 * @author  aljona
 */
public class LevelRangeFilter extends Filter {

    boolean acceptOnMatch = false;

    Level levelMin;
    Level levelMax;

    @Override
    public int decide(LoggingEvent event) {

        if (this.levelMin != null) {
            if (event.getLevel().isGreaterOrEqual(levelMin) == false) {
                // event level is less than minimum --> deny
                return Filter.DENY;
            }
        }

        if (this.levelMax != null) {
            if (event.getLevel().toInt() > levelMax.toInt()) {
                // event level is greater than maximum --> deny
                // toInt is used because there is no Level.isGreater method
                return Filter.DENY;
            }
        }

        if (acceptOnMatch) {
            // return accept if level in range
            return Filter.ACCEPT;
        } else {
            // event is ok for this filter; allow later filters to have a look..
            return Filter.DENY;
        }
    }


    public Level getLevelMax() {

        return levelMax;
    }


    public Level getLevelMin() {

        return levelMin;
    }


    public boolean getAcceptOnMatch() {

        return acceptOnMatch;
    }


    public void setLevelMax(Level levelMax) {

        this.levelMax = levelMax;
    }


    public void setLevelMin(Level levelMin) {

        this.levelMin = levelMin;
    }


    public void setAcceptOnMatch(boolean acceptOnMatch) {

        this.acceptOnMatch = acceptOnMatch;
    }
}
