const absenceTypeItems = document.querySelector("#absence-type-list");

absenceTypeItems?.addEventListener("click", function (event) {
  if (event.target.closest("[data-col-status]")) {
    // enable/disable clicked
    const item = event.target.closest("li");
    const checkbox = item.querySelector("input[type='checkbox']");
    item.dataset.enabled = checkbox.checked;
  }
});
