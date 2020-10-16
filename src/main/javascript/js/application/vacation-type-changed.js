import $ from "jquery";

export default function vacationTypeChanged(value) {
  var $reason = $("#form-group--reason");
  var $hours = $("#form-group--hours");

  var isRequiredCssClass = "is-required";

  if (value === "SPECIALLEAVE") {
    $hours.removeClass(isRequiredCssClass);
    $reason.addClass(isRequiredCssClass);
  } else if (value === "OVERTIME") {
    $reason.removeClass(isRequiredCssClass);
    $hours.addClass(isRequiredCssClass);
  } else {
    $reason.removeClass(isRequiredCssClass);
    $hours.removeClass(isRequiredCssClass);
  }
}
