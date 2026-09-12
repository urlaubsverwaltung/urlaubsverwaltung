import { createDatepicker } from "../../components/datepicker";

// blackout periods are not tied to a specific person, therefore no absence overlay is shown in the calendar.
function getPersonId() {
  // no return value
}

document.addEventListener("DOMContentLoaded", function () {
  const apiPrefix = globalThis.uv.apiPrefix;

  createDatepicker("#startDate", { urlPrefix: apiPrefix, getPersonId });
  createDatepicker("#endDate", { urlPrefix: apiPrefix, getPersonId });
});
