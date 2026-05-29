import { post } from "../../js/fetch";
import { disposeTooltip, prepareTooltip } from "../tooltip/tooltip";

const NAV_COLLAPSED_ATTR = "data-nav-collapsed";

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
    const nowCollapsed = !document.documentElement.hasAttribute(NAV_COLLAPSED_ATTR);
    applyNavState(nowCollapsed);
    post("/api/persons/me/settings", {
      body: JSON.stringify({ navigationCollapsed: nowCollapsed }),
      headers: {
        "Content-Type": "application/json",
      },
    });
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
});

function addTooltips() {
  for (let element of document.querySelectorAll(".navigation-link")) {
    // ignore submenu groups
    const li = element.closest("li");
    if (li && !li.querySelector(".subnav-group")) {
      prepareTooltip(element, element.querySelector(".nav-link-text").textContent);
    }
  }

  const helpButton = document.querySelector("#global-help-button.navigation-link");
  prepareTooltip(helpButton, helpButton.querySelector(".nav-link-text").textContent);

  const toggleButton = document.querySelector("#nav-toggle");
  prepareTooltip(toggleButton, toggleButton.getAttribute("aria-label"));
}

function removeTooltips() {
  for (let element of document.querySelectorAll(".navigation-link")) {
    disposeTooltip(element);
  }
}
