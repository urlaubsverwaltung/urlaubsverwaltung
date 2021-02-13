package org.synyx.urlaubsverwaltung.ui;

import org.openqa.selenium.support.ui.ExpectedCondition;

import java.util.function.BooleanSupplier;

public class PageConditions {

    public static ExpectedCondition<Boolean> pageIsVisible(Page page) {
        return page::isVisible;
    }

    public static ExpectedCondition<Boolean> isTrue(BooleanSupplier supplier) {
        return driver -> supplier.getAsBoolean();
    }
}
