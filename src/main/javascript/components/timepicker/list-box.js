export class ListBox extends HTMLDivElement {
  #cleanup = () => {};

  #open = false;
  #focusedOptionIndex = -1;
  #value;
  #ul;

  constructor(options) {
    super();

    this.classList.add(
      "list-box",
      "dark:tw-border",
      "dark:tw-border-zinc-700",
      "dark:tw-bg-zinc-900",
      "dark:tw-rounded",
      "dark:tw-mt-2",
      "dark:tw-backdrop-filter",
      "dark:tw-backdrop-blur-xl",
      "supports-backdrop-blur:dark:tw-bg-zinc-900/70",
    );
    this.style.overflow = "auto";
    this.style.height = "10rem";

    const ul = (this.#ul = document.createElement("ul"));
    ul.style.listStyleType = "none";
    ul.style.margin = "0";
    ul.style.padding = "0";
    ul.style.fontVariantNumeric = "tabular-nums";
    ul.setAttribute("role", "listbox");
    ul.append(...options);
    this.append(ul);
  }

  get selectedOptionId() {
    return this.#ul.getAttribute("aria-activedescendant");
  }

  set selectedOptionId(selectedId) {
    if (selectedId) {
      this.#ul.setAttribute("aria-activedescendant", selectedId);
    } else {
      this.#ul.removeAttribute("aria-activedescendant");
    }
  }

  get value() {
    return this.#value;
  }

  set opened(open) {
    if (this.#open !== open) {
      this.#open = open;
      if (open) {
        this.#renderFocusedElement(this.#ul.querySelectorAll("li"));
        document.body.append(this);
      } else {
        this.remove();
      }
    }
  }

  get opened() {
    return this.#open;
  }

  open({ relativeTo }) {
    if (relativeTo) {
      const { left, top, height } = relativeTo.getBoundingClientRect();
      this.style.position = "absolute";
      this.style.left = `${left}px`;
      this.style.top = `calc(${top + height}px + .125rem)`;
    }
    this.opened = true;
  }

  close() {
    this.opened = false;
  }

  focusValue(value) {
    const options = this.#ul.querySelectorAll("li");
    for (let [index, option] of options.entries()) {
      if (option.value.startsWith(value)) {
        this.#focusedOptionIndex = index;
        this.#renderFocusedElement(options);
        break;
      }
    }
  }

  focusNextOption() {
    const options = this.#ul.querySelectorAll("li");
    if (this.#focusedOptionIndex < options.length - 1) {
      this.#focusedOptionIndex++;
      this.#renderFocusedElement(options);
    }
  }

  focusPreviousOption() {
    if (this.#focusedOptionIndex > 0) {
      const options = this.#ul.querySelectorAll("li");
      this.#focusedOptionIndex--;
      this.#renderFocusedElement(options);
    }
  }

  connectedCallback() {
    if (this.#focusedOptionIndex > -1) {
      this.selectedOptionId = this.#ul.querySelector(`li:nth-child(${this.#focusedOptionIndex})`)?.getAttribute("id");
    }

    const handleGlobalKeyDown = (event) => {
      if (!this.opened) {
        return;
      }

      const options = this.#ul.querySelectorAll("li");

      if (event.key === "Enter") {
        event.preventDefault(); // prevent form submit

        let selectedOption;

        for (const [index, option] of options.entries()) {
          const selected = index === this.#focusedOptionIndex;
          if (selected) {
            option.selected = true;
            this.#value = option.value;
            selectedOption = option;
          } else {
            option.selected = false;
          }
        }

        this.dispatchEvent(
          new CustomEvent("list-box:value-changed", {
            detail: {
              value: this.#value,
              option: selectedOption,
            },
            bubbles: true,
          }),
        );

        this.close();
      }

      if (event.key === "ArrowDown" || event.key === "ArrowUp") {
        event.preventDefault(); // prevent scrolling TODO required?
        if (event.key === "ArrowDown") {
          this.focusNextOption();
        } else if (event.key === "ArrowUp") {
          this.focusPreviousOption();
        }
      }
    };

    const handleClick = (event) => {
      const target = event.target;
      const clickedOption = target.tag === "LI" ? target : target.closest("li");

      if (clickedOption) {
        // TODO is this event.preventDefault a special case for color-picker?
        // prevent scrolling up to the absolute positioned checkbox.
        event.preventDefault();

        const options = this.#ul.querySelectorAll("li");
        for (const [index, option] of options.entries()) {
          if (option === clickedOption) {
            option.selected = true;
            this.#value = option.value;
            this.#focusedOptionIndex = index;
          } else {
            option.selected = false;
          }
        }

        this.dispatchEvent(
          new CustomEvent("list-box:value-changed", {
            detail: {
              value: clickedOption.value,
              option: clickedOption,
            },
            bubbles: true,
          }),
        );
      }
    };

    // defer event registration to avoid events that could have opened this list-box recently
    // (e.g. a text input with ArrowDown/ArrowUp, see timepicker)
    setTimeout(() => {
      document.addEventListener("keydown", handleGlobalKeyDown);
      this.addEventListener("click", handleClick);
    });

    this.#cleanup = () => {
      document.removeEventListener("keydown", handleGlobalKeyDown);
      this.removeEventListener("click", handleClick);
    };
  }

  #renderFocusedElement(options) {
    let focusedOption;

    for (const [index, option] of options.entries()) {
      if (index === this.#focusedOptionIndex) {
        option.focused = true;
        this.selectedOptionId = option.getAttribute("id");
        focusedOption = option;
      } else {
        option.focused = false;
      }
    }

    if (focusedOption) {
      const { height: optionHeight, top: optionTop } = focusedOption.getBoundingClientRect();
      const { height: containerHeight } = this.getBoundingClientRect();
      if (optionTop > containerHeight) {
        this.scrollTop = focusedOption.offsetTop - optionHeight;
      }
    }
  }

  disconnectedCallback() {
    this.#cleanup();
    // reset index to focus selected option or nothing when list-box is opened again
    this.#focusedOptionIndex = [...this.#ul.querySelectorAll("li")].indexOf(
      this.#ul.querySelector("li[aria-selected='true']"),
    );
  }
}

customElements.define("uv-list-box", ListBox, { extends: "div" });
