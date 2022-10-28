import $ from "jquery";
import "tablesorter";
import "../components/popover";
import { dateParser } from "../components/table-sortable/parser-date";

$(document).ready(function () {
  $.tablesorter.addParser(dateParser);

  $("#department-table").tablesorter({
    sortList: [[0, 0]],
    headers: {
      3: { sorter: dateParser.id },
      4: false,
    },
  });
});
