import $ from "jquery";
import "tablesorter";

$(function () {
  $("#absence-table").tablesorter({
    sortList: [[1, 0]],
    headers: {
      ".non-sortable": {
        sorter: false,
      },
    },
  });

  const form = document.querySelector("#absenceOverviewForm");

  form.addEventListener("change", () => {
    form.submit();
  });
});
