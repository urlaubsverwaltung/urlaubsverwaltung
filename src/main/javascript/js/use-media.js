import { observable } from "./observable";

/**
 * Creates an object to determine if the document supports the media query
 * @param {string} query
 * @return {{matches: observable}}
 */
export function useMedia(query) {
  const media = globalThis.matchMedia(query);
  const matches = observable(media.matches);

  try {
    media.addEventListener("change", function () {
      matches.value = media.matches;
    });
  } catch {
    // safari (https://stackoverflow.com/a/60000747)
    try {
      media.addListener(function () {
        matches.value = media.matches;
      });
    } catch (error) {
      // eslint-disable-next-line no-undef
      if (process.env.NODE_ENV !== "test") {
        console.error("could not add matchMedia listener for query='%s'", query, error);
      }
    }
  }

  return {
    matches,
  };
}
