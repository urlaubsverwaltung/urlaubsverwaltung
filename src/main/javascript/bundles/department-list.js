import $ from "jquery";
import "tablesorter";
import "../components/popover";

document.addEventListener("DOMContentLoaded", () => {
  $("table.sortable").tablesorter({
    sortList: [[0, 0]],
  });
});
