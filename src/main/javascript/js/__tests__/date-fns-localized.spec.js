import { vi, describe, test, expect, afterEach } from "vitest";
import { de as localeDE } from "date-fns/locale/de";
import { deAT as localeDEAT } from "date-fns/locale/de-AT";
import { el as localeEL } from "date-fns/locale/el";
import { enGB as localeENGB } from "date-fns/locale/en-GB";

vi.mock("../../lib/date-fns/locale-resolver");

describe("date-fns-localized", () => {
  let uvLanguage = "";
  globalThis.uv = {};

  Object.defineProperty(globalThis.uv, "language", {
    get() {
      return uvLanguage;
    },
  });

  afterEach(() => {
    uvLanguage = "";
    vi.clearAllMocks();
    vi.resetModules();
  });

  test.each([
    ["de", "de", localeDE],
    ["de-DE", "de", localeDE],
    ["de-AT", "deAT", localeDEAT],
    ["el", "el", localeEL],
    ["en-GB", "enGB", localeENGB],
  ])("loads date-fn locale for window.uv.language=%s", async (givenLanguage, expectedCode, expectedLocaleStuff) => {
    uvLanguage = givenLanguage;

    const { setLocale } = await import("../../lib/date-fns/locale-resolver");

    await import("../date-fns-localized");

    // wait for resolved promise in implementation
    // which then calls `setLocale`
    await wait();

    // using expect.objectContaining since the actual argument contains `{[code]: x, default: x}`
    expect(setLocale).toHaveBeenCalledWith(
      expect.objectContaining({
        [expectedCode]: expectedLocaleStuff,
      }),
    );
  });

  test.each([["en"], ["en-US"]])("loads date-fn english locale for window.uv.language=%s", async (givenLanguage) => {
    uvLanguage = givenLanguage;

    const { setLocale } = await import("../../lib/date-fns/locale-resolver");

    await import("../date-fns-localized");

    // wait for resolved promise in implementation
    // which then calls `setLocale`
    await wait();

    expect(setLocale).not.toHaveBeenCalled();
  });

  function wait(delay = 0) {
    return new Promise((resolve) => {
      setTimeout(resolve, delay);
    });
  }
});
