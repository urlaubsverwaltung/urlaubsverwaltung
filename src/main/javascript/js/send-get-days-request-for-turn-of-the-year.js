// disabling date-fns#format is ok since we're formatting dates for api requests
// eslint-disable-next-line @urlaubsverwaltung/no-date-fns
import { isAfter, getYear, format, endOfYear, startOfYear } from "date-fns";
import formatNumber from "./format-number";
import { getJSON } from "./fetch";

export default async function sendGetDaysRequestForTurnOfTheYear(
  urlPrefix,
  startDate,
  toDate,
  dayLength,
  personId,
  elementSelector,
) {
  const element = document.querySelector(elementSelector);
  element.innerHTML = "";

  if ((!personId && !startDate && !toDate) || isAfter(startDate, toDate)) {
    return;
  }

  const [workDaysBefore, workDaysAfter] = await Promise.all([
    getWorkdaysForDateRange(urlPrefix, dayLength, personId, startDate, endOfYear(startDate)),
    getWorkdaysForDateRange(urlPrefix, dayLength, personId, startOfYear(toDate), toDate),
  ]);

  const daysBefore = formatNumber(workDaysBefore);
  const daysAfter = formatNumber(workDaysAfter);

  element.innerHTML = `<br />(${daysBefore} in ${getYear(startDate)} und ${daysAfter} in ${getYear(toDate)})`;
}

async function getWorkdaysForDateRange(urlPrefix, dayLength, personId, fromDate, toDate) {
  const startDate = format(fromDate, "yyyy-MM-dd");
  const endDate = format(toDate, "yyyy-MM-dd");
  let url = urlPrefix + "/persons/" + personId + "/workdays?from=" + startDate + "&to=" + endDate;
  if (dayLength) {
    url = url + "&length=" + dayLength;
  }

  const json = await getJSON(url);
  return json.workDays;
}
