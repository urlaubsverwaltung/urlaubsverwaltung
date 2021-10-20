import $ from "jquery";
import "tablesorter";
import "./absences-overview.css";

document.addEventListener("DOMContentLoaded", () => {
  $("table.sortable").tablesorter({
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
