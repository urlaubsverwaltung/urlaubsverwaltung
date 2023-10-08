export function initAutosubmit() {
  let keyupSubmit;

  document.addEventListener("input", function (event) {
    if (event.defaultPrevented) {
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
    const { defaultPrevented, target } = event;
    if (defaultPrevented || noTextInput(target)) {
      // `change` is not of interest for text inputs which are triggered by `keyup`
      return;
    }

    if ("autoSubmit" in target.dataset) {
      const { autoSubmit = "" } = target.dataset;
      const element = autoSubmit ? document.querySelector("#" + autoSubmit) : target.closest("form");
      if (element instanceof HTMLFormElement) {
        element.requestSubmit();
      } else {
        element.closest("form").requestSubmit(element);
      }
    }
  });
}

function noTextInput(element) {
  return [
    "input[type='text']",
    "input[type='mail']",
    "input[type='search']",
    "input[type='password']",
    "textarea",
  ].some((selector) => element.matches(selector));
}
