import { defineConfig } from "@playwright/test";

// Visual regression tests, see docs/visual-regression-tests.md
//
// Expects the application running with the profiles 'demodata,visualtest' and the browser server running in the
// pinned container (npm run test:visual:browser). There is no fallback to a local browser on purpose, screenshots
// must always be rendered on the same platform.

const wsEndpoint = process.env.PLAYWRIGHT_WS_ENDPOINT ?? "ws://127.0.0.1:3000/";

const variants = [
  {
    name: "desktop",
    use: { viewport: { width: 1280, height: 800 }, deviceScaleFactor: 1 },
  },
  {
    name: "mobile",
    use: { viewport: { width: 390, height: 844 }, deviceScaleFactor: 1, isMobile: true, hasTouch: true },
  },
];

export default defineConfig({
  testDir: "src/test/visual",
  outputDir: "target/visual-tests/results",
  // no {platform}: there is only one baseline set, rendered in the pinned linux container
  snapshotPathTemplate: "src/test/visual/__screenshots__/{projectName}/{arg}{ext}",
  fullyParallel: true,
  forbidOnly: Boolean(process.env.CI),
  retries: 0,
  // limited by the single application instance and the browser container, not by playwright
  workers: 4,
  // CI must never accept new baselines on its own
  updateSnapshots: process.env.CI ? "none" : "missing",
  reporter: [["list"], ["html", { outputFolder: "target/visual-tests/report", open: "never" }]],
  use: {
    baseURL: "http://localhost:8080",
    browserName: "chromium",
    // the browser runs in the container, exposeNetwork tunnels its localhost requests to this machine
    // (application on 8080, keycloak redirect on 8090)
    connectOptions: { wsEndpoint, exposeNetwork: "<loopback>" },
    locale: "de-DE",
    timezoneId: "Europe/Berlin",
    colorScheme: "light",
  },
  projects: [
    { name: "setup", testMatch: /auth\.setup\.mjs/ },
    {
      name: "discovery",
      testMatch: /discovery\.spec\.mjs/,
      dependencies: ["setup"],
      use: variants[0].use,
    },
    ...variants.map((variant) => ({
      name: variant.name,
      testMatch: /pages\.spec\.mjs/,
      dependencies: ["setup"],
      use: variant.use,
    })),
  ],
});
