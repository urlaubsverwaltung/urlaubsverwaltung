import sendGetDaysRequestForTurnOfTheYear from "../../send-get-days-request-for-turn-of-the-year";

describe("overview-app-list", function () {
  afterEach(function () {
    delete globalThis.sendGetDaysRequestForTurnOfTheYear;
  });

  it("exposes sendGetDaysRequestForTurnOfTheYear on globalThis", async function () {
    await import("../overview-app-list");

    expect(globalThis.sendGetDaysRequestForTurnOfTheYear).toBe(sendGetDaysRequestForTurnOfTheYear);
  });
});
