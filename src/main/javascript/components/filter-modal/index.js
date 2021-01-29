import { createDatepicker } from "../datepicker";

const getPersonId = () => {};
const urlPrefix = window.uv.apiPrefix;

createDatepicker("#startDate", { urlPrefix, getPersonId });
createDatepicker("#endDate", { urlPrefix, getPersonId });
