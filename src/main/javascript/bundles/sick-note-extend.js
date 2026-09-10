import "../js/common";
import { createDatepicker, morphKeepingDatepickers } from "../components/datepicker";

document.addEventListener("turbo:before-render", function (event) {
  // morph all the things!
  event.detail.render = morphKeepingDatepickers;
});

await createDatepicker("#extend-to-date-input", {
  urlPrefix: globalThis.uv.apiPrefix,
  getPersonId: () => {
    return globalThis.uv.personId;
  },
});
