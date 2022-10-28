package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AccountFormTest {

    @Test
    void ensureHasDefaultValuesForHolidaysAccountPeriod() {
        final AccountForm accountForm = new AccountForm(2014);
        assertThat(accountForm.getHolidaysAccountYear()).isEqualTo(2014);
        assertThat(accountForm.getHolidaysAccountValidFrom()).isEqualTo(LocalDate.of(2014, 1, 1));
        assertThat(accountForm.getHolidaysAccountValidTo()).isEqualTo(LocalDate.of(2014, 12, 31));
    }

    @Test
    void ensureUsesValuesOfGivenHolidaysAccount() {

        final Account account = new Account();

        final LocalDate localDateFrom = LocalDate.now().minusDays(20);
        account.setValidFrom(localDateFrom);

        final LocalDate localDateTo = LocalDate.now().plusDays(10);
        account.setValidTo(localDateTo);

        account.setAnnualVacationDays(BigDecimal.ZERO);
        account.setActualVacationDays(BigDecimal.TEN);
        account.setRemainingVacationDays(BigDecimal.ONE);
        account.setRemainingVacationDaysNotExpiring(BigDecimal.ZERO);

        final AccountForm form = new AccountForm(account);
        assertThat(form.getHolidaysAccountYear()).isEqualTo(localDateFrom.getYear());
        assertThat(form.getHolidaysAccountValidFrom()).isEqualTo(localDateFrom);
        assertThat(form.getHolidaysAccountValidTo()).isEqualTo(localDateTo);
        assertThat(form.getAnnualVacationDays()).isEqualTo(BigDecimal.ZERO);
        assertThat(form.getActualVacationDays()).isEqualTo(BigDecimal.TEN);
        assertThat(form.getRemainingVacationDays()).isEqualTo(BigDecimal.ONE);
        assertThat(form.getRemainingVacationDaysNotExpiring()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void ensureEmptyHolidaysAccountValidFromIsoValue() {

        final AccountForm accountForm = new AccountForm(2020);

        accountForm.setHolidaysAccountValidFrom(null);

        assertThat(accountForm.getHolidaysAccountValidFromIsoValue()).isEmpty();
    }

    @Test
    void ensureHolidaysAccountValidFromIsoValue() {

        final AccountForm accountForm = new AccountForm(2020);

        accountForm.setHolidaysAccountValidFrom(LocalDate.parse("2020-10-30"));

        assertThat(accountForm.getHolidaysAccountValidFromIsoValue()).isEqualTo("2020-10-30");
    }

    @Test
    void ensureEmptyHolidaysAccountValidToIsoValue() {

        final AccountForm accountForm = new AccountForm(2020);

        accountForm.setHolidaysAccountValidTo(null);

        assertThat(accountForm.getHolidaysAccountValidToIsoValue()).isEmpty();
    }

    @Test
    void ensureHolidaysAccountValidToIsoValue() {

        final AccountForm accountForm = new AccountForm(2020);

        accountForm.setHolidaysAccountValidTo(LocalDate.parse("2020-10-30"));

        assertThat(accountForm.getHolidaysAccountValidTo()).isEqualTo("2020-10-30");
    }
}
