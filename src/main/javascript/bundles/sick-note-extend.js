import "../js/common";
import * as Turbo from "@hotwired/turbo";
import { createDatepicker } from "../components/datepicker";
import { initAutosubmit } from "../components/form";

// opt-in to turbo with `data-turbo="true"`
Turbo.session.drive = false;

initAutosubmit();

await createDatepicker("#extend-to-date-input", {
  urlPrefix: window.uv.apiPrefix,
  getPersonId: () => {
    return window.uv.personId;
  },
});
