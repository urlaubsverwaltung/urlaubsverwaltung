class ColorPicker extends HTMLDivElement {
  static get observedAttributes() {
    return ["data-open"];
  }

  #open = false;
  #dialogToggleButton;
  #dialogToggleCheckbox;

  attributeChangedCallback(name, oldValue, newValue) {
    this.#open = typeof newValue === "string";
    this.#render();
  }

  connectedCallback() {
    this.#dialogToggleButton = this.querySelector("[class~='color-picker-button']");
    this.#dialogToggleCheckbox = this.querySelector(`#${this.#dialogToggleButton.getAttribute("for")}`);

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

    this.#dialogToggleButton.addEventListener("click", handleDialogToggleButtonClick);
    this.addEventListener("click", handleClick);

    this.cleanup = function () {
      this.#dialogToggleButton.removeEventListener("click", handleDialogToggleButtonClick);
      this.removeEventListener("click", handleClick);
    };
  }

  disconnectedCallback() {
    this.cleanup();
  }

  #render() {
    this.#dialogToggleCheckbox.checked = this.#open;
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
