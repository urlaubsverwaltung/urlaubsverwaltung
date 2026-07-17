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

  it("dragging from one day to another on desktop marks and books the whole range in between", async () => {
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

    renderCalendar(holidayService);

    const dayFrom = document.querySelector(`.datepicker-day[data-datepicker-date="2017-12-11"]`);
    const dayTo = document.querySelector(`.datepicker-day[data-datepicker-date="2017-12-15"]`);
    const dayBetween = document.querySelector(`.datepicker-day[data-datepicker-date="2017-12-13"]`);

    dayFrom.dispatchEvent(new MouseEvent("mousedown", { bubbles: true, button: 0 }));
    dayTo.dispatchEvent(new MouseEvent("mouseover", { bubbles: true }));

    // days between drag-start and the currently hovered day are marked while the button is still held
    expect(dayBetween.classList.contains("datepicker-day-selected")).toBe(true);
    expect(holidayService.bookHoliday).not.toHaveBeenCalled();

    document.body.dispatchEvent(new MouseEvent("mouseup", { bubbles: true }));
    dayTo.click();

    expect(holidayService.bookHoliday).toHaveBeenCalledWith(parseISO("2017-12-11"), parseISO("2017-12-15"));
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

  describe("swipe gestures", () => {
    const expectedInitialCaptions = [
      "August 2017",
      "September 2017",
      "October 2017",
      "November 2017",
      "December 2017",
      "January 2018",
      "February 2018",
      "March 2018",
      "April 2018",
      "May 2018",
    ];

    function touchEvent(type, x, y) {
      const event = new Event(type, { bubbles: true, cancelable: true });
      event.touches = [{ clientX: x, clientY: y }];
      return event;
    }

    function swipe(container, { startX, endX, startY = 100, endY = 100 }) {
      container.dispatchEvent(touchEvent("touchstart", startX, startY));
      container.dispatchEvent(touchEvent("touchmove", endX, endY));
      container.dispatchEvent(touchEvent("touchend", endX, endY));
    }

    function flush() {
      return new Promise((resolve) => setTimeout(resolve, 0));
    }

    function monthCaptions() {
      return [...document.querySelectorAll(".calendar-month-caption")].map((element) => element.textContent);
    }

    // resolves the swipe animation started by `swipe`, mirroring the browser
    // firing `transitionend` once the CSS slide animation finishes
    function settleSwipeAnimation() {
      const currentMonthElement = document.querySelectorAll(".calendar-month-container")[4];
      currentMonthElement.dispatchEvent(new Event("transitionend", { bubbles: true }));
      return flush();
    }

    async function setUpMobileCalendar() {
      // pretend to be on a touch-sized viewport so swipe handling is active
      globalThis.matchMedia = vi.fn().mockReturnValue({ matches: true, addEventListener: vi.fn() });

      // today is 2017-12-01
      mockDate(1_512_130_448_379);

      fetchMock.route(
        "/persons/42/absences?from=2017-01-01&to=2017-12-31&absence-types=vacation%2Csick_note%2Cno_workday",
        {
          absences: [],
        },
      );
      fetchMock.route(`/persons/42/public-holidays?from=2017-01-01&to=2017-12-31`, { publicHolidays: [] });
      mockEmptyYear(42, 2018);

      await calendarTestSetup();

      renderCalendar(createHolidayService({ webPrefix: "", apiPrefix: "", personId: 42 }));

      return document.querySelector(".calendar-container");
    }

    it("swiping left past the commit threshold navigates to the next month, like the next button", async () => {
      const container = await setUpMobileCalendar();
      expect(monthCaptions()).toEqual(expectedInitialCaptions);

      swipe(container, { startX: 200, endX: 100 });
      await settleSwipeAnimation();

      expect(monthCaptions()).toEqual([
        "September 2017",
        "October 2017",
        "November 2017",
        "December 2017",
        "January 2018",
        "February 2018",
        "March 2018",
        "April 2018",
        "May 2018",
        "June 2018",
      ]);
    });

    it("swiping right past the commit threshold navigates to the previous month, like the previous button", async () => {
      const container = await setUpMobileCalendar();

      swipe(container, { startX: 100, endX: 200 });
      await settleSwipeAnimation();

      expect(monthCaptions()).toEqual([
        "July 2017",
        "August 2017",
        "September 2017",
        "October 2017",
        "November 2017",
        "December 2017",
        "January 2018",
        "February 2018",
        "March 2018",
        "April 2018",
      ]);
    });

    it("does not navigate when the swipe distance stays under the commit threshold", async () => {
      const container = await setUpMobileCalendar();

      swipe(container, { startX: 200, endX: 180 });
      await settleSwipeAnimation();

      expect(monthCaptions()).toEqual(expectedInitialCaptions);
    });

    it("ignores a predominantly vertical touch move so page scrolling keeps working", async () => {
      const container = await setUpMobileCalendar();

      const preventDefaultSpy = vi.fn();
      container.dispatchEvent(touchEvent("touchstart", 200, 100));
      const moveEvent = touchEvent("touchmove", 205, 180);
      moveEvent.preventDefault = preventDefaultSpy;
      container.dispatchEvent(moveEvent);
      container.dispatchEvent(touchEvent("touchend", 205, 180));

      expect(preventDefaultSpy).not.toHaveBeenCalled();
      expect(monthCaptions()).toEqual(expectedInitialCaptions);
    });

    it("does not react to touch gestures outside the mobile single-month view", async () => {
      globalThis.matchMedia = vi.fn().mockReturnValue({ matches: false, addEventListener: vi.fn() });

      mockDate(1_512_130_448_379);
      await calendarTestSetup();
      renderCalendar(createHolidayService({ personId: 42 }));

      const container = document.querySelector(".calendar-container");
      swipe(container, { startX: 200, endX: 50 });

      expect(document.querySelectorAll(".calendar-month-container.calendar-month-swipe-active").length).toBe(0);
      expect(monthCaptions()).toEqual(expectedInitialCaptions);
    });
  });

  describe("mobile day range selection (tap to mark)", () => {
    async function setUpMobileCalendarNoAbsences() {
      // pretend to be on a touch-sized viewport so tap-to-mark handling is active
      globalThis.matchMedia = vi.fn().mockReturnValue({ matches: true, addEventListener: vi.fn() });

      // today is 2017-12-01
      mockDate(1_512_130_448_379);

      await calendarTestSetup();

      const holidayService = createHolidayService({ personId: 42 });
      holidayService.bookHoliday = vi.fn();

      renderCalendar(holidayService);

      return holidayService;
    }

    function day(date) {
      return document.querySelector(`.datepicker-day[data-datepicker-date="${date}"]`);
    }

    function isMarked(date) {
      return day(date).classList.contains("datepicker-day-selected");
    }

    it("marks a day on first tap without booking, marks the range on a second tap, and books on a third tap inside the range", async () => {
      const holidayService = await setUpMobileCalendarNoAbsences();

      day("2017-12-11").click();
      expect(holidayService.bookHoliday).not.toHaveBeenCalled();
      expect(isMarked("2017-12-11")).toBe(true);
      expect(isMarked("2017-12-15")).toBe(false);

      day("2017-12-15").click();
      expect(holidayService.bookHoliday).not.toHaveBeenCalled();
      expect(isMarked("2017-12-11")).toBe(true);
      expect(isMarked("2017-12-13")).toBe(true);
      expect(isMarked("2017-12-15")).toBe(true);

      day("2017-12-13").click();
      expect(holidayService.bookHoliday).toHaveBeenCalledWith(parseISO("2017-12-11"), parseISO("2017-12-15"));
      expect(isMarked("2017-12-11")).toBe(false);
      expect(isMarked("2017-12-15")).toBe(false);
    });

    it("confirms a single-day booking when the already-marked day is tapped again", async () => {
      const holidayService = await setUpMobileCalendarNoAbsences();

      day("2017-12-11").click();
      day("2017-12-11").click();

      expect(holidayService.bookHoliday).toHaveBeenCalledWith(parseISO("2017-12-11"), parseISO("2017-12-11"));
    });

    it("extends the marked range relative to the original anchor when tapping outside it", async () => {
      const holidayService = await setUpMobileCalendarNoAbsences();

      day("2017-12-11").click(); // anchor
      day("2017-12-15").click(); // marks range 11-15
      day("2017-12-20").click(); // outside 11-15, re-anchored to 11-20

      expect(holidayService.bookHoliday).not.toHaveBeenCalled();
      expect(isMarked("2017-12-13")).toBe(true);
      expect(isMarked("2017-12-18")).toBe(true);

      day("2017-12-18").click();
      expect(holidayService.bookHoliday).toHaveBeenCalledWith(parseISO("2017-12-11"), parseISO("2017-12-20"));
    });

    it("still navigates directly to an existing absence on tap, without engaging range marking", async () => {
      globalThis.matchMedia = vi.fn().mockReturnValue({ matches: true, addEventListener: vi.fn() });
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

      renderCalendar(holidayService);

      day("2017-12-15").click();

      expect(holidayService.navigateToApplicationForLeave).toHaveBeenCalledWith("1337");
    });
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

  function renderCalendar(holidayService) {
    // note: Date is mocked in calendarTestSetup to return a fixed date value
    const referenceDate = new Date();
    const i18n = (messageKey) => `i18n:${messageKey}`;
    globalThis.Urlaubsverwaltung.Calendar.init(
      document.querySelector("#datepicker"),
      holidayService,
      referenceDate,
      i18n,
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
