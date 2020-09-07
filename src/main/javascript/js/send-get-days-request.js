// disabling date-fns#format is ok since we're formatting dates for api requests
// eslint-disable-next-line @urlaubsverwaltung/no-date-fns
import { isAfter, format, getYear } from "date-fns";
import formatNumber from "./format-number";
import sendGetDaysRequestForTurnOfTheYear from "./send-get-days-request-for-turn-of-the-year";
import { getJSON } from "../js/fetch";

export default async function sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, personId, elementSelector) {
  const element = document.querySelector(elementSelector);
  element.innerHTML = "";

  if (!startDate && !toDate) {
    return;
  }

  if (isAfter(startDate, toDate)) {
    return;
  }

  const startDateString = format(startDate, "yyyy-MM-dd");
  const toDateString = format(toDate, "yyyy-MM-dd");
  const url =
    urlPrefix +
    "/persons/" +
    personId +
    "/workdays?from=" +
    startDateString +
    "&to=" +
    toDateString +
    "&length=" +
    dayLength;

  const data = await getJSON(url);
  const workDays = data.workDays;

  let text;

  if (Number.isNaN(workDays)) {
    text = window.uv.i18n["application.applier.invalidPeriod"];
  } else if (workDays === "1.0") {
    text = formatNumber(workDays) + " " + window.uv.i18n["application.applier.day"];
  } else {
    text = formatNumber(workDays) + " " + window.uv.i18n["application.applier.days"];
  }

  element.innerHTML = text;

  if (getYear(startDate) !== getYear(toDate)) {
    element.innerHTML += '<span class="days-turn-of-the-year"></span>';
    await sendGetDaysRequestForTurnOfTheYear(
      urlPrefix,
      startDate,
      toDate,
      dayLength,
      personId,
      `${elementSelector} .days-turn-of-the-year`,
    );
  }
}
