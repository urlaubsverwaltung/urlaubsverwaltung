/**
 * Creates a random string of the given length.
 *
 * Random is based on `Math.random()`.
 * So the consumer has to decide how random it is.
 *
 * @param length length of the generated string
 * @return {string} a random string
 */
export function generateId(length = 8) {
  let result = "";
  let characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  let charactersLength = characters.length;
  for (let index = 0; index < length; index++) {
    result += characters.charAt(Math.floor(Math.random() * charactersLength));
  }
  return result;
}
