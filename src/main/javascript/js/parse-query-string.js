export default function parseQueryString(queryString) {
  const input = queryString.replace(/^\?/, "");
  return input.split("&").reduce((query, tuple) => {
    const [key, value] = tuple.split("=");
    return {
      ...query,
      [key]: value,
    };
  }, {});
}
