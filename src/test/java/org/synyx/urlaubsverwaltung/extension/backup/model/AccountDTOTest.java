package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.account.AccountEntity;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AccountDTOTest {

    @Test
    void happyPath() {
        final AccountDTO accountDTO = new AccountDTO(
            LocalDate.of(2023, 1, 1),
            LocalDate.of(2023, 12, 31),
            true,
            false,
            LocalDate.of(2023, 12, 31),
            null,
            LocalDate.of(2023, 11, 1),
            BigDecimal.valueOf(30),
            BigDecimal.valueOf(25),
            BigDecimal.valueOf(5),
            BigDecimal.valueOf(2),
            "Test comment"
        );
        final Person accountOwner = new Person();

        final AccountEntity accountEntity = accountDTO.toAccountEntity(accountOwner);

        assertThat(accountEntity.getPerson()).isEqualTo(accountOwner);
        assertThat(accountEntity.getValidFrom()).isEqualTo(accountDTO.validFrom());
        assertThat(accountEntity.getValidTo()).isEqualTo(accountDTO.validTo());
        assertThat(accountEntity.isDoRemainingVacationDaysExpire()).isEqualTo(accountDTO.doRemainingVacationDaysExpireLocally());
        assertThat(accountEntity.getExpiryDate()).isEqualTo(accountDTO.expiryDateLocally());
        assertThat(accountEntity.getExpiryNotificationSentDate()).isEqualTo(accountDTO.expiryNotificationSentDate());
        assertThat(accountEntity.getAnnualVacationDays()).isEqualTo(accountDTO.annualVacationDays());
        assertThat(accountEntity.getActualVacationDays()).isEqualTo(accountDTO.actualVacationDays());
        assertThat(accountEntity.getRemainingVacationDays()).isEqualTo(accountDTO.remainingVacationDays());
        assertThat(accountEntity.getRemainingVacationDaysNotExpiring()).isEqualTo(accountDTO.remainingVacationDaysNotExpiring());
        assertThat(accountEntity.getComment()).isEqualTo(accountDTO.comment());

    }

}
