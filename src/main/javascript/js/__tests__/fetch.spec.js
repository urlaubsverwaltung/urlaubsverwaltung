import fetchMock from "fetch-mock";
import { getJSON, post } from "../fetch";

describe("fetch", () => {
  afterEach(function () {
    fetchMock.restore();
  });

  describe("getJSON", () => {
    it("fetches contentType=json with GET method", async () => {
      fetchMock.mock("https://awesome-stuff.com/api/give-me-json", {});

      await getJSON("https://awesome-stuff.com/api/give-me-json");

      expect(fetchMock.lastUrl()).toEqual("https://awesome-stuff.com/api/give-me-json");
      expect(fetchMock.lastOptions()).toEqual({
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
      });
    });

    it("returns json parsed response", async () => {
      fetchMock.mock("https://awesome-stuff.com/api/give-me-json", {
        data: {
          hero: "batman",
        },
      });

      const json = await getJSON("https://awesome-stuff.com/api/give-me-json");
      expect(json.data).toEqual({ hero: "batman" });
    });

    it('throws when server returns "not ok"', async () => {
      expect.hasAssertions();

      fetchMock.mock("https://awesome-stuff.com/api/give-me-json", 500);

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
      fetchMock.mock("https://awesome-stuff.com/api/post", {});

      await post("https://awesome-stuff.com/api/post", {
        body: 42,
      });

      expect(fetchMock.lastUrl()).toEqual("https://awesome-stuff.com/api/post");
      expect(fetchMock.lastOptions()).toEqual(
        expect.objectContaining({
          method: "POST",
          body: 42,
        }),
      );
    });

    it("includes credentials", async () => {
      fetchMock.mock("https://awesome-stuff.com/api/post", {});

      await post("https://awesome-stuff.com/api/post");

      expect(fetchMock.lastUrl()).toEqual("https://awesome-stuff.com/api/post");
      expect(fetchMock.lastOptions()).toEqual(
        expect.objectContaining({
          credentials: "include",
        }),
      );
    });

    it("includes 'X-Requested-Width=ajax' header", async () => {
      fetchMock.mock("https://awesome-stuff.com/api/post", {});

      await post("https://awesome-stuff.com/api/post");

      expect(fetchMock.lastUrl()).toEqual("https://awesome-stuff.com/api/post");
      expect(fetchMock.lastOptions()).toEqual(
        expect.objectContaining({
          headers: {
            "X-Requested-With": "ajax",
          },
        }),
      );
    });

    it("merges headers", async () => {
      fetchMock.mock("https://awesome-stuff.com/api/post", {});

      await post("https://awesome-stuff.com/api/post", {
        headers: {
          Accept: "text/html",
        },
      });

      expect(fetchMock.lastUrl()).toEqual("https://awesome-stuff.com/api/post");
      expect(fetchMock.lastOptions()).toEqual(
        expect.objectContaining({
          headers: {
            "X-Requested-With": "ajax",
            Accept: "text/html",
          },
        }),
      );
    });
  });
});
