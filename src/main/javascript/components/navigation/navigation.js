document.addEventListener("click", function (event) {
  /** @type HTMLElement */
  const target = event.target;
  navlink(target);
  subnavlink(target);
});

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
