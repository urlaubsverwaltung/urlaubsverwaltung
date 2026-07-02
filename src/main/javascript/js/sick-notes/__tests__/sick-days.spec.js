import "../sick-days";

vi.mock("../../../components/datepicker");

describe("sick-days", function () {
  afterEach(function () {
    // cleanup DOM
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
  });

  describe("period form submit button", function () {
    it("is enabled again on restore render (history back)", function () {
      const newBody = createPeriodFormBody();
      const button = newBody.querySelector("button[type='submit']");
      expect(button.hasAttribute("disabled")).toBe(true);

      dispatchTurboVisit("restore");
      dispatchTurboBeforeRender(newBody);

      expect(button.hasAttribute("disabled")).toBe(false);
    });

    it("is not touched on advancing render", function () {
      const newBody = createPeriodFormBody();
      const button = newBody.querySelector("button[type='submit']");

      dispatchTurboVisit("advance");
      dispatchTurboBeforeRender(newBody);

      expect(button.hasAttribute("disabled")).toBe(true);
    });
  });

  describe("period popover", function () {
    it("is closed when period form has been submitted", function () {
      document.body.innerHTML = `
        <div popover>
          <form id="form-date-from-to"></form>
        </div>
      `;

      const popover = document.querySelector("[popover]");
      popover.hidePopover = vi.fn();

      const form = document.querySelector("#form-date-from-to");
      form.dispatchEvent(new CustomEvent("turbo:submit-end", { bubbles: true }));

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

      const form = document.querySelector("#some-other-form");
      form.dispatchEvent(new CustomEvent("turbo:submit-end", { bubbles: true }));

      expect(popover.hidePopover).not.toHaveBeenCalled();
    });
  });
});

function createPeriodFormBody() {
  const newBody = document.createElement("body");
  newBody.innerHTML = `
    <form id="form-date-from-to">
      <button type="submit" disabled>Bestätigen</button>
    </form>
  `;
  return newBody;
}

function dispatchTurboVisit(action) {
  document.dispatchEvent(new CustomEvent("turbo:visit", { detail: { action } }));
}

function dispatchTurboBeforeRender(newBody) {
  document.dispatchEvent(new CustomEvent("turbo:before-render", { detail: { newBody } }));
}
