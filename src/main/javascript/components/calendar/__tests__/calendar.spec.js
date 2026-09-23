import fetchMock from "fetch-mock";
import { parseISO } from "date-fns";

describe("calendar", () => {
  const RealDate = Date;
  const dateInstanceIdentifier = Symbol("date-identifier");

  // mocking Date with overridden instanceof operator o_O phew°°°
  // required since datefn verifies with foo instanceof Date
  function mockDate(isoDate) {
    /* eslint-disable unicorn/prevent-abbreviations */

    globalThis.Date = class extends RealDate {
      static [Symbol.hasInstance](instance) {
        return instance[dateInstanceIdentifier];
      }

      static now() {
        return new RealDate(isoDate).getTime();
      }

      // noinspection JSAnnotator
      constructor(...args) {
        // NOSONAR
        let d = args.length === 0 ? new RealDate(isoDate) : new RealDate(...args);
        d[dateInstanceIdentifier] = true;
        return d;
      }
    };
  }

  beforeAll(() => {
    fetchMock.mockGlobal();
  });

  afterEach(() => {
    vi.resetModules();
  });

  beforeEach(() => {
    globalThis.matchMedia = vi.fn().mockReturnValue({ matches: false, addEventListener: vi.fn() });
  });

  afterEach(() => {
    fetchMock.removeRoutes();
    fetchMock.clearHistory();
    globalThis.Date = RealDate;
  });

  it("renders", async () => {
    // 01.12.2017
    mockDate(1_512_130_448_379);
    await calendarTestSetup();

    renderCalendar(createHolidayService());
    expect(document.body).toMatchSnapshot();
  });

  it("clicking on empty day redirects to new application", async () => {
    // today is 2017-12-01
    mockDate(1_512_130_448_379);

    fetchMock.route(
      "/persons/42/absences?from=2017-01-01&to=2017-12-31&absence-types=vacation%2Csick_note%2Cno_workday",
      {
        absences: [],
      },
    );

    await calendarTestSetup();

    const holidayService = createHolidayService({ personId: 42 });
    holidayService.bookHoliday = vi.fn();
    // fetch personal holiday data and cache the (mocked) response
    // the response is used when the calendar renders
    await holidayService.fetchAbsences(2017);

    renderCalendar(holidayService);

    const someDay = document.querySelector(`.datepicker-day[data-datepicker-date="2017-12-15"]`);
    expect(someDay).toBeTruthy();
    someDay.dispatchEvent(new MouseEvent("mousedown", { bubbles: true, button: 0 }));
    someDay.click();

    expect(holidayService.bookHoliday).toHaveBeenCalledWith(parseISO("2017-12-15"), parseISO("2017-12-15"));
  });

  it("clicking on an empty day in the past redirects to new application", async () => {
    // today is 2017-12-01
    mockDate(1_512_130_448_379);
    mockEmptyYear(42, 2017);

    await calendarTestSetup();

    const holidayService = createHolidayService({ personId: 42 });
    holidayService.bookHoliday = vi.fn();
    await holidayService.fetchAbsences(2017);

    renderCalendar(holidayService);

    const day = document.querySelector(`.datepicker-day[data-datepicker-date="2017-11-28"]`);
    expect(day.dataset.datepickerSelectable).toBe("true");
    day.dispatchEvent(new MouseEvent("mousedown", { bubbles: true, button: 0 }));
    day.click();

    expect(holidayService.bookHoliday).toHaveBeenCalledWith(new Date(2017, 10, 28), new Date(2017, 10, 28));
  });

  it("clicking on day with absence redirects to existing application", async () => {
    // today is 2017-12-01
    mockDate(1_512_130_448_379);

    fetchMock.route(
      "/persons/42/absences?from=2017-01-01&to=2017-12-31&absence-types=vacation%2Csick_note%2Cno_workday",
      {
        absences: [
          {
            date: "2017-12-15",
            absenceType: "VACATION",
            id: 1337,
            absent: "FULL",
            absentNumeric: 1,
            status: "ALLOWED",
            typeId: 1,
          },
        ],
      },
    );

    await calendarTestSetup();

    const holidayService = createHolidayService({ personId: 42 });
    holidayService.navigateToApplicationForLeave = vi.fn();
    // fetch personal holiday data and cache the (mocked) response
    // the response is used when the calendar renders
    await holidayService.fetchAbsences(2017);

    renderCalendar(holidayService);

    const someDay = document.querySelector(`.datepicker-day[data-datepicker-date="2017-12-15"]`);
    expect(someDay).toBeTruthy();
    someDay.click();

    expect(holidayService.navigateToApplicationForLeave).toHaveBeenCalledWith("1337");
  });

  describe("when the signed in user is not allowed to apply for leave for the person", () => {
    it("does not redirect to new application when clicking on an empty day", async () => {
      // today is 2017-12-01
      mockDate(1_512_130_448_379);

      fetchMock.route(
        "/persons/42/absences?from=2017-01-01&to=2017-12-31&absence-types=vacation%2Csick_note%2Cno_workday",
        {
          absences: [],
        },
      );

      await calendarTestSetup();

      const holidayService = createHolidayService({ personId: 42 });
      holidayService.bookHoliday = vi.fn();
      await holidayService.fetchAbsences(2017);

      renderCalendar(holidayService, { canApplyForLeave: false });

      const someDay = document.querySelector(`.datepicker-day[data-datepicker-date="2017-12-15"]`);
      expect(someDay).toBeTruthy();
      someDay.dispatchEvent(new MouseEvent("mousedown", { bubbles: true, button: 0 }));
      someDay.click();

      expect(holidayService.bookHoliday).not.toHaveBeenCalled();
    });

    it("renders an empty day as not selectable", async () => {
      // today is 2017-12-01
      mockDate(1_512_130_448_379);

      fetchMock.route(
        "/persons/42/absences?from=2017-01-01&to=2017-12-31&absence-types=vacation%2Csick_note%2Cno_workday",
        {
          absences: [],
        },
      );

      await calendarTestSetup();

      const holidayService = createHolidayService({ personId: 42 });
      await holidayService.fetchAbsences(2017);

      renderCalendar(holidayService, { canApplyForLeave: false });

      const someDay = document.querySelector(`.datepicker-day[data-datepicker-date="2017-12-15"]`);
      expect(someDay.dataset.datepickerSelectable).toBe("false");
    });

    it("does not select days when dragging over the calendar", async () => {
      // today is 2017-12-01
      mockDate(1_512_130_448_379);

      fetchMock.route(
        "/persons/42/absences?from=2017-01-01&to=2017-12-31&absence-types=vacation%2Csick_note%2Cno_workday",
        {
          absences: [],
        },
      );

      await calendarTestSetup();

      const holidayService = createHolidayService({ personId: 42 });
      await holidayService.fetchAbsences(2017);

      renderCalendar(holidayService, { canApplyForLeave: false });

      const dayFrom = document.querySelector(`.datepicker-day[data-datepicker-date="2017-12-15"]`);
      const dayTo = document.querySelector(`.datepicker-day[data-datepicker-date="2017-12-18"]`);
      dayFrom.dispatchEvent(new MouseEvent("mousedown", { bubbles: true, button: 0 }));
      dayTo.dispatchEvent(new MouseEvent("mouseover", { bubbles: true }));

      expect(document.querySelectorAll(`.datepicker-day-selected`)).toHaveLength(0);
    });

    it("still marks a day with an absence as selectable", async () => {
      // today is 2017-12-01
      mockDate(1_512_130_448_379);

      fetchMock.route(
        "/persons/42/absences?from=2017-01-01&to=2017-12-31&absence-types=vacation%2Csick_note%2Cno_workday",
        {
          absences: [
            {
              date: "2017-12-15",
              absenceType: "VACATION",
              id: 1337,
              absent: "FULL",
              absentNumeric: 1,
              status: "ALLOWED",
              typeId: 1,
            },
          ],
        },
      );

      await calendarTestSetup();

      const holidayService = createHolidayService({ personId: 42 });
      await holidayService.fetchAbsences(2017);

      renderCalendar(holidayService, { canApplyForLeave: false });

      // the day leads to the absence, therefore it stays clickable and keeps the pointer cursor
      const someDay = document.querySelector(`.datepicker-day[data-datepicker-date="2017-12-15"]`);
      expect(someDay.dataset.datepickerSelectable).toBe("true");
    });

    it("still redirects to an existing application when clicking on a day with an absence", async () => {
      // today is 2017-12-01
      mockDate(1_512_130_448_379);

      fetchMock.route(
        "/persons/42/absences?from=2017-01-01&to=2017-12-31&absence-types=vacation%2Csick_note%2Cno_workday",
        {
          absences: [
            {
              date: "2017-12-15",
              absenceType: "VACATION",
              id: 1337,
              absent: "FULL",
              absentNumeric: 1,
              status: "ALLOWED",
              typeId: 1,
            },
          ],
        },
      );

      await calendarTestSetup();

      const holidayService = createHolidayService({ personId: 42 });
      holidayService.navigateToApplicationForLeave = vi.fn();
      await holidayService.fetchAbsences(2017);

      renderCalendar(holidayService, { canApplyForLeave: false });

      const someDay = document.querySelector(`.datepicker-day[data-datepicker-date="2017-12-15"]`);
      expect(someDay).toBeTruthy();
      someDay.click();

      expect(holidayService.navigateToApplicationForLeave).toHaveBeenCalledWith("1337");
    });
  });

  describe.each([[`.datepicker-prev`], [`.datepicker-next`]])(
    "ensure correct rendering when clicking %s ",
    (buttonSelector) => {
      test("click", async () => {
        // 01.12.2017
        mockDate(1_512_130_448_379);

        fetchMock.route(
          "/persons/42/absences?from=2017-01-01&to=2017-12-31&absence-types=vacation%2Csick_note%2Cno_workday",
          {
            absences: [
              {
                date: "2017-12-01",
                absenceType: "VACATION",
                absent: "FULL",
                absentNumeric: 1,
                status: "ALLOWED",
                typeId: 1,
              },
            ],
          },
        );

        fetchMock.route(`/persons/42/public-holidays?from=2017-01-01&to=2017-12-31`, {
          publicHolidays: [
            {
              date: "2017-12-25",
              description: "Christmas",
              dayLength: "1",
              absencePeriodName: "FULL",
            },
          ],
        });

        mockEmptyYear(42, 2018);

        await calendarTestSetup();

        renderCalendar(createHolidayService({ webPrefix: "", apiPrefix: "", personId: 42 }));

        const button = document.querySelector(buttonSelector);
        expect(button).toBeTruthy();
        button.click();

        expect(document.body).toMatchSnapshot();
      });
    },
  );

  describe.each([["ALLOWED"], ["WAITING"], ["TEMPORARY_ALLOWED"]])(
    "ensure vacation of status=%s is clickable",
    (givenStatus) => {
      test("in the past", async () => {
        // today is 2017-12-01
        mockDate(1_512_130_448_379);

        // personId -> createHolidayService (param)
        // year -> holidayService.fetchPersonal (param)
        // type -> holidayService.fetchPersonal (implementation detail)
        fetchMock.route(
          "/persons/42/absences?from=2017-01-01&to=2017-12-31&absence-types=vacation%2Csick_note%2Cno_workday",
          {
            absences: [
              {
                date: "2017-11-01",
                absenceType: "VACATION",
                absent: "FULL",
                absentNumeric: 1,
                status: givenStatus,
                typeId: 1,
              },
            ],
          },
        );

        await calendarTestSetup();

        const holidayService = createHolidayService({ personId: 42 });
        // fetch personal holiday data and cache the (mocked) response
        // the response is used when the calendar renders
        await holidayService.fetchAbsences(2017);

        renderCalendar(holidayService);

        const $ = document.querySelector.bind(document);
        expect(
          $(
            '[data-datepicker-date="2017-11-01"][data-datepicker-absence-type="VACATION"][data-datepicker-selectable="true"]',
          ),
        ).toBeTruthy();
      });

      test("in the future", async () => {
        // today is 2017-12-01
        mockDate(1_512_130_448_379);

        // personId -> createHolidayService (param)
        // year -> holidayService.fetchPersonal (param)
        // type -> holidayService.fetchPersonal (implementation detail)
        fetchMock.route(
          "/persons/42/absences?from=2017-01-01&to=2017-12-31&absence-types=vacation%2Csick_note%2Cno_workday",
          {
            absences: [
              {
                date: "2017-12-05",
                absenceType: "VACATION",
                absent: "FULL",
                absentNumeric: 1,
                status: givenStatus,
                typeId: 1,
              },
            ],
          },
        );

        await calendarTestSetup();

        const holidayService = createHolidayService({ personId: 42 });
        // fetch personal holiday data and cache the (mocked) response
        // the response is used when the calendar renders
        await holidayService.fetchAbsences(2017);

        renderCalendar(holidayService);

        const $ = document.querySelector.bind(document);
        expect(
          $(
            '[data-datepicker-date="2017-12-05"][data-datepicker-absence-type="VACATION"][data-datepicker-selectable="true"]',
          ),
        ).toBeTruthy();
      });

      test("today", async () => {
        // today is 2017-12-01
        mockDate(1_512_130_448_379);

        // personId -> createHolidayService (param)
        // year -> holidayService.fetchPersonal (param)
        // type -> holidayService.fetchPersonal (implementation detail)
        fetchMock.route(
          "/persons/42/absences?from=2017-01-01&to=2017-12-31&absence-types=vacation%2Csick_note%2Cno_workday",
          {
            absences: [
              {
                date: "2017-12-01",
                absenceType: "VACATION",
                absent: "FULL",
                absentNumeric: 1,
                status: givenStatus,
                typeId: 1,
              },
            ],
          },
        );

        await calendarTestSetup();

        const holidayService = createHolidayService({ personId: 42 });
        // fetch personal holiday data and cache the (mocked) response
        // the response is used when the calendar renders
        await holidayService.fetchAbsences(2017);

        renderCalendar(holidayService);

        const $ = document.querySelector.bind(document);
        expect(
          $(
            '[data-datepicker-date="2017-12-01"][data-datepicker-absence-type="VACATION"][data-datepicker-selectable="true"]',
          ),
        ).toBeTruthy();
      });
    },
  );

  describe.each([
    ["VACATION", "MORNING"],
    ["VACATION", "NOON"],
    ["SICK_NOTE", "MORNING"],
    ["SICK_NOTE", "NOON"],
  ])("ensure half day %s in the %s is clickable", (givenAbsenceType, givenAbsent) => {
    async function setupHalfDayAbsence() {
      // today is 2017-12-01
      mockDate(1_512_130_448_379);

      fetchMock.route(
        "/persons/42/absences?from=2017-01-01&to=2017-12-31&absence-types=vacation%2Csick_note%2Cno_workday",
        {
          absences: [
            {
              date: "2017-11-01",
              absenceType: givenAbsenceType,
              id: 1337,
              absent: givenAbsent,
              absentNumeric: 0.5,
              status: givenAbsenceType === "VACATION" ? "ALLOWED" : "ACTIVE",
              typeId: 1,
            },
          ],
        },
      );

      await calendarTestSetup();

      const holidayService = createHolidayService({ personId: 42 });
      await holidayService.fetchAbsences(2017);
      return holidayService;
    }

    test("in the past", async () => {
      renderCalendar(await setupHalfDayAbsence());

      const day = document.querySelector(`.datepicker-day[data-datepicker-date="2017-11-01"]`);
      expect(day.dataset.datepickerSelectable).toBe("true");
    });

    test("without the permission to apply for leave", async () => {
      renderCalendar(await setupHalfDayAbsence(), { canApplyForLeave: false });

      const day = document.querySelector(`.datepicker-day[data-datepicker-date="2017-11-01"]`);
      expect(day.dataset.datepickerSelectable).toBe("true");
    });
  });

  describe("highlights days", () => {
    test("weekend with absences", async () => {
      // today is 2020-12-13
      mockDate(1_607_848_867_000);

      // personId -> createHolidayService (param)
      // year -> holidayService.fetchPersonal (param)
      // type -> holidayService.fetchPersonal (implementation detail)
      fetchMock.route(
        "/persons/42/absences?from=2020-01-01&to=2020-12-31&absence-types=vacation%2Csick_note%2Cno_workday",
        {
          absences: [
            {
              date: "2020-12-05",
              id: 1,
              absent: "FULL",
              absenceType: "VACATION",
              status: "ALLOWED",
              typeId: 1,
            },
            {
              date: "2020-12-06",
              id: 2,
              absent: "MORNING",
              absenceType: "VACATION",
              status: "ALLOWED",
              typeId: 1,
            },
            {
              date: "2020-12-06",
              id: 3,
              absent: "NOON",
              absenceType: "SICK_NOTE",
              status: "ACTIVE",
            },
            {
              date: "2020-12-12",
              id: 4,
              absent: "MORNING",
              absenceType: "SICK_NOTE",
              status: "ACTIVE",
            },
            {
              date: "2020-12-12",
              id: 5,
              absent: "NOON",
              absenceType: "VACATION",
              status: "ALLOWED",
              typeId: 1,
            },
            {
              date: "2020-12-13",
              id: 6,
              absent: "FULL",
              absenceType: "SICK_NOTE",
              status: "ACTIVE",
            },
          ],
        },
      );

      await calendarTestSetup();

      const holidayService = createHolidayService({ personId: 42 });
      // fetch personal holiday data and cache the (mocked) response
      // the response is used when the calendar renders
      await holidayService.fetchAbsences(2020);

      renderCalendar(holidayService);

      const $ = document.querySelector.bind(document);
      expect(
        $(
          '[data-datepicker-date="2020-12-05"][class="datepicker-day datepicker-day-weekend datepicker-day-past datepicker-day-absence-full absence-full--solid"]',
        ),
      ).toBeTruthy();
      expect(
        $(
          '[data-datepicker-date="2020-12-06"][class="datepicker-day datepicker-day-weekend datepicker-day-past datepicker-day-absence-morning absence-morning--solid datepicker-day-sick-note-noon absence-noon--solid"]',
        ),
      ).toBeTruthy();
      expect(
        $(
          '[data-datepicker-date="2020-12-12"][class="datepicker-day datepicker-day-weekend datepicker-day-past datepicker-day-absence-noon absence-noon--solid datepicker-day-sick-note-morning absence-morning--solid"]',
        ),
      ).toBeTruthy();
      expect(
        $(
          '[data-datepicker-date="2020-12-13"][class="datepicker-day datepicker-day-today datepicker-day-weekend datepicker-day-sick-note-full absence-full--solid"]',
        ),
      ).toBeTruthy();
    });
  });

  describe("draws an absence of several days as one bar", () => {
    // today is 2017-12-01. 2017-12-11 is a monday
    async function renderWithAbsences(absences, publicHolidays = []) {
      mockDate(1_512_130_448_379);

      fetchMock.route(
        "/persons/42/absences?from=2017-01-01&to=2017-12-31&absence-types=vacation%2Csick_note%2Cno_workday",
        { absences },
      );
      fetchMock.route("/persons/42/public-holidays?from=2017-01-01&to=2017-12-31", { publicHolidays });

      await calendarTestSetup();

      const holidayService = createHolidayService({ personId: 42 });
      await holidayService.fetchAbsences(2017);
      await holidayService.fetchPublicHolidays(2017);

      renderCalendar(holidayService);
    }

    const vacation = (date, absent = "FULL", { id = 1, status = "ALLOWED" } = {}) => ({
      date,
      absent,
      absenceType: "VACATION",
      id,
      status,
      typeId: 1,
    });
    const sickNote = (date, absent = "FULL", { id = 2, status = "ACTIVE" } = {}) => ({
      date,
      absent,
      absenceType: "SICK_NOTE",
      id,
      status,
    });
    const noWorkday = (date) => ({ date, absent: "FULL", absenceType: "NO_WORKDAY", status: "ACTIVE" });
    const publicHoliday = (date) => ({ date, absencePeriodName: "FULL", dayLength: "0", description: "Christmas" });

    const classesOf = (date) => [...document.querySelector(`[data-datepicker-date="${date}"]`).classList];

    test("with rounded ends on the first and the last day only", async () => {
      await renderWithAbsences([vacation("2017-12-11"), vacation("2017-12-12"), vacation("2017-12-13")]);

      expect(classesOf("2017-12-11")).not.toContain("absence-full--continues-before");
      expect(classesOf("2017-12-11")).toContain("absence-full--continues-after");
      expect(classesOf("2017-12-12")).toContain("absence-full--continues-before");
      expect(classesOf("2017-12-12")).toContain("absence-full--continues-after");
      expect(classesOf("2017-12-13")).toContain("absence-full--continues-before");
      expect(classesOf("2017-12-13")).not.toContain("absence-full--continues-after");
    });

    test("ending where the next absence starts", async () => {
      await renderWithAbsences([vacation("2017-12-11", "FULL", { id: 1 }), vacation("2017-12-12", "FULL", { id: 3 })]);

      expect(classesOf("2017-12-11")).not.toContain("absence-full--continues-after");
      expect(classesOf("2017-12-12")).not.toContain("absence-full--continues-before");
    });

    test("bridging the weekend in between", async () => {
      await renderWithAbsences([
        vacation("2017-12-15"),
        noWorkday("2017-12-16"),
        noWorkday("2017-12-17"),
        vacation("2017-12-18"),
      ]);

      expect(classesOf("2017-12-15")).toContain("absence-full--continues-after");
      expect(classesOf("2017-12-16")).toEqual(
        expect.arrayContaining([
          "datepicker-day-absence-full",
          "absence-full--solid",
          "absence-full--bridge",
          "absence-full--continues-before",
          "absence-full--continues-after",
        ]),
      );
      expect(document.querySelector('[data-datepicker-date="2017-12-16"]').getAttribute("style")).toContain(
        "--absence-bar-color:var(--absence-color-#B4D455)",
      );
      expect(classesOf("2017-12-18")).toContain("absence-full--continues-before");
    });

    test("bridging public holidays in between", async () => {
      await renderWithAbsences(
        [vacation("2017-12-22"), noWorkday("2017-12-23"), noWorkday("2017-12-24"), vacation("2017-12-27")],
        [publicHoliday("2017-12-25"), publicHoliday("2017-12-26")],
      );

      expect(classesOf("2017-12-22")).toContain("absence-full--continues-after");
      expect(classesOf("2017-12-25")).toContain("absence-full--bridge");
      expect(classesOf("2017-12-26")).toContain("absence-full--bridge");
      expect(classesOf("2017-12-27")).toContain("absence-full--continues-before");
    });

    test("without a bridge over a weekend no absence continues across", async () => {
      await renderWithAbsences([vacation("2017-12-15"), noWorkday("2017-12-16"), noWorkday("2017-12-17")]);

      expect(classesOf("2017-12-15")).not.toContain("absence-full--continues-after");
      expect(classesOf("2017-12-16")).not.toContain("absence-full--bridge");
    });

    test("keeping the outline of a waiting absence on its bridge", async () => {
      await renderWithAbsences([
        vacation("2017-12-15", "FULL", { status: "WAITING" }),
        noWorkday("2017-12-16"),
        noWorkday("2017-12-17"),
        vacation("2017-12-18", "FULL", { status: "WAITING" }),
      ]);

      expect(classesOf("2017-12-16")).toEqual(
        expect.arrayContaining(["absence-full--outline", "absence-full--bridge"]),
      );
    });

    describe("filling the first half of the whole bar of a temporarily allowed vacation", () => {
      const temporarilyAllowed = (date, absent = "FULL") => vacation(date, absent, { status: "TEMPORARY_ALLOWED" });
      const fillOf = (date) =>
        document
          .querySelector(`[data-datepicker-date="${date}"]`)
          .getAttribute("style")
          .match(/--absence-bar-fill(-morning|-noon)?:(\d+)%/)
          ?.slice(1, 3);

      test("from monday to friday", async () => {
        await renderWithAbsences(
          ["2017-12-11", "2017-12-12", "2017-12-13", "2017-12-14", "2017-12-15"].map((date) =>
            temporarilyAllowed(date),
          ),
        );

        expect(
          ["2017-12-11", "2017-12-12", "2017-12-13", "2017-12-14", "2017-12-15"].map((date) => fillOf(date)),
        ).toEqual([
          [undefined, "100"],
          [undefined, "100"],
          [undefined, "50"],
          [undefined, "0"],
          [undefined, "0"],
        ]);
      });

      test("counting the bridged weekend in", async () => {
        await renderWithAbsences([
          temporarilyAllowed("2017-12-14"),
          temporarilyAllowed("2017-12-15"),
          noWorkday("2017-12-16"),
          noWorkday("2017-12-17"),
          temporarilyAllowed("2017-12-18"),
          temporarilyAllowed("2017-12-19"),
        ]);

        expect(
          ["2017-12-14", "2017-12-15", "2017-12-16", "2017-12-17", "2017-12-18", "2017-12-19"].map(
            (date) => fillOf(date)?.[1],
          ),
        ).toEqual(["100", "100", "100", "0", "0", "0"]);
      });

      test("as one day did before, when it is a single day", async () => {
        await renderWithAbsences([temporarilyAllowed("2017-12-13")]);

        expect(fillOf("2017-12-13")).toEqual([undefined, "50"]);
      });

      test("counting the half days of a bar starting in the afternoon", async () => {
        await renderWithAbsences([temporarilyAllowed("2017-12-13", "NOON"), temporarilyAllowed("2017-12-14")]);

        // three half days: the afternoon is the first half and a half, the next day the rest
        expect(fillOf("2017-12-13")).toEqual(["-noon", "100"]);
        expect(fillOf("2017-12-14")).toEqual([undefined, "25"]);
      });
    });

    test("in the sick note colour on the bridge of a sick note", async () => {
      await renderWithAbsences([
        sickNote("2017-12-15"),
        noWorkday("2017-12-16"),
        noWorkday("2017-12-17"),
        sickNote("2017-12-18"),
      ]);

      expect(classesOf("2017-12-16")).toEqual(
        expect.arrayContaining(["datepicker-day-sick-note-full", "absence-full--solid", "absence-full--bridge"]),
      );
    });

    test("ending in the middle of a day with a half-day absence", async () => {
      await renderWithAbsences([sickNote("2017-12-11"), sickNote("2017-12-12", "MORNING")]);

      expect(classesOf("2017-12-11")).toContain("absence-full--continues-after");
      expect(classesOf("2017-12-12")).toContain("absence-morning--continues-before");
      expect(classesOf("2017-12-12")).not.toContain("absence-morning--continues-after");
    });

    test("starting in the middle of a day with a half-day absence", async () => {
      await renderWithAbsences([vacation("2017-12-11", "NOON"), vacation("2017-12-12")]);

      expect(classesOf("2017-12-11")).not.toContain("absence-noon--continues-before");
      expect(classesOf("2017-12-11")).toContain("absence-noon--continues-after");
      expect(classesOf("2017-12-12")).toContain("absence-full--continues-before");
    });

    test("continuing into the next year after navigating to december", async () => {
      // today is 2018-06-01, the calendar shows february to november
      mockDate(1_527_847_200_000);

      for (const year of [2018, 2019]) {
        fetchMock.route(
          `/persons/42/absences?from=${year}-01-01&to=${year}-12-31&absence-types=vacation%2Csick_note%2Cno_workday`,
          { absences: year === 2018 ? [vacation("2018-12-31")] : [vacation("2019-01-01")] },
        );
        fetchMock.route(`/persons/42/public-holidays?from=${year}-01-01&to=${year}-12-31`, { publicHolidays: [] });
      }

      await calendarTestSetup();

      const holidayService = createHolidayService({ personId: 42 });
      await holidayService.fetchAbsences(2018);
      await holidayService.fetchPublicHolidays(2018);

      renderCalendar(holidayService);
      document.querySelector(".datepicker-next").click();

      await vi.waitFor(() => expect(document.querySelector('[data-datepicker-date="2018-12-31"]')).toBeTruthy());
      expect(classesOf("2018-12-31")).toContain("absence-full--continues-after");
    });

    test("continuing into the previous year after navigating to january", async () => {
      // today is 2019-06-01, the calendar shows february 2019 to november 2019
      mockDate(1_559_383_200_000);

      for (const year of [2018, 2019]) {
        fetchMock.route(
          `/persons/42/absences?from=${year}-01-01&to=${year}-12-31&absence-types=vacation%2Csick_note%2Cno_workday`,
          { absences: year === 2018 ? [vacation("2018-12-31")] : [vacation("2019-01-01")] },
        );
        fetchMock.route(`/persons/42/public-holidays?from=${year}-01-01&to=${year}-12-31`, { publicHolidays: [] });
      }

      await calendarTestSetup();

      const holidayService = createHolidayService({ personId: 42 });
      await holidayService.fetchAbsences(2019);
      await holidayService.fetchPublicHolidays(2019);

      renderCalendar(holidayService);
      document.querySelector(".datepicker-prev").click();

      await vi.waitFor(() => expect(document.querySelector('[data-datepicker-date="2019-01-01"]')).toBeTruthy());
      expect(classesOf("2019-01-01")).toContain("absence-full--continues-before");
    });

    test("bridging the afternoon off of a half working day", async () => {
      await renderWithAbsences([
        vacation("2017-12-15", "MORNING"),
        { ...noWorkday("2017-12-15"), absent: "NOON" },
        noWorkday("2017-12-16"),
        noWorkday("2017-12-17"),
        vacation("2017-12-18"),
      ]);

      expect(classesOf("2017-12-15")).toEqual(
        expect.arrayContaining([
          "absence-morning--continues-after",
          "absence-noon--bridge",
          "absence-noon--continues-before",
          "absence-noon--continues-after",
        ]),
      );
      expect(classesOf("2017-12-18")).toContain("absence-full--continues-before");
    });
  });

  test("ensure rendering of no-workdays", async () => {
    // today is 2020-12-13
    mockDate(1_607_848_867_000);

    // personId -> createHolidayService (param)
    // year -> holidayService.fetchPersonal (param)
    // type -> holidayService.fetchPersonal (implementation detail)
    fetchMock.route(
      "/persons/42/absences?from=2020-01-01&to=2020-12-31&absence-types=vacation%2Csick_note%2Cno_workday",
      {
        absences: [
          {
            // saturday
            date: "2020-12-05",
            absent: "FULL",
            absenceType: "NO_WORKDAY",
            status: "",
          },
          {
            // sunday
            date: "2020-12-06",
            absent: "FULL",
            absenceType: "NO_WORKDAY",
            status: "",
          },
          {
            // wednesday
            date: "2020-12-09",
            absent: "FULL",
            absenceType: "NO_WORKDAY",
            status: "",
          },
        ],
      },
    );

    await calendarTestSetup();

    const holidayService = createHolidayService({ personId: 42 });
    // fetch personal holiday data and cache the (mocked) response
    // the response is used when the calendar renders
    await holidayService.fetchAbsences(2020);

    renderCalendar(holidayService);

    const $ = document.querySelector.bind(document);
    expect(
      $('[data-datepicker-date="2020-12-05"][class="datepicker-day datepicker-day-weekend datepicker-day-past"] > svg'),
    ).toBeTruthy();
    expect(
      $('[data-datepicker-date="2020-12-06"][class="datepicker-day datepicker-day-weekend datepicker-day-past"] > svg'),
    ).toBeTruthy();
    expect($('[data-datepicker-date="2020-12-09"][class="datepicker-day datepicker-day-past"] > svg')).toBeTruthy();
  });

  test("ensure description of all public holidays on the same day is shown", async () => {
    // today is 2024-06-15 (so may 2024 is within the rendered months)
    mockDate(1_718_452_800_000);

    fetchMock.route(
      "/persons/42/absences?from=2024-01-01&to=2024-12-31&absence-types=vacation%2Csick_note%2Cno_workday",
      {
        absences: [],
      },
    );

    // croatia has two public holidays on 2024-05-30 (statehood day and corpus christi)
    fetchMock.route(`/persons/42/public-holidays?from=2024-01-01&to=2024-12-31`, {
      publicHolidays: [
        {
          date: "2024-05-30",
          description: "Statehood Day",
          dayLength: "1",
          absencePeriodName: "FULL",
        },
        {
          date: "2024-05-30",
          description: "Corpus Christi",
          dayLength: "1",
          absencePeriodName: "FULL",
        },
      ],
    });

    await calendarTestSetup();

    const holidayService = createHolidayService({ personId: 42 });
    // fetch personal holiday data and cache the (mocked) response
    // the response is used when the calendar renders
    await holidayService.fetchAbsences(2024);
    await holidayService.fetchPublicHolidays(2024);

    renderCalendar(holidayService);

    const $ = document.querySelector.bind(document);
    const day = $('[data-datepicker-date="2024-05-30"]');
    expect(day).toBeTruthy();
    expect(day.getAttribute("title")).toBe("Statehood Day, Corpus Christi");
  });

  it("marks days of a blackout period", async () => {
    // today is 2017-12-01
    mockDate(1_512_130_448_379);

    mockEmptyYear(42, 2017);

    fetchMock.route(`/persons/42/blackout-periods?from=2017-01-01&to=2017-12-31`, {
      blackoutPeriods: [
        {
          date: "2017-12-15",
          title: "Jahresabschluss",
          description: "Urlaubssperre: Jahresabschluss",
        },
        {
          date: "2017-12-16",
          title: "Jahresabschluss",
          description: "Urlaubssperre: Jahresabschluss",
        },
        {
          date: "2017-12-15",
          title: "Inventur",
          description: "Urlaubssperre: <b>Inventur</b>",
        },
      ],
    });

    await calendarTestSetup();

    const holidayService = createHolidayService({ personId: 42 });
    await holidayService.fetchAbsences(2017);
    await holidayService.fetchPublicHolidays(2017);
    await holidayService.fetchBlackoutPeriods(2017);

    expect(holidayService.isBlackoutPeriod(parseISO("2017-12-15"))).toBe(true);
    expect(holidayService.isBlackoutPeriod(parseISO("2017-12-16"))).toBe(true);
    expect(holidayService.isBlackoutPeriod(parseISO("2017-12-14"))).toBe(false);

    renderCalendar(holidayService);

    const $ = document.querySelector.bind(document);
    expect($('[data-datepicker-date="2017-12-15"]').classList).toContain("datepicker-day-blackout");
    expect($('[data-datepicker-date="2017-12-16"]').classList).toContain("datepicker-day-blackout");
    expect($('[data-datepicker-date="2017-12-14"]').classList).not.toContain("datepicker-day-blackout");

    expect(holidayService.getBlackoutPeriodDescription(parseISO("2017-12-15"))).toBe(
      "Urlaubssperre: Jahresabschluss · Urlaubssperre: <b>Inventur</b>",
    );
    expect(holidayService.getBlackoutPeriodDescription(parseISO("2017-12-14"))).toBe("");

    const day = $('[data-datepicker-date="2017-12-15"]');
    expect(day.getAttribute("title")).toBe("Urlaubssperre: Jahresabschluss · Urlaubssperre: <b>Inventur</b>");
    // the office-entered title is text, never markup
    expect(day.querySelector("b")).toBeNull();
    expect(day.closest("li").querySelector(".sr-only").textContent).toContain("Urlaubssperre: Jahresabschluss");
  });

  it("caches blackout periods of a year and does not fetch them twice", async () => {
    // today is 2017-12-01
    mockDate(1_512_130_448_379);

    fetchMock.route(`/persons/42/blackout-periods?from=2017-01-01&to=2017-12-31`, {
      blackoutPeriods: [
        {
          date: "2017-12-15",
          title: "Jahresabschluss",
        },
      ],
    });

    await calendarTestSetup();

    const holidayService = createHolidayService({ personId: 42 });
    await holidayService.fetchBlackoutPeriods(2017);
    await holidayService.fetchBlackoutPeriods(2017);

    expect(fetchMock.callHistory.calls(`/persons/42/blackout-periods?from=2017-01-01&to=2017-12-31`)).toHaveLength(1);
    expect(holidayService.isBlackoutPeriod(parseISO("2017-12-15"))).toBe(true);
  });

  function mockEmptyYear(personId, year) {
    fetchMock.route(
      `/persons/${personId}/absences?from=${year}-01-01&to=${year}-12-31&absence-types=vacation%2Csick_note%2Cno_workday`,
      {
        absences: [],
      },
    );

    fetchMock.route(`/persons/${personId}/public-holidays?from=${year}-01-01&to=${year}-12-31`, {
      publicHolidays: [],
    });
  }

  function createHolidayService({ webPrefix = "", apiPrefix = "", personId = 1 } = {}) {
    return globalThis.Urlaubsverwaltung.HolidayService.create(webPrefix, apiPrefix, personId);
  }

  function renderCalendar(holidayService, { canApplyForLeave = true } = {}) {
    // note: Date is mocked in calendarTestSetup to return a fixed date value
    const referenceDate = new Date();
    const i18n = (messageKey) => `i18n:${messageKey}`;
    globalThis.Urlaubsverwaltung.Calendar.init(
      document.querySelector("#datepicker"),
      holidayService,
      referenceDate,
      i18n,
      { canApplyForLeave },
    );
  }

  async function calendarTestSetup() {
    globalThis.uv = {};
    // 0=sunday, 1=monday
    globalThis.uv.weekStartsOn = 1;

    globalThis.uv.vacationTypes = {};
    globalThis.uv.vacationTypes.colors = {
      1: "#B4D455",
    };

    document.body.innerHTML = `<div id="datepicker"></div>`;

    await import("./../index.js");
  }
});
