import { chartDefaults, noHoverStates } from "../chart-defaults";

describe("chartDefaults", () => {
  test("enables animations when reduced motion is not preferred", () => {
    expect(chartDefaults(false)).toEqual({
      animations: { enabled: true, speed: 200 },
      toolbar: { show: false },
    });
  });

  test("disables animations when reduced motion is preferred", () => {
    expect(chartDefaults(true)).toEqual({
      animations: { enabled: false, speed: 200 },
      toolbar: { show: false },
    });
  });
});

describe("noHoverStates", () => {
  test("disables both hover and active filters", () => {
    expect(noHoverStates).toEqual({
      hover: { filter: { type: "none" } },
      active: { filter: { type: "none" } },
    });
  });
});
