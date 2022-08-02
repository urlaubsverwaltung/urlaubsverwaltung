import $ from "jquery";
import "bootstrap/js/tooltip";

export default function tooltip() {
  for (const tooltip of document.querySelectorAll("[data-title]")) {
    $(tooltip).tooltip({
      placement: "bottom",
      delay: {
        show: Number(tooltip.dataset.titleDelay || 500),
        hide: 0,
      },
    });
  }
}
