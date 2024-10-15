import { createDatepicker } from "../../components/datepicker";

const urlPrefix = globalThis.uv.apiPrefix;
const personId = globalThis.uv.personId;

function getPersonId() {
  return personId;
}

createDatepicker("#holidaysAccountValidFrom", { urlPrefix, getPersonId });
createDatepicker("#holidaysAccountValidTo", { urlPrefix, getPersonId });
createDatepicker("#expiryDate", { urlPrefix, getPersonId });

const form = document.querySelector("#holiday-account-settings-form");
const fieldset = document.querySelector("#remaining-vacation-days-expire-fieldset");
const fieldsetGloballyEnabled = fieldset.dataset.globallyEnabled === "true";

form.addEventListener("change", function enabledDisableVacationDaysExpireElements(event) {
  const { target } = event;
  if (
    target.matches("[name=overrideVacationDaysExpire]") ||
    target.matches("[name=doRemainingVacationDaysExpireLocally]")
  ) {
    const formData = new FormData(form);
    const override = formData.get("overrideVacationDaysExpire");
    const locally = formData.get("doRemainingVacationDaysExpireLocally");

    if ((override === "true" && locally === "true") || (override !== "true" && fieldsetGloballyEnabled)) {
      fieldset.removeAttribute("disabled");
    } else {
      fieldset.setAttribute("disabled", "");
    }

    const radioButtons = document.querySelectorAll("[name='doRemainingVacationDaysExpireLocally']");
    for (const button of radioButtons) {
      if (override === "true") {
        button.removeAttribute("disabled");
      } else {
        button.setAttribute("disabled", "");
      }
    }
  }
});
