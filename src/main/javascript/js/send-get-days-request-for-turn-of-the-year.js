// disabling date-fns#format is ok since we're formatting dates for api requests
// eslint-disable-next-line @urlaubsverwaltung/no-date-fns
import { isAfter, getYear, format, endOfYear, startOfYear } from "date-fns";
import formatNumber from "./format-number";
import { getJSON } from "../js/fetch";

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

  if (!startDate && !toDate) {
    return;
  }

  if (isAfter(startDate, toDate)) {
    return;
  }

  let before;
  let after;

  if (getYear(startDate) < getYear(toDate)) {
    before = startDate;
    after = toDate;
  } else {
    before = toDate;
    after = startDate;
  }

  // before - 31.12.
  // 1.1.   - after

  const [workDaysBefore, workDaysAfter] = await Promise.all([
    getWorkdaysForDateRange(urlPrefix, dayLength, personId, before, endOfYear(before)),
    getWorkdaysForDateRange(urlPrefix, dayLength, personId, startOfYear(after), after),
  ]);

  const daysBefore = formatNumber(workDaysBefore);
  const daysAfter = formatNumber(workDaysAfter);

  element.innerHTML = `<br />(${daysBefore} in ${getYear(before)} und ${daysAfter} in ${getYear(after)})`;
}

async function getWorkdaysForDateRange(urlPrefix, dayLength, personId, fromDate, toDate) {
  const startDate = format(fromDate, "yyyy-MM-dd");
  const endDate = format(toDate, "yyyy-MM-dd");
  const url =
    urlPrefix + "/persons/" + personId + "/workdays?from=" + startDate + "&to=" + endDate + "&length=" + dayLength;

  const json = await getJSON(url);

  return json.workDays;
}
