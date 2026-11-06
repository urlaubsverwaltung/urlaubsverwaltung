import { expect, test } from "./fixtures.mjs";
import { coveredPages, loadManifest, screenshotName } from "./manifest.mjs";
import { authFile, roles } from "./roles.mjs";

// One screenshot test per role and covered page of the manifest. The manifest is maintained by the discovery test.
// Runs once per project (desktop, mobile, desktop-dark), screenshots end up in __screenshots__/<project>/<role>/.

/**
 * The app shell fixes body to the viewport height and lets #main be the scroll container. A full page screenshot does
 * not scroll #main, everything below the first screen would be missing. So the viewport grows by the hidden part of
 * #main first, then the whole page fits into it.
 */
async function fitViewportToContent(page) {
  const size = page.viewportSize();
  const hiddenHeight = await page.locator("#main").evaluate((main) => main.scrollHeight - main.clientHeight);
  await page.setViewportSize({ width: size.width, height: size.height + hiddenHeight });
}

for (const role of roles) {
  test.describe(role.name, () => {
    test.use({ storageState: authFile(role) });

    for (const { pattern, url } of coveredPages(loadManifest(role))) {
      test(pattern, async ({ page }) => {
        await page.goto(url);
        await page.waitForLoadState("networkidle");
        await fitViewportToContent(page);
        await expect(page).toHaveScreenshot(screenshotName(role, pattern), { fullPage: true });
      });
    }
  });
}
