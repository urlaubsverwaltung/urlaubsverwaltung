import { expect, test } from "./fixtures.mjs";
import { coveredPages, loadManifest, screenshotName } from "./manifest.mjs";
import { authFile, roles } from "./roles.mjs";

// One screenshot test per role and covered page of the manifest. The manifest is maintained by the discovery test.
// Runs once per project (desktop, mobile, desktop-dark), screenshots end up in __screenshots__/<project>/<role>/.

for (const role of roles) {
  test.describe(role.name, () => {
    test.use({ storageState: authFile(role) });

    for (const { pattern, url } of coveredPages(loadManifest(role))) {
      test(pattern, async ({ page }) => {
        await page.goto(url);
        await page.waitForLoadState("networkidle");
        await expect(page).toHaveScreenshot(screenshotName(role, pattern), { fullPage: true });
      });
    }
  });
}
