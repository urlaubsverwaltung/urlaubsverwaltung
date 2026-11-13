import "../js/common";
import { createDatepicker } from "../components/datepicker";
import "../components/back-button";

// company closures apply to everybody, so the datepicker shows no absences of a person
await createDatepicker("#validFrom", { getPersonId: () => {} });
