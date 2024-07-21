/* eslint-disable unicorn/no-null */
import { createDatepickerLocalization } from "..";

describe("createDatepickerLocalization", () => {
  describe.each([["de", "de-AT", "el"]])("dateAdapter for duet-date-picker for locale=%s", (givenLocale) => {
    test("defines dateFormat", () => {
      const datepickerLocalization = createDatepickerLocalization({ locale: givenLocale });
      expect(datepickerLocalization.dateFormat).toBe("d.M.yyyy");
    });

    test("defines dateFormatShort", () => {
      const datepickerLocalization = createDatepickerLocalization({ locale: givenLocale });
      expect(datepickerLocalization.dateFormatShort).toBe("dd. MMMM");
    });

    test.each([
      ["01.01.2020", ["2020", "01", "01"]],
      ["01.1.2020", ["2020", "1", "01"]],
      ["1.01.2020", ["2020", "01", "1"]],
      ["1.1.2020", ["2020", "1", "1"]],
    ])("parses: %s", (givenDateString, expectedValues) => {
      const createDate = jest.fn().mockReturnValue(42);

      const datepickerLocalization = createDatepickerLocalization({ locale: givenLocale });
      const actual = datepickerLocalization.dateAdapter.parse(givenDateString, createDate);

      expect(createDate).toHaveBeenCalledWith(...expectedValues);
      expect(actual).toBe(42);
    });

    test.each([["2020.01.01"], ["01/01/2020"], ["2020-01-01"]])("does not parse: %s", (givenDateString) => {
      const createDate = jest.fn().mockReturnValue(42);

      const datepickerLocalization = createDatepickerLocalization({ locale: givenLocale });
      const actual = datepickerLocalization.dateAdapter.parse(givenDateString, createDate);

      expect(createDate).not.toHaveBeenCalled();
      expect(actual).toBeUndefined();
    });

    test.each([
      [false, ""],
      [null, ""],
      [undefined, ""],
      [new Date("2020-10-21"), "21.10.2020"],
    ])("formats: %s", () => {
      const datepickerLocalization = createDatepickerLocalization({ locale: givenLocale });
      const actual = datepickerLocalization.dateAdapter.format(false);
      expect(actual).toBe("");
    });
  });

  describe.each([["en", "en-CA", "en-CB", "en-PH", "en-US"]])(
    "dateAdapter for duet-date-picker for locale=%s",
    (givenLocale) => {
      test("defines dateFormat", () => {
        const datepickerLocalization = createDatepickerLocalization({ locale: givenLocale });
        expect(datepickerLocalization.dateFormat).toBe("d.M.yyyy");
      });

      test("defines dateFormatShort", () => {
        const datepickerLocalization = createDatepickerLocalization({ locale: givenLocale });
        expect(datepickerLocalization.dateFormatShort).toBe("MMMM dd");
      });

      test.each([
        ["01.01.2020", ["2020", "01", "01"]],
        ["01.1.2020", ["2020", "1", "01"]],
        ["1.01.2020", ["2020", "01", "1"]],
        ["1.1.2020", ["2020", "1", "1"]],
      ])("parses: %s", (givenDateString, expectedValues) => {
        const createDate = jest.fn().mockReturnValue(42);

        const datepickerLocalization = createDatepickerLocalization({ locale: givenLocale });
        const actual = datepickerLocalization.dateAdapter.parse(givenDateString, createDate);

        expect(createDate).toHaveBeenCalledWith(...expectedValues);
        expect(actual).toBe(42);
      });

      test.each([["2020.01.01"], ["01/01/2020"], ["2020-01-01"]])("does not parse: %s", (givenDateString) => {
        const createDate = jest.fn().mockReturnValue(42);

        const datepickerLocalization = createDatepickerLocalization({ locale: givenLocale });
        const actual = datepickerLocalization.dateAdapter.parse(givenDateString, createDate);

        expect(createDate).not.toHaveBeenCalled();
        expect(actual).toBeUndefined();
      });

      test.each([
        [false, ""],
        [null, ""],
        [undefined, ""],
        [new Date("2020-10-21"), "21.10.2020"],
      ])("formats: %s", () => {
        const datepickerLocalization = createDatepickerLocalization({ locale: givenLocale });
        const actual = datepickerLocalization.dateAdapter.format(false);
        expect(actual).toBe("");
      });
    },
  );

  describe.each([["en-AU", "en-BZ", "en-GB", "en-IN", "en-IE", "en-JM", "en-NZ", "en-ZA", "en-TT"]])(
    "dateAdapter for duet-date-picker for locale=%s",
    (givenLocale) => {
      test("defines dateFormat", () => {
        const datepickerLocalization = createDatepickerLocalization({ locale: givenLocale });
        expect(datepickerLocalization.dateFormat).toBe("d.M.yyyy");
      });

      test("defines dateFormatShort", () => {
        const datepickerLocalization = createDatepickerLocalization({ locale: givenLocale });
        expect(datepickerLocalization.dateFormatShort).toBe("dd MMMM");
      });

      test.each([
        ["01.01.2020", ["2020", "01", "01"]],
        ["01.1.2020", ["2020", "1", "01"]],
        ["1.01.2020", ["2020", "01", "1"]],
        ["1.1.2020", ["2020", "1", "1"]],
      ])("parses: %s", (givenDateString, expectedValues) => {
        const createDate = jest.fn().mockReturnValue(42);

        const datepickerLocalization = createDatepickerLocalization({ locale: givenLocale });
        const actual = datepickerLocalization.dateAdapter.parse(givenDateString, createDate);

        expect(createDate).toHaveBeenCalledWith(...expectedValues);
        expect(actual).toBe(42);
      });

      test.each([["2020.01.01"], ["01/01/2020"], ["2020-01-01"]])("does not parse: %s", (givenDateString) => {
        const createDate = jest.fn().mockReturnValue(42);

        const datepickerLocalization = createDatepickerLocalization({ locale: givenLocale });
        const actual = datepickerLocalization.dateAdapter.parse(givenDateString, createDate);

        expect(createDate).not.toHaveBeenCalled();
        expect(actual).toBeUndefined();
      });

      test.each([
        [false, ""],
        [null, ""],
        [undefined, ""],
        [new Date("2020-10-21"), "21.10.2020"],
      ])("formats: %s", () => {
        const datepickerLocalization = createDatepickerLocalization({ locale: givenLocale });
        const actual = datepickerLocalization.dateAdapter.format(false);
        expect(actual).toBe("");
      });
    },
  );
});
