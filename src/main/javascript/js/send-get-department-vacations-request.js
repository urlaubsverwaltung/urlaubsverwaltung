// disabling no-restricted-imports of date-fns#format is ok since we're formatting dates for api requests
// eslint-disable-next-line no-restricted-imports
import { format, isAfter } from "date-fns";
import { parseISO } from "date-fns/parseISO";
import { getJSON } from "../js/fetch";

const icons = {
  check: `<svg fill="none" viewBox="0 0 24 24" stroke="currentColor" width="16px" height="16px" class="w-4 h-4 stroke-2" role="img" aria-hidden="true" focusable="false"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"></path></svg>`,
};

export default async function sendGetDepartmentVacationsRequest(
  urlPrefix,
  startDate,
  endDate,
  personId,
  elementSelector,
) {
  if (!startDate && !endDate) {
    return;
  }

  if (isAfter(startDate, endDate)) {
    return;
  }

  const startDateString = format(startDate, "yyyy-MM-dd");
  const toDateString = format(endDate, "yyyy-MM-dd");

  const url = `${urlPrefix}/persons/${personId}/vacations?from=${startDateString}&to=${toDateString}&ofDepartmentMembers`;

  const data = await getJSON(url);
  const vacations = data.vacations;

  const element = document.querySelector(elementSelector);
  element.innerHTML = `<p>${vacations.length} ${globalThis.uv.i18n["application.applier.applicationsOfColleagues"]}</p>`;

  if (vacations.length > 0) {
    let lastPersonName = -1;
    let isNewPerson = false;

    const html = vacations.map((vacation) => {
      isNewPerson = lastPersonName !== vacation.person.niceName;
      lastPersonName = vacation.person.niceName;

      return createHtmlForVacation(vacation, isNewPerson);
    });

    element.innerHTML += `<ul class="m-0 p-0">${html.join("")}</ul>`;
  }
}

function createHtmlForVacation(vacation, isNewPerson) {
  const startDate = format(parseISO(vacation.from), "dd.MM.yyyy");
  const endDate = format(parseISO(vacation.to), "dd.MM.yyyy");
  const person = vacation.person.niceName;

  let html = "";
  if (isNewPerson) {
    html += `<li class="flex items-center pt-2">${person}:</li>`;
  }

  html += `<li class="flex items-center pl-5">`;
  if (vacation.status === "ALLOWED") {
    html += `<span class="text-emerald-500 absolute -ml-5" title="${globalThis.uv.i18n["application.status.allowed"]}">${icons.check}</span>`;
  }

  let dateInformation = `${startDate} - ${endDate}`;
  if (startDate === endDate) {
    dateInformation = `${startDate}`;
  }
  html += `${dateInformation}`;

  return html + "</li>";
}
