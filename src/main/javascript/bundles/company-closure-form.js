import "../js/common";
import { createDatepicker, morphKeepingDatepickers } from "../components/datepicker";
import "../components/back-button";

// the form is submitted with turbo. an invalid form comes back by a redirect to its own url and is morphed into
// the page, so the datepicker and the listeners of the page stay alive.
document.addEventListener("turbo:before-render", function (event) {
  event.detail.render = morphKeepingDatepickers;
});

// company closures apply to everybody, so the datepicker shows no absences of a person. the edit form has one
// "valid from" per planned version besides the one of a new version.
const validFromInputs = document.querySelectorAll("[data-company-closure-valid-from]");
await Promise.all(
  [...validFromInputs].map((input) => createDatepicker(`#${CSS.escape(input.id)}`, { getPersonId: () => {} })),
);
