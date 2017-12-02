import { setup, cleanup } from './TestSetupHelper';

describe ('calendar', () => {
    beforeEach (calendarTestSetup);
    afterEach (cleanup);

    it ('renders', () => {
        // 01.12.2017
        const referenceDate = new Date(1512130448379);

        const webPrefix = 'webPrefix';
        const apiPrefix = 'apiPrefix';
        const personId = 'personId';
        const holidayService = window.Urlaubsverwaltung.HolidayService.create();
        window.Urlaubsverwaltung.Calendar.init(holidayService, referenceDate);

        expect(document.body).toMatchSnapshot();
    });

    it ('does not set halfDay on a weekend', () => {
        const referenceDate = new Date(1512130448379);

        const webPrefix = 'webPrefix';
        const apiPrefix = 'apiPrefix';
        const personId = 'personId';
        const holidayService = window.Urlaubsverwaltung.HolidayService.create();
        jest.spyOn(holidayService, 'isHalfDay').mockReturnValue(true);

        window.Urlaubsverwaltung.Calendar.init(holidayService, referenceDate);

        const christmasEve = document.body.querySelector('[data-datepicker-date="2017-12-24"]');
        expect(christmasEve).not.toBeNull();
        expect(christmasEve.classList).not.toContain('datepicker-day-half');
    });

    it ('does not set datepicker-day-personal-holiday on a weekend', () => {
        // 01.12.2017
        const referenceDate = new Date(1512130448379);

        const webPrefix = 'webPrefix';
        const apiPrefix = 'apiPrefix';
        const personId = 'personId';
        const holidayService = window.Urlaubsverwaltung.HolidayService.create();
        jest.spyOn(holidayService, 'isPersonalHoliday').mockReturnValue(true);

        window.Urlaubsverwaltung.Calendar.init(holidayService, referenceDate);

        const christmasEve = document.body.querySelector('[data-datepicker-date="2017-12-24"]');
        expect(christmasEve).not.toBeNull();
        expect(christmasEve.classList).not.toContain('datepicker-day-personal-holiday');
    });

    async function calendarTestSetup () {
        await setup();

        jest.spyOn(window.jQuery, 'ajax').mockReturnValue(Promise.reject());
        window.moment = await import('moment');

        document.body.innerHTML = `<div id="datepicker"></div>`;

        await import('../../main/resources/static/js/calendar.js');
    }
});
