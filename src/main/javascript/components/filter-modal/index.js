import $ from "jquery";
import { createDatepickerInstances } from "../datepicker";

const getPersonId = () => {};
const onSelect = () => {};

$(document).ready(async function () {
  const selectors = ["#startDate", "#endDate"];
  const urlPrefix = window.uv.apiPrefix;

  await createDatepickerInstances(selectors, urlPrefix, getPersonId, onSelect);
});
