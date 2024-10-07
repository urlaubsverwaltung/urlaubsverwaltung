globalThis.addEventListener("change", function (event) {
  if (event.target.closest("[data-list-item-selection-toggle]")) {
    // enable/disable clicked
    const item = event.target.closest("li");
    item.dataset.enabled = event.target.checked;
  }
});
