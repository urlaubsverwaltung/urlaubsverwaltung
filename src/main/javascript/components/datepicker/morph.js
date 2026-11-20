import { Idiomorph } from "idiomorph/dist/idiomorph.esm.js";

// the class duet-date-picker marks the element it has hydrated with. without it the element stays invisible.
const HYDRATED_CLASS = "hydrated";

const ERROR_ATTRIBUTES = ["aria-describedby", "aria-invalid"];

/**
 * Morphs the current page into the new one, keeping the hydrated `duet-date-picker` elements alive.
 *
 * duet-date-picker is client side only -> the html of the backend contains a date input, `input type=date` or a
 * text input. That input carries the id the picker handed to its own inner input, so idiomorph matches the two
 * and moves the input out of the picker instead of adding a new one. That would leave a picker without an input
 * behind and a second, stale field of the same name in the form. Therefore the server side input is taken out
 * of the new html and its changes are handed to the picker.
 *
 * @param currentElement {Element} element to morph, the current page
 * @param newElement {Element} the new page
 */
export function morphKeepingDatepickers(currentElement, newElement) {
  const datepickers = [];

  for (const datepicker of currentElement.querySelectorAll("duet-date-picker[identifier]")) {
    // the picker was created from the input with this id
    const dateInput = newElement.querySelector(`input[id="${datepicker.getAttribute("identifier")}"]`);
    if (dateInput) {
      // the classes of the server side input say whether the date is selected and whether it has an error
      const hydrated = datepicker.classList.contains(HYDRATED_CLASS);
      datepicker.className = dateInput.className;
      datepicker.classList.toggle(HYDRATED_CLASS, hydrated);
      // links a validation error to the input actually focused by the user
      const pickerInput = datepicker.querySelector("input.duet-date__input");
      for (const attributeName of ERROR_ATTRIBUTES) {
        if (dateInput.hasAttribute(attributeName)) {
          pickerInput?.setAttribute(attributeName, dateInput.getAttribute(attributeName));
        } else {
          pickerInput?.removeAttribute(attributeName);
        }
      }
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
}
