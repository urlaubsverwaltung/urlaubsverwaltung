
package org.synyx.urlaubsverwaltung.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * HolidaysAccount describes how many vacation days and remaining vacation days a person has in which period (validFrom, validTo).
 * 
 * @author Aljona Murygina - murygina@synyx.de
 */
public class HolidaysAccount extends AbstractPersistable<Integer> {
    
    private static final long serialVersionUID = 890434378423784389L;
    
    @ManyToOne
    private Person person;
    
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date validFrom;
    
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date validTo;
    
    private BigDecimal vacationDays;
    
    private BigDecimal remainingVacationDays;
    
    // if true: remaining vacation days expire on 1st Apr.
    // if false: remaining vacation days don't expire and may be used even after Apr. (until Dec.)
    private boolean remainingVacationDaysExpire;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public BigDecimal getRemainingVacationDays() {
        return remainingVacationDays;
    }

    public void setRemainingVacationDays(BigDecimal remainingVacationDays) {
        this.remainingVacationDays = remainingVacationDays;
    }

    public boolean isRemainingVacationDaysExpire() {
        return remainingVacationDaysExpire;
    }

    public void setRemainingVacationDaysExpire(boolean remainingVacationDaysExpire) {
        this.remainingVacationDaysExpire = remainingVacationDaysExpire;
    }

    public BigDecimal getVacationDays() {
        return vacationDays;
    }

    public void setVacationDays(BigDecimal vacationDays) {
        this.vacationDays = vacationDays;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = validTo;
    }
    
}
