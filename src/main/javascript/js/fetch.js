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
  // eslint-disable-next-line no-restricted-globals
  return fetch(url, {
    ...options,
    method: "POST",
    credentials: "include",
    headers: {
      "X-Requested-With": "ajax",
      ...options.headers,
    },
  });
}

export function doGet(url, options) {
  // eslint-disable-next-line no-restricted-globals
  return fetch(url, {
    ...options,
    method: "GET",
  });
}

class FetchError extends Error {
  constructor({ status, statusText, url }) {
    super(`fetching ${url} has failed`);
    this.status = status;
    this.statusText = statusText;
    this.url = url;
  }
}
