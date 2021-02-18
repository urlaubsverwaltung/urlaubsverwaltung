package org.synyx.urlaubsverwaltung.ui;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.util.function.BooleanSupplier;

public class PageConditions {

    public static ExpectedCondition<Boolean> pageIsVisible(Page page) {
        return page::isVisible;
    }

    public static ExpectedCondition<Boolean> isTrue(BooleanSupplier supplier) {
        return driver -> supplier.getAsBoolean();
    }

    public static ExpectedCondition<Boolean> elementHasAttributeWithValue(By locator, String attributeName, String attributeValue) {
        return driver -> {
            final WebElement element = driver.findElement(locator);
            if (element != null) {
                final String attribute = element.getAttribute(attributeName);
                return attribute != null && attribute.equals(attributeValue);
            }
            return false;
        };
    }
}
