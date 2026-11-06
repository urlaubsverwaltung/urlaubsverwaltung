import { test as base } from "@playwright/test";
import { readFixedInstant } from "./environment.mjs";

const fixedInstant = new Date(readFixedInstant());

/**
 * Freezes the browser time at the same instant as the application clock. The frontend uses the browser time as well,
 * e.g. the person overview calendar is centered on 'today'.
 */
export const test = base.extend({
  page: async ({ page }, use) => {
    await page.clock.setFixedTime(fixedInstant);
    await use(page);
  },
});

export { expect } from "@playwright/test";
