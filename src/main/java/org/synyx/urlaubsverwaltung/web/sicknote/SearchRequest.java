package org.synyx.urlaubsverwaltung.web.sicknote;

/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SearchRequest {

    private Integer personId;

    private Period period;

    public enum Period {
        YEAR,
        QUARTAL,
        MONTH;
    }

    public Integer getPersonId() {

        return personId;
    }

    public void setPersonId(Integer personId) {

        this.personId = personId;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }
}
