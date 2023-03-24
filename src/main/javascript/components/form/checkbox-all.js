export class CheckboxAll extends HTMLInputElement {
  #cleanup;

  connectedCallback() {
    const handleChange = (event) => {
      const form = this.closest("form");
      const checkboxes = [...form.querySelectorAll("input[type='checkbox']")];
      for (let checkbox of checkboxes) {
        if (checkbox !== this) {
          checkbox.checked = event.target.checked;
          checkbox.dispatchEvent(new Event("change", { bubbles: true }));
        }
      }
    };

    this.addEventListener("change", handleChange);

    this.cleanup = () => {
      this.removeEventListener("change", handleChange);
    };
  }

  disconnectedCallback() {
    this.#cleanup();
  }
}

customElements.define("uv-checkbox-all", CheckboxAll, { extends: "input" });
