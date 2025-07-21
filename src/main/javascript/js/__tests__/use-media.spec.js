import { useMedia } from "../use-media";

describe("useMedia", () => {
  const originalMatchMedia = globalThis.matchMedia;

  let mockMatchMedia;
  let mockMediaQueryList;
  let changeListeners = [];
  let addListenerCallbacks = [];

  beforeEach(() => {
    changeListeners = [];
    addListenerCallbacks = [];

    mockMediaQueryList = {
      matches: false,
      addEventListener: jest.fn().mockImplementation((event, callback) => {
        if (event === "change") {
          changeListeners.push(callback);
        }
      }),
      addListener: jest.fn().mockImplementation((callback) => {
        addListenerCallbacks.push(callback);
      }),
    };

    mockMatchMedia = jest.fn().mockImplementation(() => mockMediaQueryList);

    globalThis.matchMedia = mockMatchMedia;
  });

  afterEach(() => {
    globalThis.matchMedia = originalMatchMedia;
  });

  test("creates a media query observer", () => {
    const actual = useMedia("(prefers-color-scheme: dark)");

    expect(mockMatchMedia).toHaveBeenCalledWith("(prefers-color-scheme: dark)");
    expect(actual.matches.value).toBe(false);
  });

  test("initializes with matching media query", () => {
    // Setup media query to match
    mockMediaQueryList.matches = true;

    const actual = useMedia("(min-width: 768px)");
    expect(actual.matches.value).toBe(true);
  });

  test("updates matches when media query changes using addEventListener", () => {
    const actual = useMedia("(prefers-color-scheme: dark)");

    expect(actual.matches.value).toBe(false);
    expect(mockMediaQueryList.addEventListener).toHaveBeenCalledWith("change", expect.any(Function));

    simulateQueryMatchesChange(true);
    expect(actual.matches.value).toBe(true);
  });

  test("falls back to addListener for Safari", () => {
    // Make addEventListener throw to simulate Safari
    mockMediaQueryList.addEventListener = jest.fn().mockImplementation(() => {
      throw new Error("Not supported");
    });

    const actual = useMedia("(prefers-color-scheme: dark)");

    expect(mockMediaQueryList.addListener).toHaveBeenCalled();

    simulateQueryMatchesChange(true);
    expect(actual.matches.value).toBe(true);
  });

  test("handles errors when both addEventListener and addListener fail", () => {
    // Make both methods throw to test error handling
    mockMediaQueryList.addEventListener = jest.fn().mockImplementation(() => {
      throw new Error("addEventListener not supported");
    });

    mockMediaQueryList.addListener = jest.fn().mockImplementation(() => {
      throw new Error("addListener not supported");
    });

    const query = "(prefers-color-scheme: dark)";
    const actual = useMedia(query);

    // Verify we still get a result object
    expect(actual).toHaveProperty("matches");
  });

  function simulateQueryMatchesChange(matches) {
    mockMediaQueryList.matches = matches;
    for (const callback of changeListeners) {
      callback();
    }
    for (const callback of addListenerCallbacks) {
      callback();
    }
  }
});
