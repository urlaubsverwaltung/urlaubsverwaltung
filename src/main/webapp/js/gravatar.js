$(function () {

  // set background as style attribute for each gravatar element
  $('.gravatar').each(function () {

    var $$ = $(this);

    var gravatarURL = $$.attr('data-gravatar');
    var defaultURL = '/images/gravatar-default.jpg';

    if ($$.hasClass('gravatar--medium')) {
      defaultURL = '/images/gravatar-medium.jpg';
    }

    if ($$.hasClass('gravatar--small')) {
      defaultURL = '/images/gravatar-small.jpg';
    }

    $$.css({
      'background': 'url(' + gravatarURL + ') no-repeat center, url(' + defaultURL + ') no-repeat center',
      'background-size': 'cover'
    });

  });

});
