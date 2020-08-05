// disabling date-fns#format is ok since we're formatting dates for api requests
// eslint-disable-next-line @urlaubsverwaltung/no-date-fns
import { isAfter, getYear, format, endOfYear, startOfYear } from 'date-fns'
import buildUrl from './build-url';
import formatNumber from './format-number';
import { getJSON } from "../js/fetch"

export default async function sendGetDaysRequestForTurnOfTheYear(urlPrefix, startDate, toDate, dayLength, personId, elementSelector) {

  const element = document.querySelector(elementSelector);
  element.innerHTML = "";

  if (!startDate && !toDate) {
    return;
  }

  if (isAfter(startDate, toDate)) {
    return;
  }

  const requestUrl = urlPrefix + "/workdays";

  var before;
  var after;

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
    getWorkdaysForDateRange(requestUrl, dayLength, personId, before, endOfYear(before)),
    getWorkdaysForDateRange(requestUrl, dayLength, personId, startOfYear(after), after)
  ]);


  const daysBefore = formatNumber(workDaysBefore);
  const daysAfter = formatNumber(workDaysAfter);

  element.innerHTML = `<br />(${daysBefore} in ${getYear(before)} und ${daysAfter} in ${getYear(after)})`;
}

async function getWorkdaysForDateRange(requestUrl, dayLength, personId, fromDate, toDate) {
  const startString = format(fromDate, "yyyy-MM-dd");
  const toString = format(toDate, "yyyy-MM-dd");
  const url = buildUrl(requestUrl, startString, toString, dayLength, personId);

  const json = await getJSON(url);

  return json.workDays;
}
