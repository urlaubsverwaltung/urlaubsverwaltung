class ColorPicker extends HTMLDivElement {
  static get observedAttributes() {
    return ["data-open"];
  }

  #open = false;
  #value = "";
  #dialogToggleButton;
  #dialogToggleCheckbox;
  #dialog;
  #colorOptions;
  #focusedElementIndex;

  attributeChangedCallback(name, oldValue, newValue) {
    if (oldValue === newValue) {
      return;
    }

    this.#open = typeof newValue === "string";

    // makes the dialog visible via css
    this.#dialogToggleCheckbox.checked = this.#open;
  }

  connectedCallback() {
    this.#dialogToggleButton = this.querySelector("[class~='color-picker-button']");
    this.#dialogToggleCheckbox = this.querySelector(`#${this.#dialogToggleButton.getAttribute("for")}`);
    this.#dialog = this.querySelector(".color-picker-dialog");
    this.#colorOptions = this.#dialog.querySelectorAll("li");

    this.#focusedElementIndex = -1;

    this.setAttribute("tabindex", "0");
    this.classList.add("focus:tw-outline-2", "focus:tw-outline-blue-500");

    for (let input of this.querySelectorAll("input")) {
      input.setAttribute("tabindex", "-1");
    }

    let selectedId = "";
    for (let index = 0; index < this.#colorOptions.length; index++) {
      const option = this.#colorOptions[index];
      const optionInput = option.querySelector("input");
      const optionValue = optionInput.value;
      if (optionInput.checked) {
        this.#value = optionValue;
      }
      const id = `${this.#dialog.getAttribute("id")}-option-${index}`;
      option.setAttribute("id", id);
      option.setAttribute("role", "option");
      option.setAttribute("aria-selected", optionInput.checked ? "true" : "false");
      if (optionInput.checked) {
        selectedId = id;
        this.#focusedElementIndex = index;
      }
    }
    this.#dialog.setAttribute("role", "listbox");
    if (selectedId) {
      this.#dialog.setAttribute("aria-activedescendant", selectedId);
    }

    // prevent native checkbox selection.
    // otherwise the browser jumps to the checkbox element (which is positioned absolutely on top of the screen)
    // checkbox state is updated in #render
    // eslint-disable-next-line unicorn/consistent-function-scoping
    const handleDialogToggleButtonClick = (event) => event.preventDefault();

    const handleClick = (event) => {
      // update selected color visualisation
      for (let index = 0; index < this.#colorOptions.length; index++) {
        const option = this.#colorOptions[index];
        const optionInput = option.querySelector("input");
        if (optionInput.checked) {
          this.#value = optionInput.value;
          this.#focusedElementIndex = index;
          this.#renderSelectedColor();
          break;
        }
      }

      // toggle dialog state
      const dialogClicked = event.target.closest(".color-picker-dialog");
      const pickerButtonClicked = event.target.closest(".color-picker-button");
      if (dialogClicked || (pickerButtonClicked && this.#open)) {
        delete this.dataset.open;
      } else {
        this.dataset.open = "";
      }
    };

    const handleKeyDown = (event) => {
      if (document.activeElement === this) {
        // toggle dialog with keyboard
        // closing it with 'Escape' is handled globally
        if (event.key === "Enter") {
          if (this.#open) {
            // close dialog
            delete this.dataset.open;
            // and update selected color
            if (this.#focusedElementIndex !== undefined) {
              for (let index = 0; index < this.#colorOptions.length; index++) {
                const colorOption = this.#colorOptions[index];
                const colorInput = colorOption.querySelector("input");
                const selected = index === this.#focusedElementIndex;
                colorInput.checked = selected;
                if (selected) {
                  this.#value = colorInput.value;
                  this.#renderSelectedColor();
                }
              }
            }
          } else {
            this.dataset.open = "";
          }
        }

        if (event.key === "ArrowDown" || event.key === "ArrowUp") {
          event.preventDefault();
          if (!this.#open) {
            this.dataset.open = "";
          }
        }

        // enable keyboard navigation through color options
        if (event.key === "ArrowDown") {
          if (this.#focusedElementIndex < this.#colorOptions.length - 1) {
            this.#focusedElementIndex++;
            this.#renderFocusedElement();
          }
        } else if (event.key === "ArrowUp" && this.#focusedElementIndex > 0) {
          this.#focusedElementIndex--;
          this.#renderFocusedElement();
        }
      }
    };

    const handleFocusOut = () => {
      delete this.dataset.open;
      // TODO reset focusedElementIndex to actual selected color
    };

    this.#dialogToggleButton.addEventListener("click", handleDialogToggleButtonClick);
    this.addEventListener("click", handleClick);
    this.addEventListener("keydown", handleKeyDown);
    this.addEventListener("focusout", handleFocusOut);

    this.cleanup = function () {
      this.#dialogToggleButton.removeEventListener("click", handleDialogToggleButtonClick);
      this.removeEventListener("click", handleClick);
      this.removeEventListener("keydown", handleKeyDown);
      this.removeEventListener("focusout", handleFocusOut);
    };
  }

  disconnectedCallback() {
    this.cleanup();
  }

  #renderSelectedColor() {
    for (let index = 0; index < this.#colorOptions.length; index++) {
      const colorOption = this.#colorOptions[index];
      const selected = colorOption.querySelector("input").value === this.#value;
      if (selected) {
        this.#dialog.setAttribute("aria-activedescendant", colorOption.getAttribute("id"));
        break;
      }
    }

    this.#dialogToggleButton.style.setProperty("background-color", this.#value);
  }

  #renderFocusedElement() {
    for (let index = 0; index < this.#colorOptions.length; index++) {
      const colorOption = this.#colorOptions[index];
      if (index === this.#focusedElementIndex) {
        colorOption.classList.add("tw-opacity-80");
        this.#dialog.setAttribute("aria-activedescendant", colorOption.getAttribute("id"));
      } else {
        colorOption.classList.remove("tw-opacity-80");
      }
    }
  }
}

// hide color picker popup
document.body.addEventListener("click", function (event) {
  const closestClickedDatePicker = event.target.closest("[is='uv-color-picker']");
  for (let picker of document.querySelectorAll("[is='uv-color-picker'][data-open]")) {
    if (picker !== closestClickedDatePicker) {
      delete picker.dataset.open;
    }
  }
});

document.addEventListener("keyup", function (event) {
  if (event.key === "Escape") {
    for (let picker of document.querySelectorAll("[is='uv-color-picker'][data-open]")) {
      delete picker.dataset.open;
    }
  }
});

customElements.define("uv-color-picker", ColorPicker, { extends: "div" });
