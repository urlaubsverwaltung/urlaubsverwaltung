import { createDatepicker } from "../../components/datepicker";

function getPersonId() {
  return globalThis.uv.personId;
}

createDatepicker("#validFrom", { urlPrefix: globalThis.uv.apiPrefix, getPersonId });
