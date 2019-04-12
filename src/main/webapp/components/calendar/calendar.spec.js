import { setup, cleanup, waitForFinishedJQueryReadyCallbacks } from '../../../../test/javascript/test-setup-helper';

describe ('calendar', () => {
    const RealDate = Date;
    const dateInstanceIdentifier = Symbol('date-identifier');

    // mocking Date with overridden instanceof operator o_O phew°°°
    // required since datefn verifies with foo instanceof Date
    function mockDate(isoDate) {
      /* eslint-disable unicorn/prevent-abbreviations */

      window.Date = class extends RealDate {
        static [Symbol.hasInstance](instance) {
          return instance[dateInstanceIdentifier];
        }

        // noinspection JSAnnotator
        constructor(...args) { // NOSONAR
          let d = args.length === 0 ? new RealDate(isoDate) : new RealDate(...args);
          d[dateInstanceIdentifier] = true;
          return d;
        }
      };
    }

    afterEach(() => {
      window.Date = RealDate;
    });

    beforeEach (calendarTestSetup);
    afterEach (cleanup);

    it ('renders', () => {
        renderCalendar(createHolidayService());
        expect(document.body).toMatchSnapshot();
    });

    it ('does not set halfDay on a weekend', () => {
        const holidayService = createHolidayService();
        jest.spyOn(holidayService, 'isMorningAbsence').mockReturnValue(true);
        jest.spyOn(holidayService, 'isNoonAbsence').mockReturnValue(true);

        renderCalendar(holidayService);

        const christmasEve = document.body.querySelector('[data-datepicker-date="2017-12-24"]');
        expect(christmasEve).not.toBeNull();
        expect(christmasEve.classList).not.toContain('datepicker-day-absence-morning');
        expect(christmasEve.classList).not.toContain('datepicker-day-absence-noon');
    });

    it ('does not set datepicker-day-personal-holiday on a weekend', () => {
        const holidayService = createHolidayService();
        jest.spyOn(holidayService, 'isPersonalHoliday').mockReturnValue(true);

        renderCalendar(holidayService);

        const christmasEve = document.body.querySelector('[data-datepicker-date="2017-12-24"]');
        expect(christmasEve).not.toBeNull();
        expect(christmasEve.classList).not.toContain('datepicker-day-personal-holiday');
    });

    it ('does not set datepicker-day-sick-note on weekend', () => {
        const holidayService = createHolidayService();
        jest.spyOn(holidayService, 'isSickDay').mockReturnValue(true);

        renderCalendar(holidayService);

        const christmasEve = document.body.querySelector('[data-datepicker-date="2017-12-24"]');
        expect(christmasEve).not.toBeNull();
        expect(christmasEve.classList).not.toContain('datepicker-day-sick-note');
    });

    function createHolidayService () {
        return window.Urlaubsverwaltung.HolidayService.create();
    }

    function renderCalendar (holidayService) {
        // note: Date is mocked in calendarTestSetup to return a fixed date value
        const referenceDate = new Date();
        window.Urlaubsverwaltung.Calendar.init(holidayService, referenceDate);
    }

    async function calendarTestSetup () {
        await setup();

        // window.dateFns = await import('date-fns');
        window.uv = {};
        // 0=sunday, 1=monday
        window.uv.weekStartsOn = 1;

        // 01.12.2017
        mockDate(1512130448379);

        document.body.innerHTML = `<div id="datepicker"></div>`;

        // loading calendar.js registers a jQuery ready callback
        // which will be executed asynchronously
        await import('./index.js');

        // therefore we have to wait till ready callbacks are invoked
        return waitForFinishedJQueryReadyCallbacks();
    }
});
