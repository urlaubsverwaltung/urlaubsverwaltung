package org.synyx.urlaubsverwaltung.web;

/**
 * Represents a request to filter something by {@link FilterRequest.Period}.
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
