package org.synyx.urlaubsverwaltung.ui.extension;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.nio.file.Paths;

import static org.synyx.urlaubsverwaltung.ui.extension.UiTestStore.setBrowser;
import static org.synyx.urlaubsverwaltung.ui.extension.UiTestStore.setBrowserContext;
import static org.synyx.urlaubsverwaltung.ui.extension.UiTestStore.setPage;
import static org.synyx.urlaubsverwaltung.ui.extension.UiTestStore.setPlaywright;

public class PageParameterResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == Page.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {

        final Playwright playwright = Playwright.create(new Playwright.CreateOptions());
        final Browser browser = playwright.chromium().launch();
        final BrowserContext browserContext = browser.newContext(browserContextOptions());
        final Page page = browserContext.newPage();

        // elements must be closed after the tests. see BrowserSetupExtension.
        setPlaywright(extensionContext, playwright);
        setBrowser(extensionContext, browser);
        setBrowserContext(extensionContext, browserContext);
        setPage(extensionContext, page);

        return page;
    }

    private static Browser.NewContextOptions browserContextOptions() {
        return new Browser.NewContextOptions()
            .setRecordVideoDir(Paths.get("target"))
            .setLocale("de-DE")
            .setScreenSize(1500, 1080)
            .setViewportSize(1500, 1080);
    }
}
