import { createDatepicker } from "../../components/datepicker";

const urlPrefix = window.uv.apiPrefix;
const personId = window.uv.personId;

function getPersonId() {
  return personId;
}

createDatepicker("#holidaysAccountValidFrom", { urlPrefix, getPersonId });
createDatepicker("#holidaysAccountValidTo", { urlPrefix, getPersonId });
createDatepicker("#expiryDate", { urlPrefix, getPersonId });

document.addEventListener("change", function enabledDisableVacationDaysExpireElements(event) {
  if (event.target.matches("[name='doRemainingVacationDaysExpireLocally']")) {
    const dateElement = document.querySelector("[name='expiryDate']");
    const notExpiringElement = document.querySelector("[name='remainingVacationDaysNotExpiring']");

    const enable = event.target.dataset.value === "true";
    if (enable) {
      dateElement.removeAttribute("disabled");
      notExpiringElement.removeAttribute("disabled");
    } else {
      dateElement.setAttribute("disabled", "");
      notExpiringElement.setAttribute("disabled", "");
    }
  }
});
