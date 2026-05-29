import { post } from "../../js/fetch";
import { disposeTooltip, prepareTooltip } from "../tooltip/tooltip";

const NAV_COLLAPSED_ATTR = "data-nav-collapsed";
const TOOLTIP_DELAY = 500;

document.addEventListener("click", function (event) {
  /** @type HTMLElement */
  const target = event.target;

  if (
    event.shiftKey ||
    event.metaKey ||
    event.ctrlKey ||
    event.altKey ||
    target.getAttribute("target") === "_blank" ||
    target.closest("button:not(.navigation-link)")
  ) {
    // shiftKey: new window
    // metaKey: new tab (macOS)
    // ctrlKey: new tab (not macOS)
    // altKey: download
    return;
  }

  if (target.closest("#nav-toggle")) {
    setNavCollapsed(!isNavCollapsed());
    return;
  }

  const navLinkButton = target.closest("button.navigation-link[data-href]");
  if (navLinkButton) {
    const isCollapsed = document.documentElement.hasAttribute(NAV_COLLAPSED_ATTR);
    if (!isCollapsed) {
      navLinkButton.classList.add("navigation-link--loading");
      globalThis.location.href = navLinkButton.dataset.href;
    }
    return;
  }

  navlink(target);
  subnavlink(target);
});

document.addEventListener("turbo:before-cache", function () {
  cleanupLoadingClasses();
});

window.addEventListener("pageshow", function (event) {
  if (event.persisted) {
    // bf-cache hit: https://developer.mozilla.org/en-US/docs/Glossary/bfcache
    cleanupLoadingClasses();
  }
});

function cleanupLoadingClasses() {
  removeClasses("navigation-link--loading");
  removeClasses("navigation-sublink--loading");
}

function removeClasses(className) {
  let elements = document.querySelectorAll(`.${className}`);
  for (let element of elements) {
    element.classList.remove(className);
  }
}

/**
 *
 * @param {HTMLElement} element
 */
function navlink(element) {
  const link = element.closest(".navigation-link");
  if (link) {
    // element is replaced on response and loading class removed
    link.classList.add("navigation-link--loading");
  }
}

/**
 *
 * @param {HTMLElement} element
 */
function subnavlink(element) {
  const link = element.closest(".navigation-sublink");
  if (link) {
    // element is replaced on response and loading class removed
    link.classList.add("navigation-sublink--loading");
  }
}

function isNavCollapsed() {
  return document.documentElement.hasAttribute(NAV_COLLAPSED_ATTR);
}

/**
 * Apply the collapsed state to the DOM (attribute, aria, tooltips) and persist
 * it to the backend, but only when it actually changed.
 *
 * @param {boolean} collapsed
 */
function setNavCollapsed(collapsed) {
  if (collapsed === isNavCollapsed()) {
    return;
  }
  applyNavState(collapsed);
  persistNavCollapsed(collapsed);
}

function persistNavCollapsed(collapsed) {
  post("/api/persons/me/settings", {
    body: JSON.stringify({ navigationCollapsed: collapsed }),
    headers: {
      "Content-Type": "application/json",
    },
  });
}

function applyNavState(collapsed) {
  const toggleButton = document.querySelector("#nav-toggle");
  if (!toggleButton) return;
  if (collapsed) {
    document.documentElement.setAttribute(NAV_COLLAPSED_ATTR, "");
    toggleButton.setAttribute("aria-expanded", "false");
    addTooltips();
  } else {
    document.documentElement.removeAttribute(NAV_COLLAPSED_ATTR);
    toggleButton.setAttribute("aria-expanded", "true");
    removeTooltips();
  }
}

document.addEventListener("DOMContentLoaded", function () {
  // pair each collapsed submenu with its parent button via a unique anchor-name
  // so the fixed-positioned subnav-group can anchor() to the right button
  for (const [index, subnavGroup] of document.querySelectorAll(".navigation .subnav-group").entries()) {
    const link = subnavGroup.closest("li").querySelector(".navigation-link");
    const name = `--nav-subnav-${index}`;
    link.style.anchorName = name;
    subnavGroup.style.positionAnchor = name;
  }

  const toggleButton = document.querySelector("#nav-toggle");
  if (!toggleButton) return;

  const isCollapsed = document.documentElement.hasAttribute(NAV_COLLAPSED_ATTR);
  toggleButton.setAttribute("aria-expanded", String(!isCollapsed));
  if (isCollapsed) {
    addTooltips();
  }

  setupNavResizeHandle();
});

const NAV_WIDTH_EXPANDED = 300;
const NAV_RESIZE_THRESHOLD = 4;
const NAV_RESIZE_RESISTANCE = 0.3;
const NAV_RESIZE_MAX_OVERSHOOT = 40;
const NAV_RESIZE_HOVER_DELAY = 150;
const NAV_RESIZE_VISIBLE_CLASS = "nav-resize-handle--visible";

/**
 * Adds a drag handle on the right edge of the navigation. Dragging follows the
 * pointer to give live width feedback; on release the nav snaps to either the
 * collapsed or expanded state based on the net drag direction (right expands,
 * left collapses, no movement keeps the current state). A double-click toggles
 * like the #nav-toggle button.
 */
function setupNavResizeHandle() {
  const container = document.querySelector(".navigation-container");
  if (!container) return;

  const html = document.documentElement;
  const remInPx = Number.parseFloat(getComputedStyle(html).fontSize) || 16;
  // collapsed width mirrors `html[data-nav-collapsed] { --nav-width: 5rem }`
  const collapsedWidth = remInPx * 5;
  const midpoint = (collapsedWidth + NAV_WIDTH_EXPANDED) / 2;

  const handle = document.createElement("div");
  handle.className = "nav-resize-handle";
  handle.setAttribute("aria-hidden", "true");
  container.append(handle);

  let startX = 0;
  let lastX = 0;
  let pointerId;
  let startCollapsed = false;
  let dragging = false;
  let moved = false;
  let revealTimer;
  // cached on pointerdown so pointermove never calls getBoundingClientRect()
  // (a layout read after a style write thrashes layout, very slow in Safari)
  let containerLeft = 0;
  // coalesce style writes to one per frame
  let rafId;
  let pendingX = 0;

  function reveal() {
    handle.classList.add(NAV_RESIZE_VISIBLE_CLASS);
  }

  function hide() {
    clearTimeout(revealTimer);
    handle.classList.remove(NAV_RESIZE_VISIBLE_CLASS);
  }

  function widthForPointer(clientX) {
    let width = clientX - containerLeft;
    // rubber-band beyond the snap targets with diminishing resistance
    if (width < collapsedWidth) {
      width = collapsedWidth - Math.min(NAV_RESIZE_MAX_OVERSHOOT, (collapsedWidth - width) * NAV_RESIZE_RESISTANCE);
    } else if (width > NAV_WIDTH_EXPANDED) {
      width =
        NAV_WIDTH_EXPANDED + Math.min(NAV_RESIZE_MAX_OVERSHOOT, (width - NAV_WIDTH_EXPANDED) * NAV_RESIZE_RESISTANCE);
    }
    return width;
  }

  handle.addEventListener("pointerdown", function (event) {
    event.preventDefault();
    // a drag may start before the hover delay elapsed: reveal immediately
    clearTimeout(revealTimer);
    reveal();
    dragging = true;
    moved = false;
    startX = event.clientX;
    lastX = event.clientX;
    pointerId = event.pointerId;
    startCollapsed = isNavCollapsed();
    containerLeft = container.getBoundingClientRect().left;
    handle.setPointerCapture(event.pointerId);
    html.classList.add("nav-resizing");
  });

  function applyWidth() {
    rafId = undefined;
    const width = widthForPointer(pendingX);
    html.style.setProperty("--nav-width", `${width}px`);
    // flip the layout attribute at the midpoint for live feedback; tooltips are
    // intentionally left untouched here and reconciled once on release
    if (width < midpoint) {
      html.setAttribute(NAV_COLLAPSED_ATTR, "");
    } else {
      html.removeAttribute(NAV_COLLAPSED_ATTR);
    }
  }

  handle.addEventListener("pointermove", function (event) {
    if (!dragging) return;
    lastX = event.clientX;
    // ignore sub-threshold jitter so a plain click neither resizes nor flips
    if (!moved && Math.abs(event.clientX - startX) < NAV_RESIZE_THRESHOLD) return;
    moved = true;

    // coalesce: one layout-triggering write per frame, not per pointermove
    pendingX = event.clientX;
    if (rafId === undefined) {
      rafId = requestAnimationFrame(applyWidth);
    }
  });

  function endDrag() {
    if (!dragging) return;
    dragging = false;
    // drop any frame still queued so it can't write width after release
    if (rafId !== undefined) {
      cancelAnimationFrame(rafId);
      rafId = undefined;
    }
    if (pointerId !== undefined && handle.hasPointerCapture(pointerId)) {
      handle.releasePointerCapture(pointerId);
    }
    pointerId = undefined;

    // net direction from the drag start decides the final state; lastX is used
    // (not the event) so cleanup is correct even when pointer capture is lost
    const finalCollapsed = moved ? lastX < startX : startCollapsed;

    // reconcile DOM (attribute, aria, tooltips) for the final state
    applyNavState(finalCollapsed);
    html.classList.remove("nav-resizing");
    // drop the inline width next frame so the attribute-driven target animates
    // the snap (and any rubber-band overshoot) over the re-enabled transition
    requestAnimationFrame(() => html.style.removeProperty("--nav-width"));

    if (finalCollapsed !== startCollapsed) {
      persistNavCollapsed(finalCollapsed);
    }

    // keep visible only while still hovering, otherwise reset
    if (!handle.matches(":hover")) {
      hide();
    }
  }

  handle.addEventListener("pointerup", endDrag);
  handle.addEventListener("pointercancel", endDrag);
  // fast pointer movement can drop the implicit capture without a pointerup
  // reaching the handle; this guarantees the drag state is always cleaned up
  handle.addEventListener("lostpointercapture", endDrag);

  // reveal the cursor + line only after a short hover, hide immediately on leave
  handle.addEventListener("pointerenter", function () {
    revealTimer = setTimeout(reveal, NAV_RESIZE_HOVER_DELAY);
  });
  handle.addEventListener("pointerleave", function () {
    // keep it visible while dragging even when the pointer leaves the handle
    if (!dragging) {
      hide();
    }
  });

  handle.addEventListener("dblclick", function () {
    setNavCollapsed(!isNavCollapsed());
  });
}

function addTooltips() {
  for (let element of document.querySelectorAll(".navigation-link")) {
    // ignore submenu groups
    const li = element.closest("li");
    if (li && !li.querySelector(".subnav-group")) {
      prepareTooltip(element, element.querySelector(".nav-link-text").textContent, TOOLTIP_DELAY);
    }
  }

  const helpButton = document.querySelector("#global-help-button.navigation-link");
  prepareTooltip(helpButton, helpButton.querySelector(".nav-link-text").textContent, TOOLTIP_DELAY);

  const toggleButton = document.querySelector("#nav-toggle");
  prepareTooltip(toggleButton, toggleButton.getAttribute("aria-label"), TOOLTIP_DELAY);
}

function removeTooltips() {
  for (let element of document.querySelectorAll(".navigation-link")) {
    disposeTooltip(element);
  }
}
