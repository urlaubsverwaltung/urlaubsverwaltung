export class CheckboxAll extends HTMLInputElement {
  #cleanup;

  connectedCallback() {
    const handleChange = (event) => {
      const thisForm = this.closest("form");
      const eventTargetForm = event.target.closest("form");
      if (thisForm === eventTargetForm) {
        if (this === event.target) {
          const checkboxes = [...thisForm.querySelectorAll("input[type='checkbox']")];
          for (let checkbox of checkboxes) {
            if (checkbox !== this) {
              checkbox.checked = event.target.checked;
              checkbox.dispatchEvent(new Event("change", { bubbles: true }));
            }
          }
        } else {
          if (!event.target.checked) {
            this.checked = false;
          }
        }
      }
    };

    window.addEventListener("change", handleChange);

    this.cleanup = () => {
      window.removeEventListener("change", handleChange);
    };
  }

  disconnectedCallback() {
    this.#cleanup();
  }
}

customElements.define("uv-checkbox-all", CheckboxAll, { extends: "input" });
