vi.mock("../../../components/datepicker", () => ({ createDatepicker: vi.fn() }));

describe("account-form", function () {
  beforeEach(function () {
    globalThis.uv = {
      apiPrefix: "/api",
      personId: 42,
    };
  });

  afterEach(function () {
    // cleanup DOM
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
    vi.resetModules();
  });

  describe("vacation days expire fieldset", function () {
    it("is enabled when override is unchecked and globally enabled", async function () {
      await setupHtml({ globallyEnabled: true });

      setOverrideChecked(false);
      checkLocally();
      dispatchChange("overrideVacationDaysExpire");

      expect(fieldset().hasAttribute("disabled")).toBe(false);
    });

    it("is disabled when override is unchecked and not globally enabled", async function () {
      await setupHtml({ globallyEnabled: false });

      setOverrideChecked(false);
      checkLocally();
      dispatchChange("overrideVacationDaysExpire");

      expect(fieldset().hasAttribute("disabled")).toBe(true);
    });

    it("is enabled when override is checked and locally is true, even when not globally enabled", async function () {
      await setupHtml({ globallyEnabled: false });

      setOverrideChecked(true);
      checkLocally("true");
      dispatchChange("overrideVacationDaysExpire");

      expect(fieldset().hasAttribute("disabled")).toBe(false);
    });

    it("is disabled when override is checked and locally is false, even when globally enabled", async function () {
      await setupHtml({ globallyEnabled: true });

      setOverrideChecked(true);
      checkLocally("false");
      dispatchChange("overrideVacationDaysExpire");

      expect(fieldset().hasAttribute("disabled")).toBe(true);
    });

    it("is disabled when override is checked and locally is not set, even when globally enabled", async function () {
      await setupHtml({ globallyEnabled: true });

      setOverrideChecked(true);
      checkLocally();
      dispatchChange("overrideVacationDaysExpire");

      expect(fieldset().hasAttribute("disabled")).toBe(true);
    });

    it("reacts to change events on 'doRemainingVacationDaysExpireLocally' as well", async function () {
      await setupHtml({ globallyEnabled: false });

      setOverrideChecked(true);
      checkLocally("true");
      dispatchChange("doRemainingVacationDaysExpireLocally");

      expect(fieldset().hasAttribute("disabled")).toBe(false);
    });

    it("does not react to change events of unrelated form elements", async function () {
      await setupHtml({ globallyEnabled: true, fieldsetDisabledInitially: true });

      setOverrideChecked(true);
      checkLocally("true");

      document.querySelector("#unrelated").dispatchEvent(new Event("change", { bubbles: true }));

      expect(fieldset().hasAttribute("disabled")).toBe(true);
    });
  });

  describe("'doRemainingVacationDaysExpireLocally' radio buttons", function () {
    it("are enabled when override is checked", async function () {
      await setupHtml({ globallyEnabled: true });

      setOverrideChecked(true);
      dispatchChange("overrideVacationDaysExpire");

      for (const radioButton of locallyRadioButtons()) {
        expect(radioButton.hasAttribute("disabled")).toBe(false);
      }
    });

    it("are disabled when override is unchecked", async function () {
      await setupHtml({ globallyEnabled: true });

      setOverrideChecked(false);
      dispatchChange("overrideVacationDaysExpire");

      for (const radioButton of locallyRadioButtons()) {
        expect(radioButton.hasAttribute("disabled")).toBe(true);
      }
    });
  });

  function fieldset() {
    return document.querySelector("#remaining-vacation-days-expire-fieldset");
  }

  function overrideCheckbox() {
    return document.querySelector("[name='overrideVacationDaysExpire']");
  }

  function locallyRadioButtons() {
    return document.querySelectorAll("[name='doRemainingVacationDaysExpireLocally']");
  }

  function setOverrideChecked(checked) {
    overrideCheckbox().checked = checked;
  }

  function checkLocally(value) {
    for (const radioButton of locallyRadioButtons()) {
      radioButton.checked = radioButton.value === value;
    }
  }

  function dispatchChange(name) {
    // name is a fixed test constant (never user input), and jsdom does not implement CSS.escape
    // eslint-disable-next-line unicorn/require-css-escape
    document.querySelector(`[name='${name}']`).dispatchEvent(new Event("change", { bubbles: true }));
  }

  async function setupHtml({ globallyEnabled, fieldsetDisabledInitially = false }) {
    document.body.innerHTML = `
      <input id="holidaysAccountValidFrom" />
      <input id="holidaysAccountValidTo" />
      <input id="expiryDate" />
      <form id="holiday-account-settings-form">
        <input id="unrelated" type="checkbox" />
        <input type="checkbox" name="overrideVacationDaysExpire" value="true" />
        <input type="radio" name="doRemainingVacationDaysExpireLocally" value="true" />
        <input type="radio" name="doRemainingVacationDaysExpireLocally" value="false" />
        <fieldset
          id="remaining-vacation-days-expire-fieldset"
          data-globally-enabled="${globallyEnabled}"
          ${fieldsetDisabledInitially ? "disabled" : ""}
        ></fieldset>
      </form>
    `;

    await import("../account-form");
  }
});
