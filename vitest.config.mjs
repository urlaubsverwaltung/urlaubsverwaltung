import { defineConfig } from "vitest/config";
import { playwright } from "@vitest/browser-playwright";

export default defineConfig({
  test: {
    inspectBrk: true,
    fileParallelism: false,
    include: ["**/*.browser.js"],
    browser: {
      enabled: true,
      headless: true,
      provider: playwright(),
      instances: [{ browser: "chromium" }],
    },
  },
  // test: {
  //   experimental: {
  //     openTelemetry: {
  //       enabled: false,
  //     },
  //   },
  //   coverage: {
  //     provider: "v8",
  //     reporter: ["text", "lcov"],
  //     reportsDirectory: "target/js-coverage",
  //   },
  //   projects: [
  //     {
  //       test: {
  //         name: "jsdom",
  //         include: ["**/*.{test,spec}.js"],
  //         environment: "jsdom",
  //       },
  //     },
  //     {
  //       test: {
  //         name: "browser",
  //         include: ["**/*.{test,spec}.browser.js"],
  //         inspectBrk: true,
  //         fileParallelism: false,
  //         browser: {
  //           enabled: true,
  //           headless: true,
  //           provider: playwright(),
  //           instances: [
  //             { browser: "chromium" },
  //             // { browser: "firefox" },
  //             // no webkit since this requires the custom-elements built-in polyfill
  //             // { browser: "webkit" },
  //           ],
  //         },
  //       },
  //     },
  //   ],
  // },
});
