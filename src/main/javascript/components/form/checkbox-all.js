export class CheckboxAll extends HTMLInputElement {
  #cleanup;

  connectedCallback() {
    const handleChange = (event) => {
      const ignore = this.dataset.ignore ?? "";

      if (event.target.getAttribute("id") === ignore) {
        return;
      }

      const form = this.closest("form");

      if (form === event.target.closest("form")) {
        if (this === event.target) {
          const checkboxes = [...form.querySelectorAll("input[type='checkbox']")];
          for (let checkbox of checkboxes) {
            if (checkbox === this || checkbox.getAttribute("id") === ignore) {
              continue;
            }

            checkbox.checked = event.target.checked;
            checkbox.dispatchEvent(new Event("change", { bubbles: true }));
          }
        } else {
          if (event.target.checked) {
            const checkboxes = [...form.querySelectorAll("input[type='checkbox']")];
            const isAllChecked = checkboxes.every((checkbox) => checkbox === this || checkbox.checked);
            if (isAllChecked) {
              this.checked = true;
            }
          } else {
            this.checked = false;
          }
        }
      }
    };

    addEventListener("change", handleChange);

    this.#cleanup = () => {
      removeEventListener("change", handleChange);
    };
  }

  disconnectedCallback() {
    this.#cleanup();
  }
}

customElements.define("uv-checkbox-all", CheckboxAll, { extends: "input" });
