import { test } from "./fixtures.mjs";
import { authFile, password, roles } from "./roles.mjs";

// Logs in every role once and stores the session, so the parallel tests do not log in over and over again and
// possible writes on login (person sync from keycloak) happen before the parallel phase.
for (const role of roles) {
  test(`login ${role.name}`, async ({ page }) => {
    await page.goto("/");
    await page.fill("#username", role.username);
    await page.fill("#password", password);
    await page.click("#kc-login");
    await page.waitForURL("http://localhost:8080/web/**");
    await page.context().storageState({ path: authFile(role) });
  });
}
