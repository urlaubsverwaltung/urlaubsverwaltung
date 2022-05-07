import $ from "jquery";
import "bootstrap/js/tooltip";

export default function tooltip() {
  $("[data-title]:not([data-toggle='tooltip'])").attr("data-placement", "bottom").tooltip();
  $("[data-toggle='tooltip']").tooltip({ viewport: "body", delay: { show: 300, hide: 0 } });
}
