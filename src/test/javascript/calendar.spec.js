import { setup, cleanup } from './TestSetupHelper';

describe ('calendar', () => {
    beforeEach (calendarTestSetup);
    afterEach (cleanup);

    it ('renders', () => {
        renderCalendar(createHolidayService());
        expect(document.body).toMatchSnapshot();
    });

    it ('does not set halfDay on a weekend', () => {
        const holidayService = createHolidayService();
        jest.spyOn(holidayService, 'isHalfDay').mockReturnValue(true);

        renderCalendar(holidayService);

        const christmasEve = document.body.querySelector('[data-datepicker-date="2017-12-24"]');
        expect(christmasEve).not.toBeNull();
        expect(christmasEve.classList).not.toContain('datepicker-day-half');
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
        const webPrefix = 'webPrefix';
        const apiPrefix = 'apiPrefix';
        const personId = 'personId';
        return window.Urlaubsverwaltung.HolidayService.create();
    }

    function renderCalendar (holidayService) {
        // note: date.now is mocked in calendarTestSetup
        const referenceDate = Date.now();
        window.Urlaubsverwaltung.Calendar.init(holidayService, referenceDate);
    }

    async function calendarTestSetup () {
        await setup();

        jest.spyOn(window.jQuery, 'ajax').mockReturnValue(Promise.reject());
        window.moment = await import('moment');

        // 01.12.2017
        jest.spyOn(Date, 'now').mockReturnValue(new Date(1512130448379));

        document.body.innerHTML = `<div id="datepicker"></div>`;

        await import('../../main/resources/static/js/calendar.js');
    }
});
