package org.synyx.urlaubsverwaltung.ui.extension;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Paths;

import static com.microsoft.playwright.impl.junit.BrowserContextExtension.getOrCreateBrowserContext;
import static com.microsoft.playwright.impl.junit.PageExtension.getOrCreatePage;
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Deletes record video files of successful tests and renames videos of failed tests to the matching test name.
 */
public class TestRecordVideoExtension implements AfterEachCallback {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    @Override
    public void afterEach(ExtensionContext context) throws Exception {

        handleVideoFile(context);
    }

    private static void handleVideoFile(ExtensionContext context) {

        final Page page = getOrCreatePage(context);

        final File videoFile = new File(page.video().path().toUri());
        if (context.getExecutionException().isEmpty()) {
            // delete video files of successful tests
            final boolean isDeleted = videoFile.delete();
            if (!isDeleted) {
                LOG.info("could not delete file=%s of successful test.".formatted(videoFile.getAbsolutePath()));
            }
        } else {
            // rename video file
            final String newVideoFilePath = videoPath(context);
            final File newVideoFile = new File(Paths.get(newVideoFilePath).toUri());
            final boolean isMoved = videoFile.renameTo(newVideoFile);
            if (!isMoved) {
                LOG.info("could not rename test video file.");
            }
        }
    }

    private static String videoPath(ExtensionContext context) {

        final BrowserContext browserContext = getOrCreateBrowserContext(context);
        final String browser = browserContext.browser().browserType().name();

        return normalizeVideoFileName("target/ui-test/%s/FAILED-%s.webm".formatted(browser, context.getDisplayName()));
    }

    private static String normalizeVideoFileName(String original) {
        return original.replaceAll(" ", "_");
    }
}

