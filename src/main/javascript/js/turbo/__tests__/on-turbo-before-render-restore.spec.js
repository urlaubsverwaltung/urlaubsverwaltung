import { onTurboBeforeRenderRestore } from "../on-turbo-before-render-restore";
import "../turbo-restore-listeners";

describe("on-turbo-before-render-restore", function () {
  let unsubscribes = [];

  afterEach(function () {
    for (const unsubscribe of unsubscribes) {
      unsubscribe();
    }
    unsubscribes = [];
    vi.restoreAllMocks();
  });

  function subscribe(callback) {
    const unsubscribe = onTurboBeforeRenderRestore(callback);
    unsubscribes.push(unsubscribe);
    return unsubscribe;
  }

  function turboVisit(action) {
    document.dispatchEvent(new CustomEvent("turbo:visit", { detail: { action } }));
  }

  function turboBeforeRender(newBody = document.createElement("body")) {
    const event = new CustomEvent("turbo:before-render", { detail: { newBody } });
    document.dispatchEvent(event);
    return event;
  }

  it("invokes the callback on a before-render that follows a restore visit", function () {
    const callback = vi.fn();
    subscribe(callback);
    turboVisit("restore");

    turboBeforeRender();

    expect(callback).toHaveBeenCalledTimes(1);
  });

  it("does not invoke the callback on a before-render that follows an advance visit", function () {
    const callback = vi.fn();
    subscribe(callback);
    turboVisit("advance");

    turboBeforeRender();

    expect(callback).not.toHaveBeenCalled();
  });

  it("passes the turbo:before-render event through to the callback", function () {
    const callback = vi.fn();
    subscribe(callback);
    turboVisit("restore");

    const event = turboBeforeRender();

    expect(callback).toHaveBeenCalledWith(event);
  });

  it("keeps reacting as 'restore' on every subsequent before-render until another visit happens", function () {
    const callback = vi.fn();
    subscribe(callback);
    turboVisit("restore");

    turboBeforeRender();
    turboBeforeRender();

    expect(callback).toHaveBeenCalledTimes(2);
  });

  it("stops reacting once a non-restore visit occurs", function () {
    const callback = vi.fn();
    subscribe(callback);
    turboVisit("restore");
    turboBeforeRender();
    turboVisit("advance");

    turboBeforeRender();

    expect(callback).toHaveBeenCalledTimes(1);
  });

  it("invokes multiple subscribed callbacks", function () {
    const callback1 = vi.fn();
    const callback2 = vi.fn();
    subscribe(callback1);
    subscribe(callback2);
    turboVisit("restore");

    turboBeforeRender();

    expect(callback1).toHaveBeenCalledTimes(1);
    expect(callback2).toHaveBeenCalledTimes(1);
  });

  it("no longer invokes a callback after it unsubscribed", function () {
    const callback = vi.fn();
    const unsubscribe = subscribe(callback);
    turboVisit("restore");

    unsubscribe();
    turboBeforeRender();

    expect(callback).not.toHaveBeenCalled();
  });

  it("does not affect other callbacks when unsubscribing one of them", function () {
    const callback1 = vi.fn();
    const callback2 = vi.fn();
    const unsubscribe1 = subscribe(callback1);
    subscribe(callback2);
    turboVisit("restore");

    unsubscribe1();
    turboBeforeRender();

    expect(callback1).not.toHaveBeenCalled();
    expect(callback2).toHaveBeenCalledTimes(1);
  });

  it("swallows an error thrown by one callback and still invokes the others", function () {
    const consoleErrorSpy = vi.spyOn(console, "error").mockImplementation(() => {});
    const failingCallback = vi.fn(() => {
      throw new Error("boom");
    });
    const otherCallback = vi.fn();
    subscribe(failingCallback);
    subscribe(otherCallback);
    turboVisit("restore");

    expect(() => turboBeforeRender()).not.toThrow();

    expect(otherCallback).toHaveBeenCalledTimes(1);
    expect(consoleErrorSpy).toHaveBeenCalledWith(
      "swallowed error to continue with other turbo:before-render callbacks.",
      expect.any(Error),
    );
  });
});
