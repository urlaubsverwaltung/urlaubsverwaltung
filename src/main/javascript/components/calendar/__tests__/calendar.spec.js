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

  describe("clicking on a day with an absence opens the day popover", () => {
    // today is 2017-12-01
    async function clickDayWithAbsences(
      date,
      absences,
      { canApplyForLeave = true, publicHolidays = [], absencesOfNextYear = [] } = {},
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

      renderCalendar(holidayService, { canApplyForLeave });

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

    it("with the work days of an absence continuing in the next year", async () => {
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

    it("without a link to apply for the free half of a day in the past", async () => {
      await clickDayWithAbsences("2017-11-15", [{ absenceType: "VACATION", absent: "NOON", status: "ALLOWED" }]);

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

      await calendarTestSetup();

      const holidayService = createHolidayService({ personId: 42 });
      await holidayService.fetchAbsences(2017);

      renderCalendar(holidayService, { canApplyForLeave: false });

      const someDay = document.querySelector(`.datepicker-day[data-datepicker-date="2017-12-15"]`);
      expect(someDay).toBeTruthy();
      someDay.click();

      expect(document.querySelector("#calendar-day-popover").showPopover).toHaveBeenCalled();
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
              absent: "FULL",
              absenceType: "VACATION",
              status: "ALLOWED",
              typeId: 1,
            },
            {
              date: "2020-12-06",
              absent: "MORNING",
              absenceType: "VACATION",
              status: "ALLOWED",
              typeId: 1,
            },
            {
              date: "2020-12-06",
              absent: "NOON",
              absenceType: "SICK_NOTE",
              status: "ACTIVE",
            },
            {
              date: "2020-12-12",
              absent: "MORNING",
              absenceType: "SICK_NOTE",
              status: "ACTIVE",
            },
            {
              date: "2020-12-12",
              absent: "NOON",
              absenceType: "VACATION",
              status: "ALLOWED",
              typeId: 1,
            },
            {
              date: "2020-12-13",
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
    const i18n = (messageKey) =>
      messageKey.startsWith("overview.calendar.day-popover.work-days")
        ? `i18n:${messageKey} {0}`
        : `i18n:${messageKey}`;
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
    globalThis.uv.vacationTypes.labels = {
      1: "Erholungsurlaub",
      2: "<img src=x>",
    };

    document.body.innerHTML = `<div id="datepicker"></div>`;

    await import("./../index.js");
  }
});
