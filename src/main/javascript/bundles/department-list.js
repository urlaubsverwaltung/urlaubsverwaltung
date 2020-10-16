import $ from "jquery";
import "tablesorter";
import "../components/popover";

$(document).ready(function () {
  $("table.sortable").tablesorter({
    sortList: [[0, 0]],
  });
});
