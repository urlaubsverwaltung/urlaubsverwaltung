import { markRestoreRender, runRestoreCallbacks } from "./on-turbo-before-render-restore";

document.addEventListener("turbo:visit", function (event) {
  markRestoreRender(event.detail.action === "restore");
});

document.addEventListener("turbo:before-render", function (event) {
  runRestoreCallbacks(event);
});
