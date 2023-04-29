import { onTurboBeforeRenderRestore } from "../../js/turbo";

// there has to be made some considerations for window.history handling (navigating backwards)
// use case:
// - web page renders with closed `<details-dropdown>`
// - user opens the dropdown
// - user changes form data within the dropdown and submits the form
//   - response received
//   - current page snapshot is created (cache for client side history navigation)
//   - history.pushState() is invoked
//   - new page will be rendered
// - user navigates back with history.back()
//
// client side routing (with `hotwire/turbo` in our case) results in:
// - rendering the cached page.
//   this cached page includes the opened `<details-dropdown>` instead of the initially closed element.
//
// -> however, navigating backwards we want to render the initial element. the closed one.
//
onTurboBeforeRenderRestore(function (event) {
  // close all dropdowns
  for (let dropdown of event.detail.newBody.querySelectorAll("[is=uv-details-dropdown]")) {
    dropdown.open = false;
  }
});
