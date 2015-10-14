package org.synyx.urlaubsverwaltung.web.statistics;

import org.synyx.urlaubsverwaltung.core.person.GravatarUtil;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.math.BigDecimal;


/**
 * Encapsulates information about a person and the corresponding vacation days information (how many applications for
 * leave are waiting, how many are allowed, how many vacation days has the person left for using).
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ApplicationForLeaveStatistics {

    private final Person person;
    private final String gravatarUrl;

    private BigDecimal waitingVacationDays = BigDecimal.ZERO;
    private BigDecimal allowedVacationDays = BigDecimal.ZERO;
    private BigDecimal leftVacationDays = BigDecimal.ZERO;

    public ApplicationForLeaveStatistics(Person person) {

        this.person = person;
        this.gravatarUrl = GravatarUtil.createImgURL(person.getEmail());
    }

    public Person getPerson() {

        return person;
    }


    public String getGravatarUrl() {

        return gravatarUrl;
    }


    public BigDecimal getWaitingVacationDays() {

        return waitingVacationDays;
    }


    public BigDecimal getAllowedVacationDays() {

        return allowedVacationDays;
    }


    public BigDecimal getLeftVacationDays() {

        return leftVacationDays;
    }


    public void setWaitingVacationDays(BigDecimal waitingVacationDays) {

        this.waitingVacationDays = waitingVacationDays;
    }


    public void setAllowedVacationDays(BigDecimal allowedVacationDays) {

        this.allowedVacationDays = allowedVacationDays;
    }


    public void setLeftVacationDays(BigDecimal leftVacationDays) {

        this.leftVacationDays = leftVacationDays;
    }
}
