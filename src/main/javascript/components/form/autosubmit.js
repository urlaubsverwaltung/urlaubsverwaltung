export function initAutosubmit() {
  let keyupSubmit;

  document.addEventListener("keyup", function (event) {
    if (
      event.defaultPrevented ||
      event.metaKey ||
      whitespaceKeys.has(event.key) ||
      modifierKeys.has(event.key) ||
      navigationKeys.has(event.key) ||
      uiKeys.has(event.key) ||
      deviceKeys.has(event.key) ||
      functionKeys.has(event.key) ||
      mediaKeys.has(event.key) ||
      audioControlKeys.has(event.key)
    ) {
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

    const { autoSubmit = "" } = target.dataset;
    const element = autoSubmit ? document.querySelector("#" + autoSubmit) : target.closest("form");
    if (element instanceof HTMLFormElement) {
      element.requestSubmit();
    } else {
      element.closest("form").requestSubmit(element);
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

const whitespaceKeys = new Set(["Enter", "Tab", "Alt"]);

const modifierKeys = new Set([
  "AltGraph",
  "CapsLock",
  "Control",
  "Fn",
  "FnLock",
  "Hyper",
  "Meta",
  "NumLock",
  "ScrollLock",
  "Shift",
  "Super",
  "Symbol",
  "SymbolLock",
]);

const navigationKeys = new Set([
  "ArrowDown",
  "ArrowLeft",
  "ArrowRight",
  "ArrowUp",
  "End",
  "Home",
  "PageDown",
  "PageUp",
  "End",
]);

const uiKeys = new Set([
  "Accept",
  "ContextMenu",
  "Execute",
  "Find",
  "Help",
  "Pause",
  "Play",
  "Props",
  "Select",
  "ZoomIn",
  "ZoomOut",
]);

const deviceKeys = new Set([
  "BrightnessDown",
  "BrightnessUp",
  "Eject",
  "LogOff",
  "Power",
  "PowerOff",
  "PrintScreen",
  "Hibernate",
  "Standby",
  "WakeUp",
]);

const functionKeys = new Set([
  "F1",
  "F2",
  "F3",
  "F4",
  "F5",
  "F6",
  "F7",
  "F8",
  "F9",
  "F10",
  "F11",
  "F12",
  "F13",
  "F14",
  "F15",
  "F16",
  "F17",
  "F18",
  "F19",
  "F20",
  "Soft1",
  "Soft2",
  "Soft3",
  "Soft4",
]);

const mediaKeys = new Set([
  "ChannelDown",
  "ChannelUp",
  "MediaFastForward",
  "MediaPause",
  "MediaPlay",
  "MediaPlayPause",
  "MediaRecord",
  "MediaRewind",
  "MediaStop",
  "MediaTrackNext",
  "MediaTrackPrevious",
]);

const audioControlKeys = new Set([
  "AudioVolumeDown",
  "AudioVolumeMute",
  "AudioVolumeUp",
  "MicrophoneToggle",
  "MicrophoneVolumeDown",
  "MicrophoneVolumeMute",
  "MicrophoneVolumeUp",
]);
