import $ from "jquery";
import "tablesorter";

$(document).ready(function () {
  $("table.sortable").tablesorter({
    sortList: [[1, 0]],
    headers: {
      0: { sorter: false },
      3: { sorter: false },
      4: { sorter: false },
      5: { sorter: "commaNumber" },
      6: { sorter: "commaNumber" },
      7: { sorter: "commaNumber" },
    },
    textExtraction: function (node) {
      var sortable = $(node).find(".sortable");

      if (sortable.length > 0) {
        return sortable[0].innerHTML;
      }

      return node.innerHTML;
    },
  });
});
