const table = document.querySelector("#special-leave-table");

table?.addEventListener("click", function (event) {
  if (event.target.closest("[data-col-status]")) {
    // enable/disable clicked
    const tr = event.target.closest("tr");
    const input = tr.querySelector("input[type='checkbox']");
    tr.dataset.enabled = input.checked;
  }
});
