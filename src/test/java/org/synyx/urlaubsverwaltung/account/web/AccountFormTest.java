package org.synyx.urlaubsverwaltung.account.web;

import org.junit.Assert;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.account.domain.Account;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountFormTest {

    @Test
    public void ensureHasDefaultValuesForHolidaysAccountPeriod() {

        AccountForm accountForm = new AccountForm(2014);

        Assert.assertNotNull("Valid from date for holidays account must not be null",
            accountForm.getHolidaysAccountValidFrom());
        Assert.assertNotNull("Valid to date for holidays account must not be null",
            accountForm.getHolidaysAccountValidTo());

        Assert.assertEquals("Wrong valid from date for holidays account", LocalDate.of(2014, 1, 1),
            accountForm.getHolidaysAccountValidFrom());
        Assert.assertEquals("Wrong valid to date for holidays account", LocalDate.of(2014, 12, 31),
            accountForm.getHolidaysAccountValidTo());
    }

    @Test
    public void ensureUsesValuesOfGivenHolidaysAccount() {

        Account account = new Account();

        final LocalDate localDateFrom = LocalDate.now().minusDays(20);
        account.setValidFrom(localDateFrom);

        final LocalDate localDateTo = LocalDate.now().plusDays(10);
        account.setValidTo(localDateTo);

        account.setAnnualVacationDays(BigDecimal.ZERO);
        account.setVacationDays(BigDecimal.TEN);
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
}
