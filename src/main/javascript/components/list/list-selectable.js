document.addEventListener("click", function (event) {
  if (event.target.closest("[data-list-item-selection-toggle]")) {
    // enable/disable clicked
    const item = event.target.closest("li");
    const checkbox = item.querySelector("input[type='checkbox']");
    item.dataset.enabled = checkbox.checked;
  }
});
