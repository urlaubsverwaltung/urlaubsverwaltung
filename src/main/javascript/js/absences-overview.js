import $ from "jquery";
import "tablesorter";
import "./absences-overview.css";

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
