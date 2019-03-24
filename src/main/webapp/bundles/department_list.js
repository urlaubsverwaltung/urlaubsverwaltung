import $ from 'jquery';
import 'tablesorter';
import '../js/popover'

$(document).ready(function () {

  $("table.sortable").tablesorter({
    sortList: [[0, 0]]
  });

});
