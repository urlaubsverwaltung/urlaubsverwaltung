import $ from "jquery";

$(function () {
  const form = document.querySelector("#absenceOverviewForm");

  form.addEventListener("change", () => {
    form.submit();
  });
});
