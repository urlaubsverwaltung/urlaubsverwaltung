document.addEventListener("click", function (event) {
  /** @type HTMLElement */
  const target = event.target;
  const link = target.closest(".navigation-link");
  if (link) {
    // element is replaced on response and loading class removed
    link.classList.add("navigation-link--loading");
  }
});
