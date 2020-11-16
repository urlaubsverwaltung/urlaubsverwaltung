import { createDatepicker } from "../../components/datepicker";

const urlPrefix = window.uv.apiPrefix;
const personId = window.uv.personId;

function getPersonId() {
  return personId;
}

createDatepicker("#holidaysAccountValidFrom", { urlPrefix, getPersonId });
createDatepicker("#holidaysAccountValidTo", { urlPrefix, getPersonId });
