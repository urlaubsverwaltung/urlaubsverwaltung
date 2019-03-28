import $ from 'jquery'
import 'bootstrap/js/tooltip'
// popover depends on tooltip
import 'bootstrap/js/popover'

$(function () {

  //popover for app list table
  $('[data-toggle="popover"]').popover()

});
