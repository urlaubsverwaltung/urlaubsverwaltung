import { navigate } from "../navigate";

describe("navigate", function () {
  it("sets location.href to the given url", function () {
    navigate("#some-target");

    expect(globalThis.location.href.endsWith("#some-target")).toBe(true);
  });
});
