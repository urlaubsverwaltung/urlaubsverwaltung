import { initAutosubmit } from "../autosubmit";

describe("autosubmit", function () {
  beforeAll(function () {
    initAutosubmit();
  });

  afterEach(function () {
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
    vi.useRealTimers();
  });

  function dispatch(type, element, options = {}) {
    const event = new Event(type, { bubbles: true, cancelable: true, ...options });
    element.dispatchEvent(event);
    return event;
  }

  describe("via a target's own form", function () {
    it("submits the form on 'input' when data-auto-submit is present without a value", function () {
      document.body.innerHTML = `
        <form>
          <input id="query" data-auto-submit />
        </form>
      `;
      const form = document.querySelector("form");
      form.requestSubmit = vi.fn();

      dispatch("input", document.querySelector("#query"));

      expect(form.requestSubmit).toHaveBeenCalledTimes(1);
      expect(form.requestSubmit).toHaveBeenCalledWith();
    });

    it("does not submit on 'input' without data-auto-submit", function () {
      document.body.innerHTML = `
        <form>
          <input id="query" />
        </form>
      `;
      const form = document.querySelector("form");
      form.requestSubmit = vi.fn();

      dispatch("input", document.querySelector("#query"));

      expect(form.requestSubmit).not.toHaveBeenCalled();
    });

    it("does not submit when the input event has already been prevented", function () {
      document.body.innerHTML = `
        <form>
          <input id="query" data-auto-submit />
        </form>
      `;
      const form = document.querySelector("form");
      form.requestSubmit = vi.fn();

      const query = document.querySelector("#query");
      query.addEventListener("input", (event) => event.preventDefault());
      dispatch("input", query);

      expect(form.requestSubmit).not.toHaveBeenCalled();
    });

    it("does not submit on 'input' inside a duet-date-picker", function () {
      document.body.innerHTML = `
        <form>
          <duet-date-picker>
            <input id="query" data-auto-submit />
          </duet-date-picker>
        </form>
      `;
      const form = document.querySelector("form");
      form.requestSubmit = vi.fn();

      dispatch("input", document.querySelector("#query"));

      expect(form.requestSubmit).not.toHaveBeenCalled();
    });
  });

  describe("via an explicit target element", function () {
    it("submits the form via the referenced submit button on 'change'", function () {
      document.body.innerHTML = `
        <form>
          <select id="filter" data-auto-submit="submit-button"></select>
          <button id="submit-button" type="submit">Submit</button>
        </form>
      `;
      const form = document.querySelector("form");
      form.requestSubmit = vi.fn();

      dispatch("change", document.querySelector("#filter"));

      const submitButton = document.querySelector("#submit-button");
      expect(form.requestSubmit).toHaveBeenCalledWith(submitButton);
    });
  });

  describe("'change' event", function () {
    it("is ignored for text-like inputs", function () {
      document.body.innerHTML = `
        <form>
          <input type="text" id="query" data-auto-submit />
        </form>
      `;
      const form = document.querySelector("form");
      form.requestSubmit = vi.fn();

      dispatch("change", document.querySelector("#query"));

      expect(form.requestSubmit).not.toHaveBeenCalled();
    });

    it("is honored for a checkbox", function () {
      document.body.innerHTML = `
        <form>
          <input type="checkbox" id="active" data-auto-submit />
        </form>
      `;
      const form = document.querySelector("form");
      form.requestSubmit = vi.fn();

      dispatch("change", document.querySelector("#active"));

      expect(form.requestSubmit).toHaveBeenCalledTimes(1);
    });
  });

  describe("'duetChange' event", function () {
    it("submits when the target has a value", function () {
      document.body.innerHTML = `
        <form>
          <input id="from" data-auto-submit />
        </form>
      `;
      const form = document.querySelector("form");
      form.requestSubmit = vi.fn();

      const input = document.querySelector("#from");
      input.value = "2024-01-01";
      dispatch("duetChange", input);

      expect(form.requestSubmit).toHaveBeenCalledTimes(1);
    });

    it("does not submit when the target has no value", function () {
      document.body.innerHTML = `
        <form>
          <input id="from" data-auto-submit />
        </form>
      `;
      const form = document.querySelector("form");
      form.requestSubmit = vi.fn();

      dispatch("duetChange", document.querySelector("#from"));

      expect(form.requestSubmit).not.toHaveBeenCalled();
    });

    it("is ignored for text-like inputs even with a value", function () {
      document.body.innerHTML = `
        <form>
          <input type="text" id="from" data-auto-submit />
        </form>
      `;
      const form = document.querySelector("form");
      form.requestSubmit = vi.fn();

      const input = document.querySelector("#from");
      input.value = "2024-01-01";
      dispatch("duetChange", input);

      expect(form.requestSubmit).not.toHaveBeenCalled();
    });
  });

  describe("data-auto-submit-delay", function () {
    it("delays submitting the form by the configured amount", function () {
      vi.useFakeTimers();

      document.body.innerHTML = `
        <form>
          <input id="query" data-auto-submit data-auto-submit-delay="100" />
        </form>
      `;
      const form = document.querySelector("form");
      form.requestSubmit = vi.fn();

      dispatch("input", document.querySelector("#query"));

      expect(form.requestSubmit).not.toHaveBeenCalled();

      vi.advanceTimersByTime(99);
      expect(form.requestSubmit).not.toHaveBeenCalled();

      vi.advanceTimersByTime(1);
      expect(form.requestSubmit).toHaveBeenCalledTimes(1);
    });

    it("cancels a pending delayed submit when triggered again", function () {
      vi.useFakeTimers();

      document.body.innerHTML = `
        <form>
          <input id="query" data-auto-submit data-auto-submit-delay="100" />
        </form>
      `;
      const form = document.querySelector("form");
      form.requestSubmit = vi.fn();
      const query = document.querySelector("#query");

      dispatch("input", query);
      vi.advanceTimersByTime(50);
      dispatch("input", query);
      vi.advanceTimersByTime(50);

      // first scheduled submit should have been cancelled, second is not due yet
      expect(form.requestSubmit).not.toHaveBeenCalled();

      vi.advanceTimersByTime(50);
      expect(form.requestSubmit).toHaveBeenCalledTimes(1);
    });
  });
});
