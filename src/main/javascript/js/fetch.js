/* eslint-disable @urlaubsverwaltung/no-global-fetch */
import { defaults } from "underscore";

export async function getJSON(url) {
  const response = await doGet(url, {
    headers: {
      "Content-Type": "application/json",
    },
  });
  if (!response.ok) {
    throw new FetchError(response);
  }
  return response.json();
}

export function post(url, options = {}) {
  return fetch(
    url,
    defaults(options, {
      method: "POST",
      credentials: "include",
      headers: defaults(options.headers, {
        "X-Requested-With": "ajax",
      }),
    }),
  );
}

function doGet(url, options) {
  return fetch(
    url,
    defaults(options, {
      method: "GET",
    }),
  );
}

class FetchError extends Error {
  constructor({ status, statusText, url }) {
    super(`fetching ${url} has failed`);
    this.status = status;
    this.statusText = statusText;
    this.url = url;
  }
}
