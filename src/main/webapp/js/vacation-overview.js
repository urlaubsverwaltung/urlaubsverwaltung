import $ from 'jquery'
import 'tablesorter'
import '../css/vacation-overview.css'

$(function () {

  $("table.sortable").tablesorter({
    sortList: [[0, 0]],
    headers: {
      '.non-sortable': {
        sorter: false
      }
    }
  });

  const form = document.querySelector('#absenceOverviewForm');

  form.addEventListener('change', () => {
    form.submit();
  });

});
