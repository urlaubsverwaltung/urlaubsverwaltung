export default function parseQueryString(queryString) {
  const input = queryString.replace(/^\?/, "");
  const query = {};
  for (let tuple of input.split("&")) {
    const [key, value] = tuple.split("=");
    query[key] = value;
  }
  return query;
}
