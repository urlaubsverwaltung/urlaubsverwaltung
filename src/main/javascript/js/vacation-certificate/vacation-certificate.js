import { createDatepicker } from "../../components/datepicker";

const urlPrefix = globalThis.uv.apiPrefix;
const personId = globalThis.uv.personId;

function getPersonId() {
  return personId;
}

// the form has no inputs when the selected year has no holiday account
if (document.querySelector("#employmentFrom")) {
  createDatepicker("#employmentFrom", { urlPrefix, getPersonId });
  createDatepicker("#employmentTo", { urlPrefix, getPersonId });
}

document.querySelector("[data-print-button]")?.addEventListener("click", () => globalThis.print());
