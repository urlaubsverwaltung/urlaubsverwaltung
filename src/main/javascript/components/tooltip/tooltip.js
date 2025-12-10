import { createPopper } from "@popperjs/core";

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

// using event delegation to catch dynamically added elements, too
document.addEventListener("mouseover", maybeShow);
document.addEventListener("mouseout", maybeHide);
document.addEventListener("focusin", maybeShow);
document.addEventListener("focusout", maybeHide);

/**
 * Showing the tooltip on :hover is delayed. This value is used to clear the timeout.
 * @type {number}
 */
let tooltipDelayId;

const cache = new WeakMap();

/**
 * @param {MouseEvent|FocusEvent} event
 */
function maybeShow(event) {
  const anchor = closestTooltipAnchor(event.target);
  const isChildEvent = event.relatedTarget && anchor?.contains(event.relatedTarget);
  if (anchor && !isChildEvent) {
    const delay = event.type === "focusin" ? 0 : 500;
    showTooltip(anchor, delay);
  }
}

/**
 * @param {MouseEvent|FocusEvent} event
 */
function maybeHide(event) {
  const anchor = closestTooltipAnchor(event.target);
  const isChildEvent = event.relatedTarget && anchor?.contains(event.relatedTarget);
  if (anchor && !isChildEvent) {
    hideTooltip(anchor);
  }
}

/**
 * @param {HTMLElement} element
 * @return {HTMLElement|undefined}
 */
function closestTooltipAnchor(element) {
  if (!element) {
    return;
  }
  // read title attribute instead of #hasAttribute since tooltip should only be created when title has a value
  const title = element.getAttribute("title") || element.dataset.title;
  if (title) {
    return element;
  }
  return closestTooltipAnchor(element.parentElement);
}

/**
 * @param {HTMLElement} anchor
 * @returns {HTMLElement} the tooltip, not part of DOM yet
 */
function createTooltip(anchor) {
  const tooltip = document.createElement("div");
  tooltip.setAttribute("role", "tooltip");
  tooltip.textContent = anchor.getAttribute("title") || anchor.dataset.title;

  // remove native title tooltip
  if (anchor.hasAttribute("title")) {
    anchor.dataset.title = anchor.getAttribute("title");
    anchor.removeAttribute("title");
  }

  return tooltip;
}

/**
 * @param {HTMLElement} anchor
 * @param {number} delay
 */
function showTooltip(anchor, delay = 0) {
  tooltipDelayId = setTimeout(function () {
    const tooltip = createTooltip(anchor);
    document.body.append(tooltip);

    // using popper as long as CSS anchor positioning not widely supported
    const popperInstance = createPopper(anchor, tooltip, {
      placement: "bottom",
    });
    cache.set(anchor, {
      destroy() {
        popperInstance.destroy();
        tooltip.remove();
      },
    });
  }, delay);
}

/**
 * @param {HTMLElement} anchor
 */
function hideTooltip(anchor) {
  clearTimeout(tooltipDelayId);
  if (cache.has(anchor)) {
    cache.get(anchor).destroy();
    cache.delete(anchor);
  }
}
