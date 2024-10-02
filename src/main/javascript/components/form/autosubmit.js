let submitDelayHandle;

export function initAutosubmit() {
  document.addEventListener("input", function (event) {
    const { defaultPrevented, target } = event;
    if (defaultPrevented || target.closest("duet-date-picker")) {
      // change is handled by 'duetChange' event
      return;
    }
    submit(event);
  });

  document.addEventListener("change", function (event) {
    const { defaultPrevented, target } = event;
    if (defaultPrevented || textInput(target) || target.closest("duet-date-picker")) {
      // `change` is not of interest for text inputs which are triggered by `keyup`
      // and due-date-picker change is handled with 'duetChange'
      return;
    }
    submit(event);
  });

  document.addEventListener("duetChange", function (event) {
    const { defaultPrevented, target } = event;
    if (defaultPrevented || textInput(target) || !event.target.value) {
      // `change` is not of interest for text inputs which are triggered by `keyup`
      return;
    }
    submit(event);
  });
}

function submit(event) {
  const { target } = event;
  if ("autoSubmit" in target.dataset) {
    const { autoSubmit = "", autoSubmitDelay = 0 } = target.dataset;
    const element = autoSubmit ? document.querySelector("#" + autoSubmit) : target.closest("form");
    const submit = () => {
      if (element instanceof HTMLFormElement) {
        element.requestSubmit();
      } else {
        element.closest("form").requestSubmit(element);
      }
    };
    if (autoSubmitDelay) {
      clearTimeout(submitDelayHandle);
      submitDelayHandle = setTimeout(submit, Number(autoSubmitDelay));
    } else {
      submit();
    }
  }
}

function textInput(element) {
  return [
    "input[type='text']",
    "input[type='mail']",
    "input[type='search']",
    "input[type='password']",
    "textarea",
  ].some((selector) => element.matches(selector));
}
