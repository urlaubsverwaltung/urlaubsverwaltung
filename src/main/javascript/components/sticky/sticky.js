export class Sticky extends HTMLElement {
  static observedAttributes = ["data-sticky"];

  /** @type IntersectionObserver */
  #intersectionObserver;

  constructor() {
    super();

    // set by JavaScript since we only know whether sticky or not with this IntersectionOberserver
    this.style.position = "sticky";
    this.style.zIndex = "var(--z-index-sticky)";

    // kudos https://css-tricks.com/how-to-detect-when-a-sticky-element-gets-pinned/
    this.#intersectionObserver = new IntersectionObserver(
      ([event]) => {
        const isSticky = event.intersectionRatio < 1;
        event.target.classList.toggle("uv-sticky--stuck", isSticky);
      },
      { threshold: [1] },
    );
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (name === "data-sticky") {
      if (newValue === "bottom") {
        this.style.bottom = "-1px";
        this.style.removeProperty("top");
      } else if (newValue === "top") {
        this.style.top = "-1px";
        this.style.removeProperty("bottom");
      } else {
        this.style.removeProperty("top");
        this.style.removeProperty("bottom");
      }
    }
  }

  connectedCallback() {
    this.#intersectionObserver.observe(this);
  }

  disconnectedCallback() {
    this.#intersectionObserver.unobserve(this);
  }

  connectedMoveCallback() {
    // prevent calling connectedCallback when element is moved
  }
}

customElements.define("uv-sticky", Sticky);
