const SESSION_KEY = "uv--focus-language-after-reload";

// This module has no teardown/cleanup export: its `keyup`/`click` listeners are attached to
// `globalThis` once at import time and never removed. So the interaction tests below share a
// SINGLE persistent import + DOM fixture (built once in beforeAll) instead of re-importing per
// test - re-importing would leave stale global listeners behind that also react to later events.
// The "initial focus on reload" tests at the very end are the only ones that re-import (each needs
// a different sessionStorage/DOM precondition *before* import), so they are ordered last to avoid
// polluting the interaction tests above with leftover listeners.
describe("language-picker", function () {
  describe("interaction behavior", function () {
    let userSettingsForm;
    let localeDe;
    let localeEn;

    beforeAll(async function () {
      document.body.innerHTML = `
        <form id="user-settings-form" action="/web/person/1/notifications">
          <fieldset id="fieldset-language">
            <input type="radio" name="locale" value="de" checked />
            <input type="radio" name="locale" value="en" />
          </fieldset>
          <input type="text" name="other" />
        </form>
        <button id="outside-button">outside</button>
      `;

      userSettingsForm = document.querySelector("#user-settings-form");
      localeDe = document.querySelector("input[value='de']");
      localeEn = document.querySelector("input[value='en']");
      userSettingsForm.submit = vi.fn();

      await import("../language-picker");
    });

    beforeEach(function () {
      sessionStorage.clear();
      userSettingsForm.submit.mockClear();
      // reset the module's internal "focused" tracking to a known baseline
      dispatchOn(localeDe, "focusout");
    });

    function dispatchOn(element, type, extra = {}) {
      const event = new Event(type, { bubbles: true, ...extra });
      Object.assign(event, extra);
      element.dispatchEvent(event);
    }

    function keyup(key) {
      globalThis.dispatchEvent(new KeyboardEvent("keyup", { bubbles: true, key }));
    }

    function click(element) {
      element.dispatchEvent(new MouseEvent("click", { bubbles: true }));
    }

    it("memoizes the focus and submits the form on a keyboard-driven locale change", function () {
      dispatchOn(localeDe, "focusin");
      keyup("Tab");

      dispatchOn(localeEn, "change");

      expect(sessionStorage.getItem(SESSION_KEY)).toBe("true");
      expect(userSettingsForm.submit).toHaveBeenCalledTimes(1);
    });

    it("does not clean the memoized focus on Tab while the language group is focused", function () {
      sessionStorage.setItem(SESSION_KEY, "true");
      dispatchOn(localeDe, "focusin");

      keyup("Tab");

      expect(sessionStorage.getItem(SESSION_KEY)).toBe("true");
    });

    it("cleans the memoized focus on keyup once focus left the language group", function () {
      sessionStorage.setItem(SESSION_KEY, "true");

      keyup("Tab");

      expect(sessionStorage.getItem(SESSION_KEY)).toBeNull();
    });

    it("does not memoize when Tab was not the last key, even while focused", function () {
      dispatchOn(localeDe, "focusin");
      keyup("a");

      dispatchOn(localeEn, "change");

      expect(sessionStorage.getItem(SESSION_KEY)).toBeNull();
      expect(userSettingsForm.submit).toHaveBeenCalledTimes(1);
    });

    it("submits the form on a locale change regardless of the keyboard flag", function () {
      dispatchOn(localeEn, "change");

      expect(userSettingsForm.submit).toHaveBeenCalledTimes(1);
    });

    it("does not submit the form when a non-locale field changes", function () {
      dispatchOn(document.querySelector("[name='other']"), "change");

      expect(userSettingsForm.submit).not.toHaveBeenCalled();
    });

    it("cleans the memoized focus when clicking outside the language fieldset", function () {
      sessionStorage.setItem(SESSION_KEY, "true");

      click(document.querySelector("#outside-button"));

      expect(sessionStorage.getItem(SESSION_KEY)).toBeNull();
    });

    it("does not clean the memoized focus when clicking inside the language fieldset", function () {
      sessionStorage.setItem(SESSION_KEY, "true");

      click(localeDe);

      expect(sessionStorage.getItem(SESSION_KEY)).toBe("true");
    });

    it("does not clean the memoized focus when clicking a nested descendant of the fieldset", function () {
      sessionStorage.setItem(SESSION_KEY, "true");
      const fieldset = document.querySelector("#fieldset-language");
      fieldset.insertAdjacentHTML("beforeend", `<span id="nested"><em>deep</em></span>`);

      click(document.querySelector("#nested em"));

      expect(sessionStorage.getItem(SESSION_KEY)).toBe("true");
    });
  });

  describe("initial focus on reload", function () {
    beforeEach(function () {
      // the outer describe's beforeAll already imported this module once; force a fresh
      // evaluation so its module-level DOM/sessionStorage reads run again for this test
      vi.resetModules();
    });

    afterEach(function () {
      document.body.innerHTML = "";
      sessionStorage.clear();
    });

    function render() {
      document.body.innerHTML = `
        <form id="user-settings-form">
          <fieldset id="fieldset-language">
            <input type="radio" name="locale" value="de" checked />
            <input type="radio" name="locale" value="en" />
          </fieldset>
        </form>
      `;
    }

    it("focuses the checked locale radio when the reload-focus flag is set", async function () {
      render();
      sessionStorage.setItem(SESSION_KEY, "true");

      await import("../language-picker");

      expect(document.activeElement).toBe(document.querySelector("input[value='de']"));
    });

    it("does not focus anything when the reload-focus flag is not set", async function () {
      render();

      await import("../language-picker");

      expect(document.activeElement).not.toBe(document.querySelector("input[value='de']"));
    });
  });
});
