package org.synyx.urlaubsverwaltung.application.domain;

import org.junit.Assert;
import org.junit.Test;


public class VacationTypeTest {

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfCheckingCategoryWithNull() {

        new VacationType().isOfCategory(null);
    }


    @Test
    public void ensureReturnsTrueIfVacationTypeIsOfGivenCategory() {

        VacationType vacationType = new VacationType();
        vacationType.setCategory(VacationCategory.OVERTIME);

        Assert.assertTrue("Categories should match", vacationType.isOfCategory(VacationCategory.OVERTIME));
    }


    @Test
    public void ensureReturnsFalseIfVacationTypeIsNotOfGivenCategory() {

        VacationType vacationType = new VacationType();
        vacationType.setCategory(VacationCategory.HOLIDAY);

        Assert.assertFalse("Categories should not match", vacationType.isOfCategory(VacationCategory.OVERTIME));
    }
}
