const standardNumberOfRows = 1;
const expandedNumberOfRows = 4;

document.addEventListener("focusin", function (event) {
  if (event.target.tagName === "TEXTAREA") {
    event.target.rows = expandedNumberOfRows;
  }
});

document.addEventListener("focusout", function (event) {
  if (event.target.tagName === "TEXTAREA") {
    event.target.rows = standardNumberOfRows;
  }
});
