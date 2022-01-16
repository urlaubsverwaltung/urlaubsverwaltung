import $ from "jquery";
import "tablesorter";
import { dataValueNumberParser } from "../../components/table-sortable/parser-data-value-number";

$(document).ready(function () {
  $.tablesorter.addParser(dataValueNumberParser);

  $("#sick-note-table").tablesorter({
    sortList: [[1, 0]],
    headers: {
      0: { sorter: false },
      4: { sorter: dataValueNumberParser.id },
      5: { sorter: dataValueNumberParser.id },
      6: { sorter: false },
    },
  });
});
