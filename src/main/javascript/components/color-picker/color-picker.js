class ColorPicker extends HTMLDivElement {
  static get observedAttributes() {
    return ["data-open"];
  }

  #open = false;
  #dialogToggleButton;
  #dialogToggleCheckbox;
  #dialog;
  #colorOptions;
  #focusedElementIndex;

  attributeChangedCallback(name, oldValue, newValue) {
    this.#open = typeof newValue === "string";
    this.#render();
  }

  connectedCallback() {
    this.#dialogToggleButton = this.querySelector("[class~='color-picker-button']");
    this.#dialogToggleCheckbox = this.querySelector(`#${this.#dialogToggleButton.getAttribute("for")}`);
    this.#dialog = this.querySelector(".color-picker-dialog");
    this.#colorOptions = this.#dialog.querySelectorAll("label");

    this.#focusedElementIndex = 0; // TODO set to actual color

    this.setAttribute("tabindex", "0");
    this.classList.add("focus:tw-outline-2", "focus:tw-outline-blue-500");
    for (let input of this.querySelectorAll("input")) {
      input.setAttribute("tabindex", "-1");
    }

    // prevent native checkbox selection.
    // otherwise the browser jumps to the checkbox element (which is positioned absolutely on top of the screen)
    // checkbox state is updated in #render
    // eslint-disable-next-line unicorn/consistent-function-scoping
    const handleDialogToggleButtonClick = (event) => event.preventDefault();

    const handleClick = (event) => {
      // update selected color visualisation
      const color = this.querySelector("input[type='radio']:checked")?.value;
      // TODO no color should not happen -> see todo in jsp, there must be a selected radio button input
      if (color) {
        this.#dialogToggleButton.style.setProperty("background-color", color);
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
                let colorInput = this.#colorOptions[index].querySelector("input");
                colorInput.checked = index === this.#focusedElementIndex;
                if (index === this.#focusedElementIndex) {
                  this.#dialogToggleButton.style.setProperty("background-color", colorInput.value);
                }
              }
            }
          } else {
            this.dataset.open = "";
          }
        }

        if (event.key === "ArrowDown" || event.key === "ArrowUp") {
          event.preventDefault();
          this.dataset.open = "";
        }

        // enable keyboard navigation through color options
        if (event.key === "ArrowDown") {
          if (this.#focusedElementIndex < this.#colorOptions.length - 1) {
            this.#focusedElementIndex++;
            this.#render();
          }
        } else if (event.key === "ArrowUp" && this.#focusedElementIndex > 0) {
          this.#focusedElementIndex--;
          this.#render();
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

  #render() {
    this.#dialogToggleCheckbox.checked = this.#open;

    // update currently focused color option
    const focusedColorOption = this.#colorOptions[this.#focusedElementIndex];
    for (let colorOption of this.#colorOptions) {
      if (colorOption === focusedColorOption) {
        colorOption.classList.add("tw-opacity-80");
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
