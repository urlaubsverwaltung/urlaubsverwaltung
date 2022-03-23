import $ from "jquery";
import "tablesorter";
import {dataValueNumberParser} from "../../components/table-sortable/parser-data-value-number";

$(document).ready(function () {
  $.tablesorter.addParser(dataValueNumberParser);

  const { length: columnCount } = document.querySelectorAll("#application-statistic-table thead th");
  const isPersonnelNumberColumnRendered = columnCount === 10;

  if (isPersonnelNumberColumnRendered) {
    $("#application-statistic-table").tablesorter({
      sortList: [[2, 0]],
      headers: {
        0: { sorter: false },
        4: { sorter: false },
        5: { sorter: dataValueNumberParser.id },
        6: { sorter: dataValueNumberParser.id },
        7: { sorter: dataValueNumberParser.id },
      },
    });
  } else {
    $("#application-statistic-table").tablesorter({
      sortList: [[1, 0]],
      headers: {
        0: { sorter: false },
        3: { sorter: false },
        4: { sorter: dataValueNumberParser.id },
        5: { sorter: dataValueNumberParser.id },
        6: { sorter: dataValueNumberParser.id },
      },
    });
  }
});
