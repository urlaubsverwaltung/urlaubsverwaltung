package org.synyx.urlaubsverwaltung.account.web;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;


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
}
