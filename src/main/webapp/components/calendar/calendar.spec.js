import { setup, cleanup, waitForFinishedJQueryReadyCallbacks } from '../../../../test/javascript/test-setup-helper';

jest.mock('../../lib/date-fns/locale-resolver');

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

        static now() {
          return new RealDate(isoDate).getTime();
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

    it ('subscribes to local changed and rerenders the calendar', () => {
        // afterEach does a cleanup which resets all jest modules
        // therefore we have to load it on demand here so we have the exact same reference like calendar.js
        const { subscribeToLocaleChanged } = require('../../lib/date-fns/locale-resolver');
        // locale-resolver is mocked on the top of this test file
        expect(subscribeToLocaleChanged).toHaveBeenCalled();

        const localeChangedListener = subscribeToLocaleChanged.mock.calls[0][0];

        // since the calendar is not initialized yet it cannot be rerendered
        // this should not brake the js loop however
        expect(() => {
          localeChangedListener();
        }).not.toThrow();

        renderCalendar(createHolidayService());
        document.body.querySelector('#datepicker').innerHTML = '';

        localeChangedListener();
        expect(document.body.querySelector('#datepicker').innerHTML).not.toEqual('');
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
