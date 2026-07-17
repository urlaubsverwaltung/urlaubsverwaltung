function focusSuggestion(element) {
  element.focus();
  element.closest("li").scrollIntoView({ block: "nearest" });
}

export class PersonSearch extends HTMLElement {
  #cleanup = () => {};
  #popoverVisible = false;

  /** @type HTMLInputElement */
  get #searchInput() {
    return this.querySelector("input[type=search]");
  }

  /** @type HTMLButtonElement */
  get #submitButton() {
    return this.querySelector("[type=submit]");
  }

  /** @type HTMLElement */
  get #statusRegion() {
    return this.querySelector("[data-person-search-status]");
  }

  connectedCallback() {
    let loading = false;

    this.addEventListener("submit", () => {
      loading = true;
      setTimeout(() => {
        if (loading) {
          this.#submitButton.classList.add("button--loading");
        }
      }, 100);
    });

    const handleThisFocusin = () => {
      // trigger empty search to show initial suggestions when input is focused
      if (document.activeElement?.matches("input")) {
        this.#submit();
      }
    };

    // show popover on initial submit.
    // subsequent renders can be ignored since content is updated, not the popover itself.
    const handleFrameRender = (event) => {
      loading = false;
      this.#submitButton.classList.remove("button--loading");
      if (event.target.matches("[id=frame-persons-suggestions]")) {
        if (!this.#popoverVisible) {
          this.#showSuggestionsPopover();
        }
        this.#announceResultCount();
      }
    };

    // suggestion popover should not be closed
    // when a suggestion link is supposed to be clicked
    let pointerdownSuggestionLink = false;

    /**
     *
     * @param {PointerEvent} event
     */
    const handleGlobalPointerdown = (event) => {
      /** @type HTMLElement */
      const target = event.target;
      pointerdownSuggestionLink = Boolean(target.closest("a") && this.contains(target));
    };

    const handleGlobalPointerup = () => {
      pointerdownSuggestionLink = false;
    };

    /**
     * @param {FocusEvent} event
     */
    const handleThisFocusout = (event) => {
      if (!pointerdownSuggestionLink && !this.contains(event.relatedTarget)) {
        this.#hideSuggestionsPopover();
      }
    };

    const handleThisKeydown = (event) => {
      if (event.key !== "ArrowDown" && event.key !== "ArrowUp" && event.key !== "Escape") {
        return;
      }

      const input = this.#searchInput;
      const suggestions = [...this.querySelectorAll("[data-person-search-suggestion]")];

      if (event.key === "Escape") {
        event.preventDefault();
        this.#hideSuggestionsPopover();
        input.focus();
        return;
      }

      if (!this.#popoverVisible || suggestions.length === 0) {
        return;
      }

      event.preventDefault();
      const focusedIndex = suggestions.indexOf(document.activeElement);

      if (event.key === "ArrowDown") {
        if (focusedIndex === -1) {
          focusSuggestion(suggestions[0]);
        } else if (focusedIndex < suggestions.length - 1) {
          focusSuggestion(suggestions[focusedIndex + 1]);
        }
      } else {
        if (focusedIndex === 0) {
          input.focus();
        } else if (focusedIndex > 0) {
          focusSuggestion(suggestions[focusedIndex - 1]);
        }
      }
    };

    this.addEventListener("focusin", handleThisFocusin);
    this.addEventListener("focusout", handleThisFocusout);
    this.addEventListener("keydown", handleThisKeydown);
    document.addEventListener("pointerdown", handleGlobalPointerdown);
    document.addEventListener("pointerup", handleGlobalPointerup);
    document.addEventListener("turbo:frame-render", handleFrameRender);

    this.#cleanup = function () {
      this.removeEventListener("focusin", handleThisFocusin);
      this.removeEventListener("focusout", handleThisFocusout);
      this.removeEventListener("keydown", handleThisKeydown);
      document.removeEventListener("pointerdown", handleGlobalPointerdown);
      document.removeEventListener("pointerup", handleGlobalPointerup);
      document.removeEventListener("turbo:frame-render", handleFrameRender);
    };
  }

  disconnectedCallback() {
    this.#cleanup();
  }

  connectedMoveCallback() {
    // prevent connected/disconnected callbacks to be called when element is moved
  }

  #showSuggestionsPopover() {
    /** @type HTMLDialogElement */
    const popover = this.querySelector("[popover]");
    popover.showPopover();
    this.#popoverVisible = true;
  }

  #hideSuggestionsPopover() {
    /** @type HTMLDialogElement */
    const popover = this.querySelector("[popover]");
    popover.hidePopover();
    this.#popoverVisible = false;
    this.#statusRegion.textContent = "";
  }

  #announceResultCount() {
    const count = this.querySelectorAll("[data-person-search-suggestion]").length;

    let message;
    if (count === 0) {
      message = this.dataset.messageNothingFound ?? "";
    } else if (count === 1) {
      message = this.dataset.messageResultsOne ?? "";
    } else {
      message = (this.dataset.messageResultsOther ?? "").replace("{0}", String(count));
    }

    this.#statusRegion.textContent = message;
  }

  #submit() {
    // always query element, do not memoize it, could be rerendered!
    const form = this.querySelector("form");
    form?.requestSubmit(this.#submitButton);
  }
}

customElements.define("uv-person-search", PersonSearch);
