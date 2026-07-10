import formatNumber from "../format-number";

describe("format-number", function () {
  afterEach(function () {
    vi.restoreAllMocks();
  });

  function withLanguage(language) {
    vi.spyOn(navigator, "language", "get").mockReturnValue(language);
  }

  it("formats using the browser's language", function () {
    withLanguage("de");
    expect(formatNumber(1234.567)).toBe("1.234,6");

    withLanguage("en");
    expect(formatNumber(1234.567)).toBe("1,234.6");
  });

  it("falls back to 'de' when navigator.language is empty", function () {
    withLanguage("");
    expect(formatNumber(1234.567)).toBe("1.234,6");
  });

  it("rounds to at most one fraction digit", function () {
    withLanguage("de");
    expect(formatNumber(5.26)).toBe("5,3");
  });

  it("does not force a fraction digit on whole numbers", function () {
    withLanguage("de");
    expect(formatNumber(5)).toBe("5");
  });

  it("accepts a numeric string", function () {
    withLanguage("de");
    expect(formatNumber("7.5")).toBe("7,5");
  });
});
