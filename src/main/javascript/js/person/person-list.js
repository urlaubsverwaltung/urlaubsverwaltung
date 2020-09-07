import $ from 'jquery'
import List from 'list.js'
import 'tablesorter';

$(document).ready(function () {

  $("table.sortable").tablesorter({
    sortList: [[1, 0]],
    headers: {
      3: {sorter: 'commaNumber'},
      4: {sorter: 'commaNumber'},
      5: {sorter: 'commaNumber'},
      6: {sorter: 'commaNumber'}
    }
  });

  var options = {
    valueNames: ['firstname', 'lastname'],
    page: 500
  };

  new List('users', options);

  $('#search').on("keypress", function (event) {

    if (event.keyCode === 10 || event.keyCode === 13)
      event.preventDefault();
  });
});
