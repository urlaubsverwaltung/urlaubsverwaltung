import $ from "jquery";

$(function () {
  // do avoid clickable table cell if there is an action within
  $("table.selectable-table td").on("click", function (event) {
    const target = event.target;
    const href = $(target).attr("href");

    if (href) {
      window.location = href;
      return false;
    }
  });
});
