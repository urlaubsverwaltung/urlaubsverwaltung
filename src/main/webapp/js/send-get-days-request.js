// disabling date-fns#format is ok since we're formatting dates for api requests
// eslint-disable-next-line @urlaubsverwaltung/no-date-fns
import { isAfter, format, getYear } from 'date-fns'
import buildUrl from './build-url';
import formatNumber from './format-number';
import sendGetDaysRequestForTurnOfTheYear from './send-get-days-request-for-turn-of-the-year';
import { getJSON } from '../js/fetch'

export default async function sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, personId, elementSelector) {

  const element = document.querySelector(elementSelector);
  element.innerHTML = "";

  if (!startDate && !toDate) {
    return;
  }

  if (isAfter(startDate, toDate)) {
    return;
  }

  const requestUrl = urlPrefix + "/workdays";

  const startDateString = format(startDate, "YYYY-MM-DD");
  const toDateString = format(toDate, "YYYY-MM-DD");
  const url = buildUrl(requestUrl, startDateString, toDateString, dayLength, personId);

  const data = await getJSON(url);
  const workDays = data.response.workDays;

  var text;

  if(isNaN(workDays)) {
    text = "Ung&uuml;ltiger Zeitraum"
  } else if (workDays === 1) {
    text = formatNumber(workDays) + " Tag";
  } else {
    text = formatNumber(workDays) + " Tage";
  }

  element.innerHTML = text;

  if (getYear(startDate) !== getYear(toDate)) {
    element.innerHTML += "<span class=\"days-turn-of-the-year\"></span>";
    await sendGetDaysRequestForTurnOfTheYear(urlPrefix, startDate, toDate, dayLength, personId, `${elementSelector} .days-turn-of-the-year`);
  }
}
