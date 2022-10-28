import { createDatepicker } from "../datepicker";

const getPersonId = () => {};
const urlPrefix = ""; // not required, no absences are fetched since we have no single person (personId)

createDatepicker("#startDate", { urlPrefix, getPersonId });
createDatepicker("#endDate", { urlPrefix, getPersonId });
