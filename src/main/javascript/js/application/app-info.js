import $ from "jquery";
import sendGetDaysRequestForTurnOfTheYear from "../send-get-days-request-for-turn-of-the-year";

$(document).ready(function () {
  const dayLength = globalThis.uv.dayLength;
  const personId = globalThis.uv.personId;

  const startDate = new Date(globalThis.uv.startDate);
  const endDate = new Date(globalThis.uv.endDate);

  if (document.querySelector(".days")) {
    sendGetDaysRequestForTurnOfTheYear(globalThis.uv.apiPrefix, startDate, endDate, dayLength, personId, ".days");
  }
});
