import "../js/common";
import { createDatepicker } from "../components/datepicker";
import { initAutosubmit } from "../components/form";

initAutosubmit();

await createDatepicker("#extend-to-date-input", {
  urlPrefix: window.uv.apiPrefix,
  getPersonId: () => {
    return window.uv.personId;
  },
});
