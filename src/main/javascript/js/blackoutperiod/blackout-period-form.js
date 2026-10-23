import { createDatepicker } from "../../components/datepicker";

// number of affected persons shown before the rest is collapsed behind a button
const PERSONS_SHOWN_COLLAPSED = 12;

// blackout periods are not tied to a specific person, therefore no absence overlay is shown in the calendar.
function getPersonId() {
  // no return value
}

function text(key) {
  return globalThis.uv?.i18n?.[key] ?? key;
}

function message(key, ...parameters) {
  return text(key).replaceAll(/\{(\d+)\}/g, (match, index) => String(parameters[index] ?? match));
}

// fills the element with the message template, its placeholders replaced by the given strings or nodes
function messageInto(element, key, ...parameters) {
  const parts = text(key)
    .split(/(\{\d+\})/)
    .filter(Boolean)
    .map((part) => {
      const placeholder = /^\{(\d+)\}$/.exec(part);
      return placeholder ? parameters[Number(placeholder[1])] : part;
    });
  element.replaceChildren(...parts);
}

// changes the checkbox like a click of the user does, so listeners of the form see it
function setChecked(checkbox, checked) {
  if (checkbox.checked !== checked) {
    checkbox.checked = checked;
    checkbox.dispatchEvent(new Event("change", { bubbles: true }));
  }
}

function strong(content) {
  const element = document.createElement("strong");
  element.textContent = content;
  return element;
}

function joinWithAnd(items) {
  return items.length < 2
    ? items.join("")
    : `${items.slice(0, -1).join(", ")} ${text("blackoutperiod.impact.and")} ${items.at(-1)}`;
}

function formatIsoDate(isoDate) {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(isoDate ?? "")) {
    return "";
  }
  const [year, month, day] = isoDate.split("-").map(Number);
  const locale = document.documentElement.lang || undefined;
  return new Intl.DateTimeFormat(locale, { day: "2-digit", month: "2-digit", year: "numeric" }).format(
    new Date(year, month - 1, day),
  );
}

/**
 * Shows the selection of a scope (`data-blackout-scope-selection="<scope>"`) only while the radio
 * `data-blackout-scope="<scope>"` with value "false" ("selected …" instead of "all") is chosen.
 * The selection inside is kept while it is hidden, so choosing "selected …" again restores it.
 */
export function initScopeSelections(root) {
  for (const selection of root.querySelectorAll("[data-blackout-scope-selection]")) {
    const scope = selection.dataset.blackoutScopeSelection;
    for (const radio of root.querySelectorAll(`[data-blackout-scope='${scope}']`)) {
      radio.addEventListener("change", () => {
        selection.hidden = radio.value === "true";
      });
    }
  }
}

/**
 * The department selection: chips of the selected departments, a search and buttons to select all or none.
 */
export function initDepartmentPicker(root) {
  const picker = root.querySelector("[data-blackout-departments]");
  if (!picker) {
    return;
  }

  const rows = [...picker.querySelectorAll("[data-blackout-department]")];
  const checkboxes = rows.map((row) => row.querySelector("input[type='checkbox']"));
  const chips = picker.querySelector("[data-blackout-department-chips]");
  const noneSelected = picker.querySelector("[data-blackout-department-none]");
  const chipTemplate = picker.querySelector("[data-blackout-department-chip-template]");
  const count = picker.querySelector("[data-blackout-department-count]");
  const search = picker.querySelector("[data-blackout-department-search]");
  const notFound = picker.querySelector("[data-blackout-department-not-found]");

  const render = () => {
    const selected = checkboxes.filter((checkbox) => checkbox.checked);

    for (const chip of chips.querySelectorAll("[data-blackout-department-chip]")) {
      chip.remove();
    }
    for (const checkbox of selected) {
      const chip = chipTemplate.content.firstElementChild.cloneNode(true);
      chip.dataset.blackoutDepartmentChip = "";
      chip.querySelector("[data-blackout-department-chip-label]").textContent = checkbox.dataset.label;
      const remove = chip.querySelector("[data-blackout-department-chip-remove]");
      remove.setAttribute("aria-label", message("blackoutperiod.data.departments.remove", checkbox.dataset.label));
      remove.addEventListener("click", () => {
        setChecked(checkbox, false);
        // the chip is gone, keep the keyboard focus inside the picker
        search.focus();
      });
      chips.append(chip);
    }

    noneSelected.hidden = selected.length > 0;
    count.textContent = message("blackoutperiod.data.departments.count", selected.length, checkboxes.length);
  };

  search.addEventListener("input", () => {
    const query = search.value.trim().toLocaleLowerCase();
    let visible = 0;
    for (const row of rows) {
      row.hidden = !row.dataset.label.toLocaleLowerCase().includes(query);
      visible += row.hidden ? 0 : 1;
    }
    notFound.hidden = visible > 0;
  });

  picker.querySelector("[data-blackout-departments-select-all]").addEventListener("click", () => {
    for (const row of rows.filter((row) => !row.hidden)) {
      setChecked(row.querySelector("input[type='checkbox']"), true);
    }
  });

  picker.querySelector("[data-blackout-departments-clear]").addEventListener("click", () => {
    for (const checkbox of checkboxes) {
      setChecked(checkbox, false);
    }
  });

  picker.addEventListener("change", render);
  render();
}

/**
 * Describes who cannot request which vacation types in which period and lists the affected persons,
 * updated with every change of the form.
 *
 * @param root element containing the form
 * @param dates initial ISO dates `{ startDate, endDate }`, updated by the `duetChange` events of the datepickers
 */
export function initImpact(root, dates) {
  const impact = root.querySelector("[data-blackout-impact]");
  if (!impact) {
    return;
  }

  const form = impact.closest("form") ?? root;
  const sentence = impact.querySelector("[data-blackout-impact-sentence]");
  const onBehalf = impact.querySelector("[data-blackout-impact-on-behalf]");
  const personsBox = impact.querySelector("[data-blackout-impact-persons]");
  const personsHeading = impact.querySelector("[data-blackout-impact-persons-heading]");
  const personsToggle = impact.querySelector("[data-blackout-impact-persons-toggle]");
  const personRows = [...impact.querySelectorAll("[data-blackout-person]")];
  const conflictsFrame = impact.querySelector("[data-blackout-conflicts-frame]");
  const currentDates = { ...dates };
  let expanded = false;

  const checkedValue = (name) => form.querySelector(`input[name='${name}']:checked`)?.value;
  const checkedBoxes = (name) => [...form.querySelectorAll(`input[type='checkbox'][name='${name}']:checked`)];

  const period = () => {
    const start = formatIsoDate(currentDates.startDate);
    const end = formatIsoDate(currentDates.endDate);
    if (!start || !end) {
      return text("blackoutperiod.impact.period.unknown");
    }
    return start === end
      ? message("blackoutperiod.impact.period.day", start)
      : message("blackoutperiod.impact.period.range", start, end);
  };

  const showOnlyText = (key) => {
    sentence.textContent = text(key);
    onBehalf.hidden = true;
    personsBox.hidden = true;
  };

  // the turbo frame renders the applications colliding with the blackout period of the form
  const loadConflicts = () => {
    const formData = new FormData(form);
    // only the scope, the form also holds the csrf token which must not end up in a url
    const parameters = new URLSearchParams();
    for (const name of ["companyWide", "departmentIds", "allVacationTypes", "vacationTypeIds"]) {
      for (const value of formData.getAll(name)) {
        parameters.append(name, value);
      }
    }
    parameters.set("startDate", currentDates.startDate ?? "");
    parameters.set("endDate", currentDates.endDate ?? "");
    conflictsFrame.src = `${conflictsFrame.dataset.src}?${parameters}`;
  };

  const update = () => {
    if (conflictsFrame) {
      loadConflicts();
    }

    const companyWide = checkedValue("companyWide") !== "false";
    const allVacationTypes = checkedValue("allVacationTypes") !== "false";
    const departments = checkedBoxes("departmentIds");
    const vacationTypes = checkedBoxes("vacationTypeIds");

    if ((!companyWide && departments.length === 0) || (!allVacationTypes && vacationTypes.length === 0)) {
      showOnlyText("blackoutperiod.impact.incomplete");
      return;
    }

    // a person in several of the selected departments is affected once
    const departmentIds = new Set(departments.map((checkbox) => checkbox.value));
    const affected = personRows.filter(
      // thymeleaf omits the attribute of a person without departments
      (row) => companyWide || (row.dataset.departmentIds ?? "").split(" ").some((id) => departmentIds.has(id)),
    );
    if (affected.length === 0) {
      showOnlyText("blackoutperiod.impact.nobody");
      return;
    }

    for (const row of personRows) {
      row.hidden = true;
    }
    for (const [index, row] of affected.entries()) {
      row.hidden = !expanded && index >= PERSONS_SHOWN_COLLAPSED;
    }
    personsHeading.textContent = message("blackoutperiod.impact.persons.heading", affected.length);
    personsToggle.parentElement.hidden = affected.length <= PERSONS_SHOWN_COLLAPSED;
    personsToggle.setAttribute("aria-expanded", String(expanded));
    personsToggle.textContent = expanded
      ? text("blackoutperiod.impact.persons.less")
      : message("blackoutperiod.impact.persons.more", affected.length - PERSONS_SHOWN_COLLAPSED);

    const persons = message(
      affected.length === 1 ? "blackoutperiod.impact.persons.one" : "blackoutperiod.impact.persons.other",
      affected.length,
    );
    const who = companyWide
      ? message("blackoutperiod.impact.who.companyWide", persons)
      : message(
          "blackoutperiod.impact.who.departments",
          persons,
          joinWithAnd(departments.map((checkbox) => checkbox.dataset.label)),
        );
    const what = allVacationTypes
      ? text("blackoutperiod.impact.vacationTypes.all")
      : joinWithAnd(
          vacationTypes.map((checkbox) => message("blackoutperiod.impact.vacationType", checkbox.dataset.label)),
        );

    messageInto(
      sentence,
      affected.length === 1 ? "blackoutperiod.impact.sentence.one" : "blackoutperiod.impact.sentence.other",
      strong(who),
      period(),
      strong(what),
    );
    onBehalf.hidden = false;
    personsBox.hidden = false;
  };

  form.addEventListener("change", update);
  document.addEventListener("duetChange", (event) => {
    const name = event.target.getAttribute?.("name");
    if (name in currentDates) {
      currentDates[name] = event.detail.value;
      update();
    }
  });
  personsToggle.addEventListener("click", () => {
    expanded = !expanded;
    update();
  });
  update();
}

document.addEventListener("DOMContentLoaded", function () {
  const apiPrefix = globalThis.uv.apiPrefix;

  // read before the datepickers replace the inputs
  const dates = {
    startDate: document.querySelector("#startDate")?.dataset.isoValue ?? "",
    endDate: document.querySelector("#endDate")?.dataset.isoValue ?? "",
  };

  createDatepicker("#startDate", { urlPrefix: apiPrefix, getPersonId });
  createDatepicker("#endDate", { urlPrefix: apiPrefix, getPersonId });

  initScopeSelections(document);
  initDepartmentPicker(document);
  initImpact(document, dates);
});
