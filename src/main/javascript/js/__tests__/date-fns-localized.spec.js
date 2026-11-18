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
    vi.resetModules();
    vi.clearAllMocks();
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

    const { localeLoaded } = await import("../date-fns-localized");
    await localeLoaded;

    expect(setLocale).toHaveBeenCalledWith(expect.objectContaining({ code: expectedLocaleStuff.code }));
  });

  test.each([["en"], ["en-US"]])("loads date-fn english locale for window.uv.language=%s", async (givenLanguage) => {
    uvLanguage = givenLanguage;

    const { setLocale } = await import("../../lib/date-fns/locale-resolver");

    const { localeLoaded } = await import("../date-fns-localized");
    await localeLoaded;

    expect(setLocale).not.toHaveBeenCalled();
  });
});
