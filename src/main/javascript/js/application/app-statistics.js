import { createDatepicker } from "../../components/datepicker";

const getPersonId = () => {};
const urlPrefix = ""; // not required, no absences are fetched since we have no single person (personId)

createDatepicker("#from-date-input", { urlPrefix, getPersonId });
createDatepicker("#to-date-input", { urlPrefix, getPersonId });

document.addEventListener("turbo:submit-end", function (event) {
  if (event.target.matches("#form-date-from-to")) {
    event.target.querySelector("[is=uv-details-dropdown]").open = false;
  }
});
