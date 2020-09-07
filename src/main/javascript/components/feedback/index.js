import $ from "jquery";

$(function () {
  // FIXME: get rid of interval
  setInterval(function () {
    $(".feedback").slideUp();
  }, 5000);
});
