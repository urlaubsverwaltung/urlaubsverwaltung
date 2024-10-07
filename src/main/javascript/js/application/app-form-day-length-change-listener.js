import $ from "jquery";
import { parseISO } from "date-fns";
import sendGetDaysRequest from "../send-get-days-request";
import sendGetDepartmentVacationsRequest from "../send-get-department-vacations-request";

function getPersonId() {
  return document.querySelector("[name='person']").value;
}

$(document).ready(function () {
  // re-calculate vacation days when changing the day length

  const urlPrefix = globalThis.uv.apiPrefix;

  for (const element of document.querySelectorAll("input[name='dayLength']"))
    element.addEventListener("change", (event) => {
      const dayLength = event.target.value;

      // we have to read the value of the `duet-date-picker` component since this value is the ISO date string
      // while the value of the input[type=date] would be a string representation of the browser specific date format
      // (e.g. `dd.MM.yyyy` or `yyyy-MM-dd` or ...)
      const startDateString = document.querySelector("#from").closest("duet-date-picker").value;
      const toDateString = document.querySelector("#to").closest("duet-date-picker").value;

      if (!startDateString) {
        return;
      }

      const startDate = parseISO(startDateString);
      const toDate = toDateString ? parseISO(toDateString) : startDate;

      const personId = getPersonId();
      sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, personId, ".days");
      sendGetDepartmentVacationsRequest(urlPrefix, startDate, toDate, personId, "#departmentVacations");
    });
});
