package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

import java.util.function.Consumer;

public abstract class UvPage {

    private UvPage() {
        //
    }

    /**
     * Executes the action and waits for page reload.
     *
     * <p>
     * Note that you may have to wait for executed JavaScript yourself. This method only waits until DOMContentLoaded.
     *
     *
     * @param page {@link Page} to work with
     * @param action action to execute
     */
    public static void executeAndWaitForPageRefresh(Page page, Consumer<Page> action) {

        // wait for page refresh (skip redirects)
        page.waitForResponse(
            response -> response.ok() || (response.status() >= 400 && response.status() < 600),
            () -> action.accept(page)
        );

        // wait for loaded assets
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        // additionally, you may have to wait for executed JavaScript depending on your test case.
        // e.g. an initialized datepicker (native date or text input will be transformed to a custom date picker element)
    }
}
