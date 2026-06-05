export class PersonSearch extends HTMLElement {
  #cleanup = () => {};
  #popoverVisible = false;

  connectedCallback() {
    let loading = false;

    this.addEventListener("submit", () => {
      loading = true;
      setTimeout(() => {
        if (loading) {
          this.querySelector("[type=submit]").classList.add("button--loading");
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
      this.querySelector("[type=submit]").classList.remove("button--loading");
      if (!this.#popoverVisible && event.target.matches("[id=frame-persons-suggestions]")) {
        this.#showSuggestionsPopover();
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

    this.addEventListener("focusin", handleThisFocusin);
    this.addEventListener("focusout", handleThisFocusout);
    document.addEventListener("pointerdown", handleGlobalPointerdown);
    document.addEventListener("pointerup", handleGlobalPointerup);
    document.addEventListener("turbo:frame-render", handleFrameRender);

    this.#cleanup = function () {
      this.removeEventListener("focusin", handleThisFocusin);
      this.removeEventListener("focusout", handleThisFocusout);
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
    console.log(popover, "show");
    popover.showPopover();
    this.#popoverVisible = true;
  }

  #hideSuggestionsPopover() {
    /** @type HTMLDialogElement */
    const popover = this.querySelector("[popover]");
    popover.hidePopover();
    this.#popoverVisible = false;
  }

  #submit() {
    // always query element, do not memoize it, could be rerendered!
    const form = this.querySelector("form");
    const button = form?.querySelector("[type=submit]");
    form?.requestSubmit(button);
  }
}

customElements.define("uv-person-search", PersonSearch);
