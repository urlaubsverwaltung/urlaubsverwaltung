$(function () {

  // show tooltip on hover
  tooltip();

  // do avoid clickable table cell if there is an action within
  $('table.selectable-table td').click(function (event) {

    var target = event.target;
    var href = $(target).attr('href');

    if (href) {
      window.location = href;
      return false;
    }

  });

});

function tooltip() {
  $('[data-title]').attr('data-placement', 'bottom').tooltip();
}

