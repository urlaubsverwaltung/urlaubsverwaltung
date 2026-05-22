import { post } from "../../js/fetch";

document.addEventListener("click", function (event) {
  /** @type HTMLElement */
  const target = event.target;

  if (
    event.shiftKey ||
    event.metaKey ||
    event.ctrlKey ||
    event.altKey ||
    target.getAttribute("target") === "_blank" ||
    target.closest("button")
  ) {
    // shiftKey: new window
    // metaKey: new tab (macOS)
    // ctrlKey: new tab (not macOS)
    // altKey: download
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

// Navigation collapse toggle
const NAV_COLLAPSED_ATTR = "data-nav-collapsed";

document.addEventListener("DOMContentLoaded", function () {
  const toggleButton = document.querySelector("#nav-toggle");
  if (!toggleButton) return;

  function applyState(collapsed) {
    if (collapsed) {
      document.documentElement.setAttribute(NAV_COLLAPSED_ATTR, "");
      toggleButton.setAttribute("aria-expanded", "false");
    } else {
      document.documentElement.removeAttribute(NAV_COLLAPSED_ATTR);
      toggleButton.setAttribute("aria-expanded", "true");
    }
  }

  // sync aria-expanded on load
  const isCollapsed = document.documentElement.hasAttribute(NAV_COLLAPSED_ATTR);
  toggleButton.setAttribute("aria-expanded", String(!isCollapsed));

  toggleButton.addEventListener("click", function () {
    const nowCollapsed = !document.documentElement.hasAttribute(NAV_COLLAPSED_ATTR);
    applyState(nowCollapsed);

    post("/api/persons/me/settings", {
      body: JSON.stringify({ navigationCollapsed: nowCollapsed }),
      headers: {
        "Content-Type": "application/json",
      },
    });
  });
});
