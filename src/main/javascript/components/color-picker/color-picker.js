class ColorPicker extends HTMLDivElement {
  static get observedAttributes() {
    return ["data-open"];
  }

  #open = false;

  attributeChangedCallback(name, oldValue, newValue) {
    this.#open = typeof newValue === "string";
    this.#render();
  }

  connectedCallback() {
    const handleClick = (event) => {
      // update selected color visualisation
      const color = this.querySelector("input[type='radio']:checked")?.value;
      // TODO no color should not happen -> see todo in jsp, there must be a selected radio button input
      if (color) {
        const button = this.querySelector("label[class~='color-picker-button']");
        button.style.setProperty("background-color", color);
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

    this.addEventListener("click", handleClick);

    this.cleanup = function () {
      this.removeEventListener("click", handleClick);
    };
  }

  disconnectedCallback() {
    this.cleanup();
  }

  #render() {
    const button = this.querySelector("input[type='checkbox']");
    button.checked = this.#open;
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
