const userSettingsForm = document.querySelector("#user-settings-form");

let languageGroupFocused = false;
let languageGroupFocusedWithKeyboard = false;

const focusManager = createFocusManager();
const languageFieldset = userSettingsForm.querySelector("#fieldset-language");

if (focusManager.shouldFocusAfterReload()) {
  languageFieldset.querySelector("input[name='locale']:checked").focus();
}

// `focusin` event listener is called before `keyup`
languageFieldset.addEventListener("focusin", function (event) {
  if (event.target.matches("[name='locale']")) {
    languageGroupFocused = true;
  }
});

languageFieldset.addEventListener("focusout", function (event) {
  if (event.target.matches("[name='locale']")) {
    languageGroupFocused = false;
  }
});

userSettingsForm.addEventListener("change", function (event) {
  if (languageGroupFocusedWithKeyboard) {
    focusManager.memoize();
  }
  if (event.target.name === "locale") {
    userSettingsForm.submit();
  }
});

globalThis.addEventListener("keyup", function (event) {
  if (!languageGroupFocused) {
    focusManager.clean();
  }
  languageGroupFocusedWithKeyboard = languageGroupFocused && event.key === "Tab";
});

globalThis.addEventListener("click", function (event) {
  if (!childOfLanguage(event.target)) {
    focusManager.clean();
  }
});

function childOfLanguage(element) {
  if (!element) {
    return false;
  }
  if (element.matches("#fieldset-language")) {
    return true;
  }
  return childOfLanguage(element.parentElement);
}

function createFocusManager() {
  const focusSessionKey = "uv--focus-language-after-reload";
  const yep = "true";

  return {
    memoize() {
      sessionStorage.setItem(focusSessionKey, yep);
    },

    shouldFocusAfterReload() {
      return sessionStorage.getItem(focusSessionKey) === yep;
    },

    clean() {
      sessionStorage.removeItem(focusSessionKey);
    },
  };
}
