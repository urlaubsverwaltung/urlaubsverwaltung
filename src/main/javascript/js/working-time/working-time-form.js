import { createDatepickerInstances } from "../../components/datepicker";

createDatepickerInstances(
  ["#validFrom"],
  window.uv.apiPrefix,
  () => window.uv.personId,
  () => {},
);
