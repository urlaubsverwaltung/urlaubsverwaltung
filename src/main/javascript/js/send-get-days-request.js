import { isAfter, getYear } from "date-fns";
import formatNumber from "./format-number";
import sendGetDaysRequestForTurnOfTheYear from "./send-get-days-request-for-turn-of-the-year";
import { getWorkdays } from "./get-workdays";

export default async function sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, personId, elementSelector) {
  const element = document.querySelector(elementSelector);
  element.innerHTML = "";

  if ((!personId && !startDate && !toDate) || isAfter(startDate, toDate)) {
    return;
  }

  const workDays = await getWorkdays(urlPrefix, dayLength, personId, startDate, toDate);

  let text;

  if (!workDays) {
    text = globalThis.uv.i18n["application.applier.invalidPeriod"];
  } else if (workDays === "1.0") {
    text = formatNumber(workDays) + " " + globalThis.uv.i18n["application.applier.day"];
  } else {
    text = formatNumber(workDays) + " " + globalThis.uv.i18n["application.applier.days"];
  }

  element.innerHTML = text;
  let closest = element.closest("#days-count");
  if (closest) {
    closest.classList.remove("hidden");
  }

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
