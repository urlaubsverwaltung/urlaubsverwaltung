/* eslint-disable @urlaubsverwaltung/no-global-fetch */
import { defaults } from "underscore";

export async function getJSON(url) {
  const response = await doGet(url, {
    headers: {
      "Content-Type": "application/json"
    }
  });
  if (!response.ok) {
    throw new FetchError(response);
  }
  return response.json();
}

export async function postJSON(url, data) {
  const response = await doPost(url, {
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(data)
  });
  if (!response.ok) {
    throw new FetchError(response);
  }
  // return something as soon as business code requires it (☞ﾟヮﾟ)☞
}

function doGet(url, options) {
  return fetch(url, defaults(options, {
    method: "GET",
  }));
}

function doPost(url, options) {
  const csrfHeader = getCsrfHeader();
  // TODO request new csrf token and retry POST when the token is not valid anymore due to timeout for instance
  return fetch(url, {
    ...options,
    method: "POST",
    credentials: "same-origin",
    headers: {
      ...options.headers,
      ...csrfHeader
    }
  });
}

function getCsrfHeader() {
  const $ = document.querySelector.bind(document);
  const csrfHeaderMetaElement = $("meta[name='_csrf_header']");
  const csrfTokenMetaElement = $("meta[name='_csrf']");

  if (csrfHeaderMetaElement && csrfTokenMetaElement) {
    const name = csrfHeaderMetaElement.getAttribute("content");
    const token = csrfTokenMetaElement.getAttribute("content");
    return {
      [name]: token
    };
  }

  return {};
}

class FetchError extends Error {
  constructor({ status, statusText, url }) {
    super(`fetching ${url} has failed`);
    this.status = status;
    this.statusText = statusText;
    this.url = url;
  }
}
