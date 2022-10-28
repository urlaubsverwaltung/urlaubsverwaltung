import $ from "jquery";
import "tablesorter";
import { dataValueNumberParser } from "../../components/table-sortable/parser-data-value-number";

$(document).ready(function () {
  $.tablesorter.addParser(dataValueNumberParser);

  const { length: columnCount } = document.querySelectorAll("#sick-note-table thead th");
  const isPersonnelNumberColumnRendered = columnCount === 8;

  if (isPersonnelNumberColumnRendered) {
    $("#sick-note-table").tablesorter({
      sortList: [[2, 0]],
      headers: {
        0: { sorter: false },
        5: { sorter: dataValueNumberParser.id },
        6: { sorter: dataValueNumberParser.id },
        7: { sorter: false },
      },
    });
  } else {
    $("#sick-note-table").tablesorter({
      sortList: [[1, 0]],
      headers: {
        0: { sorter: false },
        4: { sorter: dataValueNumberParser.id },
        5: { sorter: dataValueNumberParser.id },
        6: { sorter: false },
      },
    });
  }
});
