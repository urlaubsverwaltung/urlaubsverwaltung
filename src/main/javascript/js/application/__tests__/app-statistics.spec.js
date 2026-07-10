import { createDatepicker } from "../../../components/datepicker";

vi.mock("../../../components/datepicker", () => ({ createDatepicker: vi.fn() }));

describe("app-statistics", function () {
  beforeAll(async function () {
    await import("../app-statistics");
  });

  afterEach(function () {
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
  });

  function dispatchTurboVisit(action) {
    document.dispatchEvent(new CustomEvent("turbo:visit", { detail: { action } }));
  }

  function dispatchTurboBeforeRender(newBody) {
    document.dispatchEvent(new CustomEvent("turbo:before-render", { detail: { newBody } }));
  }

  function createFormBody() {
    const newBody = document.createElement("body");
    newBody.innerHTML = `
      <form id="form-date-from-to">
        <button type="submit" disabled>Bestätigen</button>
      </form>
    `;
    return newBody;
  }

  it("creates datepickers for the from/to inputs", function () {
    expect(createDatepicker).toHaveBeenCalledWith("#from-date-input", expect.objectContaining({ urlPrefix: "" }));
    expect(createDatepicker).toHaveBeenCalledWith("#to-date-input", expect.objectContaining({ urlPrefix: "" }));
  });

  describe("period form submit button", function () {
    it("is enabled again on restore render (history back)", function () {
      const newBody = createFormBody();
      const button = newBody.querySelector("button[type='submit']");
      expect(button.hasAttribute("disabled")).toBe(true);

      dispatchTurboVisit("restore");
      dispatchTurboBeforeRender(newBody);

      expect(button.hasAttribute("disabled")).toBe(false);
    });

    it("is not touched on advancing render", function () {
      const newBody = createFormBody();
      const button = newBody.querySelector("button[type='submit']");

      dispatchTurboVisit("advance");
      dispatchTurboBeforeRender(newBody);

      expect(button.hasAttribute("disabled")).toBe(true);
    });
  });

  describe("period popover", function () {
    it("is closed when the period form has been submitted", function () {
      document.body.innerHTML = `
        <div popover>
          <form id="form-date-from-to"></form>
        </div>
      `;
      const popover = document.querySelector("[popover]");
      popover.hidePopover = vi.fn();

      document
        .querySelector("#form-date-from-to")
        .dispatchEvent(new CustomEvent("turbo:submit-end", { bubbles: true }));

      expect(popover.hidePopover).toHaveBeenCalled();
    });

    it("is not closed when another form has been submitted", function () {
      document.body.innerHTML = `
        <div popover>
          <form id="some-other-form"></form>
        </div>
      `;
      const popover = document.querySelector("[popover]");
      popover.hidePopover = vi.fn();

      document.querySelector("#some-other-form").dispatchEvent(new CustomEvent("turbo:submit-end", { bubbles: true }));

      expect(popover.hidePopover).not.toHaveBeenCalled();
    });
  });
});
