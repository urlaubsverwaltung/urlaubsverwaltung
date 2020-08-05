// disabling date-fns#format is ok since we're formatting dates for api requests
// eslint-disable-next-line @urlaubsverwaltung/no-date-fns
import { isAfter, format } from 'date-fns'
import { getJSON } from "../js/fetch"

export default async function sendGetDepartmentVacationsRequest(urlPrefix, startDate, endDate, personId, elementSelector) {

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
  const vacations = data;

  const element = document.querySelector(elementSelector);
  element.innerHTML = window.uv.i18n['application.applier.applicationsOfColleagues'] + "<br />";

  if(vacations.length > 0) {
    const html = vacations.map(vacation => createHtmlForVacation(vacation));
    element.innerHTML += html.join("<br />");
  } else {
    element.innerHTML += window.uv.i18n['application.applier.none'];
  }
}

function createHtmlForVacation(vacation) {
  const startDate = format(Date.parse(vacation.from), "dd.MM.yyyy");
  const endDate = format(Date.parse(vacation.to), "dd.MM.yyyy");
  const person = vacation.person.niceName;

  let html = `${person}: ${startDate} - ${endDate}`;

  if(vacation.status === "ALLOWED") {
    html += "&nbsp;<i class='fa fa-check positive' aria-hidden='true'></i>"
  }

  return html;
}
