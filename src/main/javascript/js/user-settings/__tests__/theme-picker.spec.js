import { post } from "../../fetch";

vi.mock("../../fetch", () => ({ post: vi.fn() }));

describe("theme-picker", function () {
  beforeEach(function () {
    vi.useFakeTimers();
  });

  afterEach(function () {
    document.body.innerHTML = "";
    document.head.innerHTML = "";
    document.documentElement.className = "";
    delete globalThis.matchMedia;
    vi.clearAllMocks();
    vi.resetModules();
    vi.useRealTimers();
  });

  function fakeMediaQueryList(matches) {
    const listeners = new Set();
    return {
      matches,
      addEventListener: vi.fn((type, callback) => listeners.add(callback)),
      removeEventListener: vi.fn((type, callback) => listeners.delete(callback)),
      addListener: vi.fn((callback) => listeners.add(callback)),
      removeListener: vi.fn((callback) => listeners.delete(callback)),
      trigger() {
        for (const callback of listeners) callback();
      },
    };
  }

  async function renderAndLoad({ htmlClass = "", matches = false } = {}) {
    document.documentElement.className = htmlClass;
    document.head.innerHTML = `<meta name="theme-color" content="#fafafa" />`;
    document.body.innerHTML = `
      <form id="user-settings-form" action="/web/person/1/notifications">
        <input type="radio" name="theme" value="LIGHT" />
        <input type="radio" name="theme" value="DARK" />
        <input type="radio" name="theme" value="SYSTEM" />
      </form>
    `;

    const mediaQueryDark = fakeMediaQueryList(matches);
    globalThis.matchMedia = vi.fn(() => mediaQueryDark);

    await import("../theme-picker");

    return { mediaQueryDark, form: document.querySelector("#user-settings-form") };
  }

  function selectTheme(form, value) {
    const radio = form.querySelector(`input[value='${value}']`);
    radio.checked = true;
    radio.dispatchEvent(new Event("change", { bubbles: true }));
  }

  async function flushRenderTimers() {
    await vi.runAllTimersAsync();
  }

  describe("selecting a theme", function () {
    it("switches to dark theme and persists the choice", async function () {
      post.mockResolvedValue({ ok: true, status: 200 });
      const { form } = await renderAndLoad({ htmlClass: "theme-light" });

      selectTheme(form, "DARK");
      await flushRenderTimers();

      expect(document.documentElement.classList.contains("theme-dark")).toBe(true);
      expect(document.documentElement.classList.contains("theme-system")).toBe(false);
      expect(document.querySelector("meta[name='theme-color']").getAttribute("content")).toBe("#18181b");
      expect(post).toHaveBeenCalledWith("http://localhost/web/person/1/notifications", {
        body: expect.any(FormData),
      });
    });

    it("switches to light theme and persists the choice", async function () {
      post.mockResolvedValue({ ok: true, status: 200 });
      const { form } = await renderAndLoad({ htmlClass: "theme-dark" });

      selectTheme(form, "LIGHT");
      await flushRenderTimers();

      expect(document.documentElement.classList.contains("theme-dark")).toBe(false);
      expect(document.querySelector("meta[name='theme-color']").getAttribute("content")).toBe("#fafafa");
    });

    it("switches to system theme using the current OS preference (dark)", async function () {
      post.mockResolvedValue({ ok: true, status: 200 });
      const { form } = await renderAndLoad({ htmlClass: "theme-light", matches: true });

      selectTheme(form, "SYSTEM");
      await flushRenderTimers();

      expect(document.documentElement.classList.contains("theme-system")).toBe(true);
      expect(document.documentElement.classList.contains("theme-dark")).toBe(true);
    });

    it("switches to system theme using the current OS preference (light)", async function () {
      post.mockResolvedValue({ ok: true, status: 200 });
      const { form } = await renderAndLoad({ htmlClass: "theme-dark", matches: false });

      selectTheme(form, "SYSTEM");
      await flushRenderTimers();

      expect(document.documentElement.classList.contains("theme-system")).toBe(true);
      expect(document.documentElement.classList.contains("theme-dark")).toBe(false);
    });

    it("ignores changes to unrelated form fields", async function () {
      const { form } = await renderAndLoad({ htmlClass: "theme-light" });
      form.insertAdjacentHTML("beforeend", `<input name="other" value="x" />`);

      form.querySelector("[name='other']").dispatchEvent(new Event("change", { bubbles: true }));
      await flushRenderTimers();

      expect(post).not.toHaveBeenCalled();
      expect(document.documentElement.classList.contains("theme-dark")).toBe(false);
    });

    it("logs when persisting the theme fails with a non-ok response", async function () {
      const consoleLogSpy = vi.spyOn(console, "log").mockImplementation(() => {});
      post.mockResolvedValue({ ok: false, status: 500 });
      const { form } = await renderAndLoad({ htmlClass: "theme-light" });

      selectTheme(form, "DARK");
      await flushRenderTimers();
      await vi.waitFor(() => expect(consoleLogSpy).toHaveBeenCalled());

      expect(consoleLogSpy).toHaveBeenCalledWith("theme change could not be persisted.");
    });

    it("logs when persisting the theme rejects (network error)", async function () {
      const consoleLogSpy = vi.spyOn(console, "log").mockImplementation(() => {});
      post.mockRejectedValue(new Error("network down"));
      const { form } = await renderAndLoad({ htmlClass: "theme-light" });

      selectTheme(form, "DARK");
      await flushRenderTimers();
      await vi.waitFor(() => expect(consoleLogSpy).toHaveBeenCalled());

      expect(consoleLogSpy).toHaveBeenCalledWith("theme change could not be persisted.");
    });
  });

  describe("reacting to OS-level color scheme changes", function () {
    it("flips the rendered theme when in system mode", async function () {
      // starts light (theme-system, OS not currently dark)
      const { mediaQueryDark } = await renderAndLoad({ htmlClass: "theme-system", matches: false });
      expect(document.documentElement.classList.contains("theme-dark")).toBe(false);

      // OS switches to dark -> the "change" event fires
      mediaQueryDark.trigger();
      await flushRenderTimers();

      expect(document.documentElement.classList.contains("theme-dark")).toBe(true);
      expect(document.querySelector("meta[name='theme-color']").getAttribute("content")).toBe("#18181b");
    });

    it("does not react when not in system mode", async function () {
      const { mediaQueryDark } = await renderAndLoad({ htmlClass: "theme-dark", matches: true });

      mediaQueryDark.trigger();
      await flushRenderTimers();

      expect(document.documentElement.classList.contains("theme-dark")).toBe(true);
    });

    it("does not call post when only reacting to an OS-level change", async function () {
      const { mediaQueryDark } = await renderAndLoad({ htmlClass: "theme-system", matches: true });

      mediaQueryDark.trigger();
      await flushRenderTimers();

      expect(post).not.toHaveBeenCalled();
    });
  });

  describe("media query listener registration", function () {
    it("prefers addEventListener when available", async function () {
      const { mediaQueryDark } = await renderAndLoad({ htmlClass: "theme-system" });

      expect(mediaQueryDark.addEventListener).toHaveBeenCalledWith("change", expect.any(Function));
      expect(mediaQueryDark.addListener).not.toHaveBeenCalled();
    });

    it("falls back to the legacy addListener API when addEventListener throws", async function () {
      document.documentElement.className = "theme-system";
      document.head.innerHTML = `<meta name="theme-color" content="#fafafa" />`;
      document.body.innerHTML = `<form id="user-settings-form" action="/x"></form>`;

      const mediaQueryDark = {
        matches: true,
        addEventListener: vi.fn(() => {
          throw new Error("not supported");
        }),
        addListener: vi.fn(),
      };
      globalThis.matchMedia = vi.fn(() => mediaQueryDark);

      await import("../theme-picker");

      expect(mediaQueryDark.addListener).toHaveBeenCalledWith(expect.any(Function));
    });

    it("logs via console.info when neither API is available", async function () {
      const consoleInfoSpy = vi.spyOn(console, "info").mockImplementation(() => {});
      document.documentElement.className = "theme-system";
      document.head.innerHTML = `<meta name="theme-color" content="#fafafa" />`;
      document.body.innerHTML = `<form id="user-settings-form" action="/x"></form>`;

      const mediaQueryDark = {
        matches: true,
        addEventListener: vi.fn(() => {
          throw new Error("not supported");
        }),
        addListener: vi.fn(() => {
          throw new Error("not supported either");
        }),
      };
      globalThis.matchMedia = vi.fn(() => mediaQueryDark);

      await import("../theme-picker");

      expect(consoleInfoSpy).toHaveBeenCalledWith(
        "could not add mediaQuery listener to toggle theme.",
        expect.any(Error),
      );
    });
  });
});
