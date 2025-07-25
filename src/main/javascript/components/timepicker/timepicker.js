import { ListBox } from "./list-box";
import { ListBoxOption } from "./list-box-option";

// to format a Date to "HH:mm"
// AM/PM is not supported currently
const dtf = new Intl.DateTimeFormat("de-DE", { hour: "numeric", minute: "numeric" });

export class Timepicker extends HTMLInputElement {
  #cleanup = () => {};

  #listBox;

  constructor() {
    super();
    this.classList.add("timepicker");
    this.#listBox = this.#createListBox();
    this.#listBox.classList.add("timepicker-list-box");
  }

  connectedCallback() {
    const handleGlobalFocusIn = (event) => {
      if (event.target === this) {
        this.#listBox.open({ relativeTo: this });
      } else {
        this.#listBox.close();
      }
    };

    // TODO move into list-box?
    const handleGlobalClick = (event) => {
      if (event.target === this) {
        // could be opened by "focus" already
        // however, we support clicking the focused input element to open the list-box
        if (!this.#listBox.opened) {
          this.#listBox.open({ relativeTo: this });
          event.stopImmediatePropagation(); // click listener called twice otherwise
        }
      } else if (!this.#listBox.contains(event.target) && this.#listBox.opened) {
        this.#listBox.close();
      }
    };

    const handleInputKeyDown = (event) => {
      if (this.#listBox.opened) {
        switch (event.key) {
          case "Enter": {
            event.preventDefault();
            break;
          }
          case "Escape": {
            this.#listBox.close();
            break;
          }
          // No default
        }
      } else {
        switch (event.key) {
          case "ArrowUp":
          case "ArrowDown": {
            event.preventDefault();
            this.#listBox.open({ relativeTo: this });
            break;
          }
          // No default
        }
      }
    };

    const focusPrediction = debounce(() => {
      if (this.value.length === 1 && this.value !== "0") {
        this.#listBox.focusValue("0" + this.value);
      } else if (this.value.length === 4) {
        this.#listBox.focusValue(`${this.value.slice(0, 2)}:${this.value.slice(2, 4)}`);
      } else {
        this.#listBox.focusValue(this.value);
      }
    }, 100);

    const handleInput = () => {
      focusPrediction();
    };

    const handleListBoxValueChanged = (event) => {
      this.value = event.detail.value;

      this.focus();
      this.setSelectionRange(this.value.length, this.value.length);

      // hide after focus. since input:focus actually shows the list-box
      this.#listBox.close();
    };

    document.body.addEventListener("focusin", handleGlobalFocusIn);
    document.body.addEventListener("click", handleGlobalClick);
    this.addEventListener("keydown", handleInputKeyDown);
    this.addEventListener("input", handleInput);
    this.#listBox.addEventListener("list-box:value-changed", handleListBoxValueChanged);

    this.#cleanup = () => {
      document.body.removeEventListener("focusin", handleGlobalFocusIn);
      document.body.removeEventListener("click", handleGlobalClick);
      this.removeEventListener("keydown", handleInputKeyDown);
      this.removeEventListener("input", handleInput);
      this.#listBox.removeEventListener("list-box:value-changed", handleListBoxValueChanged);
    };
  }

  disconnectedCallback() {
    this.#cleanup();
  }
  #createListBox() {
    const d = new Date();
    d.setHours(0);
    d.setMinutes(0);
    d.setSeconds(0);
    d.setMilliseconds(0);

    const options = [];
    const origin = d.getTime();
    let n = origin;

    while (n < origin + 8.64e7) {
      const option = new ListBoxOption();
      option.style.padding = "";
      option.textContent = dtf.format(d);
      option.value = dtf.format(d);

      options.push(option);

      n += 900_000;
      d.setTime(n);
    }

    return new ListBox(options);
  }
}

function debounce(function_, delay = 0) {
  let h;
  return (...arguments_) => {
    clearTimeout(h);
    h = setTimeout(() => {
      function_.apply(undefined, arguments_);
    }, delay);
  };
}

customElements.define("uv-timepicker", Timepicker, { extends: "input" });
