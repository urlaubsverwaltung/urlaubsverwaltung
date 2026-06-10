import fetchMock from "fetch-mock";
import { getJSON, post, patchJson } from "../fetch";

describe("fetch", () => {
  beforeAll(function () {
    fetchMock.mockGlobal();
  });

  afterEach(function () {
    fetchMock.removeRoutes();
    fetchMock.clearHistory();
  });

  describe("getJSON", () => {
    it("fetches contentType=json with GET method", async () => {
      fetchMock.get(
        "https://awesome-stuff.com/api/give-me-json",
        {},
        {
          headers: {
            "Content-Type": "application/json",
          },
        },
      );

      await getJSON("https://awesome-stuff.com/api/give-me-json");

      expect(fetchMock.callHistory.called()).toEqual(true);
    });

    it("returns json parsed response", async () => {
      fetchMock.route("https://awesome-stuff.com/api/give-me-json", {
        data: {
          hero: "batman",
        },
      });

      const json = await getJSON("https://awesome-stuff.com/api/give-me-json");
      expect(json.data).toEqual({ hero: "batman" });
    });

    it('throws when server returns "not ok"', async () => {
      expect.hasAssertions();

      fetchMock.route("https://awesome-stuff.com/api/give-me-json", 500);

      try {
        await getJSON("https://awesome-stuff.com/api/give-me-json");
      } catch (error) {
        expect(error).toEqual(
          expect.objectContaining({
            status: 500,
            statusText: "Internal Server Error",
            url: "https://awesome-stuff.com/api/give-me-json",
          }),
        );
      }
    });
  });

  describe("post", () => {
    it("does a POST", async () => {
      fetchMock.post("https://awesome-stuff.com/api/post", {});

      await post("https://awesome-stuff.com/api/post", {
        body: 42,
      });

      const lastCall = fetchMock.callHistory.lastCall();
      expect(lastCall.args[0]).toEqual("https://awesome-stuff.com/api/post");
      expect(lastCall.args[1]).toEqual(
        expect.objectContaining({
          method: "POST",
          body: 42,
        }),
      );
    });

    it("includes credentials", async () => {
      fetchMock.route("https://awesome-stuff.com/api/post", {});

      await post("https://awesome-stuff.com/api/post");

      const lastCall = fetchMock.callHistory.lastCall();
      expect(lastCall.args[0]).toEqual("https://awesome-stuff.com/api/post");
      expect(lastCall.args[1]).toEqual(
        expect.objectContaining({
          credentials: "include",
        }),
      );
    });

    it("includes 'X-Requested-Width=ajax' header", async () => {
      fetchMock.route("https://awesome-stuff.com/api/post", {});

      await post("https://awesome-stuff.com/api/post");

      const lastCall = fetchMock.callHistory.lastCall();
      expect(lastCall.args[0]).toEqual("https://awesome-stuff.com/api/post");
      expect(lastCall.args[1]).toEqual(
        expect.objectContaining({
          headers: {
            "X-Requested-With": "ajax",
          },
        }),
      );
    });

    it("merges headers", async () => {
      fetchMock.route("https://awesome-stuff.com/api/post", {});

      await post("https://awesome-stuff.com/api/post", {
        headers: {
          Accept: "text/html",
        },
      });

      const lastCall = fetchMock.callHistory.lastCall();
      expect(lastCall.args[1]).toEqual(
        expect.objectContaining({
          headers: {
            "X-Requested-With": "ajax",
            Accept: "text/html",
          },
        }),
      );
    });
  });

  describe("patchJson", () => {
    it("does a PATCH", async () => {
      fetchMock.patch("https://awesome-stuff.com/api/patch", {});

      await patchJson("https://awesome-stuff.com/api/patch", {
        field: 42,
      });

      const lastCall = fetchMock.callHistory.lastCall();
      expect(lastCall.args[0]).toEqual("https://awesome-stuff.com/api/patch");
      expect(lastCall.args[1]).toEqual(
        expect.objectContaining({
          method: "PATCH",
          body: JSON.stringify({ field: 42 }),
          headers: expect.objectContaining({
            "Content-Type": "application/json",
          }),
        }),
      );
    });

    it("includes credentials", async () => {
      fetchMock.route("https://awesome-stuff.com/api/patch", {});

      await patchJson("https://awesome-stuff.com/api/patch", { some: "data" });

      const lastCall = fetchMock.callHistory.lastCall();
      expect(lastCall.args[0]).toEqual("https://awesome-stuff.com/api/patch");
      expect(lastCall.args[1]).toEqual(
        expect.objectContaining({
          credentials: "include",
        }),
      );
    });

    it("includes 'X-Requested-Width=ajax' header", async () => {
      fetchMock.route("https://awesome-stuff.com/api/patch", {});

      await patchJson("https://awesome-stuff.com/api/patch", { some: "data" });

      const lastCall = fetchMock.callHistory.lastCall();
      expect(lastCall.args[0]).toEqual("https://awesome-stuff.com/api/patch");
      expect(lastCall.args[1]).toEqual(
        expect.objectContaining({
          headers: expect.objectContaining({
            "X-Requested-With": "ajax",
          }),
        }),
      );
    });

    it("merges headers", async () => {
      fetchMock.route("https://awesome-stuff.com/api/patch", {});

      await patchJson(
        "https://awesome-stuff.com/api/patch",
        { some: "data" },
        {
          headers: {
            Accept: "text/html",
            "Content-Type": "application/json+foo",
          },
        },
      );

      const lastCall = fetchMock.callHistory.lastCall();
      expect(lastCall.args[1]).toEqual(
        expect.objectContaining({
          headers: expect.objectContaining({
            "X-Requested-With": "ajax",
            "Content-Type": "application/json+foo",
            Accept: "text/html",
          }),
        }),
      );
    });
  });
});
