export class ListBoxOption extends HTMLLIElement {
  constructor() {
    super();
    this.setAttribute("id", `list-box-option-${randomKey()}`);
    this.setAttribute("role", "option");
    this.classList.add("list-box-option");
    this.style.cursor = "pointer";
  }
  set selected(selected) {
    this.setAttribute("aria-selected", selected ? "true" : "false");
  }

  get selected() {
    return this.dataset.selected === "true";
  }

  set focused(focused) {
    if (focused) {
      this.dataset.focused = "true";
    } else {
      this.classList.remove("active");
      delete this.dataset.focused;
    }
  }

  set label(label) {
    this.setAttribute("aria-label", label);
  }

  get label() {
    return this.getAttribute("aria-label");
  }

  set value(value) {
    this.dataset.value = value;
  }

  get value() {
    return this.dataset.value;
  }
}

function randomKey() {
  return Math.random()
    .toString(36)
    .replaceAll(/[^a-z]+/g, "")
    .slice(0, 12);
}

customElements.define("uv-list-box-option", ListBoxOption, { extends: "li" });
