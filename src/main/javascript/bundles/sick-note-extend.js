import "../js/common";
import { Idiomorph } from "idiomorph/dist/idiomorph.esm.js";
import { createDatepicker } from "../components/datepicker";

document.addEventListener("turbo:before-render", function (event) {
  // morph all the things!
  event.detail.render = (currentElement, newElement) => {
    // duet-date-picker is client side only -> the html snippet from the backend contains an `input type=date`.
    // that input carries the id the hydrated picker handed to its own inner input, so idiomorph matches the
    // two and moves the input out of the picker instead of adding a new one. that leaves a picker without an
    // input behind and a second, stale `extendToDate` field in the form.
    // therefore the server side input is taken out of the new html and its changes are handed to the picker.
    const datepickers = [];
    for (const dateInput of newElement.querySelectorAll("input[type=date]")) {
      const datepicker = document.querySelector(`duet-date-picker[name="${dateInput.getAttribute("name")}"]`);
      if (datepicker) {
        datepicker.classList.remove("sicknote-extend-button--selected", "error");
        // if the datepicker should be selected it will be added now. same for errors
        datepicker.classList.add(...dateInput.classList);
        datepickers.push(datepicker);
        dateInput.remove();
      }
    }

    Idiomorph.morph(currentElement, newElement, {
      callbacks: {
        beforeNodeRemoved(node) {
          // the datepicker has no counterpart in the new html, it must survive the morph
          return !datepickers.includes(node);
        },
      },
    });
  };
});

await createDatepicker("#extend-to-date-input", {
  urlPrefix: globalThis.uv.apiPrefix,
  getPersonId: () => {
    return globalThis.uv.personId;
  },
});
