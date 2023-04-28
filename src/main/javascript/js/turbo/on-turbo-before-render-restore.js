let callbacks = [];
let isRestoreRender = false;

document.addEventListener("turbo:visit", function (event) {
  isRestoreRender = event.detail.action === "restore";
});

document.addEventListener("turbo:before-render", function (event) {
  if (isRestoreRender) {
    for (let callback of callbacks) {
      try {
        callback(event);
      } catch (error) {
        console.error("swallowed error to continue with other turbo:before-render callbacks.", error);
      }
    }
  }
});

export function onTurboBeforeRenderRestore(callback) {
  callbacks.push(callback);
  return function unsubscribe() {
    callbacks = callbacks.filter((c) => c !== callback);
  };
}
