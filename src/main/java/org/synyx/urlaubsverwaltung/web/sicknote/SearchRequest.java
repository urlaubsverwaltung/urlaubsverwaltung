package org.synyx.urlaubsverwaltung.web.sicknote;

/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SearchRequest {

    private Period period;

    public enum Period {
        YEAR,
        QUARTAL,
        MONTH;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }
}
