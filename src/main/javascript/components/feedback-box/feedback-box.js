export class UVFeedbackBox extends HTMLDivElement {
  remove() {
    // wait for finished transition or a max value
    // THEN remove the element from DOM.

    /** @type number */
    let duration = 0; // use 0 as fallback

    // computedStyleMap may not be supported -> catch and use default
    try {
      // custom property name must match the CSS, can be controlled via inline style in HTML
      const unparsedValue = this.computedStyleMap().get("--uv-feedback-transition-duration");
      if (unparsedValue) {
        /** @type string */
        const a = unparsedValue[0];
        duration = a.endsWith("ms") ? Number.parseInt(a) : Number.parseFloat(a) * 1000;
      }
    } catch {
      // ignore, use default
    }

    Promise.race([
      new Promise((resolve) => {
        this.addEventListener("transitionend", resolve, { once: true });
      }),
      // fallback, despite 'transitionend' should work?
      // add a small delay to let transitionend win the race
      wait(duration + 50),
    ]).then(() => {
      super.remove();
    });

    // add css class to start transition
    this.classList.add("uv-feedback-box--fade-out");
  }
}

function wait(delay = 0) {
  return new Promise((resolve) => setTimeout(resolve, delay));
}

customElements.define("uv-feedback-box", UVFeedbackBox, { extends: "div" });
