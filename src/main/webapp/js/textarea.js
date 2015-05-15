$(function () {

  var standardNumberOfRows = 1;
  var expandedNumberOfRows = 4;

  var $textarea = $("textarea");

  $textarea.focus(function () {
    this.rows = expandedNumberOfRows;
  });

  $textarea.blur(function () {

    if (this.value == "") {
      this.rows = standardNumberOfRows;
    }
  });

});

