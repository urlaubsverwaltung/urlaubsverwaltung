package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Reads colours the way the browser computes them, to compare an element with a colour of the theme.
 */
public final class ThemeColors {

    private ThemeColors() {
    }

    /**
     * @param page           the page whose stylesheets define the property
     * @param customProperty a colour custom property of the theme, e.g. {@code --color-zinc-50}
     * @return the property computed as a colour on this page
     */
    public static String colorOf(Page page, String customProperty) {
        return (String) page.evaluate("""
            property => {
              const probe = document.createElement("div");
              probe.style.backgroundColor = `var(${property})`;
              document.body.append(probe);
              const color = getComputedStyle(probe).backgroundColor;
              probe.remove();
              return color;
            }""", customProperty);
    }

    /**
     * @param element       the element owning the pseudo-element
     * @param pseudoElement e.g. {@code ::after}
     * @return computed background colour of the pseudo-element
     */
    public static String backgroundColorOf(Locator element, String pseudoElement) {
        return (String) element.evaluate("(element, pseudoElement) => getComputedStyle(element, pseudoElement).backgroundColor", pseudoElement);
    }
}
