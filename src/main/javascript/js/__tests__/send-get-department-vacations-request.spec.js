import fetchMock from "fetch-mock";
import { parseISO as dateParseISOSpy } from "date-fns/parseISO";
import { format as dateFormatSpy } from "date-fns/format";
import sendGetDepartmentVacationsRequest from "../send-get-department-vacations-request";

jest.mock("date-fns/parseISO", () => {
  const original = jest.requireActual("date-fns/parseISO");
  return {
    parseISO: jest.fn(original.parseISO),
  };
});

jest.mock("date-fns/format", () => {
  const original = jest.requireActual("date-fns/format");
  return {
    format: jest.fn(original.format),
  };
});

describe("send-get-department-vacations-request", () => {
  beforeEach(() => {
    globalThis.uv = {
      i18n: {
        "application.applier.applicationsOfColleagues": "i18n:application.applier.applicationsOfColleagues",
        "application.applier.none": "i18n:application.applier.none",
      },
    };
  });

  afterEach(() => {
    // cleanup DOM
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
    // and reset mocks
    fetchMock.restore();
    jest.clearAllMocks();
  });

  it("does nothing when startDate end endDate are not defined", async () => {
    await sendGetDepartmentVacationsRequest("", undefined, undefined, "", "");

    expect(fetchMock.calls()).toHaveLength(0);
  });

  it("does nothing when startDate is after endDate", async () => {
    const startDate = new Date(2020, 7, 31);
    const endDate = new Date(2020, 7, 19);

    await sendGetDepartmentVacationsRequest("", startDate, endDate, "", "");

    expect(fetchMock.calls()).toHaveLength(0);
  });

  it("renders response", async () => {
    fetchMock.mock(`urlprefix/persons/1337/vacations?from=2020-08-16&to=2020-08-31&ofDepartmentMembers`, {
      vacations: [
        {
          status: "ALLOWED",
          from: "2020-08-19",
          to: "2020-08-21",
          person: {
            niceName: "Bruce Wayne",
          },
        },
      ],
    });

    const div = document.createElement("div");
    div.setAttribute("id", "element");
    document.body.append(div);

    const urlPrefix = "urlprefix";
    const startDate = new Date(2020, 7, 16);
    const endDate = new Date(2020, 7, 31);
    const personId = "1337";
    const elementSelector = "#element";

    await sendGetDepartmentVacationsRequest(urlPrefix, startDate, endDate, personId, elementSelector);

    expect(dateParseISOSpy).toHaveBeenCalledWith("2020-08-19");
    expect(dateParseISOSpy).toHaveBeenCalledWith("2020-08-21");
    expect(dateFormatSpy).toHaveBeenCalledWith(new Date(2020, 7, 19), "dd.MM.yyyy");
    expect(dateFormatSpy).toHaveBeenCalledWith(new Date(2020, 7, 21), "dd.MM.yyyy");
    expect(div.innerHTML).toBe(
      '1 i18n:application.applier.applicationsOfColleagues<ul class="tw-m-0 tw-p-0"><li class="tw-flex tw-items-center tw-pt-2">Bruce Wayne:</li><li class="tw-flex tw-items-center tw-pl-5"><span class="tw-text-emerald-500 tw-absolute tw--ml-5" title="undefined"><svg fill="none" viewBox="0 0 24 24" stroke="currentColor" width="16px" height="16px" class="tw-w-4 tw-h-4 tw-stroke-2" role="img" aria-hidden="true" focusable="false"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"></path></svg></span>19.08.2020 - 21.08.2020</li></ul>',
    );
  });

  it("renders empty response", async () => {
    fetchMock.mock(`urlprefix/persons/1337/vacations?from=2020-08-16&to=2020-08-31&ofDepartmentMembers`, {
      vacations: [],
    });

    const div = document.createElement("div");
    div.setAttribute("id", "element");
    document.body.append(div);

    const urlPrefix = "urlprefix";
    const startDate = new Date(2020, 7, 16);
    const endDate = new Date(2020, 7, 31);
    const personId = "1337";
    const elementSelector = "#element";

    await sendGetDepartmentVacationsRequest(urlPrefix, startDate, endDate, personId, elementSelector);

    expect(dateParseISOSpy).not.toHaveBeenCalled();
    expect(div.innerHTML).toBe("0 i18n:application.applier.applicationsOfColleagues");
  });
});
