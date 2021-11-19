const table = document.querySelector("#absence-type-table");

table?.addEventListener("click", function (event) {
  if (event.target.closest("[data-col-status]")) {
    // enable/disable clicked
    const tr = event.target.closest("tr");
    const checkbox = tr.querySelector("input[type='checkbox']");
    tr.dataset.enabled = checkbox.checked;
  }
});
