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

let tooltip;
let showTimerId;
let hideTimerId;
let activeAnchor;
let pendingAnchor;
let slideAnim;

/** @type {"idle" | "open" | "pendingShow" | "pendingHide"} */
let state = "idle";

export function setup() {
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

export function teardown() {
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
    showTimerId = setTimeout(function () {
      showOn(anchor);
    }, HOVER_SHOW_DELAY_MS);
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
  const previousRect = isHandoff ? tooltip.getBoundingClientRect() : undefined;

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
  activeAnchor = anchor;

  if (previousRect && !prefersReducedMotion()) {
    const nextRect = tooltip.getBoundingClientRect();
    const dx = previousRect.left - nextRect.left;
    const dy = previousRect.top - nextRect.top;
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
  if (anchor.hasAttribute("title")) {
    anchor.dataset.title = anchor.getAttribute("title");
    anchor.removeAttribute("title");
  }
}

function closestTooltipAnchor(element) {
  if (!element) {
    return;
  }
  if (element.getAttribute && (element.getAttribute("title") || element.dataset?.title)) {
    return element;
  }
  return closestTooltipAnchor(element.parentElement);
}

/**
 * Prepares the given element to show a tooltip on hover.
 *
 * @param {HTMLElement} element
 * @param {string} text
 */
export function prepareTooltip(element, text) {
  if (element.dataset.title) {
    element.dataset.title = text;
  } else {
    element.setAttribute("title", text);
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
