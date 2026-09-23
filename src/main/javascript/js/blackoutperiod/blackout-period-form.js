import { createDatepicker } from "../../components/datepicker";

// blackout periods are not tied to a specific person, therefore no absence overlay is shown in the calendar.
function getPersonId() {
  // no return value
}

/**
 * An "all" checkbox (`data-blackout-scope-all="<name>"`) disables the checkboxes named `<name>` while it is checked.
 * The selection of the disabled checkboxes is kept, so unchecking "all" restores it.
 */
export function initScopeToggles(root) {
  for (const toggle of root.querySelectorAll("[data-blackout-scope-all]")) {
    const name = toggle.dataset.blackoutScopeAll;
    const update = () => {
      for (const checkbox of root.querySelectorAll(`input[type='checkbox'][name='${name}']`)) {
        checkbox.disabled = toggle.checked;
      }
    };
    toggle.addEventListener("change", update);
    update();
  }
}

document.addEventListener("DOMContentLoaded", function () {
  const apiPrefix = globalThis.uv.apiPrefix;

  createDatepicker("#startDate", { urlPrefix: apiPrefix, getPersonId });
  createDatepicker("#endDate", { urlPrefix: apiPrefix, getPersonId });

  initScopeToggles(document);
});
