$(function () {

  var standardNumberOfRows = 1;
  var expandedNumberOfRows = 4;

  var $textarea = $("textarea");

  $textarea.on("focus", function () {
    this.rows = expandedNumberOfRows;
  });

  $textarea.on("blur", function () {

    if (this.value == "") {
      this.rows = standardNumberOfRows;
    }
  });

});

