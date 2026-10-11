import { defineConfig } from "@playwright/test";

// Visual regression tests, see docs/visual-regression-tests.md
//
// Expects the application running with the profiles 'demodata,visualtest' (or use the maven profile 'visual-test').
// The browser server in the pinned container is started by playwright (webServer). There is no fallback to a local
// browser on purpose, screenshots must always be rendered on the same platform.

const browserServerPort = process.env.PLAYWRIGHT_SERVER_PORT ?? "3000";
const wsEndpoint = `ws://127.0.0.1:${browserServerPort}/`;

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
  webServer: {
    name: "browser",
    command: "node src/test/visual/browser-server.mjs",
    // the websocket server answers plain http requests with 4xx, which counts as ready
    url: `http://127.0.0.1:${browserServerPort}/`,
    wait: { stdout: /Listening on/ },
    // locally a browser server that is already running is reused
    reuseExistingServer: !process.env.CI,
    // pulling the image may take a while on the first run
    timeout: 5 * 60 * 1000,
    // forwarded to 'docker run', which stops and removes the container
    gracefulShutdown: { signal: "SIGTERM", timeout: 10_000 },
  },
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
    // the teardown runs after setup and all projects depending on it
    { name: "setup", testMatch: /auth\.setup\.mjs/, teardown: "screenshots" },
    { name: "screenshots", testMatch: /screenshots\.teardown\.mjs/ },
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
