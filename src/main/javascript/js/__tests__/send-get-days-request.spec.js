import fetchMock from "fetch-mock";
import sendGetDaysRequest from "../send-get-days-request";
import sendGetDaysRequestForTurnOfTheYear from "../send-get-days-request-for-turn-of-the-year";

jest.mock("../send-get-days-request-for-turn-of-the-year");

describe("send-get-days-request", function () {
  beforeEach(function () {
    globalThis.uv = {
      // just reset keys set explicitly in the tests below
      i18n: {},
    };
  });

  afterEach(function () {
    while (document.body.firstChild) {
      document.body.firstChild.remove();
    }
    fetchMock.restore();
    jest.clearAllMocks();
  });

  it("does not fetch anything when 'personId', 'startDate' and 'toDate' is not given", async function () {
    const urlPrefix = "/url-prefix";
    const startDate = "";
    const toDate = "";
    const dayLength = "FULL";
    const personId = "";
    const elementSelector = "#awesome-element";

    document.body.innerHTML = `<div id="awesome-element"></div>`;

    await sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, personId, elementSelector);

    expect(fetchMock.called()).toBeFalsy();
  });

  it("clears the given elements html content when 'personId', 'startDate' and 'toDate' is not given", async function () {
    const urlPrefix = "/url-prefix";
    const startDate = "";
    const toDate = "";
    const dayLength = "FULL";
    const personId = "";
    const elementSelector = "#awesome-element";

    document.body.innerHTML = `<div id="awesome-element">most awesome content</div>`;

    await sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, personId, elementSelector);

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

    await sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, personId, elementSelector);

    expect(fetchMock.called()).toBeFalsy();
  });

  it("clears the given elements html content when 'startDate' is after 'toDate'", async function () {
    const urlPrefix = "/url-prefix";
    const startDate = new Date("2021-09-20");
    const toDate = new Date("2021-09-19");
    const dayLength = "FULL";
    const personId = "1";
    const elementSelector = "#awesome-element";

    document.body.innerHTML = `<div id="awesome-element">most awesome content</div>`;

    await sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, personId, elementSelector);

    expect(document.body.innerHTML).not.toContain("most awesome content");
  });

  it("renders 'invalid periods' when response returns no workDays", async function () {
    fetchMock.mock("/url-prefix/persons/1/workdays?from=2021-09-19&to=2021-09-19&length=FULL", {});

    globalThis.uv.i18n["application.applier.invalidPeriod"] = "booooo, invalid!";

    const urlPrefix = "/url-prefix";
    const startDate = new Date("2021-09-19");
    const toDate = new Date("2021-09-19");
    const dayLength = "FULL";
    const personId = "1";
    const elementSelector = "#awesome-element";

    document.body.innerHTML = `<div id="awesome-element"></div>`;

    await sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, personId, elementSelector);

    expect(document.querySelector("#awesome-element").innerHTML).toEqual("booooo, invalid!");
  });

  it("renders workDays for one day", async function () {
    fetchMock.mock("/url-prefix/persons/1/workdays?from=2021-09-19&to=2021-09-19&length=FULL", {
      workDays: "1.0",
    });

    globalThis.uv.i18n["application.applier.day"] = "day";

    const urlPrefix = "/url-prefix";
    const startDate = new Date("2021-09-19");
    const toDate = new Date("2021-09-19");
    const dayLength = "FULL";
    const personId = "1";
    const elementSelector = "#awesome-element";

    document.body.innerHTML = `<div id="awesome-element"></div>`;

    await sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, personId, elementSelector);

    expect(document.querySelector("#awesome-element").innerHTML).toEqual("1 day");
  });

  it("renders workDays for multiple days", async function () {
    fetchMock.mock("/url-prefix/persons/1/workdays?from=2021-09-19&to=2021-09-19&length=FULL", {
      workDays: "1.5",
    });

    globalThis.uv.i18n["application.applier.days"] = "days";

    const urlPrefix = "/url-prefix";
    const startDate = new Date("2021-09-19");
    const toDate = new Date("2021-09-19");
    const dayLength = "FULL";
    const personId = "1";
    const elementSelector = "#awesome-element";

    document.body.innerHTML = `<div id="awesome-element"></div>`;

    await sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, personId, elementSelector);

    expect(document.querySelector("#awesome-element").innerHTML).toEqual("1.5 days");
  });

  it("removes 'hidden' css class when '#days-count' element exists", async function () {
    fetchMock.mock("/url-prefix/persons/1/workdays?from=2021-09-19&to=2021-09-19&length=FULL", {});

    const urlPrefix = "/url-prefix";
    const startDate = new Date("2021-09-19");
    const toDate = new Date("2021-09-19");
    const dayLength = "FULL";
    const personId = "1";
    const elementSelector = "#awesome-element";

    document.body.innerHTML = `
      <div id="days-count" class="hidden">
        <div id="awesome-element"></div>
      </div>
    `;

    await sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, personId, elementSelector);

    expect(document.querySelector("#days-count").classList.contains("hidden")).toBeFalsy();
  });

  it("does not fetch info for current year and next year when given dates have same year", async function () {
    fetchMock.mock("/url-prefix/persons/1/workdays?from=2021-12-20&to=2021-12-24&length=FULL", {
      workDays: "42",
    });

    const urlPrefix = "/url-prefix";
    const startDate = new Date("2021-12-20");
    const toDate = new Date("2021-12-24");
    const dayLength = "FULL";
    const personId = "1";
    const elementSelector = "#awesome-element";

    document.body.innerHTML = `<div id="awesome-element"></div>`;

    await sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, personId, elementSelector);

    expect(sendGetDaysRequestForTurnOfTheYear).not.toHaveBeenCalled();
  });

  it("fetches info for current year and next year when given dates have different years", async function () {
    fetchMock.mock("/url-prefix/persons/1/workdays?from=2021-12-20&to=2022-01-07&length=FULL", {
      workDays: "42",
    });

    const urlPrefix = "/url-prefix";
    const startDate = new Date("2021-12-20");
    const toDate = new Date("2022-01-07");
    const dayLength = "FULL";
    const personId = "1";
    const elementSelector = "#awesome-element";

    document.body.innerHTML = `<div id="awesome-element"></div>`;

    await sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, personId, elementSelector);

    expect(sendGetDaysRequestForTurnOfTheYear).toHaveBeenCalledWith(
      "/url-prefix",
      new Date("2021-12-20"),
      new Date("2022-01-07"),
      "FULL",
      "1",
      "#awesome-element .days-turn-of-the-year",
    );
  });

  it("does not add 'length' to requested url when dayLength is unknown", async function () {
    fetchMock.mock("/url-prefix/persons/1/workdays?from=2021-09-19&to=2021-09-19", {
      workDays: "42",
    });

    const urlPrefix = "/url-prefix";
    const startDate = new Date("2021-09-19");
    const toDate = new Date("2021-09-19");
    const dayLength = "";
    const personId = "1";
    const elementSelector = "#awesome-element";

    document.body.innerHTML = `<div id="awesome-element"></div>`;

    await sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, personId, elementSelector);

    expect(document.body.innerHTML).toContain("42");
  });
});
