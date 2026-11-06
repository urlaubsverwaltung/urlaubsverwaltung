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
    // jsdom does not implement the popover api
    HTMLElement.prototype.showPopover = vi.fn();
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

  describe("clicking on a day with an absence opens the day popover", () => {
    // today is 2017-12-01
    async function clickDayWithAbsences(
      date,
      absences,
      { canApplyForLeave = true, allowHalfDays = true, publicHolidays = [], absencesOfNextYear = [] } = {},
    ) {
      mockDate(1_512_130_448_379);

      const toAbsence = (absence) => ({
        date,
        id: 1337,
        typeId: 1,
        absentNumeric: absence.absent === "FULL" ? 1 : 0.5,
        from: absence.date ?? date,
        to: absence.date ?? date,
        ...absence,
      });
      fetchMock.route(
        "/persons/42/absences?from=2017-01-01&to=2017-12-31&absence-types=vacation%2Csick_note%2Cno_workday",
        { absences: absences.map((absence) => toAbsence(absence)) },
      );
      fetchMock.route(
        "/persons/42/absences?from=2018-01-01&to=2018-12-31&absence-types=vacation%2Csick_note%2Cno_workday",
        { absences: absencesOfNextYear.map((absence) => toAbsence(absence)) },
      );
      fetchMock.route("/persons/42/public-holidays?from=2018-01-01&to=2018-12-31", { publicHolidays: [] });
      fetchMock.route("/persons/42/public-holidays?from=2017-01-01&to=2017-12-31", {
        publicHolidays: publicHolidays.map((publicHoliday) => ({ date, description: "Christmas", ...publicHoliday })),
      });

      await calendarTestSetup();

      const holidayService = createHolidayService({ webPrefix: "/web", personId: 42 });
      holidayService.bookHoliday = vi.fn();
      await holidayService.fetchAbsences(2017);
      await holidayService.fetchPublicHolidays(2017);

      renderCalendar(holidayService, { canApplyForLeave, allowHalfDays });

      const day = document.querySelector(`.datepicker-day[data-datepicker-date="${date}"]`);
      day.click();

      expect(holidayService.bookHoliday).not.toHaveBeenCalled();
      return day;
    }

    function popoverEntries() {
      return [...document.querySelectorAll("#calendar-day-popover li a")].map((a) => [
        [...a.querySelectorAll("strong, .calendar-day-popover__day-length, .sr-only")]
          .map((element) => element.textContent)
          .join(" "),
        a.getAttribute("href"),
      ]);
    }

    function popoverDetails() {
      return [...document.querySelectorAll("#calendar-day-popover .calendar-day-popover__details")].map((details) =>
        [...details.children].map((element) => element.textContent),
      );
    }

    function popoverFreeHalf() {
      const free = document.querySelector("#calendar-day-popover .calendar-day-popover__free");
      return free && [free.querySelector("span").textContent, free.querySelector("a").getAttribute("href")];
    }

    it("with a link to the vacation", async () => {
      const day = await clickDayWithAbsences("2017-12-15", [
        { absenceType: "VACATION", absent: "FULL", status: "ALLOWED" },
      ]);

      const popover = document.querySelector("#calendar-day-popover");
      expect(popover.showPopover).toHaveBeenCalled();
      expect(day.classList).toContain("datepicker-day--popover-anchor");
      expect(popover.querySelector(".calendar-day-popover__date").textContent).toBe("Friday, December 15th, 2017");
      expect(popoverEntries()).toEqual([["Erholungsurlaub i18n:FULL i18n:ALLOWED", "/web/application/1337"]]);
      expect(popover.querySelector(".calendar-day-popover__bar").classList).toContain(
        "calendar-day-popover__bar--solid",
      );
      expect(popoverFreeHalf()).toBeNull();
    });

    it("with a button closing it", async () => {
      await clickDayWithAbsences("2017-12-15", [{ absenceType: "VACATION", absent: "FULL", status: "ALLOWED" }]);

      const close = document.querySelector("#calendar-day-popover .calendar-day-popover__close");
      expect(close.getAttribute("popovertarget")).toBe("calendar-day-popover");
      expect(close.getAttribute("popovertargetaction")).toBe("hide");
      expect(close.textContent).toBe("i18n:action.close");
    });

    it("with the period and the work days of an absence over several days", async () => {
      const vacation = { absenceType: "VACATION", status: "ALLOWED", from: "2017-12-14", to: "2017-12-18" };
      await clickDayWithAbsences("2017-12-15", [
        { ...vacation, absent: "FULL", date: "2017-12-14" },
        { ...vacation, absent: "FULL" },
        { ...vacation, absent: "MORNING", date: "2017-12-18" },
        { absenceType: "SICK_NOTE", absent: "FULL", status: "ACTIVE", date: "2017-12-19", id: 7 },
      ]);

      expect(popoverDetails()).toEqual([
        [
          "i18n:absence.period",
          "Th, 12/14/2017 – Mo, 12/18/2017",
          "i18n:overview.calendar.day-popover.duration",
          "i18n:overview.calendar.day-popover.work-days 2.5",
        ],
      ]);
    });

    it("with the period of the application, which may start on a day without work", async () => {
      await clickDayWithAbsences("2017-12-11", [
        { absenceType: "VACATION", absent: "FULL", status: "ALLOWED", from: "2017-12-10", to: "2017-12-11" },
      ]);

      expect(popoverDetails()).toEqual([
        [
          "i18n:absence.period",
          "Su, 12/10/2017 – Mo, 12/11/2017",
          "i18n:overview.calendar.day-popover.duration",
          "i18n:overview.calendar.day-popover.work-days.one 1",
        ],
      ]);
    });

    it("without the no workdays and public holidays in the work days of a sick note", async () => {
      // unlike a vacation, a sick note has a day on each day of its period
      const sickNote = {
        absenceType: "SICK_NOTE",
        absent: "FULL",
        status: "ACTIVE",
        from: "2017-12-15",
        to: "2017-12-18",
      };
      await clickDayWithAbsences(
        "2017-12-15",
        [
          sickNote,
          { ...sickNote, date: "2017-12-16" },
          { absenceType: "NO_WORKDAY", absent: "FULL", status: "ACTIVE", date: "2017-12-16" },
          { ...sickNote, date: "2017-12-17" },
          { absenceType: "NO_WORKDAY", absent: "FULL", status: "ACTIVE", date: "2017-12-17" },
          { ...sickNote, date: "2017-12-18" },
        ],
        { publicHolidays: [{ date: "2017-12-18", dayLength: "0.5", absencePeriodName: "MORNING" }] },
      );

      expect(popoverDetails()).toEqual([
        [
          "i18n:absence.period",
          "Fr, 12/15/2017 – Mo, 12/18/2017",
          "i18n:overview.calendar.day-popover.duration",
          "i18n:overview.calendar.day-popover.work-days 1.5",
        ],
      ]);
    });

    it("with the work days of an absence continuing in the next year, once it is loaded", async () => {
      const vacation = {
        absenceType: "VACATION",
        absent: "FULL",
        status: "ALLOWED",
        from: "2017-12-29",
        to: "2018-01-02",
      };
      await clickDayWithAbsences("2017-12-29", [vacation], {
        absencesOfNextYear: [{ ...vacation, date: "2018-01-02" }],
      });

      expect(popoverDetails()).toEqual([
        ["i18n:absence.period", "Fr, 12/29/2017 – Tu, 01/02/2018", "i18n:overview.calendar.day-popover.duration", "…"],
      ]);

      await vi.waitFor(() =>
        expect(popoverDetails()).toEqual([
          [
            "i18n:absence.period",
            "Fr, 12/29/2017 – Tu, 01/02/2018",
            "i18n:overview.calendar.day-popover.duration",
            "i18n:overview.calendar.day-popover.work-days 2",
          ],
        ]),
      );
    });

    it("with the escaped label of a custom absence type", async () => {
      await clickDayWithAbsences("2017-12-15", [
        { absenceType: "VACATION", absent: "FULL", status: "ALLOWED", typeId: 2 },
      ]);

      expect(document.querySelector("#calendar-day-popover strong").innerHTML).toBe("&lt;img src=x&gt;");
    });

    it("with a link to the sick note", async () => {
      await clickDayWithAbsences("2017-12-15", [{ absenceType: "SICK_NOTE", absent: "FULL", status: "WAITING" }]);

      expect(popoverEntries()).toEqual([
        ["i18n:sicknote.title i18n:FULL i18n:sicknote.status.SUBMITTED", "/web/sicknote/1337"],
      ]);
      expect(document.querySelector("#calendar-day-popover .calendar-day-popover__bar").classList).toContain(
        "calendar-day-popover__bar--outline",
      );
    });

    it("with a link to both half-day absences", async () => {
      await clickDayWithAbsences("2017-12-15", [
        { absenceType: "VACATION", absent: "MORNING", status: "WAITING" },
        { absenceType: "SICK_NOTE", absent: "NOON", status: "ACTIVE", id: 42 },
      ]);

      expect(popoverEntries()).toEqual([
        ["Erholungsurlaub i18n:MORNING i18n:WAITING", "/web/application/1337"],
        ["i18n:sicknote.title i18n:NOON i18n:sicknote.status.ACTIVE", "/web/sicknote/42"],
      ]);
      expect(popoverFreeHalf()).toBeNull();
      expect(
        [...document.querySelectorAll("#calendar-day-popover .calendar-day-popover__bar")].map((bar) => [
          bar.className,
          bar.title,
        ]),
      ).toEqual([
        ["calendar-day-popover__bar calendar-day-popover__bar--outline", "i18n:WAITING"],
        ["calendar-day-popover__bar calendar-day-popover__bar--solid", "i18n:sicknote.status.ACTIVE"],
      ]);
    });

    it("with a link to apply for the free half of the day", async () => {
      await clickDayWithAbsences("2017-12-15", [{ absenceType: "VACATION", absent: "NOON", status: "ALLOWED" }]);

      expect(popoverEntries()).toEqual([["Erholungsurlaub i18n:NOON i18n:ALLOWED", "/web/application/1337"]]);
      expect(popoverFreeHalf()).toEqual([
        "i18n:overview.calendar.day-popover.free.MORNING",
        "/web/application/new?personId=42&from=2017-12-15&to=2017-12-15&dayLength=MORNING",
      ]);
    });

    it("with a link to apply for the free half of a day in the past", async () => {
      await clickDayWithAbsences("2017-11-15", [{ absenceType: "VACATION", absent: "NOON", status: "ALLOWED" }]);

      expect(popoverEntries()).toEqual([["Erholungsurlaub i18n:NOON i18n:ALLOWED", "/web/application/1337"]]);
      expect(popoverFreeHalf()).toEqual([
        "i18n:overview.calendar.day-popover.free.MORNING",
        "/web/application/new?personId=42&from=2017-11-15&to=2017-11-15&dayLength=MORNING",
      ]);
    });

    it("without a link to apply for the free half when half days are not allowed", async () => {
      await clickDayWithAbsences("2017-12-15", [{ absenceType: "VACATION", absent: "NOON", status: "ALLOWED" }], {
        allowHalfDays: false,
      });

      expect(popoverEntries()).toEqual([["Erholungsurlaub i18n:NOON i18n:ALLOWED", "/web/application/1337"]]);
      expect(popoverFreeHalf()).toBeNull();
    });

    it("without a link to apply for the free half when it is a public holiday", async () => {
      await clickDayWithAbsences("2017-12-15", [{ absenceType: "VACATION", absent: "MORNING", status: "ALLOWED" }], {
        publicHolidays: [{ dayLength: "0.5", absencePeriodName: "NOON" }],
      });

      expect(popoverEntries()).toEqual([["Erholungsurlaub i18n:MORNING i18n:ALLOWED", "/web/application/1337"]]);
      expect(popoverFreeHalf()).toBeNull();
    });

    it("without a link to apply for the free half when it is no workday", async () => {
      await clickDayWithAbsences("2017-12-15", [
        { absenceType: "VACATION", absent: "MORNING", status: "ALLOWED" },
        { absenceType: "NO_WORKDAY", absent: "NOON", status: "ACTIVE" },
      ]);

      expect(popoverEntries()).toEqual([["Erholungsurlaub i18n:MORNING i18n:ALLOWED", "/web/application/1337"]]);
      expect(popoverFreeHalf()).toBeNull();
    });

    it("without a link to apply for the free half without the permission to apply for leave", async () => {
      await clickDayWithAbsences("2017-12-15", [{ absenceType: "VACATION", absent: "NOON", status: "ALLOWED" }], {
        canApplyForLeave: false,
      });

      expect(popoverEntries()).toEqual([["Erholungsurlaub i18n:NOON i18n:ALLOWED", "/web/application/1337"]]);
      expect(popoverFreeHalf()).toBeNull();
    });
  });

  it("titles a day with its absences and public holidays like the absence overview", async () => {
    // today is 2017-12-01
    mockDate(1_512_130_448_379);

    fetchMock.route(
      "/persons/42/absences?from=2017-01-01&to=2017-12-31&absence-types=vacation%2Csick_note%2Cno_workday",
      {
        absences: [
          { date: "2017-12-15", absenceType: "VACATION", id: 1, typeId: 2, absent: "MORNING", status: "WAITING" },
          { date: "2017-12-15", absenceType: "SICK_NOTE", id: 2, typeId: 1, absent: "NOON", status: "ACTIVE" },
          { date: "2017-12-18", absenceType: "VACATION", id: 3, typeId: 1, absent: "FULL", status: "ALLOWED" },
        ],
      },
    );
    fetchMock.route("/persons/42/public-holidays?from=2017-01-01&to=2017-12-31", {
      publicHolidays: [{ date: "2017-12-18", description: "Christmas", dayLength: "1" }],
    });

    await calendarTestSetup();

    const holidayService = createHolidayService({ personId: 42 });
    await holidayService.fetchAbsences(2017);
    await holidayService.fetchPublicHolidays(2017);

    renderCalendar(holidayService);

    const title = (date) => document.querySelector(`.datepicker-day[data-datepicker-date="${date}"]`).title;
    expect(title("2017-12-15")).toBe("<img src=x>, i18n:MORNING, i18n:WAITING; i18n:sicknote.title, i18n:NOON");
    expect(title("2017-12-18")).toBe("Erholungsurlaub; Christmas");
    expect(title("2017-12-19")).toBe("");
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

    it("still opens the day popover when clicking on a day with an absence", async () => {
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
              from: "2017-12-15",
              to: "2017-12-15",
            },
          ],
        },
      );
      fetchMock.route("/persons/42/public-holidays?from=2017-01-01&to=2017-12-31", { publicHolidays: [] });

      await calendarTestSetup();

      const holidayService = createHolidayService({ personId: 42 });
      // the popover loads the years of the absence it has not loaded yet
      await holidayService.fetchAbsences(2017);
      await holidayService.fetchPublicHolidays(2017);

      renderCalendar(holidayService, { canApplyForLeave: false });

      const someDay = document.querySelector(`.datepicker-day[data-datepicker-date="2017-12-15"]`);
      expect(someDay).toBeTruthy();
      someDay.click();

      expect(document.querySelector("#calendar-day-popover").showPopover).toHaveBeenCalled();
    });
  });

  describe.each([
    [`.datepicker-prev`, "2017-11"],
    [`.datepicker-next`, "2018-01"],
  ])("remembers the shown months for the browser back button when clicking %s", (buttonSelector, expected) => {
    test("click", async () => {
      // 01.12.2017
      mockDate(1_512_130_448_379);
      mockEmptyYear(42, 2017);
      mockEmptyYear(42, 2018);

      await calendarTestSetup();
      renderCalendar(createHolidayService({ webPrefix: "", apiPrefix: "", personId: 42 }));

      document.querySelector(buttonSelector).click();

      await vi.waitFor(() => expect(new URL(globalThis.location).searchParams.get("calendarMonth")).toEqual(expected));
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
        expect($('[data-datepicker-date="2017-11-01"][data-datepicker-selectable="true"]')).toBeTruthy();
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
        expect($('[data-datepicker-date="2017-12-05"][data-datepicker-selectable="true"]')).toBeTruthy();
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
        expect($('[data-datepicker-date="2017-12-01"][data-datepicker-selectable="true"]')).toBeTruthy();
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

  describe("small screen media query", () => {
    function mockMatchMedia(matches) {
      let changeListener;
      globalThis.matchMedia = vi.fn().mockReturnValue({
        matches,
        addEventListener: (type, listener) => {
          changeListener = listener;
        },
      });
      return (event) => changeListener(event);
    }

    function buttonClassLists() {
      return [...document.querySelectorAll("#datepicker button")].map((button) => button.classList.contains("button"));
    }

    test("ensure buttons are styled as buttons on small screens", async () => {
      mockDate(1_512_130_448_379);
      mockMatchMedia(true);
      await calendarTestSetup();

      renderCalendar(createHolidayService());

      expect(buttonClassLists()).toEqual([true, true]);
    });

    test("ensure buttons follow the media query when it changes", async () => {
      mockDate(1_512_130_448_379);
      const fireChange = mockMatchMedia(false);
      await calendarTestSetup();

      renderCalendar(createHolidayService());

      fireChange({ matches: true });
      expect(buttonClassLists()).toEqual([true, true]);

      fireChange({ matches: false });
      expect(buttonClassLists()).toEqual([false, false]);
    });

    test("ensure buttons keep their style when change is fired without the media query changing", async () => {
      mockDate(1_512_130_448_379);
      const fireChange = mockMatchMedia(false);
      await calendarTestSetup();

      renderCalendar(createHolidayService());

      fireChange({ matches: false });
      expect(buttonClassLists()).toEqual([false, false]);
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

  it("fetches the absences of a year asked for again meanwhile only once", async () => {
    // today is 2017-12-01
    mockDate(1_512_130_448_379);

    const url = "/persons/42/absences?from=2017-01-01&to=2017-12-31&absence-types=vacation%2Csick_note%2Cno_workday";
    const vacation = {
      date: "2017-12-15",
      absenceType: "VACATION",
      absent: "FULL",
      status: "ALLOWED",
      id: 1337,
      typeId: 1,
      from: "2017-12-15",
      to: "2017-12-15",
    };
    fetchMock.route(url, { absences: [vacation] });
    fetchMock.route("/persons/42/public-holidays?from=2017-01-01&to=2017-12-31", { publicHolidays: [] });

    await calendarTestSetup();

    const holidayService = createHolidayService({ personId: 42 });
    await Promise.all([holidayService.fetchAbsences(2017), holidayService.fetchAbsences(2017)]);
    await holidayService.fetchPublicHolidays(2017);

    expect(fetchMock.callHistory.calls(url)).toHaveLength(1);
    expect(holidayService.getAbsencePeriod(vacation).workDays).toBe(1);
  });

  it("fetches the absences of a year again after the request failed", async () => {
    // today is 2017-12-01
    mockDate(1_512_130_448_379);

    const url = "/persons/42/absences?from=2017-01-01&to=2017-12-31&absence-types=vacation%2Csick_note%2Cno_workday";
    fetchMock.route(url, 500, { repeat: 1 });
    fetchMock.route(url, { absences: [] });

    await calendarTestSetup();

    const holidayService = createHolidayService({ personId: 42 });
    await expect(holidayService.fetchAbsences(2017)).rejects.toThrow();
    await holidayService.fetchAbsences(2017);

    expect(fetchMock.callHistory.calls(url)).toHaveLength(2);
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

  function renderCalendar(holidayService, { canApplyForLeave = true, allowHalfDays = true } = {}) {
    // note: Date is mocked in calendarTestSetup to return a fixed date value
    const referenceDate = new Date();
    const i18n = (messageKey) =>
      messageKey.startsWith("overview.calendar.day-popover.work-days")
        ? `i18n:${messageKey} {0}`
        : `i18n:${messageKey}`;
    globalThis.Urlaubsverwaltung.Calendar.init(
      document.querySelector("#datepicker"),
      holidayService,
      referenceDate,
      i18n,
      { canApplyForLeave, allowHalfDays },
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
    globalThis.uv.vacationTypes.labels = {
      1: "Erholungsurlaub",
      2: "<img src=x>",
    };

    document.body.innerHTML = `<div id="datepicker"></div>`;

    await import("./../index.js");
  }
});
