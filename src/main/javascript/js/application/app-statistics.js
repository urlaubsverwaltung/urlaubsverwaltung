import $ from "jquery";
import "tablesorter";
import { dataValueNumberParser } from "../../components/table-sortable/parser-data-value-number";

$(document).ready(function () {
  $.tablesorter.addParser(dataValueNumberParser);

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
});
