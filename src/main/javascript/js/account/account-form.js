import { createDatepickerInstances } from "../../components/datepicker";

createDatepickerInstances(
  ["#holidaysAccountValidFrom", "#holidaysAccountValidTo"],
  window.uv.apiPrefix,
  () => window.uv.personId,
  () => {},
);
