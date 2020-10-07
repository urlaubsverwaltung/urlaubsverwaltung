/* eslint-disable unicorn/no-null */
import { dateAdapter } from "../de";

describe("datepicker locale de", () => {
  test.each([
    ["01.01.2020", ["2020", "01", "01"]],
    ["01.1.2020", ["2020", "1", "01"]],
    ["1.01.2020", ["2020", "01", "1"]],
    ["1.1.2020", ["2020", "1", "1"]],
  ])("parses: %s", (givenDateString, expectedValues) => {
    const createDate = jest.fn().mockReturnValue(42);

    const actual = dateAdapter.parse(givenDateString, createDate);

    expect(createDate).toHaveBeenCalledWith(...expectedValues);
    expect(actual).toBe(42);
  });

  test.each([["2020.01.01"], ["01/01/2020"], ["2020-01-01"]])("does not parse: %s", (givenDateString) => {
    const createDate = jest.fn().mockReturnValue(42);

    const actual = dateAdapter.parse(givenDateString, createDate);

    expect(createDate).not.toHaveBeenCalled();
    expect(actual).toBeUndefined();
  });

  test.each([
    [false, ""],
    [null, ""],
    [undefined, ""],
    [new Date("2020-10-21"), "21.10.2020"],
  ])("formats: %s", () => {
    const actual = dateAdapter.format(false);
    expect(actual).toBe("");
  });
});
