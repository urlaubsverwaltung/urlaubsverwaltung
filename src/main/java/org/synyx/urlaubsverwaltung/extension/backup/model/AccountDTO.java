package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.account.AccountEntity;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountDTO(LocalDate validFrom, LocalDate validTo, Boolean doRemainingVacationDaysExpireLocally,
                         boolean doRemainingVacationDaysExpireGlobally, LocalDate expiryDateLocally,
                         LocalDate expiryDateGlobally, LocalDate expiryNotificationSentDate,
                         BigDecimal annualVacationDays, BigDecimal actualVacationDays, BigDecimal remainingVacationDays,
                         BigDecimal remainingVacationDaysNotExpiring, String comment) {

    public AccountEntity toAccountEntity(Person accountOwner) {
        AccountEntity entity = new AccountEntity(accountOwner, validFrom, validTo, doRemainingVacationDaysExpireLocally, expiryDateLocally, annualVacationDays, remainingVacationDays, remainingVacationDaysNotExpiring, comment);
        entity.setExpiryNotificationSentDate(this.expiryNotificationSentDate);
        entity.setActualVacationDays(this.actualVacationDays);
        return entity;
    }
}
