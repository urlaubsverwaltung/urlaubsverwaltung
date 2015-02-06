package org.synyx.urlaubsverwaltung.web.sicknote;

/**
 * Represents a request to filter something by {@link org.synyx.urlaubsverwaltung.web.sicknote.FilterRequest.Period}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class FilterRequest {

    public enum Period {

        YEAR,
        QUARTAL,
        MONTH
    }

    private Period period;

    public Period getPeriod() {

        return period;
    }


    public void setPeriod(Period period) {

        this.period = period;
    }
}
