import formatNumber from "../format-number";

describe("format-number", function () {
  afterEach(function () {
    document.documentElement.removeAttribute("lang");
    vi.restoreAllMocks();
  });

  function withPageLanguage(language) {
    document.documentElement.lang = language;
  }

  it("formats using the language of the page, not the one of the browser", function () {
    vi.spyOn(navigator, "language", "get").mockReturnValue("en-US");

    withPageLanguage("de");
    expect(formatNumber(1234.567)).toBe("1.234,6");

    withPageLanguage("en");
    expect(formatNumber(1234.567)).toBe("1,234.6");
  });

  it("falls back to 'de' when the page has no language", function () {
    withPageLanguage("");
    expect(formatNumber(1234.567)).toBe("1.234,6");
  });

  it("rounds to at most one fraction digit", function () {
    withPageLanguage("de");
    expect(formatNumber(5.26)).toBe("5,3");
  });

  it("does not force a fraction digit on whole numbers", function () {
    withPageLanguage("de");
    expect(formatNumber(5)).toBe("5");
  });

  it("accepts a numeric string", function () {
    withPageLanguage("de");
    expect(formatNumber("7.5")).toBe("7,5");
  });
});
