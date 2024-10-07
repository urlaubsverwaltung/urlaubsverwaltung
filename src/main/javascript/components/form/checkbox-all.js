export class CheckboxAll extends HTMLInputElement {
  #cleanup;

  connectedCallback() {
    const handleChange = (event) => {
      const ignore = this.dataset.ignore ?? "";
      const form = this.closest("form");

      if (event.target.getAttribute("id") === ignore) {
        return;
      }

      if (form === event.target.closest("form")) {
        if (this === event.target) {
          const checkboxes = [...form.querySelectorAll("input[type='checkbox']")];
          for (let checkbox of checkboxes) {
            if (checkbox !== this && checkbox.getAttribute("id") !== ignore) {
              checkbox.checked = event.target.checked;
              checkbox.dispatchEvent(new Event("change", { bubbles: true }));
            }
          }
        } else {
          if (event.target.checked) {
            const checkboxes = [...form.querySelectorAll("input[type='checkbox']")];
            const allChecked = checkboxes.every((checkbox) => checkbox === this || checkbox.checked);
            if (allChecked) {
              this.checked = true;
            }
          } else {
            this.checked = false;
          }
        }
      }
    };

    globalThis.addEventListener("change", handleChange);

    this.cleanup = () => {
      globalThis.removeEventListener("change", handleChange);
    };
  }

  disconnectedCallback() {
    this.#cleanup();
  }
}

customElements.define("uv-checkbox-all", CheckboxAll, { extends: "input" });
