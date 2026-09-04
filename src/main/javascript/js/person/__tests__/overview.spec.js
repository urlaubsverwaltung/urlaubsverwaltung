vi.mock("../../../components/calendar", () => ({}));

describe("person/overview", function () {
  beforeAll(async function () {
    await import("../overview");
  });

  beforeEach(function () {
    vi.useFakeTimers();
    vi.setSystemTime(new Date(2024, 5, 15)); // 2024-06-15, well clear of any year boundary
  });

  afterEach(function () {
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
    vi.useRealTimers();
    vi.clearAllMocks();
    globalThis.history.pushState({}, "", "/web/person/1/overview");
  });

  async function flushMicrotasks() {
    await vi.advanceTimersByTimeAsync(0);
  }

  function fakeHolidayService() {
    return {
      fetchPublicHolidays: vi.fn(() => Promise.resolve()),
      fetchAbsences: vi.fn(() => Promise.resolve()),
      fetchBlackoutPeriods: vi.fn(() => Promise.resolve()),
    };
  }

  async function renderAndLoad({ i18n } = {}) {
    document.body.innerHTML = `<div id="datepicker"></div>`;

    const holidayServiceInstance = fakeHolidayService();
    globalThis.Urlaubsverwaltung = {
      HolidayService: { create: vi.fn(() => holidayServiceInstance) },
      Calendar: { init: vi.fn() },
    };
    globalThis.uv = {
      personId: "7",
      webPrefix: "/web",
      apiPrefix: "/api",
      i18n,
    };

    document.dispatchEvent(new Event("DOMContentLoaded", { bubbles: true, cancelable: true }));
    // multiple microtask hops: initCalendar's Promise.all + the async DOMContentLoaded handler itself
    await flushMicrotasks();
    await flushMicrotasks();

    return holidayServiceInstance;
  }

  it("creates the holiday service with webPrefix/apiPrefix/personId (as a number)", async function () {
    await renderAndLoad();

    expect(globalThis.Urlaubsverwaltung.HolidayService.create).toHaveBeenCalledWith("/web", "/api", 7);
  });

  it("initializes the calendar on #datepicker with the holiday service and today's date", async function () {
    const holidayServiceInstance = await renderAndLoad();

    expect(globalThis.Urlaubsverwaltung.Calendar.init).toHaveBeenCalledTimes(1);
    const [parentElement, holidayService, date] = globalThis.Urlaubsverwaltung.Calendar.init.mock.calls[0];
    expect(parentElement).toBe(document.querySelector("#datepicker"));
    expect(holidayService).toBe(holidayServiceInstance);
    expect(date).toEqual(new Date(2024, 5, 15));
  });

  it("fetches public holidays and absences for the single year around today by default", async function () {
    const holidayServiceInstance = await renderAndLoad();

    expect(holidayServiceInstance.fetchPublicHolidays).toHaveBeenCalledTimes(1);
    expect(holidayServiceInstance.fetchPublicHolidays).toHaveBeenCalledWith(2024);
    expect(holidayServiceInstance.fetchAbsences).toHaveBeenCalledTimes(1);
    expect(holidayServiceInstance.fetchAbsences).toHaveBeenCalledWith(2024);
    expect(holidayServiceInstance.fetchBlackoutPeriods).toHaveBeenCalledTimes(1);
    expect(holidayServiceInstance.fetchBlackoutPeriods).toHaveBeenCalledWith(2024);
  });

  it("snaps to January 1st of the requested year and fetches every spanned year", async function () {
    globalThis.history.pushState({}, "", "/web/person/1/overview?year=2020");

    const holidayServiceInstance = await renderAndLoad();

    // subMonths(2020-01-01, 5) -> 2019, addMonths(2020-01-01, 5) -> 2020
    expect(holidayServiceInstance.fetchPublicHolidays).toHaveBeenCalledWith(2019);
    expect(holidayServiceInstance.fetchPublicHolidays).toHaveBeenCalledWith(2020);
    expect(holidayServiceInstance.fetchAbsences).toHaveBeenCalledWith(2019);
    expect(holidayServiceInstance.fetchAbsences).toHaveBeenCalledWith(2020);
    expect(holidayServiceInstance.fetchBlackoutPeriods).toHaveBeenCalledWith(2019);
    expect(holidayServiceInstance.fetchBlackoutPeriods).toHaveBeenCalledWith(2020);

    const date = globalThis.Urlaubsverwaltung.Calendar.init.mock.calls[0][2];
    expect(date).toEqual(new Date(2020, 0, 1));
  });

  it("does not snap the date when the requested year already matches today's year", async function () {
    globalThis.history.pushState({}, "", "/web/person/1/overview?year=2024");

    await renderAndLoad();

    const date = globalThis.Urlaubsverwaltung.Calendar.init.mock.calls[0][2];
    expect(date).toEqual(new Date(2024, 5, 15));
  });

  describe("i18n", function () {
    it("resolves a known key from globalThis.uv.i18n", async function () {
      await renderAndLoad({ i18n: { "some.key": "Translated text" } });

      const i18n = globalThis.Urlaubsverwaltung.Calendar.init.mock.calls[0][3];
      expect(i18n("some.key")).toBe("Translated text");
    });

    it("falls back to a placeholder for an unknown key", async function () {
      await renderAndLoad({ i18n: {} });

      const i18n = globalThis.Urlaubsverwaltung.Calendar.init.mock.calls[0][3];
      expect(i18n("missing.key")).toBe("/i18n:missing.key/");
    });

    it("falls back to a placeholder when no i18n messages are configured at all", async function () {
      await renderAndLoad({ i18n: undefined });

      const i18n = globalThis.Urlaubsverwaltung.Calendar.init.mock.calls[0][3];
      expect(i18n("missing.key")).toBe("/i18n:missing.key/");
    });
  });
});
