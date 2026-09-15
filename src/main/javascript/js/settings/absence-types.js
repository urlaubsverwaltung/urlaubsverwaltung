const absenceTypeItems = document.querySelector("#absence-type-list");

absenceTypeItems?.addEventListener("click", function (event) {
  if (event.target.closest("[data-col-status]")) {
    // enable/disable clicked
    const item = event.target.closest("li");
    const checkbox = item.querySelector("input[type='checkbox']");
    item.dataset.enabled = checkbox.checked;
    return;
  }

  const removeButton = event.target.closest("[data-absence-type-remove]");
  if (removeButton) {
    // the absence type has not been persisted yet, so dropping the row in the browser is all it takes.
    // the gap it leaves in the indexed form field names is dealt with server side.
    removeButton.closest("li").remove();
    // without this, focus would fall back to <body> once the row holding it is gone
    document.querySelector("[data-test-id='add-absence-type-button']")?.focus();
  }
});
