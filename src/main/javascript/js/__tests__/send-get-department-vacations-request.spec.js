import fetchMock from "fetch-mock";
import { parseISO as dateParseISOSpy } from "date-fns/parseISO";
import { format as dateFormatSpy } from "date-fns";
import sendGetDepartmentVacationsRequest from "../send-get-department-vacations-request";

vi.mock("date-fns/parseISO", async () => {
  const original = await vi.importActual("date-fns/parseISO");
  return {
    ...original,
    parseISO: vi.fn(original.parseISO),
  };
});

vi.mock("date-fns", async () => {
  const original = await vi.importActual("date-fns");
  return {
    ...original,
    format: vi.fn(original.format),
  };
});

describe("send-get-department-vacations-request", () => {
  beforeAll(function () {
    fetchMock.mockGlobal();
  });

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
    fetchMock.removeRoutes();
    fetchMock.clearHistory();
    vi.clearAllMocks();
  });

  it("does nothing when startDate end endDate are not defined", async () => {
    await sendGetDepartmentVacationsRequest("", undefined, undefined, "", "");

    expect(fetchMock.callHistory.calls()).toHaveLength(0);
  });

  it("does nothing when startDate is after endDate", async () => {
    const startDate = new Date(2020, 7, 31);
    const endDate = new Date(2020, 7, 19);

    await sendGetDepartmentVacationsRequest("", startDate, endDate, "", "");

    expect(fetchMock.callHistory.calls()).toHaveLength(0);
  });

  it("renders response", async () => {
    fetchMock.route(`urlprefix/persons/1337/vacations?from=2020-08-16&to=2020-08-31&ofDepartmentMembers`, {
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
      '<p>1 i18n:application.applier.applicationsOfColleagues</p><ul class="m-0 p-0"><li class="flex items-center pt-2">Bruce Wayne:</li><li class="flex items-center pl-5"><span class="text-emerald-500 absolute -ml-5" title="undefined"><svg fill="none" viewBox="0 0 24 24" stroke="currentColor" width="16px" height="16px" class="w-4 h-4 stroke-2" role="img" aria-hidden="true" focusable="false"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"></path></svg></span>19.08.2020 - 21.08.2020</li></ul>',
    );
  });

  it("renders empty response", async () => {
    fetchMock.route(`urlprefix/persons/1337/vacations?from=2020-08-16&to=2020-08-31&ofDepartmentMembers`, {
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
    expect(div.innerHTML).toBe("<p>0 i18n:application.applier.applicationsOfColleagues</p>");
  });
});
