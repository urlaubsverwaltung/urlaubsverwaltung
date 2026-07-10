let callbacks = [];
let isRestoreRender = false;

// used by "./turbo-restore-listeners" to feed real `document` turbo events into this module,
// kept separate so this module itself has no top-level side effects.
export function markRestoreRender(isRestore) {
  isRestoreRender = isRestore;
}

export function runRestoreCallbacks(event) {
  if (isRestoreRender) {
    for (let callback of callbacks) {
      try {
        callback(event);
      } catch (error) {
        console.error("swallowed error to continue with other turbo:before-render callbacks.", error);
      }
    }
  }
}

export function onTurboBeforeRenderRestore(callback) {
  callbacks.push(callback);
  return function unsubscribe() {
    callbacks = callbacks.filter((c) => c !== callback);
  };
}
