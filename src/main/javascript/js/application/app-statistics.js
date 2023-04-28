import { createDatepicker } from "../../components/datepicker";
import { onTurboBeforeRenderRestore } from "../turbo";

const getPersonId = () => {};
const urlPrefix = ""; // not required, no absences are fetched since we have no single person (personId)

createDatepicker("#from-date-input", { urlPrefix, getPersonId });
createDatepicker("#to-date-input", { urlPrefix, getPersonId });

onTurboBeforeRenderRestore(function (event) {
  // enable submit buttons which has been disabled by 'hotwire/turbo' after submitting the form.
  event.detail.newBody.querySelector("form#form-date-from-to button[type='submit']").removeAttribute("disabled");
});

// close `<details-dropdown>` on advancing client side navigation.
// (it's visible as long as response is loading)
document.addEventListener("turbo:submit-end", function (event) {
  if (event.target.matches("#form-date-from-to")) {
    event.target.querySelector("[is=uv-details-dropdown]").open = false;
  }
});
