$(function () {

  // show tooltip on hover
  $('[data-title]').attr('data-placement', 'bottom').tooltip();

  // do avoid clickable table cell if there is an action within
  $('table td').click(function () {

    var href = $(this).find('a').attr('href');
    if (href) {
      window.location = href;
      return false;
    }

  });

});

