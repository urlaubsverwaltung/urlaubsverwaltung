package org.synyx.urlaubsverwaltung.ui.extension;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.extension.ExtensionContext;

class UiTestStore {

    private static final ExtensionContext.Namespace PLAYWRIGHT_NAMESPACE = ExtensionContext.Namespace.create("playwright");

    static void setPlaywright(ExtensionContext context, Playwright playwright) {
        put(context, "playwright", playwright);
    }

    static Playwright getPlaywright(ExtensionContext context) {
        return get("playwright", context);
    }

    static void setBrowserContext(ExtensionContext context, BrowserContext browser) {
        put(context, "browserContext", browser);
    }

    static BrowserContext getBrowserContext(ExtensionContext context) {
        return get("browserContext", context);
    }

    static void setBrowser(ExtensionContext context, Browser browser) {
        put(context, "browser", browser);
    }

    static Browser getBrowser(ExtensionContext context) {
        return get("browser", context);
    }

    static void setPage(ExtensionContext context, Page page) {
        put(context, "page", page);
    }

    static Page getPage(ExtensionContext context) {
        return get("page", context);
    }

    private static <T> T get(String name, ExtensionContext context) {
        return (T) getStore(context).get(name);
    }

    private static void put(ExtensionContext context, String name, Object value) {
        getStore(context).put(name, value);
    }

    private static ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(PLAYWRIGHT_NAMESPACE);
    }
}
