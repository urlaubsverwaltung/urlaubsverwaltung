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
 * https://www.nngroup.com/articles/tooltip-guidelines/
 */

const HOVER_SHOW_DELAY_MS = 300;
const FADE_OUT_MS = 100;
const SLIDE_MS = 150;
const TOOLTIP_ID = "uv-tooltip";
const ANCHOR_ACTIVE_CLASS = "uv-tooltip-anchor--active";
const TOOLTIP_HIDING_CLASS = "uv-tooltip--is-hiding";

const { setup, teardown } = (function () {
  let tooltip;
  let showTimerId;
  let hideTimerId;
  let activeAnchor;
  let pendingAnchor;
  let slideAnim;

  /** @type {"idle" | "open" | "pendingShow" | "pendingHide"} */
  let state = "idle";

  function setup() {
    if (!tooltip) {
      tooltip = document.createElement("div");
      tooltip.id = TOOLTIP_ID;
      tooltip.setAttribute("role", "tooltip");
      tooltip.setAttribute("popover", "hint");
      document.body.append(tooltip);
    }
    document.addEventListener("mouseover", onPointerEnter);
    document.addEventListener("mouseout", onPointerLeave);
    document.addEventListener("focusin", onPointerEnter);
    document.addEventListener("focusout", onPointerLeave);
  }

  function teardown() {
    document.removeEventListener("mouseover", onPointerEnter);
    document.removeEventListener("mouseout", onPointerLeave);
    document.removeEventListener("focusin", onPointerEnter);
    document.removeEventListener("focusout", onPointerLeave);
    clearTimeout(showTimerId);
    clearTimeout(hideTimerId);
    showTimerId = undefined;
    hideTimerId = undefined;
    pendingAnchor = undefined;
    if (slideAnim) {
      slideAnim.cancel();
      slideAnim = undefined;
    }
    if (activeAnchor) {
      activeAnchor.classList.remove(ANCHOR_ACTIVE_CLASS);
      activeAnchor.removeAttribute("aria-describedby");
      activeAnchor = undefined;
    }
    if (tooltip) {
      tooltip.classList.remove(TOOLTIP_HIDING_CLASS);
      tooltip.remove();
      tooltip = undefined;
    }
    state = "idle";
  }

  function onPointerEnter(event) {
    const anchor = closestTooltipAnchor(event.target);
    if (!anchor || isInternalCrossing(event, anchor)) {
      return;
    }
    if (anchor === activeAnchor && state !== "pendingHide") {
      return;
    }
    if (state === "open" || state === "pendingHide") {
      handoffTo(anchor);
      return;
    }
    clearTimeout(showTimerId);
    pendingAnchor = anchor;
    state = "pendingShow";
    if (event.type === "focusin") {
      showOn(anchor);
    } else {
      const delay = anchor.dataset.tooltipDelay;
      showTimerId = setTimeout(
        function () {
          showOn(anchor);
        },
        delay === undefined ? HOVER_SHOW_DELAY_MS : Number(delay),
      );
    }
  }

  function onPointerLeave(event) {
    const anchor = closestTooltipAnchor(event.target);
    if (!anchor || isInternalCrossing(event, anchor)) {
      return;
    }
    if (anchor === pendingAnchor) {
      clearTimeout(showTimerId);
      showTimerId = undefined;
      pendingAnchor = undefined;
      state = "idle";
      return;
    }
    if (anchor === activeAnchor) {
      beginHide();
    }
  }

  function isInternalCrossing(event, anchor) {
    return event.relatedTarget instanceof Node && anchor.contains(event.relatedTarget);
  }

  function handoffTo(anchor) {
    clearTimeout(hideTimerId);
    hideTimerId = undefined;
    tooltip.classList.remove(TOOLTIP_HIDING_CLASS);
    retargetTo(anchor);
    state = "open";
  }

  function beginHide() {
    clearTimeout(showTimerId);
    if (state !== "open") {
      return;
    }
    state = "pendingHide";
    tooltip.classList.add(TOOLTIP_HIDING_CLASS);
    hideTimerId = setTimeout(finalizeHide, FADE_OUT_MS);
  }

  function finalizeHide() {
    hideTimerId = undefined;
    tooltip.classList.remove(TOOLTIP_HIDING_CLASS);
    tooltip.hidePopover();
    if (activeAnchor) {
      activeAnchor.classList.remove(ANCHOR_ACTIVE_CLASS);
      activeAnchor.removeAttribute("aria-describedby");
      activeAnchor = undefined;
    }
    state = "idle";
  }

  function showOn(anchor) {
    pendingAnchor = undefined;
    showTimerId = undefined;
    retargetTo(anchor);
    tooltip.showPopover();
    state = "open";
  }

  function retargetTo(anchor) {
    const isHandoff = activeAnchor && activeAnchor !== anchor;
    const previousAnchorRect = isHandoff ? activeAnchor.getBoundingClientRect() : undefined;

    if (slideAnim) {
      slideAnim.cancel();
      slideAnim = undefined;
    }

    if (isHandoff) {
      activeAnchor.classList.remove(ANCHOR_ACTIVE_CLASS);
      activeAnchor.removeAttribute("aria-describedby");
    }
    ensureMigratedTitle(anchor);
    anchor.classList.add(ANCHOR_ACTIVE_CLASS);
    anchor.setAttribute("aria-describedby", TOOLTIP_ID);
    tooltip.textContent = anchor.dataset.title;
    const placement = anchor.dataset.tooltipPlacement === "right" ? "right" : "top";
    tooltip.dataset.placement = placement;
    activeAnchor = anchor;

    if (previousAnchorRect && !prefersReducedMotion()) {
      const nextAnchorRect = anchor.getBoundingClientRect();
      // both ends of a handoff share the same placement, so the reference point follows it
      let dx;
      let dy;
      if (placement === "right") {
        // tooltip is left-aligned right of anchor.right and vertically centered on the anchor
        dx = previousAnchorRect.right - nextAnchorRect.right;
        dy = previousAnchorRect.top + previousAnchorRect.height / 2 - (nextAnchorRect.top + nextAnchorRect.height / 2);
      } else {
        // tooltip is centered horizontally over the anchor and bottom-aligned above anchor.top
        dx = previousAnchorRect.left + previousAnchorRect.width / 2 - (nextAnchorRect.left + nextAnchorRect.width / 2);
        dy = previousAnchorRect.top - nextAnchorRect.top;
      }
      slideAnim = tooltip.animate([{ translate: `${dx}px ${dy}px` }, { translate: "0 0" }], {
        duration: SLIDE_MS,
        easing: "ease-out",
        fill: "none",
      });
    }
  }

  function prefersReducedMotion() {
    return globalThis.matchMedia?.("(prefers-reduced-motion: reduce)").matches ?? false;
  }

  function ensureMigratedTitle(anchor) {
    if (!anchor.hasAttribute("title")) {
      return;
    }

    anchor.dataset.title = anchor.getAttribute("title");
    anchor.removeAttribute("title");
  }

  function closestTooltipAnchor(element) {
    for (let current = element; current; current = current.parentElement) {
      if (current.getAttribute && (current.getAttribute("title") || current.dataset?.title)) {
        return current;
      }
    }
  }

  return { setup, teardown };
})();

export { setup, teardown };

/**
 * @typedef {Object} TooltipOptions
 * @property {string} text the tooltip text
 * @property {"top" | "right"} [placement] where the tooltip appears relative to the element;
 *   omit to leave the existing placement untouched (defaults to "top" when never set)
 * @property {number} [delay] hover show delay in ms; omit to leave the existing delay untouched
 */

/**
 * Prepares the given element to show a tooltip on hover.
 *
 * @param {HTMLElement} element
 * @param {TooltipOptions} options
 */
export function prepareTooltip(element, { text, placement, delay }) {
  if (element.dataset.title) {
    element.dataset.title = text;
  } else {
    element.setAttribute("title", text);
  }
  if (delay !== undefined) {
    element.dataset.tooltipDelay = String(delay);
  }
  if (placement !== undefined) {
    element.dataset.tooltipPlacement = placement;
  }
}

/**
 * Removes everything tooltip related from the given element.
 *
 * @param {HTMLElement} element
 */
export function disposeTooltip(element) {
  element.removeAttribute("title");
  delete element.dataset.title;
}
