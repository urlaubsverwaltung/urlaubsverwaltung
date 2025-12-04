import { createPopper } from "@popperjs/core";
import { generateId } from "./id-factory";

/**
 * A tooltip is a brief, informative message that appears when a user interacts with an element in a
 * graphical user interface (GUI). Tooltips are usually initiated in one of two ways:
 * through a mouse-hover gesture or through a keyboard-hover gesture.
 *
 * <p>
 * Tooltip vs Popup / Popover:
 * - Tooltips are mainly for Desktop
 * - While Popovers are for Touch Devices, too
 *
 * <p>
 * A popovers paired element is usually an info icon that can be clicked/touched to show a more detailed info.
 * While a tooltip only shows a quick info for a button without text for instance.
 *
 * https://www.nngroup.com/articles/tooltip-guidelines/
 */
export default function tooltip() {
  // not using [popover] for tooltips, because
  // - a click would open the tooltip, which we do not want. only hover should show the tooltip.
  // - popover=hint is not supported by firefox (does not hide other auto popovers)

  const cache = new WeakMap();

  for (let element of document.querySelectorAll("[data-title]")) {
    // skip empty values, no tooltip without text
    if (!element.dataset.title) {
      continue;
    }

    const random = generateId(8);
    const elementId = element.id || element.getAttribute("id") || `tooltip-${random}-anchor`;
    const tooltipId = "tooltip-" + random;

    element.setAttribute("id", elementId);

    const tooltip = document.createElement("div");
    tooltip.setAttribute("id", tooltipId);
    tooltip.setAttribute("role", "tooltip");
    tooltip.setAttribute("hidden", "");

    tooltip.innerHTML = `<div>${element.dataset.title}</div>`;
    element.after(tooltip);

    let delayHandle;

    function showTooltip() {
      delayHandle = setTimeout(function () {
        const popperInstance = createPopper(element, tooltip, {
          placement: "bottom",
        });
        cache.set(element, popperInstance);
        element.setAttribute("aria-describedby", tooltipId);
        tooltip.removeAttribute("hidden");
      }, 500);
    }

    function hideTooltip() {
      clearTimeout(delayHandle);
      if (cache.has(element)) {
        tooltip.setAttribute("hidden", "");
        cache.get(element).destroy();
        cache.delete(element);
        element.removeAttribute("aria-describedby");
      }
    }

    element.addEventListener("focusin", showTooltip);
    element.addEventListener("blur", hideTooltip);
    element.addEventListener("mouseenter", showTooltip);
    element.addEventListener("mouseleave", hideTooltip);
  }

  document.addEventListener("keyup", function (event) {
    // close tooltip on Escape.
    if (event.key === "Escape") {
      // there can only be on visible at a time.
      const tooltip = document.querySelector("[role=tooltip]:not([hidden])");
      tooltip?.setAttribute("hidden", "");
    }
  });
}
