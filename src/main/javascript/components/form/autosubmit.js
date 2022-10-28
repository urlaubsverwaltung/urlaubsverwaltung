let keyupSubmit;

document.addEventListener("keyup", function (event) {
  if (event.defaultPrevented || event.key === "Enter" || event.key === "Tab") {
    return;
  }

  const { autoSubmit = "", autoSubmitDelay = 0 } = event.target.dataset;
  if (autoSubmit) {
    const button = document.querySelector("#" + autoSubmit);
    if (button) {
      const submit = () => button.click();
      if (autoSubmitDelay) {
        clearTimeout(keyupSubmit);
        keyupSubmit = setTimeout(submit, Number(autoSubmitDelay));
      } else {
        submit();
      }
    }
  }
});

document.addEventListener("change", function (event) {
  if (event.defaultPrevented) {
    return;
  }

  const { autoSubmit = "" } = event.target.dataset;
  if (autoSubmit) {
    const button = document.querySelector("#" + autoSubmit);
    if (button) {
      button.closest("form").requestSubmit(button);
    }
  }
});
