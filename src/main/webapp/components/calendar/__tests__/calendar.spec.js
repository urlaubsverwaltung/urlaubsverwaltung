import { setup, cleanup, waitForFinishedJQueryReadyCallbacks } from '../../../../../test/javascript/test-setup-helper';
import fetchMock from 'fetch-mock';

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

    beforeEach(setup);
    afterEach(cleanup);

    afterEach(() => {
      fetchMock.restore();
      window.Date = RealDate;
    });

    it ('renders', async () => {
        // 01.12.2017
        mockDate(1512130448379);
        await calendarTestSetup();

        renderCalendar(createHolidayService());
        expect(document.body).toMatchSnapshot();
    });

    describe.each([
      ['ALLOWED'],
      ['WAITING'],
    ])('ensure vacation of status=%s is clickable', (givenStatus) => {
      test('in the past', async () => {
        // today is 2017-12-01
        mockDate(1512130448379);

        // personId -> createHolidayService (param)
        // year -> holidayService.fetchPersonal (param)
        // type -> holidayService.fetchPersonal (implementation detail)
        fetchMock.mock('/absences?person=42&year=2017&type=VACATION', {
            "response": {
              "absences": [
                {
                  date: "2017-11-01",
                  dayLength: 1,
                  absencePeriodName: "FULL",
                  type: 'VACATION',
                  status: givenStatus,
                }
              ]
            }
          }
        );

        await calendarTestSetup();

        const holidayService = createHolidayService({ personId: 42 });
        // fetch personal holiday data and cache the (mocked) response
        // the response is used when the calendar renders
        await holidayService.fetchPersonal(2017);

        renderCalendar(holidayService);

        const $ = document.querySelector.bind(document);
        expect($('[data-datepicker-date="2017-11-01"][data-datepicker-absence-type="VACATION"][data-datepicker-selectable="true"]')).toBeTruthy();
      });

      test('in the future', async () => {
        // today is 2017-12-01
        mockDate(1512130448379);

        // personId -> createHolidayService (param)
        // year -> holidayService.fetchPersonal (param)
        // type -> holidayService.fetchPersonal (implementation detail)
        fetchMock.mock('/absences?person=42&year=2017&type=VACATION', {
            "response": {
              "absences": [
                {
                  date: "2017-12-05",
                  dayLength: 1,
                  absencePeriodName: "FULL",
                  type: 'VACATION',
                  status: givenStatus,
                }
              ]
            }
          }
        );

        await calendarTestSetup();

        const holidayService = createHolidayService({ personId: 42 });
        // fetch personal holiday data and cache the (mocked) response
        // the response is used when the calendar renders
        await holidayService.fetchPersonal(2017);

        renderCalendar(holidayService);

        const $ = document.querySelector.bind(document);
        expect($('[data-datepicker-date="2017-12-05"][data-datepicker-absence-type="VACATION"][data-datepicker-selectable="true"]')).toBeTruthy();
      });

      test('today', async () => {
        // today is 2017-12-01
        mockDate(1512130448379);

        // personId -> createHolidayService (param)
        // year -> holidayService.fetchPersonal (param)
        // type -> holidayService.fetchPersonal (implementation detail)
        fetchMock.mock('/absences?person=42&year=2017&type=VACATION', {
            "response": {
              "absences": [
                {
                  date: "2017-12-01",
                  dayLength: 1,
                  absencePeriodName: "FULL",
                  type: 'VACATION',
                  status: givenStatus,
                }
              ]
            }
          }
        );

        await calendarTestSetup();

        const holidayService = createHolidayService({ personId: 42 });
        // fetch personal holiday data and cache the (mocked) response
        // the response is used when the calendar renders
        await holidayService.fetchPersonal(2017);

        renderCalendar(holidayService);

        const $ = document.querySelector.bind(document);
        expect($('[data-datepicker-date="2017-12-01"][data-datepicker-absence-type="VACATION"][data-datepicker-selectable="true"]')).toBeTruthy();
      });
    });

    function createHolidayService ({ webPrefix = '', apiPrefix = '', personId = 1 } = {}) {
        return window.Urlaubsverwaltung.HolidayService.create(webPrefix, apiPrefix, personId);
    }

    function renderCalendar (holidayService) {
        // note: Date is mocked in calendarTestSetup to return a fixed date value
        const referenceDate = new Date();
        window.Urlaubsverwaltung.Calendar.init(holidayService, referenceDate);
    }

    async function calendarTestSetup () {
        window.uv = {};
        // 0=sunday, 1=monday
        window.uv.weekStartsOn = 1;

        document.body.innerHTML = `<div id="datepicker"></div>`;

        // loading calendar.js registers a jQuery ready callback
        // which will be executed asynchronously
        await import('./../index.js');

        // therefore we have to wait till ready callbacks are invoked
        return waitForFinishedJQueryReadyCallbacks();
    }
});
