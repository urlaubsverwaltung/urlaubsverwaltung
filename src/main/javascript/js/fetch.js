export async function getJSON(url) {
  const response = await doFetch(url, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  });
  if (!response.ok) {
    throw new FetchError(response);
  }
  return response.json();
}

/**
 * POST data
 *
 * @param {string} url
 * @param {RequestInit} [options]
 * @return {Promise<Response>}
 */
export function post(url, options = {}) {
  return doFetch(url, {
    ...options,
    method: "POST",
  });
}

/**
 * PATCH json
 *
 * @param {string} url
 * @param {object} data
 * @param {RequestInit} [options]
 * @return {Promise<Response>}
 */
export function patchJson(url, data, options = {}) {
  return doFetch(url, {
    ...options,
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      ...options?.headers,
    },
    body: JSON.stringify(data),
  });
}

/**
 *
 * @param {string} url
 * @param {RequestInit} [options]
 * @return {Promise<Response>}
 */
function doFetch(url, options) {
  // eslint-disable-next-line no-restricted-globals
  return fetch(url, {
    ...options,
    credentials: "include",
    headers: {
      "X-Requested-With": "ajax",
      ...options?.headers,
    },
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
