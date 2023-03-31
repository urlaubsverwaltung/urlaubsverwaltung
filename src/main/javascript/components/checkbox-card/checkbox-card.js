export class CheckboxCard extends HTMLElement {
  #cleanup = () => {};

  connectedCallback() {
    const handleClick = (event) => {
      const { target } = event;
      const checkbox = this.querySelector("input[type='checkbox']");
      if (target !== checkbox && !target.matches("label")) {
        checkbox.checked = !checkbox.checked;
        checkbox.dispatchEvent(new Event("change", { bubbles: true }));
      }
    };

    this.addEventListener("click", handleClick);

    this.cleanup = () => {
      this.removeEventListener("click", handleClick);
    };
  }

  disconnectedCallback() {
    this.#cleanup();
  }
}

customElements.define("uv-checkbox-card", CheckboxCard);
