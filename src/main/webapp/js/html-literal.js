export default function htmlLiteral(templateObject, ...substitutions) {
  let result = "";

  // eslint-disable-next-line unicorn/no-for-loop
  for (let i = 0; i < substitutions.length; i++) {
    const subst = substitutions[i];

    result += templateObject[i];

    if (Array.isArray(subst)) {
      result += subst.join('');
    } else {
      result += subst;
    }
  }

  result += templateObject[templateObject.length - 1];

  return result;
}
