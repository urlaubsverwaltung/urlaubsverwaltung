package org.synyx.urlaubsverwaltung.web.account;

import lombok.Data;
import org.joda.time.DateMidnight;
import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * @author Aljona Murygina - murygina@synyx.de
 */
@Data
class AccountForm {

    private int holidaysAccountYear;

    private DateMidnight holidaysAccountValidFrom;

    private DateMidnight holidaysAccountValidTo;

    private BigDecimal annualVacationDays;

    private BigDecimal actualVacationDays;

    private BigDecimal remainingVacationDays;

    private BigDecimal remainingVacationDaysNotExpiring;

    private String comment;

    AccountForm(int year) {

        this.holidaysAccountYear = year;
        this.holidaysAccountValidFrom = DateUtil.getFirstDayOfYear(year);
        this.holidaysAccountValidTo = DateUtil.getLastDayOfYear(year);
    }

    AccountForm(int year, Optional<Account> holidaysAccountOptional) {

        if (holidaysAccountOptional.isPresent()) {
            Account holidaysAccount = holidaysAccountOptional.get();

            this.holidaysAccountYear = holidaysAccount.getValidFrom().getYear();
            this.holidaysAccountValidFrom = holidaysAccount.getValidFrom();
            this.holidaysAccountValidTo = holidaysAccount.getValidTo();
            this.annualVacationDays = holidaysAccount.getAnnualVacationDays();
            this.actualVacationDays = holidaysAccount.getVacationDays();
            this.remainingVacationDays = holidaysAccount.getRemainingVacationDays();
            this.remainingVacationDaysNotExpiring = holidaysAccount.getRemainingVacationDaysNotExpiring();
            this.comment = holidaysAccount.getComment();
        } else {
            this.holidaysAccountYear = year;
            this.holidaysAccountValidFrom = DateUtil.getFirstDayOfYear(year);
            this.holidaysAccountValidTo = DateUtil.getLastDayOfYear(year);
        }
    }
}
