/**
 * common test setup stuff (e.g. loading jquery)
 *
 * <b>note</b>: remember to call #cleanup in afterEach to have a green field for every test
 *
 * @example
 * ```
 * import { setup, cleanup } from './TestSetupHelper';
 *
 * describe('myTestSuite', () => {
 *   beforeEach(setup)
 *   afterEach(cleanup)
 *
 *   it('should success', () => {...})
 * })
 * ```
 *
 * @returns {Promise<void>}
 */
export async function setup() {
  // jest is configured with 'jsdom' environment
  // so window is already available here
  // and we're able to attach additional custom stuff
  window.jQuery = window.$ = require("jquery");

  // defined in 'actions.js' as global function
  // setting as spy function to assert things in the tests
  window.tooltip = jest.fn();

  // trigger ready event
  // so "modules" registered via $(function() { /* ... */ }) are executed immediately on file import
  window.jQuery.ready();
}

/**
 * @example
 * ```
 * // myModule.js
 * $(function() {
 *   // ...
 * });
 * ```
 *
 * ```
 * // myModule.spec.js
 * import { setup, waitForFinishedJQueryReadyCallbacks } from './TestSetupHelper';
 *
 * beforeEach(async function() {
 *   // setup jquery etc
 *   await setup();
 *
 *   // import registers ready callback
 *   await import('../../myModule');
 *
 *   // wait till callback has been invoked
 *   await waitForFinishedJQueryReadyCallbacks();
 * });
 * ```
 * @returns {Promise<any>}
 */
export function waitForFinishedJQueryReadyCallbacks() {
  return new Promise((resolve) => {
    window.jQuery.fn.ready(function () {
      resolve();
    });
  });
}

export async function cleanup() {
  // we have to reset all modules to be able to reinstantiate jquery stuff in #setup
  // jest starts a new context for test files only
  // not for tests defined within a file
  jest.resetModules();
}
