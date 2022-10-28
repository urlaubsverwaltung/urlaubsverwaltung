import $ from "jquery";
import "bootstrap/js/tooltip";
// popover depends on tooltip
import "bootstrap/js/popover";

$(function () {
  // bootstrap popover elements
  $('[data-toggle="popover"]').popover();
});

// note: still depends on bootstrap popover css classes
class Popover extends HTMLDetailsElement {
  connectedCallback() {
    let popover;
    const titleElement = this.querySelector("[data-uv-popover-title]").cloneNode(true);
    const contentElement = this.querySelector("[data-uv-popover-content]").cloneNode(true);

    for (let element of this.children) {
      if (element.tagName !== "SUMMARY") {
        // hide content visually. will be shown on mouseover.
        element.classList.add("tw-sr-only");
      }
    }

    // with javascript enabled, the content is shown on mouseover.
    // therefore remove cursor-pointer hint to toggle details.
    const summaryElement = this.querySelector("summary");
    summaryElement.style.cursor = "default";
    // and make it non-focusable
    summaryElement.setAttribute("tabindex", "-1");

    const handleMouseover = () => {
      const title = document.createElement("div");
      title.classList.add("popover-title");
      title.append(titleElement.cloneNode(true));

      const content = document.createElement("div");
      content.classList.add("popover-content");
      content.append(contentElement);

      const arrow = document.createElement("div");
      arrow.classList.add("arrow");

      popover = document.createElement("div");
      popover.classList.add("popover", "right");
      popover.append(arrow, title, content);

      const pivot = this.querySelector("[data-uv-popover-placement]");
      const { width, height, top, left } = (pivot || this).getBoundingClientRect();

      popover.style.position = "absolute";
      popover.style.top = `${top - height / 2}px`;
      popover.style.left = `${width + left}px`;
      popover.style.transform = `translateY(-33%)`;
      popover.style.display = "block"; // override default `display:hidden` from `.popover`. bootstrap still lives.

      document.body.append(popover);
    };

    const handleMouseout = () => {
      popover.remove();
    };

    this.addEventListener("mouseover", handleMouseover);
    this.addEventListener("mouseout", handleMouseout);

    this.cleanup = () => {
      this.removeEventListener("mouseover", handleMouseover);
      this.removeEventListener("mouseout", handleMouseout);
      popover?.remove();
    };
  }

  disconnectedCallback() {
    this.cleanup();
  }
}

customElements.define("uv-popover", Popover, { extends: "details" });
