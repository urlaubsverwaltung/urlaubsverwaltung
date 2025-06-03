import { addMonths, getYear, setYear, startOfYear, subMonths } from "date-fns";
import "../../components/calendar";

document.addEventListener("DOMContentLoaded", async () => {
  const { personId, webPrefix, apiPrefix, i18n: i18nMessages } = globalThis.uv;

  const i18n = (key) => i18nMessages?.[key] ?? `/i18n:${key}/`;
  const getUrlParameter = (name) => new URL(globalThis.location).searchParams.get(name);

  const initCalendar = async () => {
    const yearParameter = getUrlParameter("year");
    let date = new Date();

    if (yearParameter && Number(yearParameter) !== getYear(date)) {
      date = startOfYear(setYear(date, Number(yearParameter)));
    }

    const holidayService = Urlaubsverwaltung.HolidayService.create(webPrefix, apiPrefix, +personId);

    const shownMonths = 10;
    const startDate = subMonths(date, shownMonths / 2);
    const endDate = addMonths(date, shownMonths / 2);

    const yearOfStartDate = getYear(startDate);
    const yearOfEndDate = getYear(endDate);

    const fetchPromises = [];
    for (let year = yearOfStartDate; year <= yearOfEndDate; year++) {
      fetchPromises.push(holidayService.fetchPublicHolidays(year), holidayService.fetchAbsences(year));
    }

    await Promise.all(fetchPromises);

    const calendarParentElement = document.querySelector("#datepicker");
    Urlaubsverwaltung.Calendar.init(calendarParentElement, holidayService, date, i18n);
  };

  await initCalendar();
});
