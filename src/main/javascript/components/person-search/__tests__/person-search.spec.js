describe("person-search", function () {
  beforeAll(async function () {
    Element.prototype.scrollIntoView = vi.fn();
    await import("../person-search");
  });

  afterEach(function () {
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
    vi.restoreAllMocks();
    vi.useRealTimers();
  });

  function renderPersonSearch({
    suggestionCount = 2,
    messageNothingFound = "Nichts gefunden",
    messageResultsOne = "1 Ergebnis",
    messageResultsOther = "{0} Ergebnisse",
  } = {}) {
    const suggestions = Array.from(
      { length: suggestionCount },
      (_, index) => `<li><a href="#suggestion-${index}" data-person-search-suggestion>Suggestion ${index}</a></li>`,
    ).join("");

    document.body.innerHTML = `
      <uv-person-search
        data-message-nothing-found="${messageNothingFound}"
        data-message-results-one="${messageResultsOne}"
        data-message-results-other="${messageResultsOther}"
      >
        <form>
          <input type="search" id="person-search-input" />
          <button id="person-search-submit" type="submit">Suchen</button>
        </form>
        <div popover="manual" class="popover">
          <turbo-frame id="frame-persons-suggestions">
            <ul class="person-search__list">
              ${suggestions}
            </ul>
          </turbo-frame>
        </div>
        <div class="sr-only" role="status" data-person-search-status></div>
      </uv-person-search>
    `;

    const form = document.querySelector("form");
    const popover = document.querySelector("[popover]");
    form.requestSubmit = vi.fn();
    popover.showPopover = vi.fn();
    popover.hidePopover = vi.fn();

    return {
      widget: document.querySelector("uv-person-search"),
      form,
      input: document.querySelector("#person-search-input"),
      submitButton: document.querySelector("#person-search-submit"),
      popover,
      frame: document.querySelector("#frame-persons-suggestions"),
      statusRegion: document.querySelector("[data-person-search-status]"),
      suggestions: [...document.querySelectorAll("[data-person-search-suggestion]")],
    };
  }

  function dispatch(type, element, options = {}) {
    const event = new Event(type, { bubbles: true, cancelable: true, ...options });
    element.dispatchEvent(event);
    return event;
  }

  function dispatchFocusout(element, relatedTarget) {
    const event = new FocusEvent("focusout", { bubbles: true, cancelable: true, relatedTarget });
    element.dispatchEvent(event);
    return event;
  }

  function dispatchKey(type, element, key) {
    const event = new KeyboardEvent(type, { bubbles: true, cancelable: true, key });
    element.dispatchEvent(event);
    return event;
  }

  function frameRender(target) {
    const event = new CustomEvent("turbo:frame-render", { bubbles: true });
    Object.defineProperty(event, "target", { value: target });
    document.dispatchEvent(event);
  }

  describe("focusing the search input", function () {
    it("submits the form to fetch initial suggestions", function () {
      const { input, form, submitButton } = renderPersonSearch();

      input.focus();
      dispatch("focusin", input);

      expect(form.requestSubmit).toHaveBeenCalledWith(submitButton);
    });

    it("does not submit when a non-input element gets focus", function () {
      const { widget, form, submitButton } = renderPersonSearch();

      widget.focus();
      dispatch("focusin", widget);

      expect(form.requestSubmit).not.toHaveBeenCalledWith(submitButton);
    });
  });

  describe("suggestions popover", function () {
    it("opens once the suggestions frame renders", function () {
      const { frame, popover } = renderPersonSearch();

      frameRender(frame);

      expect(popover.showPopover).toHaveBeenCalledTimes(1);
    });

    it("does not open for renders of a different frame", function () {
      const { popover } = renderPersonSearch();
      const otherFrame = document.createElement("turbo-frame");
      otherFrame.id = "some-other-frame";
      document.body.append(otherFrame);

      frameRender(otherFrame);

      expect(popover.showPopover).not.toHaveBeenCalled();
    });

    it("does not re-open once already visible", function () {
      const { frame, popover } = renderPersonSearch();

      frameRender(frame);
      frameRender(frame);

      expect(popover.showPopover).toHaveBeenCalledTimes(1);
    });

    it("closes on focusout when focus leaves the widget", function () {
      const { widget, popover, frame } = renderPersonSearch();
      frameRender(frame);

      dispatchFocusout(widget, document.body);

      expect(popover.hidePopover).toHaveBeenCalledTimes(1);
    });

    it("stays open on focusout when focus moves within the widget", function () {
      const { widget, popover, frame, suggestions } = renderPersonSearch();
      frameRender(frame);

      dispatchFocusout(widget, suggestions[0]);

      expect(popover.hidePopover).not.toHaveBeenCalled();
    });

    it("stays open on focusout while a suggestion link is being clicked, even without a relatedTarget", function () {
      const { widget, popover, frame, suggestions } = renderPersonSearch();
      frameRender(frame);

      dispatch("pointerdown", suggestions[0]);
      dispatchFocusout(widget);

      expect(popover.hidePopover).not.toHaveBeenCalled();
    });

    it("resumes closing on focusout once the suggestion click completes (pointerup)", function () {
      const { widget, popover, frame, suggestions } = renderPersonSearch();
      frameRender(frame);

      dispatch("pointerdown", suggestions[0]);
      document.dispatchEvent(new Event("pointerup", { bubbles: true }));
      dispatchFocusout(widget);

      expect(popover.hidePopover).toHaveBeenCalledTimes(1);
    });
  });

  describe("result count announcement", function () {
    it("announces the 'nothing found' message when there are no suggestions", function () {
      const { frame, statusRegion } = renderPersonSearch({
        suggestionCount: 0,
        messageNothingFound: "Nichts gefunden",
      });

      frameRender(frame);

      expect(statusRegion.textContent).toBe("Nichts gefunden");
    });

    it("announces the singular results message for one suggestion", function () {
      const { frame, statusRegion } = renderPersonSearch({
        suggestionCount: 1,
        messageResultsOne: "1 Ergebnis",
      });

      frameRender(frame);

      expect(statusRegion.textContent).toBe("1 Ergebnis");
    });

    it("announces the plural results message with the count for multiple suggestions", function () {
      const { frame, statusRegion } = renderPersonSearch({
        suggestionCount: 3,
        messageResultsOther: "{0} Ergebnisse",
      });

      frameRender(frame);

      expect(statusRegion.textContent).toBe("3 Ergebnisse");
    });

    it("re-announces the result count on subsequent renders while the popover stays open", function () {
      const { frame, statusRegion } = renderPersonSearch({ suggestionCount: 2 });

      frameRender(frame);
      frameRender(frame);

      expect(statusRegion.textContent).toBe("{0} Ergebnisse".replace("{0}", "2"));
    });

    it("clears the announcement once the popover closes", function () {
      const { widget, frame, statusRegion } = renderPersonSearch();
      frameRender(frame);

      dispatchFocusout(widget, document.body);

      expect(statusRegion.textContent).toBe("");
    });
  });

  describe("submit button loading state", function () {
    it("shows the loading state 100ms after submit if still loading", function () {
      vi.useFakeTimers();
      const { form, submitButton } = renderPersonSearch();

      dispatch("submit", form);
      vi.advanceTimersByTime(100);

      expect(submitButton.classList.contains("button--loading")).toBe(true);
    });

    it("does not show the loading state once the frame has already rendered", function () {
      vi.useFakeTimers();
      const { form, submitButton, frame } = renderPersonSearch();

      dispatch("submit", form);
      vi.advanceTimersByTime(50);
      frameRender(frame);
      vi.advanceTimersByTime(50);

      expect(submitButton.classList.contains("button--loading")).toBe(false);
    });

    it("removes the loading state once the frame renders", function () {
      const { form, submitButton, frame } = renderPersonSearch();
      submitButton.classList.add("button--loading");

      dispatch("submit", form);
      frameRender(frame);

      expect(submitButton.classList.contains("button--loading")).toBe(false);
    });
  });

  describe("keyboard navigation", function () {
    it("ignores keys other than ArrowUp/ArrowDown/Escape", function () {
      const { widget, popover, frame } = renderPersonSearch();
      frameRender(frame);

      dispatchKey("keydown", widget, "a");

      expect(popover.hidePopover).not.toHaveBeenCalled();
    });

    it("closes the popover and refocuses the input on Escape", function () {
      const { widget, popover, frame, input } = renderPersonSearch();
      frameRender(frame);
      const focusSpy = vi.spyOn(input, "focus");

      const event = dispatchKey("keydown", widget, "Escape");

      expect(event.defaultPrevented).toBe(true);
      expect(popover.hidePopover).toHaveBeenCalledTimes(1);
      expect(focusSpy).toHaveBeenCalledTimes(1);
    });

    it("does nothing on ArrowDown when the popover is closed", function () {
      const { widget, suggestions } = renderPersonSearch();
      const focusSpy = vi.spyOn(suggestions[0], "focus");

      dispatchKey("keydown", widget, "ArrowDown");

      expect(focusSpy).not.toHaveBeenCalled();
    });

    it("does nothing on ArrowDown when there are no suggestions", function () {
      const { widget, frame } = renderPersonSearch({ suggestionCount: 0 });
      frameRender(frame);

      expect(() => dispatchKey("keydown", widget, "ArrowDown")).not.toThrow();
    });

    it("focuses the first suggestion on the first ArrowDown", function () {
      const { widget, frame, suggestions } = renderPersonSearch();
      frameRender(frame);

      const event = dispatchKey("keydown", widget, "ArrowDown");

      expect(document.activeElement).toBe(suggestions[0]);
      expect(event.defaultPrevented).toBe(true);
    });

    it("moves to the next suggestion on subsequent ArrowDown", function () {
      const { widget, frame, suggestions } = renderPersonSearch();
      frameRender(frame);

      dispatchKey("keydown", widget, "ArrowDown");
      suggestions[0].focus();
      dispatchKey("keydown", widget, "ArrowDown");

      expect(document.activeElement).toBe(suggestions[1]);
    });

    it("stays on the last suggestion when ArrowDown is pressed again", function () {
      const { widget, frame, suggestions } = renderPersonSearch();
      frameRender(frame);
      suggestions.at(-1).focus();

      dispatchKey("keydown", widget, "ArrowDown");

      expect(document.activeElement).toBe(suggestions.at(-1));
    });

    it("moves back to the search input on ArrowUp from the first suggestion", function () {
      const { widget, frame, suggestions, input } = renderPersonSearch();
      frameRender(frame);
      suggestions[0].focus();
      const focusSpy = vi.spyOn(input, "focus");

      dispatchKey("keydown", widget, "ArrowUp");

      expect(focusSpy).toHaveBeenCalledTimes(1);
    });

    it("moves to the previous suggestion on ArrowUp", function () {
      const { widget, frame, suggestions } = renderPersonSearch();
      frameRender(frame);
      suggestions[1].focus();

      dispatchKey("keydown", widget, "ArrowUp");

      expect(document.activeElement).toBe(suggestions[0]);
    });
  });
});
