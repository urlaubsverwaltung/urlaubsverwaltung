import $ from "jquery";
import "bootstrap/js/tooltip";

export default function tooltip() {
  $("[data-title]").attr("data-placement", "bottom").tooltip();
}
