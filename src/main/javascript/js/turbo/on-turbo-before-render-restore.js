// used by "./turbo-restore-listeners" to feed real `document` turbo events into this module,
// kept separate so this module itself has no top-level side effects.
const { markRestoreRender, runRestoreCallbacks, onTurboBeforeRenderRestore } = (function () {
  let callbacks = [];
  let isRestoreRender = false;

  function markRestoreRender(isRestore) {
    isRestoreRender = isRestore;
  }

  function runRestoreCallbacks(event) {
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

  function onTurboBeforeRenderRestore(callback) {
    callbacks.push(callback);
    return function unsubscribe() {
      callbacks = callbacks.filter((c) => c !== callback);
    };
  }

  return { markRestoreRender, runRestoreCallbacks, onTurboBeforeRenderRestore };
})();

export { markRestoreRender, runRestoreCallbacks, onTurboBeforeRenderRestore };
