{
  const userSettingsForm = document.querySelector("#user-settings-form");

  let isLanguageGroupFocused = false;
  let isLanguageGroupFocusedWithKeyboard = false;

  const focusManager = createFocusManager();
  const languageFieldset = userSettingsForm.querySelector("#fieldset-language");

  if (focusManager.shouldFocusAfterReload()) {
    languageFieldset.querySelector("input[name='locale']:checked").focus();
  }

  // `focusin` event listener is called before `keyup`
  languageFieldset.addEventListener("focusin", function (event) {
    if (event.target.matches("[name='locale']")) {
      isLanguageGroupFocused = true;
    }
  });

  languageFieldset.addEventListener("focusout", function (event) {
    if (event.target.matches("[name='locale']")) {
      isLanguageGroupFocused = false;
    }
  });

  userSettingsForm.addEventListener("change", function (event) {
    if (isLanguageGroupFocusedWithKeyboard) {
      focusManager.memoize();
    }
    if (event.target.name === "locale") {
      userSettingsForm.submit();
    }
  });

  addEventListener("keyup", function (event) {
    if (!isLanguageGroupFocused) {
      focusManager.clean();
    }
    isLanguageGroupFocusedWithKeyboard = isLanguageGroupFocused && event.key === "Tab";
  });

  addEventListener("click", function (event) {
    if (!childOfLanguage(event.target)) {
      focusManager.clean();
    }
  });

  function childOfLanguage(element) {
    for (let current = element; current; current = current.parentElement) {
      if (current.matches("#fieldset-language")) {
        return true;
      }
    }
    return false;
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
}
