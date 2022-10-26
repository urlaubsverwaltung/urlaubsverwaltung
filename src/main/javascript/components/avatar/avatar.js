import { doGet } from "../../js/fetch";

class Avatar extends HTMLImageElement {
  connectedCallback() {
    if (this.complete) {
      doGet(this.src).then((response) => {
        if (!response.ok || (response.status >= 400 && response < 500)) {
          this.#useFallback();
        }
      });
    } else {
      this.addEventListener("error", () => this.#useFallback());
    }
  }

  async #useFallback() {
    const r = await doGet(this.dataset.fallback);
    const svg = await r.text();

    const width = this.getAttribute("width");
    const height = this.getAttribute("height");

    const t = document.createElement("template");
    t.innerHTML = svg;

    const svgElement = t.content.querySelector("svg");
    svgElement.setAttribute("width", width);
    svgElement.setAttribute("height", height);

    const clazzes = [...this.classList.entries()].map(([, clazz]) => clazz);

    const parent = this.parentElement;
    parent.replaceChild(t.content, this);
    parent.querySelector("svg").classList.add(...clazzes);
  }
}

customElements.define("uv-avatar", Avatar, { extends: "img" });
