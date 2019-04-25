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

  const startDateString = format(startDate, "YYYY-MM-DD");
  const toDateString = format(endDate, "YYYY-MM-DD");

  const url = `${urlPrefix}/vacations?departmentMembers=true&from=${startDateString}&to=${toDateString}&person=${personId}`;

  const data = await getJSON(url);
  const vacations = data.response.vacations;

  const element = document.querySelector(elementSelector);
  element.innerHTML = window.uv.i18n['application.applier.applicationsOfColleagues'] + " ";

  if(vacations.length > 0) {
    const html = vacations.map(vacation => createHtmlForVacation(vacation));
    element.innerHTML += html.join("<br />");
  } else {
    element.innerHTML += "&nbsp;" + window.uv.i18n['application.applier.none'];
  }
}

function createHtmlForVacation(vacation) {
  const startDate = format(vacation.from, "DD.MM.YYYY");
  const endDate = format(vacation.to, "DD.MM.YYYY");
  const person = vacation.person.niceName;

  let html = `${person}: ${startDate} - ${endDate}`;

  if(vacation.status === "ALLOWED") {
    html += "&nbsp;<i class='fa fa-check positive' aria-hidden='true'></i>"
  }

  return html;
}
