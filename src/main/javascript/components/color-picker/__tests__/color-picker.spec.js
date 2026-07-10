describe("color-picker", function () {
  beforeAll(async function () {
    await import("../color-picker");
  });

  afterEach(function () {
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
  });

  function renderColorPicker() {
    document.body.innerHTML = `
      <div is="uv-color-picker">
        <label id="color-picker-label" for="color-picker-checkbox" class="color-picker-button">
          <span class="color-picker-button-color"></span>
          <span class="sr-only">Farbe</span>
        </label>
        <input type="checkbox" id="color-picker-checkbox" class="shrink-0" />
        <ul id="color-popup" class="color-picker-dialog list-none">
          <li class="color-picker-option">
            <label for="color-radio-0">
              <input type="radio" name="color" id="color-radio-0" value="RED" checked />
            </label>
          </li>
          <li class="color-picker-option">
            <label for="color-radio-1">
              <input type="radio" name="color" id="color-radio-1" value="BLUE" />
            </label>
          </li>
          <li class="color-picker-option">
            <label for="color-radio-2">
              <input type="radio" name="color" id="color-radio-2" value="GREEN" />
            </label>
          </li>
        </ul>
      </div>
    `;
    return {
      picker: document.querySelector("[is='uv-color-picker']"),
      toggleButton: document.querySelector(".color-picker-button"),
      toggleCheckbox: document.querySelector("#color-picker-checkbox"),
      dialog: document.querySelector(".color-picker-dialog"),
      options: [...document.querySelectorAll(".color-picker-option")],
    };
  }

  function click(element, options = {}) {
    const event = new MouseEvent("click", { bubbles: true, cancelable: true, ...options });
    element.dispatchEvent(event);
    return event;
  }

  function keydown(element, key) {
    const event = new KeyboardEvent("keydown", { bubbles: true, cancelable: true, key });
    element.dispatchEvent(event);
    return event;
  }

  describe("initial setup", function () {
    it("marks the dialog as a listbox referencing the toggle button", function () {
      const { dialog } = renderColorPicker();

      expect(dialog.getAttribute("role")).toBe("listbox");
      expect(dialog.getAttribute("aria-labelledby")).toBe("color-picker-label");
    });

    it("marks the currently selected option and sets aria-activedescendant", function () {
      const { dialog, options } = renderColorPicker();

      expect(options[0].getAttribute("aria-selected")).toBe("true");
      expect(options[1].getAttribute("aria-selected")).toBe("false");
      expect(options[2].getAttribute("aria-selected")).toBe("false");
      expect(dialog.getAttribute("aria-activedescendant")).toBe(options[0].id);
    });

    it("labels every option with its lowercased color value", function () {
      const { options } = renderColorPicker();

      expect(options[0].getAttribute("aria-label")).toBe("red");
      expect(options[1].getAttribute("aria-label")).toBe("blue");
    });

    it("makes the radio inputs unfocusable via tab (navigation is via arrow keys)", function () {
      renderColorPicker();

      for (const input of document.querySelectorAll("input")) {
        expect(input.getAttribute("tabindex")).toBe("-1");
      }
    });

    it("makes the picker itself focusable", function () {
      const { picker } = renderColorPicker();

      expect(picker.getAttribute("tabindex")).toBe("0");
    });
  });

  describe("opening and closing via click", function () {
    it("opens the dialog when the toggle button is clicked", function () {
      const { picker, toggleButton, toggleCheckbox } = renderColorPicker();

      click(toggleButton);

      expect(picker.dataset.open).toBe("");
      expect(toggleCheckbox.checked).toBe(true);
    });

    it("closes the dialog when the toggle button is clicked again", function () {
      const { picker, toggleButton } = renderColorPicker();

      click(toggleButton);
      click(toggleButton);

      expect(picker.dataset.open).toBeUndefined();
    });

    it("prevents the native checkbox toggle behavior on the toggle button", function () {
      const { toggleButton } = renderColorPicker();

      const event = click(toggleButton);

      expect(event.defaultPrevented).toBe(true);
    });

    it("selects a color and closes the dialog when clicking an option", function () {
      const { picker, toggleButton, options } = renderColorPicker();
      click(toggleButton); // open

      const blueInput = options[1].querySelector("input");
      click(options[1].querySelector("label"));

      expect(blueInput.checked).toBe(true);
      expect(picker.dataset.open).toBeUndefined();
    });

    it("closes when clicking outside of the picker", function () {
      const { picker, toggleButton } = renderColorPicker();
      document.body.insertAdjacentHTML("beforeend", `<button id="outside">outside</button>`);
      click(toggleButton); // open
      expect(picker.dataset.open).toBe("");

      click(document.querySelector("#outside"));

      expect(picker.dataset.open).toBeUndefined();
    });

    it("does not close when clicking the toggle button while already open other than via toggle", function () {
      const { picker, toggleButton } = renderColorPicker();
      click(toggleButton); // open

      // clicking inside the picker itself (not the dialog, not the toggle button) should not close it
      click(picker);

      expect(picker.dataset.open).toBe("");
    });
  });

  describe("keyboard interaction", function () {
    it("ignores key presses when the picker itself is not focused", function () {
      const { picker } = renderColorPicker();

      keydown(picker, "ArrowDown");

      expect(picker.dataset.open).toBeUndefined();
    });

    it("opens the dialog on Enter", function () {
      const { picker } = renderColorPicker();
      picker.focus();

      const event = keydown(picker, "Enter");

      expect(picker.dataset.open).toBe("");
      expect(event.defaultPrevented).toBe(false);
    });

    it("opens the dialog on ArrowDown and focuses the first (selected) option", function () {
      const { picker, dialog, options } = renderColorPicker();
      picker.focus();

      keydown(picker, "ArrowDown");

      expect(picker.dataset.open).toBe("");
      expect(dialog.getAttribute("aria-activedescendant")).toBe(options[0].id);
    });

    it("moves the focused option forward on ArrowDown while open", function () {
      const { picker, dialog, options } = renderColorPicker();
      picker.focus();
      keydown(picker, "ArrowDown"); // opens, focuses option 0

      keydown(picker, "ArrowDown"); // moves to option 1

      expect(dialog.getAttribute("aria-activedescendant")).toBe(options[1].id);
      expect(options[1].classList.contains("active")).toBe(true);
      expect(options[0].classList.contains("active")).toBe(false);
    });

    it("does not move past the last option", function () {
      const { picker, dialog, options } = renderColorPicker();
      picker.focus();
      keydown(picker, "ArrowDown");
      keydown(picker, "ArrowDown");
      keydown(picker, "ArrowDown");

      keydown(picker, "ArrowDown"); // already at last option

      expect(dialog.getAttribute("aria-activedescendant")).toBe(options[2].id);
    });

    it("moves the focused option backward on ArrowUp", function () {
      const { picker, dialog, options } = renderColorPicker();
      picker.focus();
      keydown(picker, "ArrowDown");
      keydown(picker, "ArrowDown"); // focus option 1

      keydown(picker, "ArrowUp"); // back to option 0

      expect(dialog.getAttribute("aria-activedescendant")).toBe(options[0].id);
    });

    it("does not move before the first option", function () {
      const { picker, dialog, options } = renderColorPicker();
      picker.focus();
      keydown(picker, "ArrowDown"); // focus option 0

      keydown(picker, "ArrowUp"); // already at first option

      expect(dialog.getAttribute("aria-activedescendant")).toBe(options[0].id);
    });

    it("selects the focused option and closes the dialog on Enter while open", function () {
      const { picker, options } = renderColorPicker();
      picker.focus();
      keydown(picker, "ArrowDown"); // open, focus option 0 (already selected)
      keydown(picker, "ArrowDown"); // focus option 1

      keydown(picker, "Enter");

      expect(options[1].querySelector("input").checked).toBe(true);
      expect(options[0].querySelector("input").checked).toBe(false);
      expect(picker.dataset.open).toBeUndefined();
    });

    it("is not itself responsible for Escape (closing is handled globally on keyup)", function () {
      const { picker } = renderColorPicker();
      picker.focus();
      keydown(picker, "ArrowDown"); // open

      keydown(picker, "Escape");

      expect(picker.dataset.open).toBe("");
    });
  });

  describe("global Escape handling", function () {
    it("closes any open picker on keyup Escape, even without focus", function () {
      const { picker, toggleButton } = renderColorPicker();
      click(toggleButton); // open without focusing the picker element itself

      document.dispatchEvent(new KeyboardEvent("keyup", { bubbles: true, key: "Escape" }));

      expect(picker.dataset.open).toBeUndefined();
    });
  });
});
