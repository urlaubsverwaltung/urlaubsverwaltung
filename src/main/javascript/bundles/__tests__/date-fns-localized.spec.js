// eslint-disable-next-line @urlaubsverwaltung/no-date-fns
import localeDE from "date-fns/locale/de";
// eslint-disable-next-line @urlaubsverwaltung/no-date-fns
import localeDEAT from "date-fns/locale/de-AT";

jest.mock("../../lib/date-fns/locale-resolver");

describe("date-fns-localized", () => {
  let navigatorLanguage = "";
  const originalNavigatorLanguage = window.navigator.language;

  Object.defineProperty(window.navigator, "language", {
    get() {
      return navigatorLanguage || originalNavigatorLanguage;
    },
  });

  afterEach(() => {
    navigatorLanguage = "";
    jest.resetModules();
  });

  test.each([
    ["de", localeDE],
    ["de-DE", localeDE],
    ["de-AT", localeDEAT],
  ])("loads date-fn locale for window.navigator.language=%s", async (givenLanguage, expectedLocaleStuff) => {
    navigatorLanguage = givenLanguage;

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

  test.each([["en"], ["en-US"]])(
    "loads date-fn english locale for window.navigator.language=%s",
    async (givenLanguage) => {
      navigatorLanguage = givenLanguage;

      const { setLocale } = await import("../../lib/date-fns/locale-resolver");

      await import("../date-fns-localized");

      // wait for resolved promise in implementation
      // which then calls `setLocale`
      await wait();

      expect(setLocale).not.toHaveBeenCalled();
    },
  );

  function wait(delay = 0) {
    return new Promise((resolve) => {
      setTimeout(resolve, delay);
    });
  }
});
