import "../js/common";
import { createDatepicker } from "../components/datepicker";

await createDatepicker("#extend-to-date-input", {
  urlPrefix: window.uv.apiPrefix,
  getPersonId: () => {
    return window.uv.personId;
  },
});
