import $ from 'jquery';
// disabling date-fns#format is ok since we're formatting dates for api requests
// eslint-disable-next-line @urlaubsverwaltung/no-date-fns
import { isAfter, getYear, format, endOfYear, startOfYear } from 'date-fns'
import buildUrl from './build-url';
import formatNumber from './format-number';

export default async function sendGetDaysRequestForTurnOfTheYear(urlPrefix, startDate, toDate, dayLength, personId, element) {

  $(element).empty();

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

  $(element).html(`<br />(${daysBefore} in ${getYear(before)} und ${daysAfter} in ${getYear(after)})`);
}

function getWorkdaysForDateRange(requestUrl, dayLength, personId, fromDate, toDate) {
  return new Promise(resolve => {

    const startString = format(fromDate, "YYYY-MM-DD");
    const toString = format(toDate, "YYYY-MM-DD");
    const url = buildUrl(requestUrl, startString, toString, dayLength, personId);

    $.get(url, function (data) {
      resolve(data.response.workDays)
    });
  })
}
