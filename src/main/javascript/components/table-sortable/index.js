import $ from "jquery";
import "tablesorter";

$(document).ready(function () {
  $("table.sortable").tablesorter({
    sortList: [[1, 0]],
  });
});
