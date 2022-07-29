const userSettingsForm = document.querySelector("#user-settings-form");

userSettingsForm.addEventListener("change", function (event) {
  if (event.target.name === "locale") {
    userSettingsForm.submit();
  }
});
