const originalQuerySelector = document.querySelector;

describe("useTheme", () => {
  let mockHtmlElement;
  let mockPrefersDark;
  let useMediaMock;

  beforeEach(() => {
    vi.resetModules();

    mockHtmlElement = {
      classList: {
        contains: vi.fn(),
      },
    };
    document.querySelector = vi.fn().mockReturnValue(mockHtmlElement);

    mockPrefersDark = {
      value: false,
      subscribe: vi.fn(),
    };

    useMediaMock = vi.fn().mockReturnValue({ matches: mockPrefersDark });

    vi.doMock("../use-media", () => ({
      useMedia: useMediaMock,
    }));
  });

  afterEach(() => {
    document.querySelector = originalQuerySelector;
  });

  test("initializes with system theme preference - light mode", async () => {
    mockHtmlElement.classList.contains.mockImplementation((className) => className === "theme-system");
    mockPrefersDark.value = false; // Light mode

    const themeModule = await import("../use-theme");
    const { theme } = themeModule.useTheme();

    expect(theme.value).toBe("light");
  });

  test("initializes with system theme preference - dark mode", async () => {
    mockHtmlElement.classList.contains.mockImplementation((className) => className === "theme-system");
    mockPrefersDark.value = true; // Dark mode

    const themeModule = await import("../use-theme");
    const { theme } = themeModule.useTheme();

    expect(theme.value).toBe("dark");
  });

  test("initializes with explicitly set light theme", async () => {
    mockHtmlElement.classList.contains.mockImplementation((className) => {
      if (className === "theme-system") return false;
      if (className === "theme-dark") return false;
      return false;
    });

    const themeModule = await import("../use-theme");
    const { theme } = themeModule.useTheme();

    expect(theme.value).toBe("light");
    // Verify no subscription was made to prefersDark (only happens in system mode)
    expect(mockPrefersDark.subscribe).not.toHaveBeenCalled();
  });

  test("initializes with explicitly set dark theme", async () => {
    mockHtmlElement.classList.contains.mockImplementation((className) => {
      if (className === "theme-system") return false;
      if (className === "theme-dark") return true;
      return false;
    });

    const themeModule = await import("../use-theme");
    const { theme } = themeModule.useTheme();

    expect(theme.value).toBe("dark");
  });

  test("updates theme when system preference changes", async () => {
    mockHtmlElement.classList.contains.mockImplementation((className) => className === "theme-system");
    mockPrefersDark.value = false; // Light mode initially

    // Get the callback function for subscription
    let prefersDarkCallback;
    mockPrefersDark.subscribe.mockImplementation((callback) => {
      prefersDarkCallback = callback;
      return vi.fn();
    });

    const themeModule = await import("../use-theme");
    const { theme } = themeModule.useTheme();

    expect(theme.value).toBe("light");
    expect(mockPrefersDark.subscribe).toHaveBeenCalled();

    // Simulate change in system preference to dark mode
    prefersDarkCallback(true);
    expect(theme.value).toBe("dark");

    // Simulate change back to light mode
    prefersDarkCallback(false);
    expect(theme.value).toBe("light");
  });
});
