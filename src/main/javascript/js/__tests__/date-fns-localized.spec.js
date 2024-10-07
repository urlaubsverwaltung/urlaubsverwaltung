import { de as localeDE } from "date-fns/locale/de";
import { deAT as localeDEAT } from "date-fns/locale/de-AT";
import { el as localeEL } from "date-fns/locale/el";
import { enGB as localeENGB } from "date-fns/locale/en-GB";

jest.mock("../../lib/date-fns/locale-resolver");

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
    jest.resetModules();
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

    // locale data is dynamically loaded on runtime with `import(..)` by date-fns-localized.js
    // the loaded locale data module has an attached { "default": (..) } part which the `localeDE` of this test has not.
    // this is due to babel configuration settings I think. I didn't look for details...
    // the attached `default` part has something to do with JavaScript Module and CommonJS Module compatibility
    // however, I don't care here. therefore just expect an object containing the original locale :o)
    expect(setLocale).toHaveBeenCalledWith({
      // this is not correct actually... the code running in the browser is slightly different...
      // actually it should be `toHaveBeenCalledWith(expect.objectContaining({ code: expectedLocaleStuff.code }))`
      // however... babel transpiles the code differently to the rollup setup (at least I think so) (jest uses babel)
      // jest/babel uses datefns de.js files while the rollup build bundles the de.mjs files
      [expectedCode]: expect.objectContaining({ code: expectedLocaleStuff.code }),
    });
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
