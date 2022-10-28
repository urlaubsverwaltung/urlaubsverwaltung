import $ from "jquery";
import sendGetDaysRequestForTurnOfTheYear from "../send-get-days-request-for-turn-of-the-year";

$(document).ready(function () {
  const dayLength = window.uv.dayLength;
  const personId = window.uv.personId;

  const startDate = new Date(window.uv.startDate);
  const endDate = new Date(window.uv.endDate);

  if (document.querySelector(".days")) {
    sendGetDaysRequestForTurnOfTheYear(window.uv.apiPrefix, startDate, endDate, dayLength, personId, ".days");
  }
});
