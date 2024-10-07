import { post } from "../fetch";

const html = document.querySelector("html");
const themeColorMetaElement = document.querySelector("meta[name='theme-color']");
const mediaQueryDark = globalThis.matchMedia("(prefers-color-scheme: dark)");
const userSettingsForm = document.querySelector("#user-settings-form");

let darkTheme = html.classList.contains("tw-dark") || (html.classList.contains("tw-system") && mediaQueryDark.matches);

userSettingsForm.addEventListener("change", function (event) {
  if (event.target.name === "theme") {
    const value = event.target.value;

    if (/system/i.test(value)) {
      html.classList.add("tw-system");
    } else {
      html.classList.remove("tw-system");
    }

    darkTheme = /system/i.test(value) ? mediaQueryDark.matches : /dark/i.test(value);
    render();

    const formData = new FormData(userSettingsForm);

    post(userSettingsForm.action, {
      body: formData,
    })
      .then(function (response) {
        if (!response.ok || response.status < 200 || response.status >= 300) {
          console.log("theme change could not be persisted.");
        }
      })
      .catch(function () {
        console.log("theme change could not be persisted.");
      });
  }
});

try {
  mediaQueryDark.addEventListener("change", handleMediaQueryChange);
} catch {
  // safari (https://stackoverflow.com/a/60000747)
  try {
    mediaQueryDark.addListener(handleMediaQueryChange);
  } catch (error_) {
    console.info("could not add mediaQuery listener to toggle theme.", error_);
  }
}

function handleMediaQueryChange() {
  if (html.classList.contains("tw-system")) {
    darkTheme = !darkTheme;
    render();
  }
}

function render() {
  const transitionStyle = document.createElement("style");
  transitionStyle.innerHTML = `*, *::before, *::after { transition: all 75ms ease-in; }`;
  document.head.append(transitionStyle);

  setTimeout(function () {
    if (darkTheme) {
      html.classList.add("tw-dark");
      themeColorMetaElement.setAttribute("content", "#18181b");
    } else {
      html.classList.remove("tw-dark");
      themeColorMetaElement.setAttribute("content", "#fafafa");
    }

    setTimeout(function () {
      transitionStyle.remove();
    }, 100);
  });
}
