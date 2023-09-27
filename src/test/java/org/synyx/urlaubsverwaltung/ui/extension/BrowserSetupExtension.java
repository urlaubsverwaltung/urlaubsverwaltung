package org.synyx.urlaubsverwaltung.ui.extension;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Paths;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.ui.extension.UiTestStore.getBrowser;
import static org.synyx.urlaubsverwaltung.ui.extension.UiTestStore.getBrowserContext;
import static org.synyx.urlaubsverwaltung.ui.extension.UiTestStore.getPage;
import static org.synyx.urlaubsverwaltung.ui.extension.UiTestStore.getPlaywright;

/**
 * Handle stuff like closing the Browser or saving recorded test videos.
 */
public class BrowserSetupExtension implements AfterEachCallback {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    @Override
    public void afterEach(ExtensionContext context) throws Exception {

        handleVideoFile(context);
        close(context);
    }

    private static void handleVideoFile(ExtensionContext context) {

        final Page page = getPage(context);

        final File videoFile = new File(page.video().path().toUri());
        if (context.getExecutionException().isEmpty()) {
            // delete video files of successful tests
            final boolean isDeleted = videoFile.delete();
            if (!isDeleted) {
                LOG.info("could not delete file=%s of successful test.".formatted(videoFile.getAbsolutePath()));
            }
        } else {
            // rename video file
            final String newVideoFilePath = normalizeVideoFileName("target/FAILED-%s.webm".formatted(context.getDisplayName()));
            final File newVideoFile = new File(Paths.get(newVideoFilePath).toUri());
            final boolean isMoved = videoFile.renameTo(newVideoFile);
            if (!isMoved) {
                LOG.info("could not rename test video file.");
            }
        }
    }

    private static String normalizeVideoFileName(String original) {
        return original.replaceAll(" ", "_");
    }

    private static void close(ExtensionContext context) {

        final Page page = getPage(context);
        final BrowserContext browserContext = getBrowserContext(context);
        final Browser browser = getBrowser(context);
        final Playwright playwright = getPlaywright(context);

        page.close();
        browserContext.close();
        browser.close();
        playwright.close();
    }
}
