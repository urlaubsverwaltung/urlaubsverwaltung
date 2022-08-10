import $ from "jquery";
import "tablesorter";
import { dataValueNumberParser } from "../../components/table-sortable/parser-data-value-number";
import { createDatepicker } from "../../components/datepicker";

$(document).ready(function () {
  $.tablesorter.addParser(dataValueNumberParser);

  const { length: columnCount } = document.querySelectorAll("#application-statistic-table thead th");
  const isPersonnelNumberColumnRendered = columnCount === 11;

  if (isPersonnelNumberColumnRendered) {
    $("#application-statistic-table").tablesorter({
      sortList: [[2, 0]],
      headers: {
        0: { sorter: false },
        4: { sorter: false },
        5: { sorter: dataValueNumberParser.id },
        6: { sorter: dataValueNumberParser.id },
        7: { sorter: dataValueNumberParser.id },
        8: { sorter: dataValueNumberParser.id },
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
        7: { sorter: dataValueNumberParser.id },
      },
    });
  }
});

const getPersonId = () => {};
const urlPrefix = ""; // not required, no absences are fetched since we have no single person (personId)

createDatepicker("#from-date-input", { urlPrefix, getPersonId });
createDatepicker("#to-date-input", { urlPrefix, getPersonId });
