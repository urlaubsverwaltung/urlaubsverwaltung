import $ from 'jquery';
import sendGetDaysRequestForTurnOfTheYear from '../sendGetDaysRequestForTurnOfTheYear';

$(document).ready(function() {
  var dayLength = window.uv.dayLength;
  var personId = window.uv.personId;

  var startDate = new Date(window.uv.startDate);
  var endDate = new Date(window.uv.endDate);

  if (document.querySelector('.days')) {
    sendGetDaysRequestForTurnOfTheYear(window.uv.apiPrefix, startDate, endDate, dayLength, personId, ".days");
  }
});
