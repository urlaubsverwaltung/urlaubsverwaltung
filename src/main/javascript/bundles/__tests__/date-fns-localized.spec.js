// eslint-disable-next-line @urlaubsverwaltung/no-date-fns
import localeDE from "date-fns/locale/de";
// eslint-disable-next-line @urlaubsverwaltung/no-date-fns
import localeDEAT from "date-fns/locale/de-AT";
// eslint-disable-next-line @urlaubsverwaltung/no-date-fns
import localeEL from "date-fns/locale/el";

jest.mock("../../lib/date-fns/locale-resolver");

describe("date-fns-localized", () => {
  let uvLanguage = "";
  window.uv = {};

  Object.defineProperty(window.uv, "language", {
    get() {
      return uvLanguage;
    },
  });

  afterEach(() => {
    uvLanguage = "";
    jest.resetModules();
  });

  test.each([
    ["de", localeDE],
    ["de-DE", localeDE],
    ["de-AT", localeDEAT],
    ["el", localeEL],
  ])("loads date-fn locale for window.uv.language=%s", async (givenLanguage, expectedLocaleStuff) => {
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
    expect(setLocale).toHaveBeenCalledWith(expect.objectContaining({ code: expectedLocaleStuff.code }));
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
