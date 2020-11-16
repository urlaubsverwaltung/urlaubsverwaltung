import { createDatepicker } from "../../components/datepicker";

function getPersonId() {
  return window.uv.personId;
}

createDatepicker("#validFrom", { urlPrefix: window.uv.apiPrefix, getPersonId });
