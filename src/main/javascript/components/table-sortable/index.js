import $ from "jquery";
import "tablesorter";

$(document).ready(function () {
  $.tablesorter.addParser({
    id: "germanDate",
    is: function () {
      return false;
    },
    format: function (s) {
      var d;
      if (s.length > 10) {
        d = s.slice(0, 10);
      } else if (s.length == 10) {
        d = s;
      } else {
        return -1;
      }

      var a = d.split(".");
      a[1] = a[1].replace(/^0+/g, "");
      return new Date(a.reverse().join("/")).getTime();
    },
    type: "numeric",
  });

  $.tablesorter.addParser({
    id: "commaNumber",
    is: function () {
      return false;
    },
    format: function (s) {
      var reg = new RegExp("\\d+");

      if (reg.test(s)) {
        s = s.replace(/[,.]/g, ".");

        // possible that string is sth like that: 30 + 2
        return eval(s);
      } else {
        return -1;
      }
    },
    type: "numeric",
  });

  $("table.sortable").tablesorter({
    sortList: [[1, 0]],
  });
});
