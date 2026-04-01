document.addEventListener("click", function (event) {
  /** @type HTMLElement */
  const target = event.target;
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
