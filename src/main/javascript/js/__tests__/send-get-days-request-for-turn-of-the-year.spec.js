import fetchMock from "fetch-mock";
import sendGetDaysRequestForTurnOfTheYear from "../send-get-days-request-for-turn-of-the-year";

describe("send-get-days-request-for-turn-of-the-year", function () {
  beforeAll(function () {
    fetchMock.mockGlobal();
  });

  beforeEach(function () {
    globalThis.uv = { language: "de", i18n: { "application.applier.inYear": "in" } };
  });

  afterEach(function () {
    while (document.body.firstChild) {
      document.body.firstChild.remove();
    }
    fetchMock.removeRoutes();
    fetchMock.clearHistory();
    vi.clearAllMocks();
  });

  it("does not fetch anything when 'personId', 'startDate' and 'toDate' is not given", async function () {
    const urlPrefix = "/url-prefix";
    const startDate = "";
    const toDate = "";
    const dayLength = "FULL";
    const personId = "";
    const elementSelector = "#awesome-element";

    document.body.innerHTML = `<div id="awesome-element"></div>`;

    await sendGetDaysRequestForTurnOfTheYear(urlPrefix, startDate, toDate, dayLength, personId, elementSelector);

    expect(fetchMock.callHistory.called()).toBeFalsy();
  });

  it("clears the given elements html content when 'personId', 'startDate' and 'toDate' is not given", async function () {
    const urlPrefix = "/url-prefix";
    const startDate = "";
    const toDate = "";
    const dayLength = "FULL";
    const personId = "";
    const elementSelector = "#awesome-element";

    document.body.innerHTML = `<div id="awesome-element">most awesome content</div>`;

    await sendGetDaysRequestForTurnOfTheYear(urlPrefix, startDate, toDate, dayLength, personId, elementSelector);

    expect(document.body.innerHTML).not.toContain("most awesome content");
  });

  it("does not fetch anything when 'startDate' is after 'toDate'", async function () {
    const urlPrefix = "/url-prefix";
    const startDate = new Date("2021-09-20");
    const toDate = new Date("2021-09-19");
    const dayLength = "FULL";
    const personId = "1";
    const elementSelector = "#awesome-element";

    document.body.innerHTML = `<div id="awesome-element"></div>`;

    await sendGetDaysRequestForTurnOfTheYear(urlPrefix, startDate, toDate, dayLength, personId, elementSelector);

    expect(fetchMock.callHistory.called()).toBeFalsy();
  });

  it("clears the given elements html content when 'startDate' is after 'toDate'", async function () {
    const urlPrefix = "/url-prefix";
    const startDate = new Date("2021-09-20");
    const toDate = new Date("2021-09-19");
    const dayLength = "FULL";
    const personId = "1";
    const elementSelector = "#awesome-element";

    document.body.innerHTML = `<div id="awesome-element">most awesome content</div>`;

    await sendGetDaysRequestForTurnOfTheYear(urlPrefix, startDate, toDate, dayLength, personId, elementSelector);

    expect(document.body.innerHTML).not.toContain("most awesome content");
  });

  it("fetches info for all years, async function", async function () {
    fetchMock.route("/url-prefix/persons/1/workdays?from=2021-12-20&to=2021-12-31&length=FULL", {
      workDays: "1.0",
    });
    fetchMock.route("/url-prefix/persons/1/workdays?from=2022-01-01&to=2022-12-31&length=FULL", {
      workDays: "2.0",
    });
    fetchMock.route("/url-prefix/persons/1/workdays?from=2023-01-01&to=2023-01-07&length=FULL", {
      workDays: "3.0",
    });

    const urlPrefix = "/url-prefix";
    const startDate = new Date("2021-12-20");
    const toDate = new Date("2023-01-07");
    const dayLength = "FULL";
    const personId = "1";
    const elementSelector = "#awesome-element";

    document.body.innerHTML = `<div id="awesome-element">most awesome content</div>`;

    await sendGetDaysRequestForTurnOfTheYear(urlPrefix, startDate, toDate, dayLength, personId, elementSelector);

    expect(document.querySelector("#awesome-element").innerHTML).toEqual("<br>(1 in 2021, 2 in 2022 und 3 in 2023)");
  });

  it("includes the new year when the period ends on new year's day", async function () {
    fetchMock.route("/url-prefix/persons/1/workdays?from=2025-12-29&to=2025-12-31&length=FULL", {
      workDays: "3.0",
    });
    fetchMock.route("/url-prefix/persons/1/workdays?from=2026-01-01&to=2026-01-01&length=FULL", {
      workDays: "0.0",
    });

    document.body.innerHTML = `<div id="awesome-element"></div>`;

    await sendGetDaysRequestForTurnOfTheYear(
      "/url-prefix",
      new Date(2025, 11, 29),
      new Date(2026, 0, 1),
      "FULL",
      "1",
      "#awesome-element",
    );

    expect(document.querySelector("#awesome-element").innerHTML).toEqual("<br>(3 in 2025 und 0 in 2026)");
  });

  it("uses the language and translation of the user", async function () {
    globalThis.uv = { language: "en", i18n: { "application.applier.inYear": "in" } };

    fetchMock.route("/url-prefix/persons/1/workdays?from=2021-12-20&to=2021-12-31&length=FULL", {
      workDays: "1.0",
    });
    fetchMock.route("/url-prefix/persons/1/workdays?from=2022-01-01&to=2022-01-07&length=FULL", {
      workDays: "2.0",
    });

    document.body.innerHTML = `<div id="awesome-element"></div>`;

    await sendGetDaysRequestForTurnOfTheYear(
      "/url-prefix",
      new Date(2021, 11, 20),
      new Date(2022, 0, 7),
      "FULL",
      "1",
      "#awesome-element",
    );

    expect(document.querySelector("#awesome-element").innerHTML).toEqual("<br>(1 in 2021 and 2 in 2022)");
  });

  it("does not add 'length' to requested url when dayLength is unknown", async function () {
    fetchMock.route("/url-prefix/persons/1/workdays?from=2021-12-20&to=2021-12-31", {
      workDays: "1.0",
    });

    fetchMock.route("/url-prefix/persons/1/workdays?from=2022-01-01&to=2022-01-07", {
      workDays: "2.0",
    });

    const urlPrefix = "/url-prefix";
    const startDate = new Date("2021-12-20");
    const toDate = new Date("2022-01-07");
    const dayLength = "";
    const personId = "1";
    const elementSelector = "#awesome-element";

    document.body.innerHTML = `<div id="awesome-element">most awesome content</div>`;

    await sendGetDaysRequestForTurnOfTheYear(urlPrefix, startDate, toDate, dayLength, personId, elementSelector);

    expect(document.querySelector("#awesome-element").innerHTML).toEqual("<br>(1 in 2021 und 2 in 2022)");
  });
});
