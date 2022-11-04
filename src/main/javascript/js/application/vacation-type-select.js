class VacationTypeSelect extends HTMLSelectElement {
  connectedCallback() {
    const handleChange = (event) => {
      event.preventDefault();

      const value = this.selectedOptions[0].dataset.vacationtypeCategory;

      const overtime = document.querySelector("#overtime");
      const specialLeave = document.querySelector("#special-leave");

      if (value === "SPECIALLEAVE") {
        overtime?.classList.add("hidden");
        specialLeave?.classList.remove("hidden");
      } else if (value === "OVERTIME") {
        overtime?.classList.remove("hidden");
        specialLeave?.classList.add("hidden");
      } else {
        overtime?.classList.add("hidden");
        specialLeave?.classList.add("hidden");
      }
    };

    this.addEventListener("change", handleChange);

    this.cleanup = () => {
      this.removeEventListener("change", handleChange);
    };
  }

  disconnectedCallback() {
    this.cleanup();
  }
}

customElements.define("uv-vacation-type-select", VacationTypeSelect, { extends: "select" });
