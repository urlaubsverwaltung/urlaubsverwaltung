import $ from 'jquery'
import './navbar.css'

// UGLY FIX BECAUSE BOOTSTRAP DROPDOWN IN NAVBAR NOT WORKING ON SOME MOBILE PHONES

$(document).ready(function() {
  $('.dropdown-toggle').on("click", function (event) {
    event.preventDefault();
    setTimeout($.proxy(function () {
      if ('ontouchstart' in document.documentElement) {
        $(this).siblings('.dropdown-backdrop').off().remove();
      }
    }, this), 0);
  });
});
